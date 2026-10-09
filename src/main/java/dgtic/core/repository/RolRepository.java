package dgtic.core.repository;

import dgtic.core.model.entity.RolBd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<RolBd, Integer> {
    Optional<RolBd> findByNombre(String nombre);
}
