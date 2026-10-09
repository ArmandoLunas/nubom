package dgtic.core.controller;

import dgtic.core.security.UsuarioActual;
import org.springframework.stereotype.Controller;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// Solo muestra las pantallas: el POST /login y el POST /logout los procesa
// Spring Security (formLogin y logout de la cadena web en SecurityConfig).
@Controller
public class LoginController {

    @GetMapping("/")
    public String raiz() {
        return UsuarioActual.principal() != null ? "redirect:/inicio" : "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        if (UsuarioActual.principal() != null) {
            return "redirect:/inicio";
        }
        return "login";
    }

    // Destino de accessDeniedPage: usuario autenticado sin permiso para la ruta.
    // Acepta cualquier metodo: Spring Security reenvia aqui (forward) la peticion original,
    // que puede ser un POST rechazado por falta de token CSRF.
    @RequestMapping("/acceso-denegado")
    public String accesoDenegado(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return "acceso-denegado";
    }
}
