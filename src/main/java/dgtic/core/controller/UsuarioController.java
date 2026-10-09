package dgtic.core.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import dgtic.core.security.UsuarioActual;
import dgtic.core.model.dto.CambiarContrasenaDTO;
import dgtic.core.model.dto.PerfilUsuarioDTO;
import dgtic.core.model.entity.UsuarioBd;
import dgtic.core.service.UsuarioService;
import dgtic.core.validation.CambiarContrasenaValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CambiarContrasenaValidator cambiarContrasenaValidator;

    @GetMapping
    public String verPerfil(HttpSession session, Model model) {
        model.addAttribute("menuActivo", "usuario");
        Integer idUsuario = UsuarioActual.id();
        UsuarioBd usuario = usuarioService.obtenerPorId(idUsuario);

        model.addAttribute("usuarioBd", usuario);
        if (!model.containsAttribute("perfil")) {
            model.addAttribute("perfil", new PerfilUsuarioDTO(usuario.getNombre()));
        }
        if (!model.containsAttribute("cambioContrasena")) {
            model.addAttribute("cambioContrasena", new CambiarContrasenaDTO());
        }
        model.addAttribute("puedeEliminarCuenta", usuarioService.puedeEliminarCuenta(idUsuario));
        return "usuario";
    }

    @PostMapping("/actualizar")
    public String actualizarPerfil(@Valid @ModelAttribute("perfil") PerfilUsuarioDTO dto,
                                    BindingResult bindingResult,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {

        Integer idUsuario = UsuarioActual.id();

        if (bindingResult.hasErrors()) {
            model.addAttribute("menuActivo", "usuario");
            model.addAttribute("usuarioBd", usuarioService.obtenerPorId(idUsuario));
            model.addAttribute("cambioContrasena", new CambiarContrasenaDTO());
            model.addAttribute("puedeEliminarCuenta", usuarioService.puedeEliminarCuenta(idUsuario));
            return "usuario";
        }

        usuarioService.actualizarPerfil(idUsuario, dto);
        // El menu toma el nombre del usuario autenticado: se actualiza tambien ahi.
        UsuarioActual.principal().setNombre(dto.getNombre());
        redirectAttributes.addFlashAttribute("mensajeExito", "Tu perfil se actualizó correctamente");
        return "redirect:/usuario";
    }

    @PostMapping("/cambiar-contrasena")
    public String cambiarContrasena(@Valid @ModelAttribute("cambioContrasena") CambiarContrasenaDTO dto,
                                     BindingResult bindingResult,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {

        Integer idUsuario = UsuarioActual.id();

        if (bindingResult.hasErrors()) {
            model.addAttribute("menuActivo", "usuario");
            model.addAttribute("usuarioBd", usuarioService.obtenerPorId(idUsuario));
            model.addAttribute("perfil", new PerfilUsuarioDTO(usuarioService.obtenerPorId(idUsuario).getNombre()));
            model.addAttribute("puedeEliminarCuenta", usuarioService.puedeEliminarCuenta(idUsuario));
            return "usuario";
        }

        try {
            usuarioService.cambiarContrasena(idUsuario, dto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Tu contraseña se actualizó correctamente");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }

        return "redirect:/usuario";
    }

    @PostMapping("/eliminar")
    public String eliminarCuenta(HttpSession session, HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        Integer idUsuario = UsuarioActual.id();

        if (!usuarioService.puedeEliminarCuenta(idUsuario)) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "No puedes eliminar tu cuenta mientras seas propietario de un hogar activo. Elimina el hogar primero.");
            return "redirect:/usuario";
        }

        usuarioService.eliminarCuenta(idUsuario);
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @InitBinder("cambioContrasena")
    public void initBinderContrasena(WebDataBinder binder) {
        binder.addValidators(cambiarContrasenaValidator);
    }
}
