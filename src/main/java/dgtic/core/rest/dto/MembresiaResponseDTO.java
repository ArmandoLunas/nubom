package dgtic.core.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO de salida para la relacion N:M usuario<->hogar (tabla usuario_hogar).
// Aplana los tres lados (usuario, hogar, rol) para evitar exponer las
// entidades completas y sus colecciones asociadas.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembresiaResponseDTO {
    private Integer idUsuarioHogar;
    private Integer idUsuario;
    private String nombreUsuario;
    private Integer idHogar;
    private String nombreHogar;
    private Integer idRol;
    private String nombreRol;
    private LocalDateTime fechaUnion;
    private Boolean activo;
}
