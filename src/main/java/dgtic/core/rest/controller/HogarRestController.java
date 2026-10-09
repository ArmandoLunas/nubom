package dgtic.core.rest.controller;

import org.springframework.security.core.Authentication;
import dgtic.core.service.HogarService;
import dgtic.core.security.UsuarioActual;
import dgtic.core.security.HogarSeguridad;
import dgtic.core.model.dto.HogarDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import dgtic.core.model.entity.HogarBd;
import dgtic.core.rest.dto.HogarRequestDTO;
import dgtic.core.rest.dto.HogarResponseDTO;
import dgtic.core.rest.dto.InventarioRequestDTO;
import dgtic.core.rest.dto.InventarioResponseDTO;
import dgtic.core.rest.dto.ListaCompraRequestDTO;
import dgtic.core.rest.dto.ListaCompraResponseDTO;
import dgtic.core.rest.dto.MembresiaResponseDTO;
import dgtic.core.rest.mapper.HogarMapper;
import dgtic.core.rest.mapper.InventarioMapper;
import dgtic.core.rest.mapper.ListaCompraMapper;
import dgtic.core.rest.mapper.MembresiaMapper;
import dgtic.core.rest.service.HogarRestService;
import dgtic.core.rest.service.InventarioRestService;
import dgtic.core.rest.service.ListaCompraRestService;
import dgtic.core.rest.service.MembresiaRestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * API REST del recurso Hogar. Ademas del CRUD propio, expone las relaciones
 * 1:N hogar -> inventario y hogar -> lista_compra (consultar y crear
 * informacion asociada a traves de sub-recursos anidados en la URL), y el
 * lado "hogar" de la relacion N:M usuario &lt;-&gt; hogar.
 */
@RestController
@RequestMapping("/api/v1/hogares")
@RequiredArgsConstructor
public class HogarRestController {

    private final HogarRestService hogarService;
    private final HogarService hogarWebService;
    private final HogarSeguridad hogarSeguridad;
    private final InventarioRestService inventarioService;
    private final ListaCompraRestService listaCompraService;
    private final MembresiaRestService membresiaService;

    private final HogarMapper hogarMapper;
    private final InventarioMapper inventarioMapper;
    private final ListaCompraMapper listaCompraMapper;
    private final MembresiaMapper membresiaMapper;

    // GET /api/v1/hogares -> 200 OK
    @GetMapping
    public ResponseEntity<List<HogarResponseDTO>> listar(Authentication auth) {
        // Un administrador ve todos los hogares; cualquier otro usuario, solo el suyo.
        List<HogarBd> visibles = UsuarioActual.esAdmin()
                ? hogarService.listarTodos()
                : hogarSeguridad.idHogarDe(auth).map(hogarService::obtenerPorId).map(List::of).orElse(List.of());
        List<HogarResponseDTO> hogares = visibles.stream()
                .map(hogarMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(hogares);
    }

    // GET /api/v1/hogares/{id} -> 200 OK | 404 Not Found
    @GetMapping("/{id}")
    @PreAuthorize("@hogarSeguridad.esMiembro(authentication, #id)")
    public ResponseEntity<HogarResponseDTO> obtener(@PathVariable("id") Integer id) {
        HogarBd hogar = hogarService.obtenerPorId(id);
        return ResponseEntity.ok(hogarMapper.toResponseDTO(hogar));
    }

    // POST /api/v1/hogares -> 201 Created (con header Location) | 400 Bad Request
    @PostMapping
    public ResponseEntity<HogarResponseDTO> crear(@Valid @RequestBody HogarRequestDTO dto) {
        // Misma regla que en la aplicacion web: quien crea el hogar queda como
        // PROPIETARIO y se generan su refrigerador y su alacena.
        Integer idHogar = hogarWebService.crearHogar(UsuarioActual.id(), new HogarDTO(dto.getNombre())).getIdHogar();
        HogarBd creado = hogarService.obtenerPorId(idHogar);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getIdHogar())
                .toUri();
        return ResponseEntity.created(ubicacion).body(hogarMapper.toResponseDTO(creado));
    }

    // PUT /api/v1/hogares/{id} -> 200 OK | 404 Not Found | 400 Bad Request
    @PutMapping("/{id}")
    @PreAuthorize("@hogarSeguridad.esPropietario(authentication, #id)")
    public ResponseEntity<HogarResponseDTO> actualizar(@PathVariable("id") Integer id,
                                                         @Valid @RequestBody HogarRequestDTO dto) {
        HogarBd actualizado = hogarService.actualizar(id, dto);
        return ResponseEntity.ok(hogarMapper.toResponseDTO(actualizado));
    }

    // DELETE /api/v1/hogares/{id} -> 204 No Content | 404 Not Found
    // Elimina en cascada (relacion 1:N) sus inventarios, productos y lista de compras.
    @DeleteMapping("/{id}")
    @PreAuthorize("@hogarSeguridad.esPropietario(authentication, #id)")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        hogarService.obtenerPorId(id); // 404 si no existe
        // Elimina tambien las membresias; inventarios, productos y lista se van en cascada.
        hogarWebService.eliminarHogar(id, UsuarioActual.id());
        return ResponseEntity.noContent().build();
    }

    // ---- Relacion 1:N hogar -> inventario ----

    // GET /api/v1/hogares/{id}/inventarios -> 200 OK | 404 Not Found
    @GetMapping("/{id}/inventarios")
    @PreAuthorize("@hogarSeguridad.esMiembro(authentication, #id)")
    public ResponseEntity<List<InventarioResponseDTO>> listarInventarios(@PathVariable("id") Integer id) {
        List<InventarioResponseDTO> inventarios = inventarioService.listarPorHogar(id).stream()
                .map(inventarioMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(inventarios);
    }

    // POST /api/v1/hogares/{id}/inventarios -> 201 Created | 404 Not Found | 400 Bad Request
    @PostMapping("/{id}/inventarios")
    @PreAuthorize("@hogarSeguridad.esPropietario(authentication, #id)")
    public ResponseEntity<InventarioResponseDTO> crearInventario(@PathVariable("id") Integer id,
                                                                   @Valid @RequestBody InventarioRequestDTO dto) {
        var creado = inventarioService.crearParaHogar(id, dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/inventarios/{idInventario}")
                .buildAndExpand(creado.getIdInventario())
                .toUri();
        return ResponseEntity.created(ubicacion).body(inventarioMapper.toResponseDTO(creado));
    }

    // ---- Relacion 1:N hogar -> lista_compra ----

    // GET /api/v1/hogares/{id}/lista-compra -> 200 OK | 404 Not Found
    @GetMapping("/{id}/lista-compra")
    @PreAuthorize("@hogarSeguridad.esMiembro(authentication, #id)")
    public ResponseEntity<List<ListaCompraResponseDTO>> listarListaCompra(@PathVariable("id") Integer id) {
        List<ListaCompraResponseDTO> items = listaCompraService.listarPorHogar(id).stream()
                .map(listaCompraMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(items);
    }

    // POST /api/v1/hogares/{id}/lista-compra -> 201 Created | 404 Not Found | 400 Bad Request
    @PostMapping("/{id}/lista-compra")
    @PreAuthorize("@hogarSeguridad.esMiembro(authentication, #id)")
    public ResponseEntity<ListaCompraResponseDTO> agregarAListaCompra(@PathVariable("id") Integer id,
                                                                        @Valid @RequestBody ListaCompraRequestDTO dto) {
        var creado = listaCompraService.agregarParaHogar(id, dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/lista-compra/{idItem}")
                .buildAndExpand(creado.getIdItem())
                .toUri();
        return ResponseEntity.created(ubicacion).body(listaCompraMapper.toResponseDTO(creado));
    }

    // ---- Relacion N:M usuario <-> hogar (lado "hogar") ----

    // GET /api/v1/hogares/{id}/usuarios -> 200 OK | 404 Not Found
    // Consulta los usuarios asociados a este hogar (con su rol), del lado
    // "hogar" de la relacion N:M usuario<->hogar.
    @GetMapping("/{id}/usuarios")
    @PreAuthorize("@hogarSeguridad.esMiembro(authentication, #id)")
    public ResponseEntity<List<MembresiaResponseDTO>> listarUsuarios(@PathVariable("id") Integer id) {
        List<MembresiaResponseDTO> miembros = membresiaService.listarUsuariosDeHogar(id).stream()
                .map(membresiaMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(miembros);
    }
}
