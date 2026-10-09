package dgtic.core.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUsuarioDTO {

    @NotEmpty(message = "El nombre es obligatorio")
    private String nombre;
}
