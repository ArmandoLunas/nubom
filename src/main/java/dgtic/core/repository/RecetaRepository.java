package dgtic.core.repository;

import dgtic.core.model.entity.EstadoReceta;
import dgtic.core.model.entity.RecetaBd;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaRepository extends JpaRepository<RecetaBd, Integer> {

    // Recetario de la comunidad: paginado y con busqueda por nombre.
    Page<RecetaBd> findByEstadoAndNombreContainingIgnoreCase(EstadoReceta estado, String nombre, Pageable pageable);

    List<RecetaBd> findByAutor_IdUsuarioOrderByFechaCreacionDesc(Integer idUsuario);

    // Bandeja de moderacion: primero las que llevan mas tiempo esperando.
    List<RecetaBd> findByEstadoOrderByFechaModificacionAsc(EstadoReceta estado);
}
