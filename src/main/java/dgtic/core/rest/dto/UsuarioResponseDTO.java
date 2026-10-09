package dgtic.core.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO de salida: nunca incluye la contrasena (a diferencia de la entidad
// UsuarioBd, que solo la protege con @JsonIgnore como defensa adicional).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Integer idUsuario;
    private String nombre;
    private String correo;
    private LocalDateTime fechaRegistro;
    private Boolean activo;
    private String rolSistema;
}
