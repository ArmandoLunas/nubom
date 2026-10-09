package dgtic.core.scheduler;

import dgtic.core.security.jwt.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Elimina cada noche los refresh tokens vencidos para que la tabla no crezca sin limite.
@Slf4j
@Component
@RequiredArgsConstructor
public class LimpiezaTokensScheduler {

    private final RefreshTokenService refreshTokenService;

    @Scheduled(cron = "${nubom.jwt.limpieza-cron}")
    public void purgarTokensVencidos() {
        long eliminados = refreshTokenService.purgarExpirados();
        log.info("Refresh tokens vencidos eliminados: {}", eliminados);
    }
}
