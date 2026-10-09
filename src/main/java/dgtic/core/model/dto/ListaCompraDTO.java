package dgtic.core.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListaCompraDTO {

    private Integer idItem; // null si es alta, con valor si es edicion

    @NotEmpty(message = "El nombre del producto es obligatorio")
    private String nombre;

    @NotEmpty(message = "Selecciona una categoría")
    private String categoria;

    @NotNull(message = "Indica la cantidad")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotEmpty(message = "Selecciona una unidad")
    private String unidad;
}
