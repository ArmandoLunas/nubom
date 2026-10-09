package dgtic.core.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDTO {
    private String tokenType;     // siempre "Bearer"
    private String accessToken;   // JWT de corta duracion
    private long expiresIn;       // segundos de vigencia del access token
    private String refreshToken;  // de un solo uso: cada renovacion entrega uno nuevo
}
