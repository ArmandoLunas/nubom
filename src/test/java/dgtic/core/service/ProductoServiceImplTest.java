package dgtic.core.service;

import dgtic.core.model.dto.ProductoDTO;
import dgtic.core.model.entity.InventarioBd;
import dgtic.core.model.entity.ProductoBd;
import dgtic.core.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Reglas de negocio del inventario, con el repositorio simulado (Mockito).
@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    private static final int ID_HOGAR = 1;
    private static final int ID_INVENTARIO = 10;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private ProductoServiceImpl servicio;

    private InventarioBd refrigerador;

    @BeforeEach
    void preparar() {
        refrigerador = InventarioBd.builder()
                .idInventario(ID_INVENTARIO).nombre("Refrigerador").capacidadMaxima(20).build();
    }

    @Test
    void agregaElProductoCuandoHayEspacio() {
        when(inventarioService.obtenerDeHogar(ID_HOGAR, ID_INVENTARIO)).thenReturn(refrigerador);
        when(productoRepository.countByInventario_IdInventarioAndActivoTrue(ID_INVENTARIO)).thenReturn(19L);
        when(productoRepository.save(any(ProductoBd.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoBd creado = servicio.agregar(ID_HOGAR, producto(LocalDate.now().plusDays(5), ""));

        assertEquals("Leche", creado.getNombre());
        assertTrue(creado.getActivo());
        assertNull(creado.getCodigoBarras(), "un código vacío se guarda como nulo");
        assertEquals("VIGENTE", creado.getEstadoCaducidad());
    }

    @Test
    void rechazaElAltaCuandoElInventarioEstaLleno() {
        when(inventarioService.obtenerDeHogar(ID_HOGAR, ID_INVENTARIO)).thenReturn(refrigerador);
        when(productoRepository.countByInventario_IdInventarioAndActivoTrue(ID_INVENTARIO)).thenReturn(20L);

        assertThrows(IllegalStateException.class, () -> servicio.agregar(ID_HOGAR, producto(null, null)));

        verify(productoRepository, never()).save(any());
    }

    @Test
    void rechazaUnaFechaDeCaducidadAnteriorAHoy() {
        when(inventarioService.obtenerDeHogar(ID_HOGAR, ID_INVENTARIO)).thenReturn(refrigerador);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.agregar(ID_HOGAR, producto(LocalDate.now().minusDays(1), null)));

        verify(productoRepository, never()).save(any());
    }

    @Test
    void noPermiteAgregarAUnInventarioDeOtroHogar() {
        when(inventarioService.obtenerDeHogar(ID_HOGAR, ID_INVENTARIO))
                .thenThrow(new IllegalArgumentException("Este inventario no pertenece a tu hogar"));

        assertThrows(IllegalArgumentException.class, () -> servicio.agregar(ID_HOGAR, producto(null, null)));
    }

    @Test
    void eliminarEsUnaBajaLogica() {
        ProductoBd existente = ProductoBd.builder()
                .idProducto(5).nombre("Leche").inventario(refrigerador).activo(true).build();
        when(productoRepository.findById(5)).thenReturn(Optional.of(existente));
        when(inventarioService.obtenerDeHogar(ID_HOGAR, ID_INVENTARIO)).thenReturn(refrigerador);

        servicio.eliminar(ID_HOGAR, 5);

        ArgumentCaptor<ProductoBd> guardado = ArgumentCaptor.forClass(ProductoBd.class);
        verify(productoRepository).save(guardado.capture());
        assertFalse(guardado.getValue().getActivo());
        verify(productoRepository, never()).deleteById(any());
    }

    @Test
    void elSemaforoDeCaducidadDependeDeLosDiasRestantes() {
        assertEquals("SIN_FECHA", conCaducidad(null).getEstadoCaducidad());
        assertEquals("VENCIDO", conCaducidad(LocalDate.now().minusDays(1)).getEstadoCaducidad());
        assertEquals("POR_VENCER", conCaducidad(LocalDate.now()).getEstadoCaducidad());
        assertEquals("POR_VENCER", conCaducidad(LocalDate.now().plusDays(3)).getEstadoCaducidad());
        assertEquals("VIGENTE", conCaducidad(LocalDate.now().plusDays(4)).getEstadoCaducidad());
    }

    private ProductoDTO producto(LocalDate caducidad, String codigo) {
        ProductoDTO dto = new ProductoDTO();
        dto.setIdInventario(ID_INVENTARIO);
        dto.setNombre("Leche");
        dto.setCantidad(1);
        dto.setUnidad("L");
        dto.setCategoria("Lácteos");
        dto.setFechaCaducidad(caducidad);
        dto.setCodigoBarras(codigo);
        return dto;
    }

    private ProductoBd conCaducidad(LocalDate fecha) {
        return ProductoBd.builder().nombre("X").fechaCaducidad(fecha).build();
    }
}
