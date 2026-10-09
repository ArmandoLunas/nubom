package dgtic.core.controller;

import dgtic.core.converter.CorreoNormalizadoConverter;
import dgtic.core.model.dto.RegistroUsuarioDTO;
import dgtic.core.service.UsuarioService;
import dgtic.core.validation.RegistroUsuarioValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RegistroUsuarioValidator registroUsuarioValidator;

    @GetMapping("/registro")
    public String verFormulario(Model model) {
        if (!model.containsAttribute("registro")) {
            model.addAttribute("registro", new RegistroUsuarioDTO());
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registro") RegistroUsuarioDTO dto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "registro";
        }

        usuarioService.registrar(dto);

        // La cuenta queda creada; el inicio de sesion lo hace Spring Security
        // con el formulario, para que haya un unico camino de autenticacion.
        redirectAttributes.addFlashAttribute("mensajeExito", "Tu cuenta se creó. Inicia sesión para continuar.");
        redirectAttributes.addFlashAttribute("correoRegistrado", dto.getCorreo());
        return "redirect:/login";
    }

    @InitBinder("registro")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(registroUsuarioValidator);
        binder.registerCustomEditor(String.class, "correo", new CorreoNormalizadoConverter());
    }
}
