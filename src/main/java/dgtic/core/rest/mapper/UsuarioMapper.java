package dgtic.core.rest.mapper;

import dgtic.core.model.entity.UsuarioBd;
import dgtic.core.rest.dto.UsuarioRequestDTO;
import dgtic.core.rest.dto.UsuarioResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UsuarioMapper {

    public UsuarioBd toEntity(UsuarioRequestDTO dto) {
        return UsuarioBd.builder()
                .nombre(dto.getNombre())
                .correo(dto.getCorreo())
                .contrasena(dto.getContrasena())
                .fechaRegistro(LocalDateTime.now())
                .activo(true)
                .build();
    }

    // La contrasena NUNCA se copia a la respuesta: UsuarioResponseDTO ni
    // siquiera declara ese campo.
    public UsuarioResponseDTO toResponseDTO(UsuarioBd entidad) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setIdUsuario(entidad.getIdUsuario());
        dto.setNombre(entidad.getNombre());
        dto.setCorreo(entidad.getCorreo());
        dto.setFechaRegistro(entidad.getFechaRegistro());
        dto.setActivo(entidad.getActivo());
        dto.setRolSistema(entidad.getRolSistema() != null ? entidad.getRolSistema().name() : null);
        return dto;
    }
}
