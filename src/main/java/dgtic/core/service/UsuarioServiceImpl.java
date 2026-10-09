package dgtic.core.service;

import dgtic.core.model.dto.CambiarContrasenaDTO;
import dgtic.core.model.dto.PerfilUsuarioDTO;
import dgtic.core.model.dto.RegistroUsuarioDTO;
import dgtic.core.model.entity.RolBd;
import dgtic.core.model.entity.RolSistema;
import dgtic.core.model.entity.UsuarioBd;
import dgtic.core.model.entity.UsuarioHogarBd;
import dgtic.core.repository.RolRepository;
import dgtic.core.repository.UsuarioHogarRepository;
import dgtic.core.repository.UsuarioRepository;
import dgtic.core.security.jwt.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioHogarRepository usuarioHogarRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    @Override
    public UsuarioBd registrar(RegistroUsuarioDTO dto) {
        UsuarioBd nuevo = UsuarioBd.builder()
                .nombre(dto.getNombre())
                .correo(dto.getCorreo())
                // Nunca se guarda la contrasena en claro: solo su hash BCrypt.
                .contrasena(passwordEncoder.encode(dto.getContrasena()))
                .rolSistema(RolSistema.USUARIO)
                .fechaRegistro(LocalDateTime.now())
                .activo(true)
                .build();
        return usuarioRepository.save(nuevo);
    }

    @Transactional(readOnly = true)
    @Override
    public UsuarioBd obtenerPorId(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Transactional
    @Override
    public void actualizarPerfil(Integer idUsuario, PerfilUsuarioDTO dto) {
        UsuarioBd usuario = obtenerPorId(idUsuario);
        usuario.setNombre(dto.getNombre());
        usuarioRepository.save(usuario);
    }

    @Transactional
    @Override
    public void cambiarContrasena(Integer idUsuario, CambiarContrasenaDTO dto) {
        UsuarioBd usuario = obtenerPorId(idUsuario);
        if (!passwordEncoder.matches(dto.getContrasenaActual(), usuario.getContrasena())) {
            throw new IllegalStateException("La contraseña actual no es correcta");
        }
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasenaNueva()));
        usuarioRepository.save(usuario);
        // Con la contrasena anterior ya no debe poder renovarse ninguna sesion de la API.
        refreshTokenService.revocarTodos(idUsuario);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean puedeEliminarCuenta(Integer idUsuario) {
        Optional<UsuarioHogarBd> membresia = membresiaActivaDe(idUsuario);
        if (membresia.isEmpty()) {
            return true; // no pertenece a ningun hogar, no hay nada que bloquee la baja
        }
        Optional<RolBd> rolPropietario = rolRepository.findByNombre("PROPIETARIO");
        if (rolPropietario.isEmpty()) {
            return true; // catalogo de roles no configurado, no bloqueamos por esto
        }
        // Bloqueado si su rol activo es justo PROPIETARIO
        return !membresia.get().getRol().getIdRol().equals(rolPropietario.get().getIdRol());
    }

    @Transactional
    @Override
    public void eliminarCuenta(Integer idUsuario) {
        UsuarioBd usuario = obtenerPorId(idUsuario);

        usuario.setActivo(false);
        usuario.setCorreo("eliminado_" + idUsuario + "@baja.nubom");
        usuarioRepository.save(usuario);

        membresiaActivaDe(idUsuario).ifPresent(uh -> {
            uh.setActivo(false);
            usuarioHogarRepository.save(uh);
        });
        refreshTokenService.revocarTodos(idUsuario);
    }

    private Optional<UsuarioHogarBd> membresiaActivaDe(Integer idUsuario) {
        return usuarioHogarRepository.findByUsuario_IdUsuarioAndActivoTrue(idUsuario).stream().findFirst();
    }
}
