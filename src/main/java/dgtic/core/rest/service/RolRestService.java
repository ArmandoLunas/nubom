package dgtic.core.rest.service;

import dgtic.core.model.entity.RolBd;
import dgtic.core.repository.RolRepository;
import dgtic.core.repository.UsuarioHogarRepository;
import dgtic.core.rest.dto.RolRequestDTO;
import dgtic.core.rest.exception.ConflictoIntegridadException;
import dgtic.core.rest.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolRestService {

    private final RolRepository rolRepository;
    private final UsuarioHogarRepository usuarioHogarRepository;

    @Transactional(readOnly = true)
    public List<RolBd> listarTodos() {
        return rolRepository.findAll();
    }

    @Transactional(readOnly = true)
    public RolBd obtenerPorId(Integer idRol) {
        return rolRepository.findById(idRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un rol con id " + idRol));
    }

    @Transactional(readOnly = true)
    public long contarMembresiasActivas(Integer idRol) {
        return usuarioHogarRepository.countByRol_IdRolAndActivoTrue(idRol);
    }

    @Transactional
    public RolBd crear(RolRequestDTO dto) {
        RolBd rol = RolBd.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .build();
        return rolRepository.save(rol);
    }

    @Transactional
    public RolBd actualizar(Integer idRol, RolRequestDTO dto) {
        RolBd rol = obtenerPorId(idRol);
        rol.setNombre(dto.getNombre());
        rol.setDescripcion(dto.getDescripcion());
        return rolRepository.save(rol);
    }

    // Ejemplo de conflicto de integridad (409): no se puede borrar un rol
    // mientras existan membresias activas (usuario_hogar) que lo referencien.
    @Transactional
    public void eliminar(Integer idRol) {
        RolBd rol = obtenerPorId(idRol);
        long enUso = usuarioHogarRepository.countByRol_IdRolAndActivoTrue(idRol);
        if (enUso > 0) {
            throw new ConflictoIntegridadException(
                    "No se puede eliminar el rol \"" + rol.getNombre() + "\": está asignado a " +
                            enUso + " membresía(s) activa(s)");
        }
        rolRepository.delete(rol);
    }
}
