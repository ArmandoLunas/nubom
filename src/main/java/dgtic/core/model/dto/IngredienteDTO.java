package dgtic.core.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredienteDTO {

    @NotBlank(message = "Cada ingrediente necesita un nombre")
    private String nombre;

    @NotNull(message = "Indica la cantidad de cada ingrediente")
    @DecimalMin(value = "0.01", message = "La cantidad de cada ingrediente debe ser mayor a 0")
    private Double cantidad;

    @NotBlank(message = "Selecciona la unidad de cada ingrediente")
    private String unidad;

    private boolean principal;
}
