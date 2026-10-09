package dgtic.core.service;

import dgtic.core.model.dto.ProductoDTO;
import dgtic.core.model.entity.InventarioBd;
import dgtic.core.model.entity.ProductoBd;
import dgtic.core.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioService inventarioService;

    @Transactional(readOnly = true)
    @Override
    public List<ProductoBd> listar(Integer idInventario) {
        return productoRepository.findByInventario_IdInventarioAndActivoTrueOrderByFechaIngresoDesc(idInventario);
    }

    @Transactional
    @Override
    public ProductoBd agregar(Integer idHogar, ProductoDTO dto) {
        // Valida que el inventario destino exista Y pertenezca al hogar del usuario
        InventarioBd inventario = inventarioService.obtenerDeHogar(idHogar, dto.getIdInventario());

        if (dto.getFechaCaducidad() != null && dto.getFechaCaducidad().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de caducidad no puede ser anterior a hoy");
        }

        // Regla de negocio: no se puede rebasar la capacidad maxima (numero de objetos)
        long ocupados = productoRepository.countByInventario_IdInventarioAndActivoTrue(inventario.getIdInventario());
        if (ocupados >= inventario.getCapacidadMaxima()) {
            throw new IllegalStateException(
                    "\"" + inventario.getNombre() + "\" ya está lleno (" + inventario.getCapacidadMaxima() + " objetos máx.)");
        }

        ProductoBd producto = ProductoBd.builder()
                .inventario(inventario)
                .nombre(dto.getNombre())
                .cantidad(dto.getCantidad())
                .unidad(dto.getUnidad())
                .categoria(dto.getCategoria())
                .fechaCaducidad(dto.getFechaCaducidad())
                .codigoBarras(vacioANulo(dto.getCodigoBarras()))
                .fechaIngreso(LocalDateTime.now())
                .activo(true)
                .build();
        return productoRepository.save(producto);
    }

    @Transactional
    @Override
    public ProductoBd actualizar(Integer idHogar, ProductoDTO dto) {
        ProductoBd producto = obtenerActivoDeHogar(idHogar, dto.getIdProducto());
        producto.setNombre(dto.getNombre());
        producto.setCantidad(dto.getCantidad());
        producto.setUnidad(dto.getUnidad());
        producto.setCategoria(dto.getCategoria());
        producto.setFechaCaducidad(dto.getFechaCaducidad());
        producto.setCodigoBarras(vacioANulo(dto.getCodigoBarras()));
        return productoRepository.save(producto);
    }

    @Transactional
    @Override
    public void eliminar(Integer idHogar, Integer idProducto) {
        // Baja logica: deja de ocupar espacio y de generar avisos, pero conserva su historial.
        ProductoBd producto = obtenerActivoDeHogar(idHogar, idProducto);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private ProductoBd obtenerActivoDeHogar(Integer idHogar, Integer idProducto) {
        ProductoBd producto = productoRepository.findById(idProducto)
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        // Valida que el producto sea de un inventario del hogar del usuario
        inventarioService.obtenerDeHogar(idHogar, producto.getInventario().getIdInventario());
        return producto;
    }

    private String vacioANulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
