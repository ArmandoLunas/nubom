package dgtic.core.controller;

import dgtic.core.model.dto.HogarResumenDTO;
import dgtic.core.security.UsuarioActual;
import dgtic.core.service.HogarService;
import dgtic.core.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class InicioController {

    @Autowired
    private HogarService hogarService;

    @Autowired
    private ReporteService reporteService;

    @GetMapping("/inicio")
    public String inicio(Model model) {
        model.addAttribute("menuActivo", "inicio");

        Optional<HogarResumenDTO> resumen = hogarService.obtenerResumenDeUsuario(UsuarioActual.id());
        model.addAttribute("tieneHogar", resumen.isPresent());
        model.addAttribute("productosPorCaducar", List.of());
        model.addAttribute("ocupacion", List.of());

        resumen.ifPresent(r -> {
            model.addAttribute("hogarActual", r);
            var ocupacion = reporteService.ocupacion(r.getIdHogar());
            model.addAttribute("ocupacion", ocupacion);
            model.addAttribute("totalProductos", ocupacion.stream().mapToLong(o -> o.getProductos()).sum());
            // Vencidos o que vencen en la proxima semana, del mas urgente al menos urgente.
            model.addAttribute("productosPorCaducar",
                    reporteService.porCaducar(r.getIdHogar(), 7).stream().limit(8).toList());
        });

        model.addAttribute("recetasDestacadas", reporteService.recetasMejorCalificadas(3));
        return "inicio";
    }
}
