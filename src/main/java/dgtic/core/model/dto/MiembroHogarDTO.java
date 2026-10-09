package dgtic.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiembroHogarDTO {
    private Integer idUsuario;
    private String nombre;
    private String correo;
    private String rol; // PROPIETARIO o FAMILIAR
}
