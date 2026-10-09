package dgtic.core.repository;

import dgtic.core.model.entity.RefreshTokenBd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenBd, Integer> {

    Optional<RefreshTokenBd> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update refresh_token t set t.revocado = true where t.usuario.idUsuario = :idUsuario and t.revocado = false")
    int revocarTodosDeUsuario(@Param("idUsuario") Integer idUsuario);

    long deleteByExpiraEnBefore(LocalDateTime fecha);
}
