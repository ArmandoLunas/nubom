package dgtic.core.repository;

import dgtic.core.model.dto.MiembroHogarDTO;
import dgtic.core.model.entity.UsuarioHogarBd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioHogarRepository extends JpaRepository<UsuarioHogarBd, Integer> {

    // Membresias activas de un usuario. Se modela como List (y no Optional)
    // porque la relacion usuario<->hogar es N:M a nivel de base de datos; la
    // regla de negocio de la app ("un usuario, un hogar activo a la vez") se
    // aplica en la capa de servicio, no aqui.
    List<UsuarioHogarBd> findByUsuario_IdUsuarioAndActivoTrue(Integer idUsuario);

    List<UsuarioHogarBd> findByHogar_IdHogarAndActivoTrue(Integer idHogar);

    Optional<UsuarioHogarBd> findByUsuario_IdUsuarioAndHogar_IdHogarAndActivoTrue(Integer idUsuario, Integer idHogar);

    // Miembros activos de un hogar, con su nombre/correo/rol, navegando la
    // relacion N:M ya modelada con @ManyToOne (usuario_hogar.usuario / .rol).
    @Query("select new dgtic.core.model.dto.MiembroHogarDTO(uh.usuario.idUsuario, uh.usuario.nombre, uh.usuario.correo, uh.rol.nombre) " +
           "from usuario_hogar uh " +
           "where uh.hogar.idHogar = :idHogar and uh.activo = true " +
           "order by uh.rol.nombre asc, uh.usuario.nombre asc")
    List<MiembroHogarDTO> buscarMiembrosDeHogar(@Param("idHogar") Integer idHogar);

    long countByHogar_IdHogarAndActivoTrue(Integer idHogar);

    long countByRol_IdRolAndActivoTrue(Integer idRol);
}
