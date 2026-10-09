package dgtic.core.rest.controller;

import dgtic.core.model.entity.RolBd;
import dgtic.core.rest.dto.RolRequestDTO;
import dgtic.core.rest.dto.RolResponseDTO;
import dgtic.core.rest.mapper.RolMapper;
import dgtic.core.rest.service.RolRestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * API REST del catalogo Rol (PROPIETARIO, FAMILIAR), el atributo propio de la
 * relacion N:M usuario &lt;-&gt; hogar. Sirve tambien para demostrar el 409
 * Conflict por integridad: no se puede borrar un rol con membresias activas.
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RolRestController {

    private final RolRestService rolService;
    private final RolMapper rolMapper;

    // GET /api/v1/roles -> 200 OK
    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> listar() {
        List<RolResponseDTO> roles = rolService.listarTodos().stream()
                .map(r -> rolMapper.toResponseDTO(r, rolService.contarMembresiasActivas(r.getIdRol())))
                .toList();
        return ResponseEntity.ok(roles);
    }

    // GET /api/v1/roles/{id} -> 200 OK | 404 Not Found
    @GetMapping("/{id}")
    public ResponseEntity<RolResponseDTO> obtener(@PathVariable("id") Integer id) {
        RolBd rol = rolService.obtenerPorId(id);
        return ResponseEntity.ok(rolMapper.toResponseDTO(rol, rolService.contarMembresiasActivas(id)));
    }

    // POST /api/v1/roles -> 201 Created | 400 Bad Request
    @PostMapping
    public ResponseEntity<RolResponseDTO> crear(@Valid @RequestBody RolRequestDTO dto) {
        RolBd creado = rolService.crear(dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getIdRol())
                .toUri();
        return ResponseEntity.created(ubicacion).body(rolMapper.toResponseDTO(creado, 0));
    }

    // PUT /api/v1/roles/{id} -> 200 OK | 404 Not Found | 400 Bad Request
    @PutMapping("/{id}")
    public ResponseEntity<RolResponseDTO> actualizar(@PathVariable("id") Integer id,
                                                       @Valid @RequestBody RolRequestDTO dto) {
        RolBd actualizado = rolService.actualizar(id, dto);
        return ResponseEntity.ok(rolMapper.toResponseDTO(actualizado, rolService.contarMembresiasActivas(id)));
    }

    // DELETE /api/v1/roles/{id} -> 204 No Content | 404 Not Found | 409 Conflict (en uso)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
