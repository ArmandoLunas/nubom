package dgtic.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    // RestTemplate dedicado a Open Food Facts: URL base, tiempos limite cortos
    // (su lentitud no debe bloquear el alta de productos) y un User-Agent que
    // identifica a la aplicacion, como pide ese servicio.
    @Bean
    RestTemplate openFoodFactsRestTemplate(RestTemplateBuilder builder,
                                           @Value("${nubom.openfoodfacts.base-url}") String baseUrl,
                                           @Value("${nubom.openfoodfacts.connect-timeout}") Duration conexion,
                                           @Value("${nubom.openfoodfacts.read-timeout}") Duration lectura) {
        return builder
                .rootUri(baseUrl)
                .connectTimeout(conexion)
                .readTimeout(lectura)
                .defaultHeader(HttpHeaders.USER_AGENT, "NUBOM/1.0 (proyecto academico DGTIC UNAM)")
                .build();
    }
}
