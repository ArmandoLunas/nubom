package dgtic.core.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// Acceso corto al usuario autenticado de la peticion en curso.
public final class UsuarioActual {

    private UsuarioActual() {
    }

    public static UsuarioPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal;
        }
        return null;
    }

    public static Integer id() {
        UsuarioPrincipal principal = principal();
        return principal != null ? principal.getIdUsuario() : null;
    }

    public static boolean esAdmin() {
        UsuarioPrincipal principal = principal();
        return principal != null && principal.esAdmin();
    }
}
