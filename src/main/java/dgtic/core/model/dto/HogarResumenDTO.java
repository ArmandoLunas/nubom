package dgtic.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO de apoyo (no viene de una query "new", se arma en el servicio)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HogarResumenDTO {
    private Integer idHogar;
    private String nombre;
    private LocalDateTime fechaCreacion;
    private String rolUsuarioActual; // PROPIETARIO o FAMILIAR
    private boolean esPropietario;
    private long totalMiembros;
}
