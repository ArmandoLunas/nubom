package dgtic.core.controller;

import dgtic.core.security.UsuarioActual;
import dgtic.core.model.dto.HogarResumenDTO;
import dgtic.core.model.dto.ListaCompraDTO;
import dgtic.core.service.HogarService;
import dgtic.core.service.ListaCompraService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/lista-compras")
public class ListaComprasController {

    @Autowired
    private HogarService hogarService;

    @Autowired
    private ListaCompraService listaCompraService;

    private static final List<String> CATEGORIAS = List.of(
            "Lácteos", "Verduras", "Frutas", "Carnes", "Granos",
            "Panadería", "Bebidas", "Abarrotes", "Limpieza", "Otros"
    );

    private static final List<String> UNIDADES = List.of("pza", "kg", "g", "L", "ml", "paquete");

    @GetMapping
    public String listaCompras(HttpSession session, Model model) {
        model.addAttribute("menuActivo", "lista-compras");

        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);

        model.addAttribute("tieneHogar", resumen.isPresent());
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("unidades", UNIDADES);

        if (resumen.isEmpty()) {
            return "lista-compras";
        }

        model.addAttribute("nombreHogarActual", resumen.get().getNombre());
        model.addAttribute("items", listaCompraService.listar(resumen.get().getIdHogar()));

        if (!model.containsAttribute("item")) {
            model.addAttribute("item", new ListaCompraDTO());
        }

        return "lista-compras";
    }

    @PostMapping("/agregar")
    public String agregar(@Valid @ModelAttribute("item") ListaCompraDTO dto,
                           BindingResult bindingResult,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);

        if (resumen.isEmpty()) {
            return "redirect:/lista-compras";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Revisa los datos del producto");
            return "redirect:/lista-compras";
        }

        listaCompraService.agregar(resumen.get().getIdHogar(), dto);
        redirectAttributes.addFlashAttribute("mensajeExito", "\"" + dto.getNombre() + "\" se agregó a la lista");
        return "redirect:/lista-compras";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(@PathVariable("id") Integer id,
                              @Valid @ModelAttribute("item") ListaCompraDTO dto,
                              BindingResult bindingResult,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);

        if (resumen.isEmpty()) {
            return "redirect:/lista-compras";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Revisa los datos del producto");
            return "redirect:/lista-compras";
        }

        dto.setIdItem(id);
        try {
            listaCompraService.actualizar(resumen.get().getIdHogar(), dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Producto actualizado");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        return "redirect:/lista-compras";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Integer id, HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);

        if (resumen.isPresent()) {
            try {
                listaCompraService.eliminar(resumen.get().getIdHogar(), id);
                redirectAttributes.addFlashAttribute("mensajeExito", "Producto eliminado de la lista");
            } catch (IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            }
        }

        return "redirect:/lista-compras";
    }

    @PostMapping("/comprado/{id}")
    public String marcarComprado(@PathVariable("id") Integer id,
                                  @RequestParam(value = "comprado", defaultValue = "false") boolean comprado,
                                  HttpSession session) {
        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);
        resumen.ifPresent(r -> listaCompraService.marcarComprado(r.getIdHogar(), id, comprado));
        return "redirect:/lista-compras";
    }
}
