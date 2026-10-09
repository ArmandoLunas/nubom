package dgtic.core.converter;

import java.beans.PropertyEditorSupport;

// Conversion: normaliza el correo (quita espacios y lo pasa a minusculas)
// antes de que llegue al DTO. Evita que "Armando@Correo.com" y
// "armando@correo.com" se traten como cuentas distintas.
public class CorreoNormalizadoConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {
        if (text == null) {
            setValue(null);
            return;
        }
        setValue(text.trim().toLowerCase());
    }
}
