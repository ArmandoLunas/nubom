package dgtic.core.rest.mapper;

import dgtic.core.model.entity.HogarBd;
import dgtic.core.model.entity.InventarioBd;
import dgtic.core.model.entity.InventarioTipoBd;
import dgtic.core.rest.dto.InventarioRequestDTO;
import dgtic.core.rest.dto.InventarioResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Clase Mapper para InventarioBd. El armado de la relacion 1:N (asociar el
// hogar y el tipo) se hace aqui, pasando las entidades ya resueltas por el
// servicio (que valida su existencia antes de llamar a este mapper).
@Component
public class InventarioMapper {

    public InventarioBd toEntity(InventarioRequestDTO dto, HogarBd hogar, InventarioTipoBd tipo) {
        return InventarioBd.builder()
                .hogar(hogar)
                .tipo(tipo)
                .nombre(dto.getNombre())
                .capacidadMaxima(dto.getCapacidadMaxima())
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    public InventarioResponseDTO toResponseDTO(InventarioBd entidad) {
        InventarioResponseDTO dto = new InventarioResponseDTO();
        dto.setIdInventario(entidad.getIdInventario());
        dto.setNombre(entidad.getNombre());
        dto.setCapacidadMaxima(entidad.getCapacidadMaxima());
        dto.setEstilo(entidad.getEstilo());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setIdHogar(entidad.getHogar() != null ? entidad.getHogar().getIdHogar() : null);
        if (entidad.getTipo() != null) {
            dto.setIdTipo(entidad.getTipo().getIdTipo());
            dto.setTipoNombre(entidad.getTipo().getNombre());
        }
        dto.setTotalProductos(entidad.getProductos() != null ? entidad.getProductos().size() : 0);
        return dto;
    }
}
