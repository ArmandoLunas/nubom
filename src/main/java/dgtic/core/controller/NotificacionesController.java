package dgtic.core.controller;

import dgtic.core.security.UsuarioActual;
import dgtic.core.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionesController {

    private final NotificacionService notificacionService;

    @GetMapping
    public String notificaciones(Model model) {
        model.addAttribute("menuActivo", "notificaciones");
        model.addAttribute("avisos", notificacionService.listar(UsuarioActual.id(), false));
        return "notificaciones";
    }

    @PostMapping("/{id}/leida")
    public String marcarLeida(@PathVariable("id") Integer id) {
        notificacionService.marcarLeida(id, UsuarioActual.id());
        return "redirect:/notificaciones";
    }

    @PostMapping("/leer-todas")
    public String marcarTodas() {
        notificacionService.marcarTodasLeidas(UsuarioActual.id());
        return "redirect:/notificaciones";
    }
}
