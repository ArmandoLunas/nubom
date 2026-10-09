package dgtic.core.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de entrada para crear/actualizar un hogar via la API REST.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HogarRequestDTO {

    @NotBlank(message = "El nombre del hogar es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;
}
