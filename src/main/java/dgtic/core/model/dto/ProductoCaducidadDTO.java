package dgtic.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Fila del reporte de productos proximos a caducar.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCaducidadDTO {
    private Integer idProducto;
    private String nombre;
    private String inventario;
    private String categoria;
    private Integer cantidad;
    private String unidad;
    private LocalDate fechaCaducidad;
    private long diasParaCaducar;
    private String estado; // VENCIDO o POR_VENCER
}
