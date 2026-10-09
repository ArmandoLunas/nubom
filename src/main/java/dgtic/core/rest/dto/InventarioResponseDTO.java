package dgtic.core.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResponseDTO {
    private Integer idInventario;
    private String nombre;
    private Integer capacidadMaxima;
    private LocalDateTime fechaCreacion;
    private Integer idHogar;
    private Integer idTipo;
    private String tipoNombre;
    private int totalProductos;
    private String estilo;
}
