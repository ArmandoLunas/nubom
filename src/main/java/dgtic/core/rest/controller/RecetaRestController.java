package dgtic.core.rest.controller;

import dgtic.core.model.dto.RecetaDTO;
import dgtic.core.model.dto.RecetaVistaDTO;
import dgtic.core.model.entity.RecetaBd;
import dgtic.core.rest.dto.CalificacionRequestDTO;
import dgtic.core.rest.dto.ModeracionRequestDTO;
import dgtic.core.rest.dto.PaginaDTO;
import dgtic.core.rest.dto.RecetaResponseDTO;
import dgtic.core.rest.mapper.RecetaMapper;
import dgtic.core.security.UsuarioPrincipal;
import dgtic.core.service.RecetaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

// Recetas: CRUD del autor, comparticion, calificacion y moderacion.
// Que una receta sea del usuario se comprueba en RecetaService (403 si no lo es);
// /moderacion/** esta reservado a ADMIN en SecurityConfig.
@RestController
@RequestMapping("/api/v1/recetas")
@RequiredArgsConstructor
public class RecetaRestController {

    private final RecetaService recetaService;
    private final RecetaMapper recetaMapper;

    // GET /api/v1/recetas?nombre=...&page=0&size=20 -> recetario de la comunidad
    @GetMapping
    public ResponseEntity<PaginaDTO<RecetaVistaDTO>> comunidad(
            @RequestParam(value = "nombre", required = false) String nombre,
            @PageableDefault(size = 20, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok(PaginaDTO.de(recetaService.comunidad(nombre, pageable, usuario.getIdUsuario())));
    }

    // GET /api/v1/recetas/mias -> recetas del usuario en cualquier estado
    @GetMapping("/mias")
    public ResponseEntity<List<RecetaVistaDTO>> mias(@AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok(recetaService.propias(usuario.getIdUsuario()));
    }

    // GET /api/v1/recetas/moderacion -> pendientes de revision (ADMIN)
    @GetMapping("/moderacion")
    public ResponseEntity<List<RecetaVistaDTO>> pendientes() {
        return ResponseEntity.ok(recetaService.pendientes());
    }

    // PUT /api/v1/recetas/moderacion/{id} -> aprobar o rechazar (ADMIN)
    @PutMapping("/moderacion/{id}")
    public ResponseEntity<RecetaResponseDTO> moderar(@PathVariable("id") Integer id,
                                                      @Valid @RequestBody ModeracionRequestDTO dto,
                                                      @AuthenticationPrincipal UsuarioPrincipal usuario) {
        RecetaBd receta = recetaService.moderar(id, dto.getAprobar());
        return ResponseEntity.ok(detalle(receta.getIdReceta(), usuario));
    }

    // GET /api/v1/recetas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<RecetaResponseDTO> obtener(@PathVariable("id") Integer id,
                                                      @AuthenticationPrincipal UsuarioPrincipal usuario) {
        return ResponseEntity.ok(detalle(id, usuario));
    }

    // POST /api/v1/recetas
    @PostMapping
    public ResponseEntity<RecetaResponseDTO> crear(@Valid @RequestBody RecetaDTO dto,
                                                    @AuthenticationPrincipal UsuarioPrincipal usuario) {
        RecetaBd creada = recetaService.crear(usuario.getIdUsuario(), dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.getIdReceta())
                .toUri();
        return ResponseEntity.created(ubicacion).body(detalle(creada.getIdReceta(), usuario));
    }

    // PUT /api/v1/recetas/{id}
    @PutMapping("/{id}")
    public ResponseEntity<RecetaResponseDTO> actualizar(@PathVariable("id") Integer id,
                                                         @Valid @RequestBody RecetaDTO dto,
                                                         @AuthenticationPrincipal UsuarioPrincipal usuario) {
        recetaService.actualizar(id, usuario.getIdUsuario(), dto);
        return ResponseEntity.ok(detalle(id, usuario));
    }

    // DELETE /api/v1/recetas/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id,
                                          @AuthenticationPrincipal UsuarioPrincipal usuario) {
        recetaService.eliminar(id, usuario.getIdUsuario());
        return ResponseEntity.noContent().build();
    }

    // POST /api/v1/recetas/{id}/compartir -> pasa a PENDIENTE
    @PostMapping("/{id}/compartir")
    public ResponseEntity<RecetaResponseDTO> compartir(@PathVariable("id") Integer id,
                                                        @AuthenticationPrincipal UsuarioPrincipal usuario) {
        recetaService.compartir(id, usuario.getIdUsuario());
        return ResponseEntity.ok(detalle(id, usuario));
    }

    // PUT /api/v1/recetas/{id}/calificacion -> registra o cambia la calificacion del usuario
    @PutMapping("/{id}/calificacion")
    public ResponseEntity<RecetaResponseDTO> calificar(@PathVariable("id") Integer id,
                                                        @Valid @RequestBody CalificacionRequestDTO dto,
                                                        @AuthenticationPrincipal UsuarioPrincipal usuario) {
        recetaService.calificar(id, usuario.getIdUsuario(), dto.getPuntuacion());
        return ResponseEntity.ok(detalle(id, usuario));
    }

    // DELETE /api/v1/recetas/{id}/calificacion
    @DeleteMapping("/{id}/calificacion")
    public ResponseEntity<Void> retirarCalificacion(@PathVariable("id") Integer id,
                                                     @AuthenticationPrincipal UsuarioPrincipal usuario) {
        recetaService.retirarCalificacion(id, usuario.getIdUsuario());
        return ResponseEntity.noContent().build();
    }

    private RecetaResponseDTO detalle(Integer idReceta, UsuarioPrincipal usuario) {
        RecetaBd receta = recetaService.obtenerVisible(idReceta, usuario.getIdUsuario(), usuario.esAdmin());
        return recetaMapper.toResponseDTO(receta, recetaService.vistaDe(receta, usuario.getIdUsuario()));
    }
}
