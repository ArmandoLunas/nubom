package dgtic.core.service;

import dgtic.core.model.dto.ListaCompraDTO;
import dgtic.core.model.entity.ListaCompraBd;

import java.util.List;

public interface ListaCompraService {

    List<ListaCompraBd> listar(Integer idHogar);

    ListaCompraBd agregar(Integer idHogar, ListaCompraDTO dto);

    // Lanza IllegalArgumentException si el item no pertenece a ese hogar
    ListaCompraBd actualizar(Integer idHogar, ListaCompraDTO dto);

    void eliminar(Integer idHogar, Integer idItem);

    void marcarComprado(Integer idHogar, Integer idItem, boolean comprado);
}
