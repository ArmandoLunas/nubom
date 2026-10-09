package dgtic.core.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

// Refresh token emitido para la API. Nunca se guarda el token en claro:
// solo su hash SHA-256, para que una fuga de la tabla no permita usarlos.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "refresh_token")
@Table(name = "refresh_token")
public class RefreshTokenBd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idToken;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioBd usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "revocado", nullable = false)
    private Boolean revocado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
