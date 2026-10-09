package dgtic.core.rest.mapper;

import dgtic.core.model.entity.RolBd;
import dgtic.core.rest.dto.RolRequestDTO;
import dgtic.core.rest.dto.RolResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class RolMapper {

    public RolBd toEntity(RolRequestDTO dto) {
        return RolBd.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .build();
    }

    public RolResponseDTO toResponseDTO(RolBd entidad, long totalMembresiasActivas) {
        RolResponseDTO dto = new RolResponseDTO();
        dto.setIdRol(entidad.getIdRol());
        dto.setNombre(entidad.getNombre());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setTotalMembresiasActivas(totalMembresiasActivas);
        return dto;
    }
}
