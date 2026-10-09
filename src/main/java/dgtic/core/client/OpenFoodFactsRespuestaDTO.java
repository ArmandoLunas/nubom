package dgtic.core.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// Respuesta de GET /api/v2/product/{codigo}.json de Open Food Facts.
// Solo se declaran los campos que NUBOM usa; el resto se ignora.
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenFoodFactsRespuestaDTO {

    private int status; // 1 = producto encontrado, 0 = no encontrado
    private Producto product;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Producto {

        @JsonProperty("product_name")
        private String productName;

        private String brands;

        @JsonProperty("categories_tags")
        private List<String> categoriesTags;

        private String quantity;
    }
}
