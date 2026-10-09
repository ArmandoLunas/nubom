package dgtic.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// Envio de correos. Esta desactivado por omision (nubom.mail.enabled=false) para
// que la aplicacion funcione sin una cuenta SMTP; un fallo de envio nunca
// interrumpe la operacion que lo origino, solo queda registrado en bitacora.
@Slf4j
@Service
public class CorreoService {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean habilitado;
    private final String remitente;

    public CorreoService(ObjectProvider<JavaMailSender> mailSender,
                         @Value("${nubom.mail.enabled}") boolean habilitado,
                         @Value("${nubom.mail.from}") String remitente) {
        this.mailSender = mailSender;
        this.habilitado = habilitado;
        this.remitente = remitente;
    }

    public boolean enviar(String destinatario, String asunto, String cuerpo) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (!habilitado || sender == null) {
            log.info("Correo no enviado a {} (envío desactivado): {}", destinatario, asunto);
            return false;
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            sender.send(mensaje);
            return true;
        } catch (RuntimeException ex) {
            log.error("No se pudo enviar el correo a {}: {}", destinatario, ex.getMessage());
            return false;
        }
    }
}
