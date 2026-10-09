package dgtic.core.rest.mapper;

import dgtic.core.model.entity.UsuarioHogarBd;
import dgtic.core.rest.dto.MembresiaResponseDTO;
import org.springframework.stereotype.Component;

// Clase Mapper de la relacion N:M usuario<->hogar (entidad de union
// UsuarioHogarBd). Aplana las tres relaciones @ManyToOne (usuario, hogar,
// rol) en un solo DTO, evitando exponer las entidades completas.
@Component
public class MembresiaMapper {

    public MembresiaResponseDTO toResponseDTO(UsuarioHogarBd entidad) {
        MembresiaResponseDTO dto = new MembresiaResponseDTO();
        dto.setIdUsuarioHogar(entidad.getIdUsuarioHogar());
        dto.setIdUsuario(entidad.getUsuario().getIdUsuario());
        dto.setNombreUsuario(entidad.getUsuario().getNombre());
        dto.setIdHogar(entidad.getHogar().getIdHogar());
        dto.setNombreHogar(entidad.getHogar().getNombre());
        dto.setIdRol(entidad.getRol().getIdRol());
        dto.setNombreRol(entidad.getRol().getNombre());
        dto.setFechaUnion(entidad.getFechaUnion());
        dto.setActivo(entidad.getActivo());
        return dto;
    }
}
