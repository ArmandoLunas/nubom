package dgtic.core.rest.mapper;

import dgtic.core.model.entity.HogarBd;
import dgtic.core.model.entity.ListaCompraBd;
import dgtic.core.rest.dto.ListaCompraRequestDTO;
import dgtic.core.rest.dto.ListaCompraResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ListaCompraMapper {

    public ListaCompraBd toEntity(ListaCompraRequestDTO dto, HogarBd hogar) {
        return ListaCompraBd.builder()
                .hogar(hogar)
                .nombre(dto.getNombre())
                .categoria(dto.getCategoria())
                .cantidad(dto.getCantidad())
                .unidad(dto.getUnidad())
                .comprado(dto.getComprado() != null && dto.getComprado())
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    public ListaCompraResponseDTO toResponseDTO(ListaCompraBd entidad) {
        ListaCompraResponseDTO dto = new ListaCompraResponseDTO();
        dto.setIdItem(entidad.getIdItem());
        dto.setNombre(entidad.getNombre());
        dto.setCategoria(entidad.getCategoria());
        dto.setCantidad(entidad.getCantidad());
        dto.setUnidad(entidad.getUnidad());
        dto.setComprado(entidad.getComprado());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setIdHogar(entidad.getHogar() != null ? entidad.getHogar().getIdHogar() : null);
        return dto;
    }
}
