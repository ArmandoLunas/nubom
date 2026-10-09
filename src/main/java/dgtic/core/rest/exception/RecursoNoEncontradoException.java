package dgtic.core.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando se solicita (consultar, modificar o eliminar) un recurso
 * que no existe en la base de datos. El manejador global la traduce a
 * HTTP 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
