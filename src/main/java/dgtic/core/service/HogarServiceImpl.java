package dgtic.core.service;

import org.springframework.security.access.prepost.PreAuthorize;
import dgtic.core.model.dto.HogarDTO;
import dgtic.core.model.dto.HogarResumenDTO;
import dgtic.core.model.dto.MiembroHogarDTO;
import dgtic.core.model.entity.HogarBd;
import dgtic.core.model.entity.RolBd;
import dgtic.core.model.entity.UsuarioBd;
import dgtic.core.model.entity.UsuarioHogarBd;
import dgtic.core.repository.HogarRepository;
import dgtic.core.repository.RolRepository;
import dgtic.core.repository.UsuarioHogarRepository;
import dgtic.core.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HogarServiceImpl implements HogarService {

    private final HogarRepository hogarRepository;
    private final UsuarioHogarRepository usuarioHogarRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final InventarioService inventarioService;

    @Transactional(readOnly = true)
    @Override
    public Optional<HogarResumenDTO> obtenerResumenDeUsuario(Integer idUsuario) {
        Optional<UsuarioHogarBd> membresia = membresiaActivaDe(idUsuario);
        if (membresia.isEmpty()) {
            return Optional.empty();
        }
        UsuarioHogarBd uh = membresia.get();
        HogarBd hogar = uh.getHogar();
        RolBd rol = uh.getRol();
        long totalMiembros = usuarioHogarRepository.countByHogar_IdHogarAndActivoTrue(hogar.getIdHogar());

        HogarResumenDTO resumen = new HogarResumenDTO();
        resumen.setIdHogar(hogar.getIdHogar());
        resumen.setNombre(hogar.getNombre());
        resumen.setFechaCreacion(hogar.getFechaCreacion());
        resumen.setRolUsuarioActual(rol.getNombre());
        resumen.setEsPropietario("PROPIETARIO".equals(rol.getNombre()));
        resumen.setTotalMiembros(totalMiembros);
        return Optional.of(resumen);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MiembroHogarDTO> obtenerMiembros(Integer idHogar) {
        return usuarioHogarRepository.buscarMiembrosDeHogar(idHogar);
    }

    @Transactional
    @Override
    public HogarResumenDTO crearHogar(Integer idUsuario, HogarDTO dto) {
        if (membresiaActivaDe(idUsuario).isPresent()) {
            throw new IllegalStateException("Ya perteneces a un hogar activo");
        }

        HogarBd hogar = HogarBd.builder()
                .nombre(dto.getNombre())
                .fechaCreacion(LocalDateTime.now())
                .build();
        hogar = hogarRepository.save(hogar);

        RolBd rolPropietario = rolRepository.findByNombre("PROPIETARIO")
                .orElseThrow(() -> new IllegalStateException("El catálogo de roles no está inicializado"));

        UsuarioBd usuario = usuarioRepository.getReferenceById(idUsuario);

        UsuarioHogarBd relacion = UsuarioHogarBd.builder()
                .usuario(usuario)
                .hogar(hogar)
                .rol(rolPropietario)
                .fechaUnion(LocalDateTime.now())
                .activo(true)
                .build();
        usuarioHogarRepository.save(relacion);

        // Regla de negocio: cada hogar nace con exactamente 2 inventarios
        // (Refrigerador y Alacena). Ya no se crean inventarios "a mano".
        inventarioService.crearInventariosIniciales(hogar.getIdHogar());

        return obtenerResumenDeUsuario(idUsuario).orElseThrow();
    }

    @Transactional
    @Override
    @PreAuthorize("@hogarSeguridad.esPropietario(authentication, #idHogar)")
    public void renombrarHogar(Integer idHogar, Integer idUsuarioSolicitante, HogarDTO dto) {
        validarEsPropietario(idHogar, idUsuarioSolicitante);
        HogarBd hogar = hogarRepository.findById(idHogar)
                .orElseThrow(() -> new IllegalArgumentException("Hogar no encontrado"));
        hogar.setNombre(dto.getNombre());
        hogarRepository.save(hogar);
    }

    @Transactional
    @Override
    @PreAuthorize("@hogarSeguridad.esPropietario(authentication, #idHogar)")
    public void eliminarHogar(Integer idHogar, Integer idUsuarioSolicitante) {
        validarEsPropietario(idHogar, idUsuarioSolicitante);

        List<UsuarioHogarBd> relaciones = usuarioHogarRepository.findByHogar_IdHogarAndActivoTrue(idHogar);
        usuarioHogarRepository.deleteAll(relaciones);

        // Regla de negocio ("no corromper los datos"): al eliminar un hogar se
        // eliminan en cascada sus inventarios (y, a su vez, los productos que
        // contengan) y su lista de compras, gracias a las relaciones 1:N
        // (cascade = ALL, orphanRemoval = true) declaradas en HogarBd.
        HogarBd hogar = hogarRepository.findById(idHogar)
                .orElseThrow(() -> new IllegalArgumentException("Hogar no encontrado"));
        hogarRepository.delete(hogar);
    }

    private void validarEsPropietario(Integer idHogar, Integer idUsuarioSolicitante) {
        UsuarioHogarBd membresia = membresiaActivaDe(idUsuarioSolicitante)
                .orElseThrow(() -> new IllegalStateException("No perteneces a ningún hogar"));

        if (!membresia.getHogar().getIdHogar().equals(idHogar)) {
            throw new IllegalStateException("No perteneces a este hogar");
        }

        RolBd rolPropietario = rolRepository.findByNombre("PROPIETARIO")
                .orElseThrow(() -> new IllegalStateException("El catálogo de roles no está inicializado"));

        if (!membresia.getRol().getIdRol().equals(rolPropietario.getIdRol())) {
            throw new IllegalStateException("Solo el propietario del hogar puede hacer esta acción");
        }
    }

    // La relacion usuario<->hogar es N:M en el modelo de datos (tabla
    // usuario_hogar), pero esta app solo permite a un usuario tener UNA
    // membresia activa a la vez; de ahi tomar la primera de la lista.
    private Optional<UsuarioHogarBd> membresiaActivaDe(Integer idUsuario) {
        return usuarioHogarRepository.findByUsuario_IdUsuarioAndActivoTrue(idUsuario).stream().findFirst();
    }

    @Transactional
    @Override
    public void agregarMiembro(Integer idHogar, Integer idUsuarioSolicitante, String correo) {
        validarEsPropietario(idHogar, idUsuarioSolicitante);

        String normalizado = correo == null ? "" : correo.trim().toLowerCase();
        UsuarioBd invitado = usuarioRepository.findByCorreoAndActivoTrue(normalizado)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay una cuenta activa con el correo " + normalizado + ". Pídele que se registre primero."));
        if (membresiaActivaDe(invitado.getIdUsuario()).isPresent()) {
            throw new IllegalStateException(invitado.getNombre() + " ya pertenece a un hogar");
        }
        RolBd rolFamiliar = rolRepository.findByNombre("FAMILIAR")
                .orElseThrow(() -> new IllegalStateException("El catálogo de roles no está inicializado"));

        usuarioHogarRepository.save(UsuarioHogarBd.builder()
                .usuario(invitado)
                .hogar(hogarRepository.getReferenceById(idHogar))
                .rol(rolFamiliar)
                .fechaUnion(LocalDateTime.now())
                .activo(true)
                .build());
    }

    @Transactional
    @Override
    public void quitarMiembro(Integer idHogar, Integer idUsuarioSolicitante, Integer idUsuarioMiembro) {
        validarEsPropietario(idHogar, idUsuarioSolicitante);
        if (idUsuarioSolicitante.equals(idUsuarioMiembro)) {
            throw new IllegalStateException("El propietario no puede salir de su propio hogar");
        }
        UsuarioHogarBd membresia = usuarioHogarRepository
                .findByUsuario_IdUsuarioAndHogar_IdHogarAndActivoTrue(idUsuarioMiembro, idHogar)
                .orElseThrow(() -> new IllegalArgumentException("Esa persona no pertenece a tu hogar"));
        usuarioHogarRepository.delete(membresia);
    }
}
