package dgtic.core.security.jwt;

import dgtic.core.model.entity.RolSistema;
import dgtic.core.model.entity.UsuarioBd;
import dgtic.core.security.UsuarioPrincipal;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Prueba unitaria pura: no levanta el contexto de Spring.
class JwtServiceTest {

    private static final String SECRETO =
            Base64.getEncoder().encodeToString("clave-de-prueba-de-al-menos-32-bytes!!".getBytes());
    private static final String OTRO_SECRETO =
            Base64.getEncoder().encodeToString("otra-clave-distinta-de-al-menos-32-b!!".getBytes());

    private final UsuarioPrincipal usuario = new UsuarioPrincipal(UsuarioBd.builder()
            .idUsuario(7)
            .nombre("Prueba")
            .correo("prueba@correo.com")
            .contrasena("hash")
            .rolSistema(RolSistema.USUARIO)
            .activo(true)
            .build());

    @Test
    void unTokenRecienEmitidoDevuelveElCorreoDelUsuario() {
        JwtService servicio = new JwtService(SECRETO, Duration.ofMinutes(15));

        String token = servicio.generarAccessToken(usuario);

        assertEquals("prueba@correo.com", servicio.extraerCorreo(token).orElseThrow());
        assertEquals(900, servicio.segundosDeVigencia());
    }

    @Test
    void unTokenExpiradoNoEsValido() {
        JwtService servicio = new JwtService(SECRETO, Duration.ofSeconds(-5));

        String token = servicio.generarAccessToken(usuario);

        assertTrue(servicio.extraerCorreo(token).isEmpty());
    }

    @Test
    void unTokenFirmadoConOtraClaveNoEsValido() {
        String tokenAjeno = new JwtService(OTRO_SECRETO, Duration.ofMinutes(15)).generarAccessToken(usuario);

        assertTrue(new JwtService(SECRETO, Duration.ofMinutes(15)).extraerCorreo(tokenAjeno).isEmpty());
    }

    @Test
    void unTextoQueNoEsJwtNoEsValido() {
        JwtService servicio = new JwtService(SECRETO, Duration.ofMinutes(15));

        assertTrue(servicio.extraerCorreo("esto-no-es-un-token").isEmpty());
        assertTrue(servicio.extraerCorreo("").isEmpty());
    }
}
