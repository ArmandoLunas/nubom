package dgtic.core.rest.exception;

// Un servicio externo (Open Food Facts) no respondio a tiempo. Se traduce a 503.
public class ServicioExternoNoDisponibleException extends RuntimeException {

    public ServicioExternoNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
