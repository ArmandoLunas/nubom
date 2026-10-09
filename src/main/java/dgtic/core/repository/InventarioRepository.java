package dgtic.core.repository;

import dgtic.core.model.entity.InventarioBd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventarioRepository extends JpaRepository<InventarioBd, Integer> {

    List<InventarioBd> findByHogar_IdHogarOrderByFechaCreacionDesc(Integer idHogar);

    List<InventarioBd> findByTipo_IdTipo(Integer idTipo);

    long countByTipo_IdTipo(Integer idTipo);
}
