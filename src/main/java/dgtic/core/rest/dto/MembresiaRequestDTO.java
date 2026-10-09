package dgtic.core.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de entrada para asociar un usuario con un hogar
// (POST /api/v1/usuarios/{idUsuario}/hogares/{idHogar}). idUsuario e idHogar
// viajan en la URL; aqui solo va el atributo propio de la asociacion N:M.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembresiaRequestDTO {

    @NotNull(message = "Debe indicarse el rol (idRol) del usuario en ese hogar")
    private Integer idRol;
}
