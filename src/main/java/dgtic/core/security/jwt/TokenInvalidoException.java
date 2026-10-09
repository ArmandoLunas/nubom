package dgtic.core.security.jwt;

import org.springframework.security.core.AuthenticationException;

// Refresh token inexistente, vencido o revocado. Se traduce a 401.
public class TokenInvalidoException extends AuthenticationException {

    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}
