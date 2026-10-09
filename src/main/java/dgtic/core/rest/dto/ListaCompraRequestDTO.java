package dgtic.core.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de entrada para crear/actualizar un item de la lista de compras.
// El id del hogar NO viaja aqui: se toma del path (/hogares/{id}/lista-compra).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListaCompraRequestDTO {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    @NotBlank(message = "Selecciona una categoría")
    private String categoria;

    @NotNull(message = "Indica la cantidad")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotBlank(message = "Selecciona una unidad")
    private String unidad;

    // Opcional: si se omite, al crear queda en false y al actualizar se conserva el valor previo.
    private Boolean comprado;
}
