package dgtic.core.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de entrada para crear/actualizar un inventario. idTipo referencia el
// catalogo inventario_tipo; la capacidad se restringe al catalogo {20,40,80}
// como regla de negocio (validada en el servicio, ya que jakarta.validation
// no expresa facilmente "pertenece a este conjunto de valores").
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioRequestDTO {

    @NotBlank(message = "El nombre del inventario es obligatorio")
    private String nombre;

    @NotNull(message = "La capacidad máxima es obligatoria")
    private Integer capacidadMaxima;

    @NotNull(message = "Debe indicarse el tipo de inventario (id_tipo)")
    private Integer idTipo;

    // Opcional: clasico, moderno o retro.
    private String estilo;
}
