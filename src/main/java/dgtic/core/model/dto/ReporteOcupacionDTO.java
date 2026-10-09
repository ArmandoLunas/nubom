package dgtic.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteOcupacionDTO {
    private Integer idInventario;
    private String inventario;
    private long productos;
    private int capacidad;
    private int porcentaje;
}
