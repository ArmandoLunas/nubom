package dgtic.core.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModeracionRequestDTO {

    // true = aprobar (se publica); false = rechazar.
    @NotNull(message = "Indica si la receta se aprueba (true) o se rechaza (false)")
    private Boolean aprobar;
}
