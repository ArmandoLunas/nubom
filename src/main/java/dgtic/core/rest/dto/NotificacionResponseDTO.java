package dgtic.core.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponseDTO {
    private Integer idNotificacion;
    private String mensaje;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    private Integer idProducto;
}
