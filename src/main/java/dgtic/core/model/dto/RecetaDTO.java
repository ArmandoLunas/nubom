package dgtic.core.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// Formulario (web) y cuerpo de solicitud (API) de una receta.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecetaDTO {

    @NotBlank(message = "El nombre de la receta es obligatorio")
    @Size(max = 120, message = "El nombre no puede pasar de 120 caracteres")
    private String nombre;

    @NotBlank(message = "Agrega una descripción breve")
    @Size(max = 500, message = "La descripción no puede pasar de 500 caracteres")
    private String descripcion;

    @NotBlank(message = "Escribe los pasos de preparación")
    private String pasos;

    @NotNull(message = "Indica el tiempo de preparación")
    @Min(value = 1, message = "El tiempo debe ser de al menos 1 minuto")
    @Max(value = 1440, message = "El tiempo no puede pasar de 1440 minutos")
    private Integer tiempoMinutos;

    @Valid
    private List<IngredienteDTO> ingredientes = new ArrayList<>();
}
