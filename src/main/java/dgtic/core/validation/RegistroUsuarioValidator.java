package dgtic.core.validation;

import dgtic.core.model.dto.RegistroUsuarioDTO;
import dgtic.core.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

// Reglas de negocio del registro que no se pueden expresar con anotaciones simples:
// 1) El correo no debe repetirse en la base de datos.
// 2) La contraseña y su confirmacion deben coincidir.
@Component
public class RegistroUsuarioValidator implements Validator {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return RegistroUsuarioDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RegistroUsuarioDTO dto = (RegistroUsuarioDTO) target;

        if (dto.getCorreo() != null && !dto.getCorreo().isBlank()
                && usuarioRepository.existsByCorreoIgnoreCase(dto.getCorreo())) {
            errors.rejectValue("correo", "correo.duplicado", "Este correo ya está registrado");
        }

        if (dto.getContrasena() != null && dto.getConfirmarContrasena() != null
                && !dto.getContrasena().equals(dto.getConfirmarContrasena())) {
            errors.rejectValue("confirmarContrasena", "confirmar.nomatch", "Las contraseñas no coinciden");
        }
    }
}
