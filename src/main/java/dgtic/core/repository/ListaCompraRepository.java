package dgtic.core.repository;

import dgtic.core.model.entity.ListaCompraBd;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListaCompraRepository extends JpaRepository<ListaCompraBd, Integer> {

    List<ListaCompraBd> findByHogar_IdHogarOrderByFechaCreacionDesc(Integer idHogar);
}
