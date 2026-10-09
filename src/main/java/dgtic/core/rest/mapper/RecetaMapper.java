package dgtic.core.rest.mapper;

import dgtic.core.model.dto.IngredienteDTO;
import dgtic.core.model.dto.RecetaVistaDTO;
import dgtic.core.model.entity.RecetaBd;
import dgtic.core.rest.dto.RecetaResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class RecetaMapper {

    // "resumen" aporta el promedio y el numero de calificaciones ya calculados.
    public RecetaResponseDTO toResponseDTO(RecetaBd entidad, RecetaVistaDTO resumen) {
        RecetaResponseDTO dto = new RecetaResponseDTO();
        dto.setIdReceta(entidad.getIdReceta());
        dto.setNombre(entidad.getNombre());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setPasos(entidad.getListaPasos());
        dto.setTiempoMinutos(entidad.getTiempoMinutos());
        dto.setEstado(entidad.getEstado().name());
        dto.setIdAutor(entidad.getAutor().getIdUsuario());
        dto.setAutor(entidad.getAutor().getNombre());
        dto.setIngredientes(entidad.getIngredientes().stream()
                .map(i -> new IngredienteDTO(i.getNombre(), i.getCantidad(), i.getUnidad(),
                        Boolean.TRUE.equals(i.getPrincipal())))
                .toList());
        dto.setPromedio(resumen.getPromedio());
        dto.setTotalCalificaciones(resumen.getTotalCalificaciones());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setFechaModificacion(entidad.getFechaModificacion());
        return dto;
    }
}
