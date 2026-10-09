package dgtic.core.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Version simplificada de lo que devuelve Open Food Facts, ya traducida a los
// terminos de NUBOM (la categoria es una de las del catalogo de la aplicacion).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoExternoDTO {
    private String codigoBarras;
    private String nombre;
    private String marca;
    private String categoriaSugerida;
    private String presentacion;
}
