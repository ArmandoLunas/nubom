package dgtic.core.rest.controller;

import dgtic.core.rest.dto.LoginRequestDTO;
import dgtic.core.rest.dto.RefreshRequestDTO;
import dgtic.core.rest.dto.TokenResponseDTO;
import dgtic.core.security.UsuarioPrincipal;
import dgtic.core.security.jwt.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Autenticacion de la API: unicos endpoints publicos de /api/v1.
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    // Valida correo y contrasena y entrega access token + refresh token.
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getCorreo().trim().toLowerCase(), dto.getContrasena()));
        UsuarioPrincipal principal = (UsuarioPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(refreshTokenService.emitirPar(principal));
    }

    // Entrega un par nuevo y revoca el refresh token usado (rotacion).
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO dto) {
        return ResponseEntity.ok(refreshTokenService.rotar(dto.getRefreshToken()));
    }

    // Cierre de sesion: el refresh token deja de servir.
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequestDTO dto) {
        refreshTokenService.revocar(dto.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
