package dgtic.core.converter;

import java.beans.PropertyEditorSupport;

// Conversion: convierte un texto libre a "Formato Título"
// (ej. "casa luna"  ->  "Casa Luna"), usado para nombres de hogar e inventario.
public class TituloConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {
        if (text == null || text.isBlank()) {
            setValue(text);
            return;
        }
        String limpio = text.trim().replaceAll("\\s+", " ");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : limpio.split(" ")) {
            if (palabra.isEmpty()) continue;
            resultado.append(Character.toUpperCase(palabra.charAt(0)))
                    .append(palabra.substring(1).toLowerCase())
                    .append(" ");
        }
        setValue(resultado.toString().trim());
    }
}
