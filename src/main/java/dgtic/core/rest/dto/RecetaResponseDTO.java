package dgtic.core.rest.dto;

import dgtic.core.model.dto.IngredienteDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// Detalle completo de una receta.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecetaResponseDTO {
    private Integer idReceta;
    private String nombre;
    private String descripcion;
    private List<String> pasos;
    private Integer tiempoMinutos;
    private String estado;
    private Integer idAutor;
    private String autor;
    private List<IngredienteDTO> ingredientes;
    private Double promedio;
    private long totalCalificaciones;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
}
