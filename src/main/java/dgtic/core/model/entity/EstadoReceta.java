package dgtic.core.model.entity;

// Ciclo de vida de una receta respecto a la comunidad:
// PRIVADA -> (compartir) -> PENDIENTE -> (moderacion) -> APROBADA o RECHAZADA.
public enum EstadoReceta {
    PRIVADA, PENDIENTE, APROBADA, RECHAZADA
}
