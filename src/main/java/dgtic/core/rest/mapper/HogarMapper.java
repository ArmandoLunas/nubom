package dgtic.core.rest.mapper;

import dgtic.core.model.entity.HogarBd;
import dgtic.core.rest.dto.HogarRequestDTO;
import dgtic.core.rest.dto.HogarResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Clase Mapper: convierte entre la entidad HogarBd y sus DTO de entrada/salida.
@Component
public class HogarMapper {

    public HogarBd toEntity(HogarRequestDTO dto) {
        return HogarBd.builder()
                .nombre(dto.getNombre())
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    public HogarResponseDTO toResponseDTO(HogarBd entidad) {
        HogarResponseDTO dto = new HogarResponseDTO();
        dto.setIdHogar(entidad.getIdHogar());
        dto.setNombre(entidad.getNombre());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setTotalInventarios(entidad.getInventarios() != null ? entidad.getInventarios().size() : 0);
        dto.setTotalListaCompra(entidad.getListaCompra() != null ? entidad.getListaCompra().size() : 0);
        return dto;
    }
}
