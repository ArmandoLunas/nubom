package dgtic.core.repository;

import dgtic.core.model.dto.RecetaRankingDTO;
import dgtic.core.model.entity.RecetaCalificacionBd;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RecetaCalificacionRepository extends JpaRepository<RecetaCalificacionBd, Integer> {

    Optional<RecetaCalificacionBd> findByReceta_IdRecetaAndUsuario_IdUsuario(Integer idReceta, Integer idUsuario);

    // Promedio y numero de calificaciones de varias recetas en una sola consulta:
    // cada fila es [idReceta, promedio, total].
    @Query("select c.receta.idReceta, avg(c.puntuacion), count(c) " +
           "from receta_calificacion c " +
           "where c.receta.idReceta in :ids " +
           "group by c.receta.idReceta")
    List<Object[]> resumenPorRecetas(@Param("ids") Collection<Integer> ids);

    // Reporte de recetas mejor calificadas (solo las aprobadas).
    @Query("select new dgtic.core.model.dto.RecetaRankingDTO(" +
           "r.idReceta, r.nombre, r.autor.nombre, avg(c.puntuacion), count(c)) " +
           "from receta_calificacion c join c.receta r " +
           "where r.estado = dgtic.core.model.entity.EstadoReceta.APROBADA " +
           "group by r.idReceta, r.nombre, r.autor.nombre " +
           "order by avg(c.puntuacion) desc, count(c) desc")
    List<RecetaRankingDTO> mejorCalificadas(Pageable pageable);
}
