package dgtic.core.validation;

import dgtic.core.model.dto.CambiarContrasenaDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

// La contraseña actual se valida contra la BD en el servicio (necesita saber que
// usuario es), asi que aqui solo validamos que la nueva contraseña y su
// confirmacion coincidan.
@Component
public class CambiarContrasenaValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return CambiarContrasenaDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CambiarContrasenaDTO dto = (CambiarContrasenaDTO) target;
        if (dto.getContrasenaNueva() != null && dto.getConfirmarContrasenaNueva() != null
                && !dto.getContrasenaNueva().equals(dto.getConfirmarContrasenaNueva())) {
            errors.rejectValue("confirmarContrasenaNueva", "confirmar.nomatch", "Las contraseñas no coinciden");
        }
    }
}
