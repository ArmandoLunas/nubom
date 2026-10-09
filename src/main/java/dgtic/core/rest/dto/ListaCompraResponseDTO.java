package dgtic.core.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListaCompraResponseDTO {
    private Integer idItem;
    private String nombre;
    private String categoria;
    private Integer cantidad;
    private String unidad;
    private Boolean comprado;
    private LocalDateTime fechaCreacion;
    private Integer idHogar;
}
