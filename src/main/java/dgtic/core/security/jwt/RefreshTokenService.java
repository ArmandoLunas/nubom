package dgtic.core.security.jwt;

import dgtic.core.model.entity.RefreshTokenBd;
import dgtic.core.model.entity.UsuarioBd;
import dgtic.core.repository.RefreshTokenRepository;
import dgtic.core.repository.UsuarioRepository;
import dgtic.core.rest.dto.TokenResponseDTO;
import dgtic.core.security.UsuarioPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

// Emision, rotacion y revocacion de refresh tokens.
//
// El refresh token es una cadena aleatoria de 256 bits sin informacion. Al
// cliente se le entrega en claro una sola vez; en la base solo queda su hash.
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final Duration vigencia;
    private final SecureRandom aleatorio = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UsuarioRepository usuarioRepository,
                               JwtService jwtService,
                               @Value("${nubom.jwt.refresh-expiration}") Duration vigencia) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.vigencia = vigencia;
    }

    // Par de tokens para un usuario que acaba de autenticarse.
    @Transactional
    public TokenResponseDTO emitirPar(UsuarioPrincipal principal) {
        UsuarioBd usuario = usuarioRepository.getReferenceById(principal.getIdUsuario());
        return new TokenResponseDTO(
                "Bearer",
                jwtService.generarAccessToken(principal),
                jwtService.segundosDeVigencia(),
                emitir(usuario));
    }

    @Transactional
    public String emitir(UsuarioBd usuario) {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        String tokenPlano = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        refreshTokenRepository.save(RefreshTokenBd.builder()
                .usuario(usuario)
                .tokenHash(hash(tokenPlano))
                .expiraEn(LocalDateTime.now().plus(vigencia))
                .revocado(false)
                .fechaCreacion(LocalDateTime.now())
                .build());
        return tokenPlano;
    }

    // Rotacion: el token presentado deja de servir y se entrega un par nuevo.
    // noRollbackFor: si se detecta reutilizacion, la revocacion masiva debe
    // quedar guardada aunque la operacion termine en excepcion.
    @Transactional(noRollbackFor = TokenInvalidoException.class)
    public TokenResponseDTO rotar(String tokenPlano) {
        RefreshTokenBd actual = refreshTokenRepository.findByTokenHash(hash(tokenPlano))
                .orElseThrow(() -> new TokenInvalidoException("El refresh token no es válido"));
        UsuarioBd usuario = actual.getUsuario();

        if (Boolean.TRUE.equals(actual.getRevocado())) {
            // Un token ya usado que vuelve a aparecer es senal de posible robo.
            refreshTokenRepository.revocarTodosDeUsuario(usuario.getIdUsuario());
            throw new TokenInvalidoException(
                    "El refresh token ya fue utilizado; se cerraron todas las sesiones de la API");
        }
        if (actual.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new TokenInvalidoException("El refresh token expiró; inicia sesión de nuevo");
        }
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new TokenInvalidoException("La cuenta ya no está activa");
        }

        actual.setRevocado(true);
        refreshTokenRepository.save(actual);

        return new TokenResponseDTO(
                "Bearer",
                jwtService.generarAccessToken(new UsuarioPrincipal(usuario)),
                jwtService.segundosDeVigencia(),
                emitir(usuario));
    }

    // Cierre de sesion de la API. No falla si el token no existe.
    @Transactional
    public void revocar(String tokenPlano) {
        refreshTokenRepository.findByTokenHash(hash(tokenPlano)).ifPresent(token -> {
            token.setRevocado(true);
            refreshTokenRepository.save(token);
        });
    }

    // Se usa al cambiar la contrasena o dar de baja la cuenta.
    @Transactional
    public void revocarTodos(Integer idUsuario) {
        refreshTokenRepository.revocarTodosDeUsuario(idUsuario);
    }

    @Transactional
    public long purgarExpirados() {
        return refreshTokenRepository.deleteByExpiraEnBefore(LocalDateTime.now());
    }

    static String hash(String tokenPlano) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha256.digest(tokenPlano.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
