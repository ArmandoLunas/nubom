package dgtic.core.repository;

import dgtic.core.model.entity.NotificacionBd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<NotificacionBd, Integer> {

    List<NotificacionBd> findByUsuario_IdUsuarioOrderByFechaCreacionDesc(Integer idUsuario);

    List<NotificacionBd> findByUsuario_IdUsuarioAndLeidaFalseOrderByFechaCreacionDesc(Integer idUsuario);

    long countByUsuario_IdUsuarioAndLeidaFalse(Integer idUsuario);

    // Evita repetir el aviso del mismo producto al mismo usuario en el mismo dia.
    boolean existsByUsuario_IdUsuarioAndProducto_IdProductoAndFechaCreacionGreaterThanEqual(
            Integer idUsuario, Integer idProducto, LocalDateTime desde);

    @Modifying
    @Query("update notificacion n set n.leida = true where n.usuario.idUsuario = :idUsuario and n.leida = false")
    int marcarTodasLeidas(@Param("idUsuario") Integer idUsuario);
}
