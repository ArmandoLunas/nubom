package dgtic.core.rest.controller;

import dgtic.core.model.entity.NotificacionBd;
import dgtic.core.rest.dto.NotificacionResponseDTO;
import dgtic.core.security.UsuarioPrincipal;
import dgtic.core.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Avisos de caducidad del usuario autenticado.
@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
public class NotificacionRestController {

    private final NotificacionService notificacionService;

    // GET /api/v1/notificaciones?soloNoLeidas=true
    @GetMapping
    public ResponseEntity<List<NotificacionResponseDTO>> listar(
            @RequestParam(value = "soloNoLeidas", defaultValue = "false") boolean soloNoLeidas,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        List<NotificacionResponseDTO> avisos = notificacionService.listar(usuario.getIdUsuario(), soloNoLeidas)
                .stream()
                .map(this::aDTO)
                .toList();
        return ResponseEntity.ok(avisos);
    }

    // PUT /api/v1/notificaciones/{id}/leida
    @PutMapping("/{id}/leida")
    public ResponseEntity<NotificacionResponseDTO> marcarLeida(@PathVariable("id") Integer id,
                                                                @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok(aDTO(notificacionService.marcarLeida(id, usuario.getIdUsuario())));
    }

    private NotificacionResponseDTO aDTO(NotificacionBd n) {
        return new NotificacionResponseDTO(
                n.getIdNotificacion(),
                n.getMensaje(),
                n.getLeida(),
                n.getFechaCreacion(),
                n.getProducto() != null ? n.getProducto().getIdProducto() : null);
    }
}
