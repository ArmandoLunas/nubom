package dgtic.core.model.dto;

import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private Integer idProducto; // null si es alta, con valor si es edicion

    @NotNull(message = "Falta indicar el inventario")
    private Integer idInventario;

    @NotEmpty(message = "El nombre del producto es obligatorio")
    private String nombre;

    @NotNull(message = "Indica la cantidad")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotEmpty(message = "Selecciona una unidad")
    private String unidad;

    @NotEmpty(message = "Selecciona una categoría")
    private String categoria;

    // Opcional. En el alta no puede ser anterior a hoy (regla en el servicio).
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaCaducidad;

    // Opcional: de 8 a 13 digitos (EAN-8, UPC-A, EAN-13).
    @Pattern(regexp = "^(\\d{8,13})?$", message = "El código de barras debe tener de 8 a 13 dígitos")
    private String codigoBarras;
}
