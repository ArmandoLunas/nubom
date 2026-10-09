package dgtic.core.service;

import dgtic.core.model.dto.ListaCompraDTO;
import dgtic.core.model.entity.HogarBd;
import dgtic.core.model.entity.ListaCompraBd;
import dgtic.core.repository.HogarRepository;
import dgtic.core.repository.ListaCompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListaCompraServiceImpl implements ListaCompraService {

    @Autowired
    private ListaCompraRepository listaCompraRepository;

    @Autowired
    private HogarRepository hogarRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ListaCompraBd> listar(Integer idHogar) {
        return listaCompraRepository.findByHogar_IdHogarOrderByFechaCreacionDesc(idHogar);
    }

    @Transactional
    @Override
    public ListaCompraBd agregar(Integer idHogar, ListaCompraDTO dto) {
        HogarBd hogar = hogarRepository.findById(idHogar)
                .orElseThrow(() -> new IllegalArgumentException("Hogar no encontrado"));

        ListaCompraBd item = ListaCompraBd.builder()
                .hogar(hogar)
                .nombre(dto.getNombre())
                .categoria(dto.getCategoria())
                .cantidad(dto.getCantidad())
                .unidad(dto.getUnidad())
                .comprado(false)
                .fechaCreacion(LocalDateTime.now())
                .build();

        return listaCompraRepository.save(item);
    }

    @Transactional
    @Override
    public ListaCompraBd actualizar(Integer idHogar, ListaCompraDTO dto) {
        ListaCompraBd item = obtenerDeHogar(idHogar, dto.getIdItem());

        item.setNombre(dto.getNombre());
        item.setCategoria(dto.getCategoria());
        item.setCantidad(dto.getCantidad());
        item.setUnidad(dto.getUnidad());

        return listaCompraRepository.save(item);
    }

    @Transactional
    @Override
    public void eliminar(Integer idHogar, Integer idItem) {
        obtenerDeHogar(idHogar, idItem);
        listaCompraRepository.deleteById(idItem);
    }

    @Transactional
    @Override
    public void marcarComprado(Integer idHogar, Integer idItem, boolean comprado) {
        ListaCompraBd item = obtenerDeHogar(idHogar, idItem);
        item.setComprado(comprado);
        listaCompraRepository.save(item);
    }

    private ListaCompraBd obtenerDeHogar(Integer idHogar, Integer idItem) {
        ListaCompraBd item = listaCompraRepository.findById(idItem)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en la lista"));

        if (!item.getHogar().getIdHogar().equals(idHogar)) {
            throw new IllegalArgumentException("Este producto no pertenece a tu lista de compras");
        }

        return item;
    }
}
