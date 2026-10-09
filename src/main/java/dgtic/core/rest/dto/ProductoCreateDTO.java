package dgtic.core.rest.dto;

import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de entrada para crear un producto directamente en POST /api/v1/productos
// (variante "plana" de la creacion, alternativa a POST /inventarios/{id}/productos).
// A diferencia de ProductoRequestDTO, aqui el inventario destino SI viaja en el
// cuerpo (idInventario), porque no hay {id} de inventario en la URL.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCreateDTO {

    @NotNull(message = "Debe indicarse el inventario destino (idInventario)")
    private Integer idInventario;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    @NotNull(message = "Indica la cantidad")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @NotBlank(message = "Selecciona una unidad")
    private String unidad;

    @NotBlank(message = "Selecciona una categoría")
    private String categoria;

    // Opcional. En el alta no puede ser anterior a hoy (regla en el servicio).
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaCaducidad;

    // Opcional: de 8 a 13 digitos (EAN-8, UPC-A, EAN-13).
    @Pattern(regexp = "^(\\d{8,13})?$", message = "El código de barras debe tener de 8 a 13 dígitos")
    private String codigoBarras;
}
