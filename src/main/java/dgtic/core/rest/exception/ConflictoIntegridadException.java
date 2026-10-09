package dgtic.core.rest.exception;

/**
 * Se lanza cuando la operacion solicitada entra en conflicto con el estado
 * actual de los datos (por ejemplo: eliminar un catalogo todavia referenciado
 * por otros registros, o agregar un producto quando el inventario ya alcanzo
 * su capacidad maxima). El manejador global la traduce a HTTP 409 Conflict.
 */
public class ConflictoIntegridadException extends RuntimeException {

    public ConflictoIntegridadException(String mensaje) {
        super(mensaje);
    }
}
