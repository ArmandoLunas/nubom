package dgtic.core.service;

import dgtic.core.model.entity.NotificacionBd;
import dgtic.core.model.entity.ProductoBd;
import dgtic.core.model.entity.UsuarioBd;
import dgtic.core.model.entity.UsuarioHogarBd;
import dgtic.core.repository.NotificacionRepository;
import dgtic.core.repository.ProductoRepository;
import dgtic.core.repository.UsuarioHogarRepository;
import dgtic.core.rest.exception.RecursoNoEncontradoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Avisos de caducidad: los genera la tarea programada y los consulta cada usuario.
@Slf4j
@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioHogarRepository usuarioHogarRepository;
    private final CorreoService correoService;
    private final int diasAviso;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               ProductoRepository productoRepository,
                               UsuarioHogarRepository usuarioHogarRepository,
                               CorreoService correoService,
                               @Value("${nubom.caducidad.dias-aviso}") int diasAviso) {
        this.notificacionRepository = notificacionRepository;
        this.productoRepository = productoRepository;
        this.usuarioHogarRepository = usuarioHogarRepository;
        this.correoService = correoService;
        this.diasAviso = diasAviso;
    }

    // Crea un aviso por producto proximo a vencer para cada integrante de su hogar
    // (como maximo uno por producto, usuario y dia) y envia un correo resumen.
    @Transactional
    public int generarAvisosDeCaducidad() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDelDia = hoy.atStartOfDay();
        List<ProductoBd> productos =
                productoRepository.findByActivoTrueAndFechaCaducidadLessThanEqual(hoy.plusDays(diasAviso));

        Map<UsuarioBd, List<String>> avisosPorUsuario = new LinkedHashMap<>();
        int creados = 0;

        for (ProductoBd producto : productos) {
            Integer idHogar = producto.getInventario().getHogar().getIdHogar();
            String mensaje = mensajeDe(producto);
            for (UsuarioHogarBd membresia : usuarioHogarRepository.findByHogar_IdHogarAndActivoTrue(idHogar)) {
                UsuarioBd usuario = membresia.getUsuario();
                boolean yaAvisado = notificacionRepository
                        .existsByUsuario_IdUsuarioAndProducto_IdProductoAndFechaCreacionGreaterThanEqual(
                                usuario.getIdUsuario(), producto.getIdProducto(), inicioDelDia);
                if (yaAvisado) {
                    continue;
                }
                notificacionRepository.save(NotificacionBd.builder()
                        .usuario(usuario)
                        .producto(producto)
                        .mensaje(mensaje)
                        .leida(false)
                        .fechaCreacion(LocalDateTime.now())
                        .build());
                avisosPorUsuario.computeIfAbsent(usuario, u -> new ArrayList<>()).add(mensaje);
                creados++;
            }
        }

        avisosPorUsuario.forEach((usuario, mensajes) -> correoService.enviar(
                usuario.getCorreo(),
                "NUBOM: tienes " + mensajes.size() + " producto(s) por vencer",
                "Hola, " + usuario.getNombre() + ":\n\n- " + String.join("\n- ", mensajes)
                        + "\n\nRevisa tu inventario en NUBOM para aprovecharlos a tiempo."));

        log.info("Avisos de caducidad generados: {} ({} producto(s) revisados)", creados, productos.size());
        return creados;
    }

    @Transactional(readOnly = true)
    public List<NotificacionBd> listar(Integer idUsuario, boolean soloNoLeidas) {
        return soloNoLeidas
                ? notificacionRepository.findByUsuario_IdUsuarioAndLeidaFalseOrderByFechaCreacionDesc(idUsuario)
                : notificacionRepository.findByUsuario_IdUsuarioOrderByFechaCreacionDesc(idUsuario);
    }

    @Transactional(readOnly = true)
    public long contarSinLeer(Integer idUsuario) {
        return notificacionRepository.countByUsuario_IdUsuarioAndLeidaFalse(idUsuario);
    }

    @Transactional
    public NotificacionBd marcarLeida(Integer idNotificacion, Integer idUsuario) {
        NotificacionBd aviso = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un aviso con id " + idNotificacion));
        if (!aviso.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new AccessDeniedException("Este aviso no es tuyo");
        }
        aviso.setLeida(true);
        return notificacionRepository.save(aviso);
    }

    @Transactional
    public int marcarTodasLeidas(Integer idUsuario) {
        return notificacionRepository.marcarTodasLeidas(idUsuario);
    }

    private String mensajeDe(ProductoBd producto) {
        long dias = producto.getDiasParaCaducar();
        String cuando;
        if (dias < 0) {
            cuando = "venció hace " + (-dias) + (dias == -1 ? " día" : " días");
        } else if (dias == 0) {
            cuando = "vence hoy";
        } else if (dias == 1) {
            cuando = "vence mañana";
        } else {
            cuando = "vence en " + dias + " días";
        }
        return producto.getNombre() + " (" + producto.getInventario().getNombre() + ") " + cuando;
    }
}
