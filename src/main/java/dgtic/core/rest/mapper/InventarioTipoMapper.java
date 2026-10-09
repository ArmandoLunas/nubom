package dgtic.core.rest.mapper;

import dgtic.core.model.entity.InventarioTipoBd;
import dgtic.core.rest.dto.InventarioTipoRequestDTO;
import dgtic.core.rest.dto.InventarioTipoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class InventarioTipoMapper {

    public InventarioTipoBd toEntity(InventarioTipoRequestDTO dto) {
        return InventarioTipoBd.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .icono(dto.getIcono())
                .build();
    }

    public InventarioTipoResponseDTO toResponseDTO(InventarioTipoBd entidad) {
        InventarioTipoResponseDTO dto = new InventarioTipoResponseDTO();
        dto.setIdTipo(entidad.getIdTipo());
        dto.setNombre(entidad.getNombre());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setIcono(entidad.getIcono());
        dto.setTotalInventarios(entidad.getInventarios() != null ? entidad.getInventarios().size() : 0);
        return dto;
    }
}
