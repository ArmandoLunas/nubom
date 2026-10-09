package dgtic.core.rest.service;

import dgtic.core.model.entity.HogarBd;
import dgtic.core.model.entity.RolBd;
import dgtic.core.model.entity.UsuarioBd;
import dgtic.core.model.entity.UsuarioHogarBd;
import dgtic.core.repository.HogarRepository;
import dgtic.core.repository.RolRepository;
import dgtic.core.repository.UsuarioHogarRepository;
import dgtic.core.repository.UsuarioRepository;
import dgtic.core.rest.dto.MembresiaRequestDTO;
import dgtic.core.rest.exception.ConflictoIntegridadException;
import dgtic.core.rest.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio dedicado a la relacion N:M usuario &lt;-&gt; hogar (tabla de union
 * usuario_hogar, con el rol como atributo propio de la asociacion). Concentra
 * las tres operaciones que pide el ejercicio para una relacion N:M: asociar
 * dos registros, consultar desde cualquiera de los dos lados, y eliminar la
 * asociacion.
 */
@Service
@RequiredArgsConstructor
public class MembresiaRestService {

    private final UsuarioHogarRepository usuarioHogarRepository;
    private final UsuarioRepository usuarioRepository;
    private final HogarRepository hogarRepository;
    private final RolRepository rolRepository;

    // ---- Lado "usuario": hogares de un usuario ----
    @Transactional(readOnly = true)
    public List<UsuarioHogarBd> listarHogaresDeUsuario(Integer idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new RecursoNoEncontradoException("No existe un usuario con id " + idUsuario);
        }
        return usuarioHogarRepository.findByUsuario_IdUsuarioAndActivoTrue(idUsuario);
    }

    // ---- Lado "hogar": usuarios de un hogar ----
    @Transactional(readOnly = true)
    public List<UsuarioHogarBd> listarUsuariosDeHogar(Integer idHogar) {
        if (!hogarRepository.existsById(idHogar)) {
            throw new RecursoNoEncontradoException("No existe un hogar con id " + idHogar);
        }
        return usuarioHogarRepository.findByHogar_IdHogarAndActivoTrue(idHogar);
    }

    // ---- Asociar (crear la relacion N:M) ----
    @Transactional
    public UsuarioHogarBd asociar(Integer idUsuario, Integer idHogar, MembresiaRequestDTO dto) {
        UsuarioBd usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con id " + idUsuario));
        HogarBd hogar = hogarRepository.findById(idHogar)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un hogar con id " + idHogar));
        RolBd rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un rol con id " + dto.getIdRol()));

        if (usuarioHogarRepository.findByUsuario_IdUsuarioAndHogar_IdHogarAndActivoTrue(idUsuario, idHogar).isPresent()) {
            throw new ConflictoIntegridadException(
                    "El usuario " + idUsuario + " ya está asociado activamente al hogar " + idHogar);
        }
        // Regla de negocio de NUBOM: un usuario solo puede tener un hogar activo a la vez.
        if (!usuarioHogarRepository.findByUsuario_IdUsuarioAndActivoTrue(idUsuario).isEmpty()) {
            throw new ConflictoIntegridadException(
                    "El usuario " + idUsuario + " ya pertenece a otro hogar activo");
        }

        UsuarioHogarBd relacion = UsuarioHogarBd.builder()
                .usuario(usuario)
                .hogar(hogar)
                .rol(rol)
                .fechaUnion(LocalDateTime.now())
                .activo(true)
                .build();
        return usuarioHogarRepository.save(relacion);
    }

    // ---- Eliminar la asociacion ----
    @Transactional
    public void eliminarAsociacion(Integer idUsuario, Integer idHogar) {
        UsuarioHogarBd relacion = usuarioHogarRepository
                .findByUsuario_IdUsuarioAndHogar_IdHogarAndActivoTrue(idUsuario, idHogar)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario " + idUsuario + " no está asociado activamente al hogar " + idHogar));
        usuarioHogarRepository.delete(relacion);
    }
}
