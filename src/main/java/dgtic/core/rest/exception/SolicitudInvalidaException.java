package dgtic.core.rest.exception;

/**
 * Se lanza cuando la solicitud del cliente es sintacticamente correcta pero
 * viola una regla de negocio simple de validacion (por ejemplo, un valor
 * fuera del catalogo permitido). El manejador global la traduce a
 * HTTP 400 Bad Request.
 */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
