package dgtic.core.rest.service;

import dgtic.core.model.entity.HogarBd;
import dgtic.core.model.entity.ListaCompraBd;
import dgtic.core.repository.HogarRepository;
import dgtic.core.repository.ListaCompraRepository;
import dgtic.core.rest.dto.ListaCompraRequestDTO;
import dgtic.core.rest.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// Servicio REST para ListaCompraBd: lado "N" de la relacion 1:N hogar -> lista_compra.
@Service
@RequiredArgsConstructor
public class ListaCompraRestService {

    private final ListaCompraRepository listaCompraRepository;
    private final HogarRepository hogarRepository;

    @Transactional(readOnly = true)
    public List<ListaCompraBd> listarPorHogar(Integer idHogar) {
        obtenerHogar(idHogar);
        return listaCompraRepository.findByHogar_IdHogarOrderByFechaCreacionDesc(idHogar);
    }

    // Listado general de la lista de compras, con filtro opcional por hogar
    // (query param). Sin filtro, devuelve los items de todos los hogares.
    @Transactional(readOnly = true)
    public List<ListaCompraBd> listar(Integer idHogar) {
        if (idHogar == null) {
            return listaCompraRepository.findAll();
        }
        return listarPorHogar(idHogar);
    }

    @Transactional(readOnly = true)
    public ListaCompraBd obtenerPorId(Integer idItem) {
        return listaCompraRepository.findById(idItem)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un item de lista de compras con id " + idItem));
    }

    @Transactional
    public ListaCompraBd agregarParaHogar(Integer idHogar, ListaCompraRequestDTO dto) {
        HogarBd hogar = obtenerHogar(idHogar);
        ListaCompraBd item = ListaCompraBd.builder()
                .hogar(hogar)
                .nombre(dto.getNombre())
                .categoria(dto.getCategoria())
                .cantidad(dto.getCantidad())
                .unidad(dto.getUnidad())
                .comprado(dto.getComprado() != null && dto.getComprado())
                .fechaCreacion(LocalDateTime.now())
                .build();
        return listaCompraRepository.save(item);
    }

    @Transactional
    public ListaCompraBd actualizar(Integer idItem, ListaCompraRequestDTO dto) {
        ListaCompraBd item = obtenerPorId(idItem);
        item.setNombre(dto.getNombre());
        item.setCategoria(dto.getCategoria());
        item.setCantidad(dto.getCantidad());
        item.setUnidad(dto.getUnidad());
        if (dto.getComprado() != null) {
            item.setComprado(dto.getComprado());
        }
        return listaCompraRepository.save(item);
    }

    @Transactional
    public void eliminar(Integer idItem) {
        ListaCompraBd item = obtenerPorId(idItem);
        listaCompraRepository.delete(item);
    }

    private HogarBd obtenerHogar(Integer idHogar) {
        return hogarRepository.findById(idHogar)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un hogar con id " + idHogar));
    }
}
