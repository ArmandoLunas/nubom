package dgtic.core.controller;

import dgtic.core.model.dto.IngredienteDTO;
import dgtic.core.model.dto.RecetaDTO;
import dgtic.core.model.entity.RecetaBd;
import dgtic.core.security.UsuarioActual;
import dgtic.core.service.RecetaService;
import dgtic.core.validation.RecetaValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/recetas")
@RequiredArgsConstructor
public class RecetasController {

    private static final List<String> UNIDADES = List.of("pza", "kg", "g", "L", "ml", "taza", "cda", "cdita", "pizca");
    private static final int RECETAS_POR_PAGINA = 9;

    private final RecetaService recetaService;
    private final RecetaValidator recetaValidator;

    // /recetas                -> recetario de la comunidad (paginado, con busqueda)
    // /recetas?vista=mias     -> recetas del usuario en cualquier estado
    @GetMapping
    public String recetas(@RequestParam(value = "vista", defaultValue = "comunidad") String vista,
                          @RequestParam(value = "q", defaultValue = "") String q,
                          @RequestParam(value = "pagina", defaultValue = "0") int pagina,
                          Model model) {
        model.addAttribute("menuActivo", "recetas");
        model.addAttribute("vista", vista);
        model.addAttribute("q", q);

        if ("mias".equals(vista)) {
            model.addAttribute("recetas", recetaService.propias(UsuarioActual.id()));
        } else {
            var page = recetaService.comunidad(q,
                    PageRequest.of(Math.max(pagina, 0), RECETAS_POR_PAGINA, Sort.by("nombre")),
                    UsuarioActual.id());
            model.addAttribute("recetas", page.getContent());
            model.addAttribute("paginaActual", page.getNumber());
            model.addAttribute("totalPaginas", page.getTotalPages());
        }
        return "recetas";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        if (!model.containsAttribute("receta")) {
            RecetaDTO dto = new RecetaDTO();
            dto.getIngredientes().add(new IngredienteDTO(null, null, "pza", true));
            model.addAttribute("receta", dto);
        }
        return formulario(model, null);
    }

    @PostMapping("/nueva")
    public String crear(@Valid @ModelAttribute("receta") RecetaDTO dto,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return formulario(model, null);
        }
        RecetaBd creada = recetaService.crear(UsuarioActual.id(), dto);
        redirectAttributes.addFlashAttribute("mensajeExito", "La receta se guardó. Por ahora solo tú puedes verla.");
        return "redirect:/recetas/" + creada.getIdReceta();
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("menuActivo", "recetas");
        Integer idUsuario = UsuarioActual.id();
        RecetaBd receta = recetaService.obtenerVisible(id, idUsuario, UsuarioActual.esAdmin());
        // No se llama "receta": ese nombre lo usa el formulario y tiene su propio validador (@InitBinder).
        model.addAttribute("detalle", receta);
        model.addAttribute("resumen", recetaService.vistaDe(receta, idUsuario));
        model.addAttribute("miCalificacion", recetaService.calificacionDe(id, idUsuario).orElse(0));
        return "receta-detalle";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Integer id, Model model) {
        if (!model.containsAttribute("receta")) {
            model.addAttribute("receta", recetaService.aFormulario(id, UsuarioActual.id()));
        }
        return formulario(model, id);
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable("id") Integer id,
                             @Valid @ModelAttribute("receta") RecetaDTO dto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return formulario(model, id);
        }
        RecetaBd actualizada = recetaService.actualizar(id, UsuarioActual.id(), dto);
        redirectAttributes.addFlashAttribute("mensajeExito",
                "PENDIENTE".equals(actualizada.getEstado().name())
                        ? "Cambios guardados. Como la receta estaba publicada, volvió a revisión."
                        : "Cambios guardados.");
        return "redirect:/recetas/" + id;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        recetaService.eliminar(id, UsuarioActual.id());
        redirectAttributes.addFlashAttribute("mensajeExito", "La receta se eliminó.");
        return "redirect:/recetas?vista=mias";
    }

    @PostMapping("/{id}/compartir")
    public String compartir(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            recetaService.compartir(id, UsuarioActual.id());
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Enviaste la receta a revisión. Se publicará cuando un administrador la apruebe.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/recetas/" + id;
    }

    @PostMapping("/{id}/calificar")
    public String calificar(@PathVariable("id") Integer id,
                            @RequestParam("puntuacion") int puntuacion,
                            RedirectAttributes redirectAttributes) {
        try {
            recetaService.calificar(id, UsuarioActual.id(), puntuacion);
            redirectAttributes.addFlashAttribute("mensajeExito", "Tu calificación se registró.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/recetas/" + id;
    }

    @PostMapping("/{id}/calificacion/quitar")
    public String quitarCalificacion(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        recetaService.retirarCalificacion(id, UsuarioActual.id());
        redirectAttributes.addFlashAttribute("mensajeExito", "Retiraste tu calificación.");
        return "redirect:/recetas/" + id;
    }

    private String formulario(Model model, Integer idReceta) {
        model.addAttribute("menuActivo", "recetas");
        model.addAttribute("idReceta", idReceta);
        model.addAttribute("unidades", UNIDADES);
        return "receta-form";
    }

    @InitBinder("receta")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(recetaValidator);
    }
}
