package dgtic.core.config;

import dgtic.core.security.UsuarioActual;
import dgtic.core.security.UsuarioPrincipal;
import dgtic.core.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// Datos del usuario autenticado disponibles en todas las vistas Thymeleaf
// (menu superior). Solo aplica a los controladores MVC, no a la API REST.
@ControllerAdvice(basePackages = "dgtic.core.controller")
@RequiredArgsConstructor
public class SesionModelAdvice {

    private final NotificacionService notificacionService;

    @ModelAttribute("nombreUsuario")
    public String nombreUsuario() {
        UsuarioPrincipal principal = UsuarioActual.principal();
        return principal != null ? principal.getNombre() : null;
    }

    @ModelAttribute("correoUsuario")
    public String correoUsuario() {
        UsuarioPrincipal principal = UsuarioActual.principal();
        return principal != null ? principal.getUsername() : null;
    }

    // Numero que se muestra en la campana del menu.
    @ModelAttribute("avisosSinLeer")
    public long avisosSinLeer() {
        Integer idUsuario = UsuarioActual.id();
        return idUsuario != null ? notificacionService.contarSinLeer(idUsuario) : 0;
    }
}
