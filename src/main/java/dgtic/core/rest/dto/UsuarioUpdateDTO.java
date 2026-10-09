package dgtic.core.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de entrada para actualizar un usuario via PUT /api/v1/usuarios/{id}.
// No incluye contrasena a proposito (cambiarla es un flujo aparte en la app,
// para no exponer ni sobrescribir credenciales desde una actualizacion general).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    private String correo;

    private Boolean activo;
}
