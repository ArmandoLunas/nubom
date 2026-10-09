package dgtic.core.service;

import dgtic.core.model.entity.HogarBd;
import dgtic.core.model.entity.InventarioBd;
import dgtic.core.model.entity.InventarioTipoBd;
import dgtic.core.repository.HogarRepository;
import dgtic.core.repository.InventarioRepository;
import dgtic.core.repository.InventarioTipoRepository;
import dgtic.core.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private InventarioTipoRepository inventarioTipoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private HogarRepository hogarRepository;

    // ============================================================
    // REGLA DE NEGOCIO: cada hogar recibe automaticamente, al crearse,
    // exactamente 2 inventarios: un Refrigerador y una Alacena.
    // La capacidad maxima se expresa en NUMERO DE OBJETOS que caben
    // (no en litros ni kilogramos). Al crearse toma un valor por
    // defecto, pero el propietario puede cambiarlo despues entre
    // los tamaños permitidos: 20, 40 u 80 objetos.
    // ============================================================
    private static final int CAPACIDAD_REFRIGERADOR_INICIAL = 20; // objetos
    private static final int CAPACIDAD_ALACENA_INICIAL = 40;      // objetos
    private static final Set<Integer> TAMANOS_PERMITIDOS = Set.of(20, 40, 80);
    private static final Set<String> ESTILOS_PERMITIDOS = Set.of("clasico", "moderno", "retro");

    @Transactional
    @Override
    public void crearInventariosIniciales(Integer idHogar) {
        HogarBd hogar = hogarRepository.findById(idHogar)
                .orElseThrow(() -> new IllegalArgumentException("Hogar no encontrado"));

        InventarioTipoBd refrigerador = inventarioTipoRepository.findAll().stream()
                .filter(t -> "Refrigerador".equalsIgnoreCase(t.getNombre()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El catálogo de tipos de inventario no está inicializado"));

        InventarioTipoBd alacena = inventarioTipoRepository.findAll().stream()
                .filter(t -> "Alacena".equalsIgnoreCase(t.getNombre()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("El catálogo de tipos de inventario no está inicializado"));

        InventarioBd inventarioRefrigerador = InventarioBd.builder()
                .hogar(hogar)
                .tipo(refrigerador)
                .nombre("Refrigerador")
                .capacidadMaxima(CAPACIDAD_REFRIGERADOR_INICIAL)
                .fechaCreacion(LocalDateTime.now())
                .build();

        InventarioBd inventarioAlacena = InventarioBd.builder()
                .hogar(hogar)
                .tipo(alacena)
                .nombre("Alacena")
                .capacidadMaxima(CAPACIDAD_ALACENA_INICIAL)
                .fechaCreacion(LocalDateTime.now())
                .build();

        inventarioRepository.save(inventarioRefrigerador);
        inventarioRepository.save(inventarioAlacena);
    }

    @Transactional(readOnly = true)
    @Override
    public List<InventarioBd> listarPorHogar(Integer idHogar) {
        return inventarioRepository.findByHogar_IdHogarOrderByFechaCreacionDesc(idHogar);
    }

    @Transactional(readOnly = true)
    @Override
    public InventarioBd obtenerDeHogar(Integer idHogar, Integer idInventario) {
        InventarioBd inventario = inventarioRepository.findById(idInventario)
                .orElseThrow(() -> new IllegalArgumentException("Inventario no encontrado"));

        if (!inventario.getHogar().getIdHogar().equals(idHogar)) {
            throw new IllegalArgumentException("Este inventario no pertenece a tu hogar");
        }

        return inventario;
    }

    @Transactional
    @Override
    public InventarioBd actualizarCapacidad(Integer idHogar, Integer idInventario, Integer nuevaCapacidad) {
        if (nuevaCapacidad == null || !TAMANOS_PERMITIDOS.contains(nuevaCapacidad)) {
            throw new IllegalArgumentException("El tamaño debe ser 20, 40 u 80 espacios");
        }

        InventarioBd inventario = obtenerDeHogar(idHogar, idInventario);

        long ocupados = productoRepository.countByInventario_IdInventarioAndActivoTrue(idInventario);
        if (nuevaCapacidad < ocupados) {
            throw new IllegalStateException(
                    "No puedes reducir \"" + inventario.getNombre() + "\" a " + nuevaCapacidad +
                            " espacios: ya tiene " + ocupados + " producto(s) guardado(s). Quita alguno primero.");
        }

        inventario.setCapacidadMaxima(nuevaCapacidad);
        return inventarioRepository.save(inventario);
    }

    @Transactional
    @Override
    public InventarioBd actualizarEstilo(Integer idHogar, Integer idInventario, String estilo) {
        if (estilo == null || !ESTILOS_PERMITIDOS.contains(estilo)) {
            throw new IllegalArgumentException("El estilo debe ser clásico, moderno o retro");
        }
        InventarioBd inventario = obtenerDeHogar(idHogar, idInventario);
        inventario.setEstilo(estilo);
        return inventarioRepository.save(inventario);
    }
}
