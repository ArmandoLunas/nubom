package dgtic.core.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioTipoRequestDTO {

    @NotBlank(message = "El nombre del tipo de inventario es obligatorio")
    private String nombre;

    private String descripcion;

    private String icono;
}
