package dgtic.core.security.jwt;

import dgtic.core.model.entity.RefreshTokenBd;
import dgtic.core.model.entity.RolSistema;
import dgtic.core.model.entity.UsuarioBd;
import dgtic.core.repository.RefreshTokenRepository;
import dgtic.core.repository.UsuarioRepository;
import dgtic.core.rest.dto.TokenResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private RefreshTokenService servicio;
    private UsuarioBd usuario;

    @BeforeEach
    void preparar() {
        String secreto = Base64.getEncoder().encodeToString("clave-de-prueba-de-al-menos-32-bytes!!".getBytes());
        JwtService jwtService = new JwtService(secreto, Duration.ofMinutes(15));
        servicio = new RefreshTokenService(refreshTokenRepository, usuarioRepository, jwtService, Duration.ofDays(7));
        usuario = UsuarioBd.builder()
                .idUsuario(1).nombre("Prueba").correo("prueba@correo.com").contrasena("hash")
                .rolSistema(RolSistema.USUARIO).activo(true).build();
    }

    @Test
    void emitirGuardaSoloElHashDelToken() {
        String tokenPlano = servicio.emitir(usuario);

        ArgumentCaptor<RefreshTokenBd> guardado = ArgumentCaptor.forClass(RefreshTokenBd.class);
        verify(refreshTokenRepository).save(guardado.capture());
        assertNotEquals(tokenPlano, guardado.getValue().getTokenHash());
        assertEquals(RefreshTokenService.hash(tokenPlano), guardado.getValue().getTokenHash());
        assertEquals(64, guardado.getValue().getTokenHash().length());
        assertFalse(guardado.getValue().getRevocado());
        assertTrue(guardado.getValue().getExpiraEn().isAfter(LocalDateTime.now().plusDays(6)));
    }

    @Test
    void rotarRevocaElTokenUsadoYEntregaUnoNuevo() {
        RefreshTokenBd vigente = token("vigente", false, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByTokenHash(RefreshTokenService.hash("vigente")))
                .thenReturn(Optional.of(vigente));

        TokenResponseDTO respuesta = servicio.rotar("vigente");

        assertTrue(vigente.getRevocado());
        assertNotEquals("vigente", respuesta.getRefreshToken());
        assertEquals("Bearer", respuesta.getTokenType());
        // Una vez para revocar el anterior y otra para guardar el nuevo.
        verify(refreshTokenRepository, times(2)).save(any(RefreshTokenBd.class));
    }

    @Test
    void reutilizarUnTokenRevocadoCierraTodasLasSesionesDelUsuario() {
        when(refreshTokenRepository.findByTokenHash(RefreshTokenService.hash("usado")))
                .thenReturn(Optional.of(token("usado", true, LocalDateTime.now().plusDays(1))));

        assertThrows(TokenInvalidoException.class, () -> servicio.rotar("usado"));

        verify(refreshTokenRepository).revocarTodosDeUsuario(1);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void unTokenExpiradoNoSePuedeRotar() {
        when(refreshTokenRepository.findByTokenHash(RefreshTokenService.hash("viejo")))
                .thenReturn(Optional.of(token("viejo", false, LocalDateTime.now().minusMinutes(1))));

        assertThrows(TokenInvalidoException.class, () -> servicio.rotar("viejo"));

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void unTokenDesconocidoNoSePuedeRotar() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThrows(TokenInvalidoException.class, () -> servicio.rotar("inventado"));
    }

    private RefreshTokenBd token(String plano, boolean revocado, LocalDateTime expira) {
        return RefreshTokenBd.builder()
                .idToken(10).usuario(usuario).tokenHash(RefreshTokenService.hash(plano))
                .revocado(revocado).expiraEn(expira).fechaCreacion(LocalDateTime.now()).build();
    }
}
