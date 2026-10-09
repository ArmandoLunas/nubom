package dgtic.core.rest.controller;

import org.springframework.security.core.Authentication;
import dgtic.core.security.HogarSeguridad;
import org.springframework.security.access.prepost.PreAuthorize;
import dgtic.core.model.entity.InventarioBd;
import dgtic.core.rest.dto.InventarioRequestDTO;
import dgtic.core.rest.dto.InventarioResponseDTO;
import dgtic.core.rest.dto.ProductoRequestDTO;
import dgtic.core.rest.dto.ProductoResponseDTO;
import dgtic.core.rest.mapper.InventarioMapper;
import dgtic.core.rest.mapper.ProductoMapper;
import dgtic.core.rest.service.InventarioRestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * API REST del recurso Inventario. Es a la vez el lado "N" de hogar ->
 * inventario y el lado "1" de inventario -> producto, por lo que expone la
 * relacion 1:N con producto como sub-recurso.
 */
@RestController
@RequestMapping("/api/v1/inventarios")
@RequiredArgsConstructor
@Validated
public class InventarioRestController {

    private final InventarioRestService inventarioService;
    private final HogarSeguridad hogarSeguridad;
    private final InventarioMapper inventarioMapper;
    private final ProductoMapper productoMapper;

    // GET /api/v1/inventarios -> 200 OK
    // GET /api/v1/inventarios?idHogar={idHogar} -> 200 OK | 404 Not Found (si idHogar no existe) | 400 (idHogar <= 0)
    // Lista todos los inventarios; con el query param idHogar filtra solo los de ese hogar.
    @GetMapping
    public ResponseEntity<List<InventarioResponseDTO>> listar(
            @RequestParam(value = "idHogar", required = false) @Positive Integer idHogar,
            Authentication auth) {
        Integer filtro = hogarSeguridad.filtroDeHogar(auth, idHogar);
        if (HogarSeguridad.SIN_HOGAR.equals(filtro)) {
            return ResponseEntity.ok(List.of());
        }
        List<InventarioResponseDTO> inventarios = inventarioService.listar(filtro).stream()
                .map(inventarioMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(inventarios);
    }

    // GET /api/v1/inventarios/{id} -> 200 OK | 404 Not Found
    @GetMapping("/{id}")
    @PreAuthorize("@hogarSeguridad.esMiembroDeInventario(authentication, #id)")
    public ResponseEntity<InventarioResponseDTO> obtener(@PathVariable("id") Integer id) {
        InventarioBd inventario = inventarioService.obtenerPorId(id);
        return ResponseEntity.ok(inventarioMapper.toResponseDTO(inventario));
    }

    // PUT /api/v1/inventarios/{id} -> 200 OK | 404 Not Found | 400 Bad Request | 409 Conflict
    @PutMapping("/{id}")
    @PreAuthorize("@hogarSeguridad.esPropietarioDeInventario(authentication, #id)")
    public ResponseEntity<InventarioResponseDTO> actualizar(@PathVariable("id") Integer id,
                                                              @Valid @RequestBody InventarioRequestDTO dto) {
        InventarioBd actualizado = inventarioService.actualizar(id, dto);
        return ResponseEntity.ok(inventarioMapper.toResponseDTO(actualizado));
    }

    // DELETE /api/v1/inventarios/{id} -> 204 No Content | 404 Not Found
    // Elimina en cascada (relacion 1:N) los productos de este inventario.
    @DeleteMapping("/{id}")
    @PreAuthorize("@hogarSeguridad.esPropietarioDeInventario(authentication, #id)")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        inventarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Relacion 1:N inventario -> producto ----

    // GET /api/v1/inventarios/{id}/productos -> 200 OK | 404 Not Found
    @GetMapping("/{id}/productos")
    @PreAuthorize("@hogarSeguridad.esMiembroDeInventario(authentication, #id)")
    public ResponseEntity<List<ProductoResponseDTO>> listarProductos(@PathVariable("id") Integer id) {
        List<ProductoResponseDTO> productos = inventarioService.listarProductos(id).stream()
                .map(productoMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(productos);
    }

    // POST /api/v1/inventarios/{id}/productos -> 201 Created | 404 Not Found | 400 Bad Request | 409 Conflict (inventario lleno)
    @PostMapping("/{id}/productos")
    @PreAuthorize("@hogarSeguridad.esMiembroDeInventario(authentication, #id)")
    public ResponseEntity<ProductoResponseDTO> agregarProducto(@PathVariable("id") Integer id,
                                                                 @Valid @RequestBody ProductoRequestDTO dto) {
        var creado = inventarioService.agregarProducto(id, dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/productos/{idProducto}")
                .buildAndExpand(creado.getIdProducto())
                .toUri();
        return ResponseEntity.created(ubicacion).body(productoMapper.toResponseDTO(creado));
    }
}
