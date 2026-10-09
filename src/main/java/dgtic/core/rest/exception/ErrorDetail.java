package dgtic.core.rest.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estructura estandarizada de respuesta para TODOS los errores de la API
 * REST. Se usa como cuerpo de respuesta en cada {@code @ExceptionHandler}
 * de {@link GlobalExceptionHandler}.
 *
 * @param timestamp momento en el que ocurrio el error
 * @param status    codigo de estado HTTP numerico (por ejemplo 404)
 * @param error     nombre corto del estado HTTP (por ejemplo "Not Found")
 * @param mensaje   descripcion legible del problema
 * @param ruta      endpoint (path) donde ocurrio el error
 * @param detalles  lista opcional con el detalle de cada error de validacion
 *                  (una entrada por campo invalido); null si no aplica
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String ruta,
        List<String> detalles
) {

    public ErrorDetail(int status, String error, String mensaje, String ruta) {
        this(LocalDateTime.now(), status, error, mensaje, ruta, null);
    }

    public ErrorDetail(int status, String error, String mensaje, String ruta, List<String> detalles) {
        this(LocalDateTime.now(), status, error, mensaje, ruta, detalles);
    }
}
