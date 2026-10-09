package dgtic.core.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioTipoResponseDTO {
    private Integer idTipo;
    private String nombre;
    private String descripcion;
    private String icono;
    private int totalInventarios;
}
