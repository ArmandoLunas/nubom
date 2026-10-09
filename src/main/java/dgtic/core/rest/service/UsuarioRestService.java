package dgtic.core.rest.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import dgtic.core.model.entity.UsuarioBd;
import dgtic.core.repository.UsuarioRepository;
import dgtic.core.rest.dto.UsuarioRequestDTO;
import dgtic.core.rest.dto.UsuarioUpdateDTO;
import dgtic.core.rest.exception.ConflictoIntegridadException;
import dgtic.core.rest.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioRestService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioBd> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UsuarioBd obtenerPorId(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un usuario con id " + idUsuario));
    }

    @Transactional
    public UsuarioBd crear(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByCorreoIgnoreCase(dto.getCorreo())) {
            throw new ConflictoIntegridadException("Ya existe un usuario registrado con el correo " + dto.getCorreo());
        }
        UsuarioBd usuario = UsuarioBd.builder()
                .nombre(dto.getNombre())
                .correo(dto.getCorreo().trim().toLowerCase())
                .contrasena(passwordEncoder.encode(dto.getContrasena()))
                .fechaRegistro(LocalDateTime.now())
                .activo(true)
                .build();
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioBd actualizar(Integer idUsuario, UsuarioUpdateDTO dto) {
        UsuarioBd usuario = obtenerPorId(idUsuario);

        boolean correoCambio = !usuario.getCorreo().equalsIgnoreCase(dto.getCorreo());
        if (correoCambio && usuarioRepository.existsByCorreoIgnoreCase(dto.getCorreo())) {
            throw new ConflictoIntegridadException("Ya existe un usuario registrado con el correo " + dto.getCorreo());
        }

        usuario.setNombre(dto.getNombre());
        usuario.setCorreo(dto.getCorreo());
        if (dto.getActivo() != null) {
            usuario.setActivo(dto.getActivo());
        }
        return usuarioRepository.save(usuario);
    }

    // No hace cascada sobre usuario_hogar a proposito: si el usuario todavia
    // tiene membresias (activas o historicas), la base de datos rechaza el
    // borrado por la llave foranea (DataIntegrityViolationException -> 409).
    // Hay que eliminar antes sus asociaciones con DELETE /usuarios/{id}/hogares/{idHogar}.
    @Transactional
    public void eliminar(Integer idUsuario) {
        UsuarioBd usuario = obtenerPorId(idUsuario);
        usuarioRepository.delete(usuario);
    }
}
