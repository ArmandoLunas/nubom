package dgtic.core.scheduler;

import dgtic.core.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Revisa una vez al dia que productos estan por vencer y genera los avisos.
@Slf4j
@Component
@RequiredArgsConstructor
public class CaducidadScheduler {

    private final NotificacionService notificacionService;

    @Value("${nubom.caducidad.ejecutar-al-iniciar}")
    private boolean ejecutarAlIniciar;

    @Scheduled(cron = "${nubom.caducidad.cron}")
    public void revisarCaducidades() {
        notificacionService.generarAvisosDeCaducidad();
    }

    // En desarrollo la base se recrea en cada arranque, asi que se genera una
    // primera tanda de avisos sin esperar a la hora programada.
    @EventListener(ApplicationReadyEvent.class)
    public void alIniciar() {
        if (ejecutarAlIniciar) {
            log.info("Generando avisos de caducidad al iniciar la aplicación");
            notificacionService.generarAvisosDeCaducidad();
        }
    }
}
