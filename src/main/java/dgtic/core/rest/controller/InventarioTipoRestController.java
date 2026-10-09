package dgtic.core.rest.controller;

import dgtic.core.model.entity.InventarioTipoBd;
import dgtic.core.rest.dto.InventarioTipoRequestDTO;
import dgtic.core.rest.dto.InventarioTipoResponseDTO;
import dgtic.core.rest.mapper.InventarioTipoMapper;
import dgtic.core.rest.service.InventarioTipoRestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * API REST del catalogo InventarioTipo (lado "1" de la relacion 1:N con
 * Inventario). Sirve tambien para demostrar el 409 Conflict por integridad:
 * no se puede borrar un tipo que todavia tenga inventarios asociados.
 */
@RestController
@RequestMapping("/api/v1/tipos-inventario")
@RequiredArgsConstructor
public class InventarioTipoRestController {

    private final InventarioTipoRestService tipoService;
    private final InventarioTipoMapper tipoMapper;

    // GET /api/v1/tipos-inventario -> 200 OK
    @GetMapping
    public ResponseEntity<List<InventarioTipoResponseDTO>> listar() {
        List<InventarioTipoResponseDTO> tipos = tipoService.listarTodos().stream()
                .map(tipoMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(tipos);
    }

    // GET /api/v1/tipos-inventario/{id} -> 200 OK | 404 Not Found
    @GetMapping("/{id}")
    public ResponseEntity<InventarioTipoResponseDTO> obtener(@PathVariable("id") Integer id) {
        InventarioTipoBd tipo = tipoService.obtenerPorId(id);
        return ResponseEntity.ok(tipoMapper.toResponseDTO(tipo));
    }

    // POST /api/v1/tipos-inventario -> 201 Created | 400 Bad Request
    @PostMapping
    public ResponseEntity<InventarioTipoResponseDTO> crear(@Valid @RequestBody InventarioTipoRequestDTO dto) {
        InventarioTipoBd creado = tipoService.crear(dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getIdTipo())
                .toUri();
        return ResponseEntity.created(ubicacion).body(tipoMapper.toResponseDTO(creado));
    }

    // PUT /api/v1/tipos-inventario/{id} -> 200 OK | 404 Not Found | 400 Bad Request
    @PutMapping("/{id}")
    public ResponseEntity<InventarioTipoResponseDTO> actualizar(@PathVariable("id") Integer id,
                                                                  @Valid @RequestBody InventarioTipoRequestDTO dto) {
        InventarioTipoBd actualizado = tipoService.actualizar(id, dto);
        return ResponseEntity.ok(tipoMapper.toResponseDTO(actualizado));
    }

    // DELETE /api/v1/tipos-inventario/{id} -> 204 No Content | 404 Not Found | 409 Conflict (en uso)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        tipoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
