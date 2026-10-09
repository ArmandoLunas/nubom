package dgtic.core.controller;

import dgtic.core.model.dto.HogarResumenDTO;
import dgtic.core.security.UsuarioActual;
import dgtic.core.service.HogarService;
import dgtic.core.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ReportesController {

    private final HogarService hogarService;
    private final ReporteService reporteService;

    @GetMapping("/reportes")
    public String reportes(@RequestParam(value = "dias", defaultValue = "7") int dias, Model model) {
        model.addAttribute("menuActivo", "reportes");
        int diasValidos = Math.min(Math.max(dias, 1), 60);
        model.addAttribute("dias", diasValidos);

        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(UsuarioActual.id());
        model.addAttribute("tieneHogar", resumen.isPresent());
        resumen.ifPresent(r -> {
            model.addAttribute("hogarActual", r);
            model.addAttribute("porCaducar", reporteService.porCaducar(r.getIdHogar(), diasValidos));
            var categorias = reporteService.porCategoria(r.getIdHogar());
            model.addAttribute("porCategoria", categorias);
            // Para dibujar las barras proporcionales a la categoria con mas productos.
            model.addAttribute("maxCategoria",
                    categorias.stream().mapToLong(c -> c.getTotal()).max().orElse(1));
            model.addAttribute("ocupacion", reporteService.ocupacion(r.getIdHogar()));
        });
        model.addAttribute("recetasTop", reporteService.recetasMejorCalificadas(10));
        return "reportes";
    }
}
