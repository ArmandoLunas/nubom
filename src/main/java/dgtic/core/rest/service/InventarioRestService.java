package dgtic.core.rest.service;

import java.time.LocalDate;
import dgtic.core.model.entity.HogarBd;
import dgtic.core.model.entity.InventarioBd;
import dgtic.core.model.entity.InventarioTipoBd;
import dgtic.core.model.entity.ProductoBd;
import dgtic.core.repository.HogarRepository;
import dgtic.core.repository.InventarioRepository;
import dgtic.core.repository.InventarioTipoRepository;
import dgtic.core.repository.ProductoRepository;
import dgtic.core.rest.dto.InventarioRequestDTO;
import dgtic.core.rest.dto.ProductoRequestDTO;
import dgtic.core.rest.exception.ConflictoIntegridadException;
import dgtic.core.rest.exception.RecursoNoEncontradoException;
import dgtic.core.rest.exception.SolicitudInvalidaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

// Servicio REST para InventarioBd: es el lado "N" de hogar->inventario y el
// lado "1" de inventario->producto, asi que concentra las dos relaciones 1:N
// centrales de la API (crear/consultar/eliminar informacion asociada).
@Service
@RequiredArgsConstructor
public class InventarioRestService {

    // Mismo catalogo de tamaños permitidos que usa la app Thymeleaf
    // (ver dgtic.core.service.InventarioServiceImpl), documentado tambien
    // aqui porque la API REST expone su propia validacion independiente.
    private static final Set<Integer> TAMANOS_PERMITIDOS = Set.of(20, 40, 80);
    private static final Set<String> ESTILOS_PERMITIDOS = Set.of("clasico", "moderno", "retro");

    private final InventarioRepository inventarioRepository;
    private final HogarRepository hogarRepository;
    private final InventarioTipoRepository inventarioTipoRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public InventarioBd obtenerPorId(Integer idInventario) {
        return inventarioRepository.findById(idInventario)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un inventario con id " + idInventario));
    }

    // Listado general de inventarios, con filtro opcional por hogar (query param).
    // Sin filtro, devuelve los inventarios de todos los hogares.
    @Transactional(readOnly = true)
    public List<InventarioBd> listar(Integer idHogar) {
        if (idHogar == null) {
            return inventarioRepository.findAll();
        }
        return listarPorHogar(idHogar);
    }

    // ---- Relacion 1:N hogar -> inventario ----

    @Transactional(readOnly = true)
    public List<InventarioBd> listarPorHogar(Integer idHogar) {
        obtenerHogar(idHogar); // valida que el hogar exista antes de listar sus inventarios
        return inventarioRepository.findByHogar_IdHogarOrderByFechaCreacionDesc(idHogar);
    }

    @Transactional
    public InventarioBd crearParaHogar(Integer idHogar, InventarioRequestDTO dto) {
        HogarBd hogar = obtenerHogar(idHogar);
        InventarioTipoBd tipo = inventarioTipoRepository.findById(dto.getIdTipo())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un tipo de inventario con id " + dto.getIdTipo()));

        validarCapacidad(dto.getCapacidadMaxima());

        InventarioBd inventario = InventarioBd.builder()
                .hogar(hogar)
                .tipo(tipo)
                .nombre(dto.getNombre())
                .capacidadMaxima(dto.getCapacidadMaxima())
                .fechaCreacion(LocalDateTime.now())
                .build();
        return inventarioRepository.save(inventario);
    }

    @Transactional
    public InventarioBd actualizar(Integer idInventario, InventarioRequestDTO dto) {
        InventarioBd inventario = obtenerPorId(idInventario);

        if (dto.getIdTipo() != null && !dto.getIdTipo().equals(inventario.getTipo().getIdTipo())) {
            InventarioTipoBd tipo = inventarioTipoRepository.findById(dto.getIdTipo())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe un tipo de inventario con id " + dto.getIdTipo()));
            inventario.setTipo(tipo);
        }

        validarCapacidad(dto.getCapacidadMaxima());
        long ocupados = productoRepository.countByInventario_IdInventarioAndActivoTrue(idInventario);
        if (dto.getCapacidadMaxima() < ocupados) {
            throw new ConflictoIntegridadException(
                    "No se puede reducir \"" + inventario.getNombre() + "\" a " + dto.getCapacidadMaxima() +
                            " espacios: ya tiene " + ocupados + " producto(s) guardado(s)");
        }

        inventario.setNombre(dto.getNombre());
        inventario.setCapacidadMaxima(dto.getCapacidadMaxima());
        if (dto.getEstilo() != null && !dto.getEstilo().isBlank()) {
            validarEstilo(dto.getEstilo());
            inventario.setEstilo(dto.getEstilo());
        }
        return inventarioRepository.save(inventario);
    }

    // Al eliminar el inventario, sus productos se eliminan en cascada gracias
    // a la relacion 1:N (cascade = ALL, orphanRemoval = true) en InventarioBd.
    @Transactional
    public void eliminar(Integer idInventario) {
        InventarioBd inventario = obtenerPorId(idInventario);
        inventarioRepository.delete(inventario);
    }

    // ---- Relacion 1:N inventario -> producto ----

    @Transactional(readOnly = true)
    public List<ProductoBd> listarProductos(Integer idInventario) {
        obtenerPorId(idInventario); // valida que el inventario exista
        return productoRepository.findByInventario_IdInventarioAndActivoTrueOrderByFechaIngresoDesc(idInventario);
    }

    @Transactional
    public ProductoBd agregarProducto(Integer idInventario, ProductoRequestDTO dto) {
        InventarioBd inventario = obtenerPorId(idInventario);

        if (dto.getFechaCaducidad() != null && dto.getFechaCaducidad().isBefore(LocalDate.now())) {
            throw new SolicitudInvalidaException("La fecha de caducidad no puede ser anterior a hoy");
        }
        long ocupados = productoRepository.countByInventario_IdInventarioAndActivoTrue(idInventario);
        if (ocupados >= inventario.getCapacidadMaxima()) {
            throw new ConflictoIntegridadException(
                    "\"" + inventario.getNombre() + "\" ya está lleno (" + inventario.getCapacidadMaxima() + " objetos máx.)");
        }

        ProductoBd producto = ProductoBd.builder()
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
        return productoRepository.save(producto);
    }

    private void validarEstilo(String estilo) {
        if (!ESTILOS_PERMITIDOS.contains(estilo)) {
            throw new SolicitudInvalidaException("El estilo debe ser clasico, moderno o retro");
        }
    }

    private void validarCapacidad(Integer capacidad) {
        if (capacidad == null || !TAMANOS_PERMITIDOS.contains(capacidad)) {
            throw new SolicitudInvalidaException("La capacidad máxima debe ser 20, 40 u 80 objetos");
        }
    }

    private HogarBd obtenerHogar(Integer idHogar) {
        return hogarRepository.findById(idHogar)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un hogar con id " + idHogar));
    }
}
