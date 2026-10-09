package dgtic.core.service;

import dgtic.core.model.dto.ProductoCaducidadDTO;
import dgtic.core.model.dto.RecetaRankingDTO;
import dgtic.core.model.dto.ReporteCategoriaDTO;
import dgtic.core.model.dto.ReporteOcupacionDTO;
import dgtic.core.repository.InventarioRepository;
import dgtic.core.repository.ProductoRepository;
import dgtic.core.repository.RecetaCalificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// Los cuatro reportes del sistema.
@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final RecetaCalificacionRepository calificacionRepository;

    // Productos del hogar ya vencidos o que vencen en los proximos "dias" dias.
    @Transactional(readOnly = true)
    public List<ProductoCaducidadDTO> porCaducar(Integer idHogar, int dias) {
        LocalDate limite = LocalDate.now().plusDays(Math.max(dias, 0));
        return productoRepository
                .findByInventario_Hogar_IdHogarAndActivoTrueAndFechaCaducidadLessThanEqualOrderByFechaCaducidadAsc(
                        idHogar, limite)
                .stream()
                .map(p -> new ProductoCaducidadDTO(
                        p.getIdProducto(),
                        p.getNombre(),
                        p.getInventario().getNombre(),
                        p.getCategoria(),
                        p.getCantidad(),
                        p.getUnidad(),
                        p.getFechaCaducidad(),
                        p.getDiasParaCaducar(),
                        p.getDiasParaCaducar() < 0 ? "VENCIDO" : "POR_VENCER"))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReporteCategoriaDTO> porCategoria(Integer idHogar) {
        return productoRepository.contarPorCategoria(idHogar);
    }

    @Transactional(readOnly = true)
    public List<ReporteOcupacionDTO> ocupacion(Integer idHogar) {
        return inventarioRepository.findByHogar_IdHogarOrderByFechaCreacionDesc(idHogar).stream()
                .map(inv -> {
                    long productos = productoRepository.countByInventario_IdInventarioAndActivoTrue(inv.getIdInventario());
                    int capacidad = inv.getCapacidadMaxima();
                    int porcentaje = capacidad == 0 ? 0 : (int) Math.round(productos * 100.0 / capacidad);
                    return new ReporteOcupacionDTO(inv.getIdInventario(), inv.getNombre(), productos, capacidad, porcentaje);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecetaRankingDTO> recetasMejorCalificadas(int limite) {
        List<RecetaRankingDTO> ranking = calificacionRepository.mejorCalificadas(PageRequest.of(0, limite));
        ranking.forEach(r -> r.setPromedio(Math.round(r.getPromedio() * 10) / 10.0));
        return ranking;
    }
}
