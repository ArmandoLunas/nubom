package dgtic.core.client;

import dgtic.core.rest.exception.ServicioExternoNoDisponibleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Cliente de la API publica Open Food Facts (https://world.openfoodfacts.org).
// Dado un codigo de barras devuelve nombre, marca y una categoria sugerida.
@Slf4j
@Component
public class OpenFoodFactsClient {

    private static final String RUTA =
            "/api/v2/product/{codigo}.json?fields=product_name,brands,categories_tags,quantity";

    // Equivalencias entre etiquetas de Open Food Facts y categorias de NUBOM.
    // El orden importa: gana la primera que coincide.
    private static final Map<String, String> EQUIVALENCIAS = new LinkedHashMap<>();

    static {
        EQUIVALENCIAS.put("en:dairies", "Lácteos");
        EQUIVALENCIAS.put("en:milks", "Lácteos");
        EQUIVALENCIAS.put("en:cheeses", "Lácteos");
        EQUIVALENCIAS.put("en:yogurts", "Lácteos");
        EQUIVALENCIAS.put("en:beverages", "Bebidas");
        EQUIVALENCIAS.put("en:meats", "Carnes");
        EQUIVALENCIAS.put("en:seafood", "Carnes");
        EQUIVALENCIAS.put("en:fruits", "Frutas");
        EQUIVALENCIAS.put("en:vegetables", "Verduras");
        EQUIVALENCIAS.put("en:breads", "Panadería");
        EQUIVALENCIAS.put("en:biscuits-and-cakes", "Panadería");
        EQUIVALENCIAS.put("en:cereals-and-their-products", "Granos");
        EQUIVALENCIAS.put("en:legumes", "Granos");
        EQUIVALENCIAS.put("en:pastas", "Granos");
        EQUIVALENCIAS.put("en:rices", "Granos");
    }

    private final RestTemplate restTemplate;

    public OpenFoodFactsClient(@Qualifier("openFoodFactsRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<ProductoExternoDTO> buscarPorCodigo(String codigo) {
        try {
            OpenFoodFactsRespuestaDTO respuesta =
                    restTemplate.getForObject(RUTA, OpenFoodFactsRespuestaDTO.class, codigo);
            if (respuesta == null || respuesta.getStatus() != 1 || respuesta.getProduct() == null) {
                return Optional.empty();
            }
            OpenFoodFactsRespuestaDTO.Producto p = respuesta.getProduct();
            if (p.getProductName() == null || p.getProductName().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(new ProductoExternoDTO(
                    codigo,
                    p.getProductName().trim(),
                    p.getBrands(),
                    categoriaDe(p.getCategoriesTags()),
                    p.getQuantity()));
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        } catch (RestClientException ex) {
            log.warn("Open Food Facts no respondió para el código {}: {}", codigo, ex.getMessage());
            throw new ServicioExternoNoDisponibleException(
                    "El servicio de consulta de productos no está disponible; captura los datos manualmente", ex);
        }
    }

    static String categoriaDe(List<String> etiquetas) {
        if (etiquetas != null) {
            for (Map.Entry<String, String> equivalencia : EQUIVALENCIAS.entrySet()) {
                if (etiquetas.contains(equivalencia.getKey())) {
                    return equivalencia.getValue();
                }
            }
        }
        return "Abarrotes";
    }
}
