package dgtic.core.rest.dto;

import org.springframework.data.domain.Page;

import java.util.List;

// Sobre estable para respuestas paginadas: evita exponer la estructura interna
// de Page de Spring Data, que no esta pensada como contrato de una API.
public record PaginaDTO<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas
) {
    public static <T> PaginaDTO<T> de(Page<T> page) {
        return new PaginaDTO<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
