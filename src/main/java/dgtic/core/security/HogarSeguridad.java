package dgtic.core.security;

import dgtic.core.model.entity.UsuarioHogarBd;
import dgtic.core.repository.InventarioRepository;
import dgtic.core.repository.ListaCompraRepository;
import dgtic.core.repository.NotificacionRepository;
import dgtic.core.repository.ProductoRepository;
import dgtic.core.repository.RecetaRepository;
import dgtic.core.repository.UsuarioHogarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

// Comprobaciones de pertenencia usadas en @PreAuthorize. El rol dentro del hogar
// (PROPIETARIO / FAMILIAR) no es una autoridad global del usuario porque depende
// del hogar, asi que se consulta aqui para el recurso concreto de cada peticion.
//
// Cuando el recurso no existe se devuelve true a proposito: asi la peticion
// llega al servicio y la respuesta es 404 en lugar de un 403 enganoso.
@Component("hogarSeguridad")
@RequiredArgsConstructor
public class HogarSeguridad {

    private static final String PROPIETARIO = "PROPIETARIO";

    // Valor de filtroDeHogar para un usuario que aun no pertenece a ningun hogar.
    public static final Integer SIN_HOGAR = -1;

    private final UsuarioHogarRepository usuarioHogarRepository;
    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final ListaCompraRepository listaCompraRepository;
    private final RecetaRepository recetaRepository;
    private final NotificacionRepository notificacionRepository;

    // ---- Usuario ----

    public boolean esUsuario(Authentication auth, Integer idUsuario) {
        Integer actual = idDe(auth);
        return actual != null && actual.equals(idUsuario);
    }

    // Hogar activo del usuario autenticado, si tiene.
    @Transactional(readOnly = true)
    public Optional<Integer> idHogarDe(Authentication auth) {
        Integer actual = idDe(auth);
        if (actual == null) {
            return Optional.empty();
        }
        return usuarioHogarRepository.findByUsuario_IdUsuarioAndActivoTrue(actual).stream()
                .findFirst()
                .map(uh -> uh.getHogar().getIdHogar());
    }

    // Hogar por el que debe filtrarse un listado general de la API:
    //   - administrador: el que pida, o null (sin filtro) si no pide ninguno;
    //   - cualquier otro: solo el suyo. Si pide otro, 403; si no tiene, SIN_HOGAR.
    @Transactional(readOnly = true)
    public Integer filtroDeHogar(Authentication auth, Integer idHogarPedido) {
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal && principal.esAdmin()) {
            return idHogarPedido;
        }
        if (idHogarPedido != null) {
            if (!esMiembro(auth, idHogarPedido)) {
                throw new AccessDeniedException("No perteneces a ese hogar");
            }
            return idHogarPedido;
        }
        return idHogarDe(auth).orElse(SIN_HOGAR);
    }

    // ---- Hogar ----

    @Transactional(readOnly = true)
    public boolean esMiembro(Authentication auth, Integer idHogar) {
        return membresia(auth, idHogar).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean esPropietario(Authentication auth, Integer idHogar) {
        return membresia(auth, idHogar)
                .map(uh -> PROPIETARIO.equals(uh.getRol().getNombre()))
                .orElse(false);
    }

    // ---- Recursos que cuelgan de un hogar ----

    @Transactional(readOnly = true)
    public boolean esMiembroDeInventario(Authentication auth, Integer idInventario) {
        return inventarioRepository.findById(idInventario)
                .map(inv -> esMiembro(auth, inv.getHogar().getIdHogar()))
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public boolean esPropietarioDeInventario(Authentication auth, Integer idInventario) {
        return inventarioRepository.findById(idInventario)
                .map(inv -> esPropietario(auth, inv.getHogar().getIdHogar()))
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public boolean esMiembroDeProducto(Authentication auth, Integer idProducto) {
        return productoRepository.findById(idProducto)
                .map(p -> esMiembro(auth, p.getInventario().getHogar().getIdHogar()))
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public boolean esMiembroDeItem(Authentication auth, Integer idItem) {
        return listaCompraRepository.findById(idItem)
                .map(item -> esMiembro(auth, item.getHogar().getIdHogar()))
                .orElse(true);
    }

    // ---- Recetas y avisos ----

    @Transactional(readOnly = true)
    public boolean esAutorReceta(Authentication auth, Integer idReceta) {
        Integer actual = idDe(auth);
        return recetaRepository.findById(idReceta)
                .map(r -> r.getAutor().getIdUsuario().equals(actual))
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public boolean esDestinatario(Authentication auth, Integer idNotificacion) {
        Integer actual = idDe(auth);
        return notificacionRepository.findById(idNotificacion)
                .map(n -> n.getUsuario().getIdUsuario().equals(actual))
                .orElse(true);
    }

    // ---- Auxiliares ----

    private Optional<UsuarioHogarBd> membresia(Authentication auth, Integer idHogar) {
        Integer actual = idDe(auth);
        if (actual == null || idHogar == null) {
            return Optional.empty();
        }
        return usuarioHogarRepository.findByUsuario_IdUsuarioAndHogar_IdHogarAndActivoTrue(actual, idHogar);
    }

    private Integer idDe(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal.getIdUsuario();
        }
        return null;
    }
}
