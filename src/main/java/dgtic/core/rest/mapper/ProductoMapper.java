package dgtic.core.rest.mapper;

import dgtic.core.model.entity.InventarioBd;
import dgtic.core.model.entity.ProductoBd;
import dgtic.core.rest.dto.ProductoCreateDTO;
import dgtic.core.rest.dto.ProductoRequestDTO;
import dgtic.core.rest.dto.ProductoResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProductoMapper {

    // Adapta el DTO "plano" (con idInventario incluido) al DTO de negocio que
    // ya usa InventarioRestService.agregarProducto, para reutilizar la misma
    // validacion de capacidad sin duplicar logica.
    public ProductoRequestDTO toRequestDTO(ProductoCreateDTO dto) {
        return new ProductoRequestDTO(dto.getNombre(), dto.getCantidad(), dto.getUnidad(), dto.getCategoria(),
                dto.getFechaCaducidad(), dto.getCodigoBarras());
    }

    public ProductoBd toEntity(ProductoRequestDTO dto, InventarioBd inventario) {
        return ProductoBd.builder()
                .inventario(inventario)
                .nombre(dto.getNombre())
                .cantidad(dto.getCantidad())
                .unidad(dto.getUnidad())
                .categoria(dto.getCategoria())
                .fechaCaducidad(dto.getFechaCaducidad())
                .codigoBarras(dto.getCodigoBarras())
                .fechaIngreso(LocalDateTime.now())
                .activo(true)
                .build();
    }

    public ProductoResponseDTO toResponseDTO(ProductoBd entidad) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setIdProducto(entidad.getIdProducto());
        dto.setNombre(entidad.getNombre());
        dto.setCantidad(entidad.getCantidad());
        dto.setUnidad(entidad.getUnidad());
        dto.setCategoria(entidad.getCategoria());
        dto.setFechaIngreso(entidad.getFechaIngreso());
        dto.setActivo(entidad.getActivo());
        dto.setFechaCaducidad(entidad.getFechaCaducidad());
        dto.setCodigoBarras(entidad.getCodigoBarras());
        dto.setEstadoCaducidad(entidad.getEstadoCaducidad());
        dto.setIdInventario(entidad.getInventario() != null ? entidad.getInventario().getIdInventario() : null);
        return dto;
    }
}
