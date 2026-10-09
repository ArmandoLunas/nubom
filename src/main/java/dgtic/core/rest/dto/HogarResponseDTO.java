package dgtic.core.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO de salida: lo que la API expone de un hogar. No expone la entidad
// HogarBd directamente (evita filtrar las colecciones @OneToMany completas).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HogarResponseDTO {
    private Integer idHogar;
    private String nombre;
    private LocalDateTime fechaCreacion;
    private int totalInventarios;
    private int totalListaCompra;
}
