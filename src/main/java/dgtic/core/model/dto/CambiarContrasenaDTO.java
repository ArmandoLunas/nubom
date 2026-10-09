package dgtic.core.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarContrasenaDTO {

    @NotEmpty(message = "Ingresa tu contraseña actual")
    private String contrasenaActual;

    @NotEmpty(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    private String contrasenaNueva;

    @NotEmpty(message = "Confirma tu nueva contraseña")
    private String confirmarContrasenaNueva;
}
