package dgtic.core.rest.controller;

import jakarta.validation.constraints.Pattern;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import dgtic.core.rest.exception.RecursoNoEncontradoException;
import dgtic.core.rest.dto.PaginaDTO;
import dgtic.core.client.ProductoExternoDTO;
import dgtic.core.client.OpenFoodFactsClient;
import dgtic.core.security.UsuarioActual;
import dgtic.core.security.HogarSeguridad;
import org.springframework.security.access.prepost.PreAuthorize;
import dgtic.core.model.entity.ProductoBd;
import dgtic.core.rest.dto.ProductoCreateDTO;
import dgtic.core.rest.dto.ProductoRequestDTO;
import dgtic.core.rest.dto.ProductoResponseDTO;
import dgtic.core.rest.mapper.ProductoMapper;
import dgtic.core.rest.service.InventarioRestService;
import dgtic.core.rest.service.ProductoRestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Validated
public class ProductoRestController {

    private final ProductoRestService productoService;
    private final HogarSeguridad hogarSeguridad;
    private final OpenFoodFactsClient openFoodFactsClient;
    private final InventarioRestService inventarioService;
    private final ProductoMapper productoMapper;

    // POST /api/v1/productos -> 201 Created | 404 Not Found (idInventario no existe) |
    //                            400 Bad Request | 409 Conflict (inventario lleno)
    // Crea un producto indicando el inventario destino en el cuerpo (idInventario).
    @PostMapping
    @PreAuthorize("@hogarSeguridad.esMiembroDeInventario(authentication, #dto.idInventario)")
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoCreateDTO dto) {
        ProductoBd creado = inventarioService.agregarProducto(dto.getIdInventario(), productoMapper.toRequestDTO(dto));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getIdProducto())
                .toUri();
        return ResponseEntity.created(ubicacion).body(productoMapper.toResponseDTO(creado));
    }

    // GET /api/v1/productos -> 200 OK
    // GET /api/v1/productos?idHogar={idHogar} -> 200 OK | 404 Not Found
    // GET /api/v1/productos?idInventario={idInventario} -> 200 OK | 404 Not Found
    // GET /api/v1/productos?idHogar={idHogar}&idInventario={idInventario} -> 200 OK | 404 Not Found
    // Lista todos los productos activos; los query params idHogar/idInventario filtran el resultado.
    @GetMapping
    public ResponseEntity<PaginaDTO<ProductoResponseDTO>> listar(
            @RequestParam(value = "idHogar", required = false) @Positive Integer idHogar,
            @RequestParam(value = "idInventario", required = false) @Positive Integer idInventario,
            @RequestParam(value = "categoria", required = false) String categoria,
            @PageableDefault(size = 20, sort = "fechaIngreso", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication auth) {
        if (idInventario != null && !UsuarioActual.esAdmin()
                && !hogarSeguridad.esMiembroDeInventario(auth, idInventario)) {
            throw new AccessDeniedException("Ese inventario no pertenece a tu hogar");
        }
        Integer filtro = hogarSeguridad.filtroDeHogar(auth, idHogar);
        if (HogarSeguridad.SIN_HOGAR.equals(filtro)) {
            return ResponseEntity.ok(PaginaDTO.de(Page.empty(pageable)));
        }
        Page<ProductoResponseDTO> pagina = productoService.listar(filtro, idInventario, categoria, pageable)
                .map(productoMapper::toResponseDTO);
        return ResponseEntity.ok(PaginaDTO.de(pagina));
    }

    // GET /api/v1/productos/externo/{codigoBarras}
    // Datos sugeridos del producto consultando la API externa Open Food Facts.
    @GetMapping("/externo/{codigoBarras}")
    public ResponseEntity<ProductoExternoDTO> buscarExterno(
            @PathVariable("codigoBarras") @Pattern(regexp = "\\d{8,13}",
                    message = "debe tener de 8 a 13 dígitos") String codigoBarras) {
        ProductoExternoDTO producto = openFoodFactsClient.buscarPorCodigo(codigoBarras)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Open Food Facts no tiene registrado el código " + codigoBarras));
        return ResponseEntity.ok(producto);
    }

    // GET /api/v1/productos/{id} -> 200 OK | 404 Not Found
    @GetMapping("/{id}")
    @PreAuthorize("@hogarSeguridad.esMiembroDeProducto(authentication, #id)")
    public ResponseEntity<ProductoResponseDTO> obtener(@PathVariable("id") Integer id) {
        ProductoBd producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(productoMapper.toResponseDTO(producto));
    }

    // PUT /api/v1/productos/{id} -> 200 OK | 404 Not Found | 400 Bad Request
    @PutMapping("/{id}")
    @PreAuthorize("@hogarSeguridad.esMiembroDeProducto(authentication, #id)")
    public ResponseEntity<ProductoResponseDTO> actualizar(@PathVariable("id") Integer id,
                                                            @Valid @RequestBody ProductoRequestDTO dto) {
        ProductoBd actualizado = productoService.actualizar(id, dto);
        return ResponseEntity.ok(productoMapper.toResponseDTO(actualizado));
    }

    // DELETE /api/v1/productos/{id} -> 204 No Content | 404 Not Found
    @DeleteMapping("/{id}")
    @PreAuthorize("@hogarSeguridad.esMiembroDeProducto(authentication, #id)")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
