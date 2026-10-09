package dgtic.core.security.jwt;

import dgtic.core.security.UsuarioPrincipal;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

// Emite y valida los access tokens (JWT firmados con HS256).
@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration vigencia;

    public JwtService(@Value("${nubom.jwt.secret}") String secretoBase64,
                      @Value("${nubom.jwt.access-expiration}") Duration vigencia) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.vigencia = vigencia;
    }

    public String generarAccessToken(UsuarioPrincipal usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("uid", usuario.getIdUsuario())
                .claim("rol", usuario.getRolSistema().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + vigencia.toMillis()))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    // Devuelve el correo (sub) solo si la firma es correcta y el token no ha expirado.
    public Optional<String> extraerCorreo(String token) {
        try {
            return Optional.ofNullable(Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public long segundosDeVigencia() {
        return vigencia.toSeconds();
    }
}
