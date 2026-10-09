package dgtic.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecetaRankingDTO {
    private Integer idReceta;
    private String nombre;
    private String autor;
    private Double promedio;
    private Long totalCalificaciones;
}
