package dgtic.core.service;

import dgtic.core.model.dto.ProductoDTO;
import dgtic.core.model.entity.ProductoBd;

import java.util.List;

public interface ProductoService {

    List<ProductoBd> listar(Integer idInventario);

    // Lanza IllegalArgumentException si el inventario no es del hogar,
    // o IllegalStateException si ya se alcanzo la capacidad maxima.
    ProductoBd agregar(Integer idHogar, ProductoDTO dto);

    // Lanza IllegalArgumentException si el producto no pertenece a un inventario de ese hogar
    ProductoBd actualizar(Integer idHogar, ProductoDTO dto);

    void eliminar(Integer idHogar, Integer idProducto);
}
