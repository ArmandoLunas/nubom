package dgtic.core.rest.service;

import dgtic.core.model.entity.InventarioTipoBd;
import dgtic.core.repository.InventarioRepository;
import dgtic.core.repository.InventarioTipoRepository;
import dgtic.core.rest.dto.InventarioTipoRequestDTO;
import dgtic.core.rest.exception.ConflictoIntegridadException;
import dgtic.core.rest.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioTipoRestService {

    private final InventarioTipoRepository inventarioTipoRepository;
    private final InventarioRepository inventarioRepository;

    @Transactional(readOnly = true)
    public List<InventarioTipoBd> listarTodos() {
        return inventarioTipoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public InventarioTipoBd obtenerPorId(Integer idTipo) {
        return inventarioTipoRepository.findById(idTipo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un tipo de inventario con id " + idTipo));
    }

    @Transactional
    public InventarioTipoBd crear(InventarioTipoRequestDTO dto) {
        InventarioTipoBd tipo = InventarioTipoBd.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .icono(dto.getIcono())
                .build();
        return inventarioTipoRepository.save(tipo);
    }

    @Transactional
    public InventarioTipoBd actualizar(Integer idTipo, InventarioTipoRequestDTO dto) {
        InventarioTipoBd tipo = obtenerPorId(idTipo);
        tipo.setNombre(dto.getNombre());
        tipo.setDescripcion(dto.getDescripcion());
        tipo.setIcono(dto.getIcono());
        return inventarioTipoRepository.save(tipo);
    }

    // Ejemplo de conflicto de integridad (409): no se puede borrar un tipo de
    // inventario mientras existan inventarios (relacion 1:N) que lo referencien.
    @Transactional
    public void eliminar(Integer idTipo) {
        InventarioTipoBd tipo = obtenerPorId(idTipo);
        long enUso = inventarioRepository.countByTipo_IdTipo(idTipo);
        if (enUso > 0) {
            throw new ConflictoIntegridadException(
                    "No se puede eliminar el tipo \"" + tipo.getNombre() + "\": está siendo usado por " +
                            enUso + " inventario(s)");
        }
        inventarioTipoRepository.delete(tipo);
    }
}
