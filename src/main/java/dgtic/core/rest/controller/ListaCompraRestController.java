package dgtic.core.rest.controller;

import org.springframework.security.core.Authentication;
import dgtic.core.security.HogarSeguridad;
import org.springframework.security.access.prepost.PreAuthorize;
import dgtic.core.model.entity.ListaCompraBd;
import dgtic.core.rest.dto.ListaCompraRequestDTO;
import dgtic.core.rest.dto.ListaCompraResponseDTO;
import dgtic.core.rest.mapper.ListaCompraMapper;
import dgtic.core.rest.service.ListaCompraRestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST del recurso ListaCompra (lado "N" de la relacion hogar -> lista_compra).
 * La creacion vive en HogarRestController (POST /hogares/{id}/lista-compra).
 */
@RestController
@RequestMapping("/api/v1/lista-compra")
@RequiredArgsConstructor
@Validated
public class ListaCompraRestController {

    private final ListaCompraRestService listaCompraService;
    private final HogarSeguridad hogarSeguridad;
    private final ListaCompraMapper listaCompraMapper;

    // GET /api/v1/lista-compra -> 200 OK
    // GET /api/v1/lista-compra?idHogar={idHogar} -> 200 OK | 404 Not Found
    // Lista todos los items de la lista de compras de todos los hogares; el
    // query param idHogar filtra solo los de ese hogar.
    @GetMapping
    public ResponseEntity<List<ListaCompraResponseDTO>> listar(
            @RequestParam(value = "idHogar", required = false) @Positive Integer idHogar,
            Authentication auth) {
        Integer filtro = hogarSeguridad.filtroDeHogar(auth, idHogar);
        if (HogarSeguridad.SIN_HOGAR.equals(filtro)) {
            return ResponseEntity.ok(List.of());
        }
        List<ListaCompraResponseDTO> items = listaCompraService.listar(filtro).stream()
                .map(listaCompraMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(items);
    }

    // GET /api/v1/lista-compra/{id} -> 200 OK | 404 Not Found
    @GetMapping("/{id}")
    @PreAuthorize("@hogarSeguridad.esMiembroDeItem(authentication, #id)")
    public ResponseEntity<ListaCompraResponseDTO> obtener(@PathVariable("id") Integer id) {
        ListaCompraBd item = listaCompraService.obtenerPorId(id);
        return ResponseEntity.ok(listaCompraMapper.toResponseDTO(item));
    }

    // PUT /api/v1/lista-compra/{id} -> 200 OK | 404 Not Found | 400 Bad Request
    @PutMapping("/{id}")
    @PreAuthorize("@hogarSeguridad.esMiembroDeItem(authentication, #id)")
    public ResponseEntity<ListaCompraResponseDTO> actualizar(@PathVariable("id") Integer id,
                                                               @Valid @RequestBody ListaCompraRequestDTO dto) {
        ListaCompraBd actualizado = listaCompraService.actualizar(id, dto);
        return ResponseEntity.ok(listaCompraMapper.toResponseDTO(actualizado));
    }

    // DELETE /api/v1/lista-compra/{id} -> 204 No Content | 404 Not Found
    @DeleteMapping("/{id}")
    @PreAuthorize("@hogarSeguridad.esMiembroDeItem(authentication, #id)")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        listaCompraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
