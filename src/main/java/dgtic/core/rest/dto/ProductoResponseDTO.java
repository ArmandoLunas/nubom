package dgtic.core.rest.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {
    private Integer idProducto;
    private String nombre;
    private Integer cantidad;
    private String unidad;
    private String categoria;
    private LocalDateTime fechaIngreso;
    private Boolean activo;
    private Integer idInventario;
    private LocalDate fechaCaducidad;
    private String codigoBarras;
    private String estadoCaducidad; // SIN_FECHA, VIGENTE, POR_VENCER o VENCIDO
}
