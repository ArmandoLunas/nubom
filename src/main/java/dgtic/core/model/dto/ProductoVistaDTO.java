package dgtic.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO solo para pintar tarjetas de producto en las vistas (sin persistencia real)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoVistaDTO {
    private String nombre;
    private String ubicacion;      // Refrigerador o Alacena
    private String categoria;
    private Double cantidad;
    private String unidad;
    private Integer diasParaCaducar; // negativo o pequeño = urgente
    private String icono;          // clase de bootstrap-icons
}
