package dgtic.core.rest.controller;

import dgtic.core.model.dto.ProductoCaducidadDTO;
import dgtic.core.model.dto.RecetaRankingDTO;
import dgtic.core.model.dto.ReporteCategoriaDTO;
import dgtic.core.model.dto.ReporteOcupacionDTO;
import dgtic.core.rest.exception.RecursoNoEncontradoException;
import dgtic.core.security.HogarSeguridad;
import dgtic.core.service.ReporteService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Reportes. Los tres primeros son siempre del hogar del usuario autenticado,
// asi que no reciben idHogar: no hay forma de pedir los de otro hogar.
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Validated
public class ReporteRestController {

    private final ReporteService reporteService;
    private final HogarSeguridad hogarSeguridad;

    // GET /api/v1/reportes/por-caducar?dias=3
    @GetMapping("/por-caducar")
    public ResponseEntity<List<ProductoCaducidadDTO>> porCaducar(
            @RequestParam(value = "dias", defaultValue = "3") @Min(0) @Max(365) int dias,
            Authentication auth) {
        return ResponseEntity.ok(reporteService.porCaducar(hogarDe(auth), dias));
    }

    // GET /api/v1/reportes/por-categoria
    @GetMapping("/por-categoria")
    public ResponseEntity<List<ReporteCategoriaDTO>> porCategoria(Authentication auth) {
        return ResponseEntity.ok(reporteService.porCategoria(hogarDe(auth)));
    }

    // GET /api/v1/reportes/ocupacion
    @GetMapping("/ocupacion")
    public ResponseEntity<List<ReporteOcupacionDTO>> ocupacion(Authentication auth) {
        return ResponseEntity.ok(reporteService.ocupacion(hogarDe(auth)));
    }

    // GET /api/v1/reportes/recetas-top
    @GetMapping("/recetas-top")
    public ResponseEntity<List<RecetaRankingDTO>> recetasTop() {
        return ResponseEntity.ok(reporteService.recetasMejorCalificadas(10));
    }

    private Integer hogarDe(Authentication auth) {
        return hogarSeguridad.idHogarDe(auth)
                .orElseThrow(() -> new RecursoNoEncontradoException("Aún no perteneces a ningún hogar"));
    }
}
