package dgtic.core.repository;

import dgtic.core.model.dto.ReporteCategoriaDTO;
import dgtic.core.model.entity.ProductoBd;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductoRepository extends JpaRepository<ProductoBd, Integer> {

    List<ProductoBd> findByInventario_IdInventarioAndActivoTrueOrderByFechaIngresoDesc(Integer idInventario);

    long countByInventario_IdInventarioAndActivoTrue(Integer idInventario);

    // Usados por la API REST para "listar todos" con filtro opcional por hogar
    // (recorre la relacion producto -> inventario -> hogar).
    List<ProductoBd> findByInventario_Hogar_IdHogarAndActivoTrueOrderByFechaIngresoDesc(Integer idHogar);

    List<ProductoBd> findByActivoTrueOrderByFechaIngresoDesc();

    // ---- Caducidad ----

    // Productos activos de cualquier hogar que vencen en o antes de la fecha limite
    // (lo usa la tarea programada de avisos).
    List<ProductoBd> findByActivoTrueAndFechaCaducidadLessThanEqual(LocalDate limite);

    // Reporte "proximos a caducar" de un hogar, del mas urgente al menos urgente.
    List<ProductoBd> findByInventario_Hogar_IdHogarAndActivoTrueAndFechaCaducidadLessThanEqualOrderByFechaCaducidadAsc(
            Integer idHogar, LocalDate limite);

    // ---- Listado paginado de la API con filtros opcionales ----
    @Query("select p from producto p " +
           "where p.activo = true " +
           "and (:idHogar is null or p.inventario.hogar.idHogar = :idHogar) " +
           "and (:idInventario is null or p.inventario.idInventario = :idInventario) " +
           "and (:categoria is null or p.categoria = :categoria)")
    Page<ProductoBd> buscar(@Param("idHogar") Integer idHogar,
                            @Param("idInventario") Integer idInventario,
                            @Param("categoria") String categoria,
                            Pageable pageable);

    // ---- Reporte de productos por categoria ----
    @Query("select new dgtic.core.model.dto.ReporteCategoriaDTO(p.categoria, count(p)) " +
           "from producto p " +
           "where p.activo = true and p.inventario.hogar.idHogar = :idHogar " +
           "group by p.categoria " +
           "order by count(p) desc, p.categoria asc")
    List<ReporteCategoriaDTO> contarPorCategoria(@Param("idHogar") Integer idHogar);
}
