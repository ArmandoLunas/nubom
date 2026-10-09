package dgtic.core.validation;

import dgtic.core.model.dto.IngredienteDTO;
import dgtic.core.model.dto.RecetaDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

// Regla que no cabe en una anotacion de campo: una receta necesita al menos
// un ingrediente y al menos uno de ellos debe estar marcado como principal.
@Component
public class RecetaValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return RecetaDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RecetaDTO dto = (RecetaDTO) target;
        if (dto.getIngredientes() == null || dto.getIngredientes().isEmpty()) {
            errors.rejectValue("ingredientes", "ingredientes.vacio", "Agrega al menos un ingrediente");
            return;
        }
        boolean hayPrincipal = dto.getIngredientes().stream().anyMatch(IngredienteDTO::isPrincipal);
        if (!hayPrincipal) {
            errors.rejectValue("ingredientes", "ingredientes.sinPrincipal",
                    "Marca al menos un ingrediente como principal");
        }
    }
}
