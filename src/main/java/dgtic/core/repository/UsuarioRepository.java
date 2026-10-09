package dgtic.core.repository;

import dgtic.core.model.entity.UsuarioBd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioBd, Integer> {

    // La contrasena ya no se compara en la consulta: se verifica con BCrypt (Spring Security).
    Optional<UsuarioBd> findByCorreoAndActivoTrue(String correo);

    boolean existsByCorreoIgnoreCase(String correo);
}
