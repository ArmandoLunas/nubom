package dgtic.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Tarjeta de receta para los listados (web) y respuesta resumida (API).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecetaVistaDTO {
    private Integer idReceta;
    private String nombre;
    private String autor;
    private String descripcion;
    private Integer tiempoMinutos;
    private String estado;
    private Double promedio;          // null si aun no tiene calificaciones
    private long totalCalificaciones;
    private boolean propia;
}
