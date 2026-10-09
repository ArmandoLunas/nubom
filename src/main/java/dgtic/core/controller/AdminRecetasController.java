package dgtic.core.controller;

import dgtic.core.service.RecetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Bandeja de moderacion. /admin/** exige ROLE_ADMIN (ver SecurityConfig).
@Controller
@RequestMapping("/admin/recetas")
@RequiredArgsConstructor
public class AdminRecetasController {

    private final RecetaService recetaService;

    @GetMapping
    public String pendientes(Model model) {
        model.addAttribute("menuActivo", "admin");
        model.addAttribute("recetas", recetaService.pendientes());
        return "admin-recetas";
    }

    @PostMapping("/{id}/moderar")
    public String moderar(@PathVariable("id") Integer id,
                          @RequestParam("aprobar") boolean aprobar,
                          RedirectAttributes redirectAttributes) {
        try {
            recetaService.moderar(id, aprobar);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    aprobar ? "Receta aprobada: ya es visible para la comunidad." : "Receta rechazada.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/recetas";
    }
}
