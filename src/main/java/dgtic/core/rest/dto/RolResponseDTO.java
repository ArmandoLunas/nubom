package dgtic.core.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolResponseDTO {
    private Integer idRol;
    private String nombre;
    private String descripcion;
    private long totalMembresiasActivas;
}
