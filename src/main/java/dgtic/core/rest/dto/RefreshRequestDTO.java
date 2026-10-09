package dgtic.core.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequestDTO {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
