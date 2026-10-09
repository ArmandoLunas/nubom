package dgtic.core.rest.service;

import dgtic.core.model.entity.HogarBd;
import dgtic.core.repository.HogarRepository;
import dgtic.core.rest.dto.HogarRequestDTO;
import dgtic.core.rest.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HogarRestService {

    private final HogarRepository hogarRepository;

    @Transactional(readOnly = true)
    public List<HogarBd> listarTodos() {
        return hogarRepository.findAll();
    }

    @Transactional(readOnly = true)
    public HogarBd obtenerPorId(Integer idHogar) {
        return hogarRepository.findById(idHogar)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un hogar con id " + idHogar));
    }

    @Transactional
    public HogarBd crear(HogarRequestDTO dto) {
        HogarBd hogar = HogarBd.builder()
                .nombre(dto.getNombre())
                .fechaCreacion(LocalDateTime.now())
                .build();
        return hogarRepository.save(hogar);
    }

    @Transactional
    public HogarBd actualizar(Integer idHogar, HogarRequestDTO dto) {
        HogarBd hogar = obtenerPorId(idHogar);
        hogar.setNombre(dto.getNombre());
        return hogarRepository.save(hogar);
    }

    // Al eliminar el hogar, sus inventarios (y los productos de estos) y su
    // lista de compras se eliminan en cascada gracias a las relaciones 1:N
    // (cascade = ALL, orphanRemoval = true) declaradas en HogarBd.
    @Transactional
    public void eliminar(Integer idHogar) {
        HogarBd hogar = obtenerPorId(idHogar);
        hogarRepository.delete(hogar);
    }
}
