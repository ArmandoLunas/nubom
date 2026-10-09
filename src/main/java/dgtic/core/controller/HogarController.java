package dgtic.core.controller;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import dgtic.core.rest.exception.ServicioExternoNoDisponibleException;
import dgtic.core.client.ProductoExternoDTO;
import dgtic.core.client.OpenFoodFactsClient;
import dgtic.core.security.UsuarioActual;
import dgtic.core.converter.TituloConverter;
import dgtic.core.model.dto.HogarDTO;
import dgtic.core.model.dto.HogarResumenDTO;
import dgtic.core.model.dto.ProductoDTO;
import dgtic.core.model.entity.InventarioBd;
import dgtic.core.model.entity.ProductoBd;
import dgtic.core.service.HogarService;
import dgtic.core.service.InventarioService;
import dgtic.core.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/hogar")
public class HogarController {

    @Autowired
    private HogarService hogarService;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private OpenFoodFactsClient openFoodFactsClient;

    private static final List<String> CATEGORIAS = List.of(
            "Lácteos", "Verduras", "Frutas", "Carnes", "Granos",
            "Panadería", "Bebidas", "Abarrotes", "Limpieza", "Otros"
    );

    private static final List<String> UNIDADES = List.of("pza", "kg", "g", "L", "ml", "paquete");

    // ===================== VISTA PRINCIPAL: refrigerador / alacena =====================
    @GetMapping
    public String verHogar(HttpSession session, Model model) {
        model.addAttribute("menuActivo", "hogar");
        Integer idUsuario = UsuarioActual.id();

        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);

        if (resumen.isEmpty()) {
            model.addAttribute("tieneHogar", false);
            if (!model.containsAttribute("hogarNuevo")) {
                model.addAttribute("hogarNuevo", new HogarDTO());
            }
            return "hogar";
        }

        model.addAttribute("tieneHogar", true);
        model.addAttribute("hogarActual", resumen.get());

        List<InventarioBd> inventarios = inventarioService.listarPorHogar(resumen.get().getIdHogar());

        // Mapa nombre de inventario -> productos, para pintar las pestañas facilmente
        Map<InventarioBd, List<ProductoBd>> productosPorInventario = new LinkedHashMap<>();
        for (InventarioBd inv : inventarios) {
            productosPorInventario.put(inv, productoService.listar(inv.getIdInventario()));
        }

        model.addAttribute("inventarios", inventarios);
        model.addAttribute("productosPorInventario", productosPorInventario);
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("unidades", UNIDADES);

        if (!model.containsAttribute("producto")) {
            model.addAttribute("producto", new ProductoDTO());
        }

        return "hogar";
    }

    @PostMapping("/crear")
    public String crear(@Valid @ModelAttribute("hogarNuevo") HogarDTO dto,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("menuActivo", "hogar");
            model.addAttribute("tieneHogar", false);
            return "hogar";
        }

        Integer idUsuario = UsuarioActual.id();
        try {
            hogarService.crearHogar(idUsuario, dto);
        } catch (IllegalStateException ex) {
            bindingResult.reject("hogar.error", ex.getMessage());
            model.addAttribute("menuActivo", "hogar");
            model.addAttribute("tieneHogar", false);
            return "hogar";
        }

        return "redirect:/hogar";
    }

    // ===================== PRODUCTOS =====================
    @PostMapping("/producto/agregar")
    public String agregarProducto(@Valid @ModelAttribute("producto") ProductoDTO dto,
                                   BindingResult bindingResult,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {

        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);

        if (resumen.isEmpty()) {
            return "redirect:/hogar";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Revisa los datos del producto");
            return "redirect:/hogar";
        }

        try {
            productoService.agregar(resumen.get().getIdHogar(), dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "\"" + dto.getNombre() + "\" se agregó correctamente");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        return "redirect:/hogar";
    }

    @PostMapping("/producto/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Integer id, HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);

        if (resumen.isPresent()) {
            try {
                productoService.eliminar(resumen.get().getIdHogar(), id);
                redirectAttributes.addFlashAttribute("mensajeExito", "Producto eliminado");
            } catch (IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            }
        }

        return "redirect:/hogar";
    }

    @PostMapping("/producto/actualizar/{id}")
    public String actualizarProducto(@PathVariable("id") Integer id,
                                      @Valid @ModelAttribute("producto") ProductoDTO dto,
                                      BindingResult bindingResult,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {

        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);

        if (resumen.isEmpty()) {
            return "redirect:/hogar";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Revisa los datos del producto");
            return "redirect:/hogar";
        }

        dto.setIdProducto(id);
        try {
            productoService.actualizar(resumen.get().getIdHogar(), dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Producto actualizado");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        return "redirect:/hogar";
    }

    // ===================== ADMINISTRACIÓN (dropdown "Mi hogar") =====================
    @GetMapping("/administrar")
    public String administrar(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("menuActivo", "hogar");
        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);

        if (resumen.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Primero crea tu hogar");
            return "redirect:/hogar";
        }

        model.addAttribute("hogarActual", resumen.get());
        model.addAttribute("miembros", hogarService.obtenerMiembros(resumen.get().getIdHogar()));
        model.addAttribute("inventarios", inventarioService.listarPorHogar(resumen.get().getIdHogar()));

        if (!model.containsAttribute("hogarEditar")) {
            model.addAttribute("hogarEditar", new HogarDTO(resumen.get().getNombre()));
        }

        return "hogar-administrar";
    }

    @PostMapping("/renombrar")
    public String renombrar(@Valid @ModelAttribute("hogarEditar") HogarDTO dto,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);

        if (bindingResult.hasErrors()) {
            model.addAttribute("menuActivo", "hogar");
            resumen.ifPresent(r -> {
                model.addAttribute("hogarActual", r);
                model.addAttribute("miembros", hogarService.obtenerMiembros(r.getIdHogar()));
                model.addAttribute("inventarios", inventarioService.listarPorHogar(r.getIdHogar()));
            });
            return "hogar-administrar";
        }

        try {
            hogarService.renombrarHogar(resumen.get().getIdHogar(), idUsuario, dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "El nombre del hogar se actualizó correctamente");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        return "redirect:/hogar/administrar";
    }

    @PostMapping("/eliminar")
    public String eliminar(HttpSession session, RedirectAttributes redirectAttributes) {
        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);

        if (resumen.isPresent()) {
            try {
                hogarService.eliminarHogar(resumen.get().getIdHogar(), idUsuario);
                redirectAttributes.addFlashAttribute("mensajeExito", "El hogar fue eliminado");
                return "redirect:/hogar";
            } catch (IllegalStateException ex) {
                redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            }
        }

        return "redirect:/hogar/administrar";
    }

    @PostMapping("/inventario/capacidad")
    public String actualizarCapacidad(@RequestParam("idInventario") Integer idInventario,
                                       @RequestParam("capacidad") Integer capacidad,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);

        if (resumen.isPresent()) {
            try {
                if (!resumen.get().isEsPropietario()) {
                    throw new IllegalStateException("Solo el propietario puede cambiar el tamaño");
                }
                inventarioService.actualizarCapacidad(resumen.get().getIdHogar(), idInventario, capacidad);
                redirectAttributes.addFlashAttribute("mensajeExito", "Tamaño actualizado");
            } catch (IllegalStateException | IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            }
        }

        return "redirect:/hogar/administrar";
    }

    // Invitar externos: se queda deliberadamente "hardcoded" (no se persiste nada real),
    // solo confirma en pantalla que la invitacion "se envió".
    @PostMapping("/invitar")
    public String invitar(@RequestParam("correoInvitado") String correoInvitado,
                           RedirectAttributes redirectAttributes) {
        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);
        if (resumen.isPresent()) {
            try {
                hogarService.agregarMiembro(resumen.get().getIdHogar(), idUsuario, correoInvitado);
                redirectAttributes.addFlashAttribute("mensajeExito",
                        correoInvitado.trim().toLowerCase() + " ahora forma parte de tu hogar");
            } catch (IllegalStateException | IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            }
        }
        return "redirect:/hogar/administrar";
    }

    @PostMapping("/miembro/quitar/{idMiembro}")
    public String quitarMiembro(@PathVariable("idMiembro") Integer idMiembro,
                                 RedirectAttributes redirectAttributes) {
        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);
        if (resumen.isPresent()) {
            try {
                hogarService.quitarMiembro(resumen.get().getIdHogar(), idUsuario, idMiembro);
                redirectAttributes.addFlashAttribute("mensajeExito", "El integrante salió del hogar");
            } catch (IllegalStateException | IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            }
        }
        return "redirect:/hogar/administrar";
    }

    @PostMapping("/inventario/estilo")
    public String actualizarEstilo(@RequestParam("idInventario") Integer idInventario,
                                    @RequestParam("estilo") String estilo,
                                    RedirectAttributes redirectAttributes) {
        Integer idUsuario = UsuarioActual.id();
        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(idUsuario);
        if (resumen.isPresent()) {
            if (!resumen.get().isEsPropietario()) {
                redirectAttributes.addFlashAttribute("mensajeError", "Solo el propietario puede cambiar el estilo");
                return "redirect:/hogar/administrar";
            }
            try {
                inventarioService.actualizarEstilo(resumen.get().getIdHogar(), idInventario, estilo);
                redirectAttributes.addFlashAttribute("mensajeExito", "Estilo actualizado");
            } catch (IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            }
        }
        return "redirect:/hogar/administrar";
    }

    // Lo llama el formulario de alta (fetch) para autocompletar nombre y categoria
    // a partir del codigo de barras, consultando Open Food Facts.
    @GetMapping("/producto/externo")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> buscarProductoExterno(@RequestParam("codigo") String codigo) {
        if (codigo == null || !codigo.matches("\\d{8,13}")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El código de barras debe tener de 8 a 13 dígitos"));
        }
        try {
            Optional<ProductoExternoDTO> producto = openFoodFactsClient.buscarPorCodigo(codigo);
            if (producto.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("mensaje", "No encontramos ese código; captura los datos a mano"));
            }
            return ResponseEntity.ok(Map.of(
                    "nombre", producto.get().getNombre(),
                    "categoria", producto.get().getCategoriaSugerida()));
        } catch (ServicioExternoNoDisponibleException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("mensaje", ex.getMessage()));
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, "nombre", new TituloConverter());
    }
}
