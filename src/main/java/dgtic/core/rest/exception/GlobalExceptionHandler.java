package dgtic.core.rest.exception;

import dgtic.core.security.jwt.TokenInvalidoException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Manejo global de errores de la API REST (dgtic.core.rest.controller).
 * Cada metodo traduce un tipo de excepcion a una respuesta HTTP con codigo
 * de estado apropiado y un cuerpo {@link ErrorDetail} estandarizado.
 *
 * Se restringe con basePackages a los controladores REST para no interferir
 * con las paginas Thymeleaf del resto de la aplicacion (dgtic.core.controller),
 * que deben seguir devolviendo vistas HTML ante un error, no JSON.
 */
@RestControllerAdvice(basePackages = "dgtic.core.rest.controller")
public class GlobalExceptionHandler {

    // 404 - recurso inexistente
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorDetail> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 409 - conflicto de integridad / regla de negocio (p. ej. inventario lleno,
    // o catalogo todavia referenciado por otros registros)
    @ExceptionHandler(ConflictoIntegridadException.class)
    public ResponseEntity<ErrorDetail> manejarConflicto(ConflictoIntegridadException ex, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 409 - violacion de una restriccion de la base de datos (llave foranea,
    // valor unico duplicado, etc.) que no fue detectada antes en el servicio
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDetail> manejarIntegridadBd(DataIntegrityViolationException ex, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "La operación no se pudo completar porque entra en conflicto con datos relacionados existentes",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 400 - error de validacion de @Valid sobre el cuerpo (@RequestBody) de la peticion
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetail> manejarValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        ErrorDetail error = new ErrorDetail(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Los datos enviados no son válidos",
                request.getRequestURI(),
                detalles);
        return ResponseEntity.badRequest().body(error);
    }

    // 400 - error de validacion sobre parametros sueltos (@RequestParam, @PathVariable con @Validated)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDetail> manejarViolacionRestriccion(ConstraintViolationException ex, HttpServletRequest request) {
        List<String> detalles = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .toList();
        ErrorDetail error = new ErrorDetail(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Los parámetros enviados no son válidos",
                request.getRequestURI(),
                detalles);
        return ResponseEntity.badRequest().body(error);
    }

    // 400 - un {id} en la URL no tiene el tipo esperado (p. ej. /api/v1/hogares/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDetail> manejarTipoIncorrecto(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "El parámetro '" + ex.getName() + "' debe ser de tipo " +
                        (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "válido"),
                request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    // 400 - falta un parametro obligatorio (@RequestParam sin "required = false")
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorDetail> manejarParametroFaltante(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'",
                request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    // 400 - el cuerpo JSON esta mal formado o no se pudo deserializar
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetail> manejarJsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "El cuerpo de la solicitud no es un JSON válido o tiene un formato incorrecto",
                request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    // 400 - validaciones de negocio explicitas lanzadas por los servicios REST
    @ExceptionHandler({SolicitudInvalidaException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorDetail> manejarSolicitudInvalida(RuntimeException ex, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    // 500 - cualquier otro error no controlado explicitamente
    // 401: credenciales incorrectas en /auth/login o refresh token no valido.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetail> manejarNoAutenticado(AuthenticationException ex, HttpServletRequest request) {
        String mensaje = ex instanceof TokenInvalidoException ? ex.getMessage() : "Correo o contraseña incorrectos";
        ErrorDetail error = new ErrorDetail(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                mensaje,
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // 403: el usuario esta autenticado pero el recurso no es suyo o no tiene el rol
    // requerido (@PreAuthorize o comprobaciones de los servicios).
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetail> manejarAccesoDenegado(AccessDeniedException ex, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "No tienes permiso para realizar esta operación",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // 409: reglas de negocio de los servicios compartidos con la aplicacion web.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDetail> manejarEstadoInvalido(IllegalStateException ex, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 400: se pidio ordenar por una propiedad que no existe (?sort=...).
    @ExceptionHandler({PropertyReferenceException.class, InvalidDataAccessApiUsageException.class})
    public ResponseEntity<ErrorDetail> manejarOrdenInvalido(RuntimeException ex, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Los parámetros de ordenamiento o paginación no son válidos (revisa 'sort')",
                request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    // 503: la API externa no respondio.
    @ExceptionHandler(ServicioExternoNoDisponibleException.class)
    public ResponseEntity<ErrorDetail> manejarServicioExterno(ServicioExternoNoDisponibleException ex, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetail> manejarErrorGeneral(Exception ex, HttpServletRequest request) {
        ErrorDetail error = new ErrorDetail(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocurrió un error inesperado en el servidor",
                request.getRequestURI());
        return ResponseEntity.internalServerError().body(error);
    }
}
