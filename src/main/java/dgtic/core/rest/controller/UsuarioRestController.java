package dgtic.core.rest.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import dgtic.core.model.entity.UsuarioBd;
import dgtic.core.rest.dto.MembresiaRequestDTO;
import dgtic.core.rest.dto.MembresiaResponseDTO;
import dgtic.core.rest.dto.UsuarioRequestDTO;
import dgtic.core.rest.dto.UsuarioResponseDTO;
import dgtic.core.rest.dto.UsuarioUpdateDTO;
import dgtic.core.rest.mapper.MembresiaMapper;
import dgtic.core.rest.mapper.UsuarioMapper;
import dgtic.core.rest.service.MembresiaRestService;
import dgtic.core.rest.service.UsuarioRestService;
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
 * API REST del recurso Usuario. Ademas del CRUD propio, expone el lado
 * "usuario" de la relacion N:M usuario &lt;-&gt; hogar (tabla intermedia
 * usuario_hogar, con el rol como atributo propio de la asociacion):
 * asociar, consultar y eliminar la asociacion con un hogar.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Validated
public class UsuarioRestController {

    private final UsuarioRestService usuarioService;
    private final MembresiaRestService membresiaService;
    private final UsuarioMapper usuarioMapper;
    private final MembresiaMapper membresiaMapper;

    // GET /api/v1/usuarios -> 200 OK
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos().stream()
                .map(usuarioMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    // GET /api/v1/usuarios/{id} -> 200 OK | 404 Not Found
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @hogarSeguridad.esUsuario(authentication, #id)")
    public ResponseEntity<UsuarioResponseDTO> obtener(@PathVariable("id") Integer id) {
        UsuarioBd usuario = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(usuarioMapper.toResponseDTO(usuario));
    }

    // POST /api/v1/usuarios -> 201 Created | 400 Bad Request | 409 Conflict (correo duplicado)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioBd creado = usuarioService.crear(dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.getIdUsuario())
                .toUri();
        return ResponseEntity.created(ubicacion).body(usuarioMapper.toResponseDTO(creado));
    }

    // PUT /api/v1/usuarios/{id} -> 200 OK | 404 Not Found | 400 Bad Request | 409 Conflict (correo duplicado)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @hogarSeguridad.esUsuario(authentication, #id)")
    public ResponseEntity<UsuarioResponseDTO> actualizar(@PathVariable("id") Integer id,
                                                           @Valid @RequestBody UsuarioUpdateDTO dto) {
        UsuarioBd actualizado = usuarioService.actualizar(id, dto);
        return ResponseEntity.ok(usuarioMapper.toResponseDTO(actualizado));
    }

    // DELETE /api/v1/usuarios/{id} -> 204 No Content | 404 Not Found | 409 Conflict
    // (si el usuario todavia tiene membresias en usuario_hogar, la llave foranea
    // real generada por la relacion N:M rechaza el borrado: elimina antes sus
    // asociaciones con DELETE /usuarios/{id}/hogares/{idHogar}).
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Integer id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Relacion N:M usuario <-> hogar (lado "usuario") ----

    // GET /api/v1/usuarios/{idUsuario}/hogares -> 200 OK | 404 Not Found
    // Consulta los hogares asociados a este usuario (con su rol en cada uno).
    @GetMapping("/{idUsuario}/hogares")
    @PreAuthorize("hasRole('ADMIN') or @hogarSeguridad.esUsuario(authentication, #idUsuario)")
    public ResponseEntity<List<MembresiaResponseDTO>> listarHogares(
            @PathVariable("idUsuario") @Positive Integer idUsuario) {
        List<MembresiaResponseDTO> hogares = membresiaService.listarHogaresDeUsuario(idUsuario).stream()
                .map(membresiaMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(hogares);
    }

    // POST /api/v1/usuarios/{idUsuario}/hogares/{idHogar} -> 201 Created | 404 Not Found |
    //                                                         400 Bad Request | 409 Conflict
    // Asocia (relacion N:M) al usuario {idUsuario} con el hogar {idHogar}, con el
    // rol indicado en el cuerpo. Rechaza con 409 si ya existe esa asociacion activa
    // o si el usuario ya pertenece a otro hogar activo (regla de negocio de NUBOM).
    @PostMapping("/{idUsuario}/hogares/{idHogar}")
    @PreAuthorize("@hogarSeguridad.esPropietario(authentication, #idHogar)")
    public ResponseEntity<MembresiaResponseDTO> asociarConHogar(
            @PathVariable("idUsuario") @Positive Integer idUsuario,
            @PathVariable("idHogar") @Positive Integer idHogar,
            @Valid @RequestBody MembresiaRequestDTO dto) {
        var creada = membresiaService.asociar(idUsuario, idHogar, dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/usuarios/{idUsuario}/hogares")
                .buildAndExpand(idUsuario)
                .toUri();
        return ResponseEntity.created(ubicacion).body(membresiaMapper.toResponseDTO(creada));
    }

    // DELETE /api/v1/usuarios/{idUsuario}/hogares/{idHogar} -> 204 No Content | 404 Not Found
    // Elimina la asociacion (relacion N:M) entre el usuario y el hogar.
    @DeleteMapping("/{idUsuario}/hogares/{idHogar}")
    @PreAuthorize("@hogarSeguridad.esPropietario(authentication, #idHogar)")
    public ResponseEntity<Void> eliminarAsociacion(
            @PathVariable("idUsuario") @Positive Integer idUsuario,
            @PathVariable("idHogar") @Positive Integer idHogar) {
        membresiaService.eliminarAsociacion(idUsuario, idHogar);
        return ResponseEntity.noContent().build();
    }
}
