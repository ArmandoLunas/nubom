package dgtic.core.rest.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import dgtic.core.model.entity.InventarioBd;
import dgtic.core.model.entity.ProductoBd;
import dgtic.core.repository.HogarRepository;
import dgtic.core.repository.InventarioRepository;
import dgtic.core.repository.ProductoRepository;
import dgtic.core.rest.dto.ProductoRequestDTO;
import dgtic.core.rest.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoRestService {

    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final HogarRepository hogarRepository;

    // Listado general de productos, con filtros opcionales por hogar y/o
    // inventario (query params). Si se dan ambos, el inventario debe
    // pertenecer al hogar indicado. Sin filtros, devuelve todos los productos
    // activos de todos los hogares.
    @Transactional(readOnly = true)
    public Page<ProductoBd> listar(Integer idHogar, Integer idInventario, String categoria, Pageable pageable) {
        if (idInventario != null) {
            InventarioBd inventario = inventarioRepository.findById(idInventario)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe un inventario con id " + idInventario));
            if (idHogar != null && !inventario.getHogar().getIdHogar().equals(idHogar)) {
                throw new RecursoNoEncontradoException(
                        "El inventario " + idInventario + " no pertenece al hogar " + idHogar);
            }
        }
        if (idHogar != null && !hogarRepository.existsById(idHogar)) {
            throw new RecursoNoEncontradoException("No existe un hogar con id " + idHogar);
        }
        String filtroCategoria = (categoria == null || categoria.isBlank()) ? null : categoria.trim();
        return productoRepository.buscar(idHogar, idInventario, filtroCategoria, pageable);
    }

    @Transactional(readOnly = true)
    public ProductoBd obtenerPorId(Integer idProducto) {
        return productoRepository.findById(idProducto)
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un producto con id " + idProducto));
    }

    @Transactional
    public ProductoBd actualizar(Integer idProducto, ProductoRequestDTO dto) {
        ProductoBd producto = obtenerPorId(idProducto);
        producto.setNombre(dto.getNombre());
        producto.setCantidad(dto.getCantidad());
        producto.setUnidad(dto.getUnidad());
        producto.setCategoria(dto.getCategoria());
        producto.setFechaCaducidad(dto.getFechaCaducidad());
        producto.setCodigoBarras(dto.getCodigoBarras());
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminar(Integer idProducto) {
        ProductoBd producto = obtenerPorId(idProducto);
        // Baja logica: el producto conserva su historial y sus avisos.
        producto.setActivo(false);
        productoRepository.save(producto);
    }
}
