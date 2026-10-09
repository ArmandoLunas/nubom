package dgtic.core.service;

import dgtic.core.model.dto.IngredienteDTO;
import dgtic.core.model.dto.RecetaDTO;
import dgtic.core.model.dto.RecetaVistaDTO;
import dgtic.core.model.entity.EstadoReceta;
import dgtic.core.model.entity.RecetaBd;
import dgtic.core.model.entity.RecetaCalificacionBd;
import dgtic.core.model.entity.RecetaIngredienteBd;
import dgtic.core.repository.RecetaCalificacionRepository;
import dgtic.core.repository.RecetaRepository;
import dgtic.core.repository.UsuarioRepository;
import dgtic.core.rest.exception.ConflictoIntegridadException;
import dgtic.core.rest.exception.RecursoNoEncontradoException;
import dgtic.core.rest.exception.SolicitudInvalidaException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Recetas: CRUD del autor, comparticion con moderacion y calificaciones.
// Lo comparten la aplicacion web y la API REST.
@Service
@RequiredArgsConstructor
public class RecetaService {

    private final RecetaRepository recetaRepository;
    private final RecetaCalificacionRepository calificacionRepository;
    private final UsuarioRepository usuarioRepository;

    // ------------------------------------------------------------ consultas

    // Recetario de la comunidad: solo recetas aprobadas.
    @Transactional(readOnly = true)
    public Page<RecetaVistaDTO> comunidad(String texto, Pageable pageable, Integer idUsuarioActual) {
        String filtro = texto == null ? "" : texto.trim();
        Page<RecetaBd> pagina = recetaRepository
                .findByEstadoAndNombreContainingIgnoreCase(EstadoReceta.APROBADA, filtro, pageable);
        Map<Integer, double[]> resumen = resumenDe(pagina.getContent());
        return pagina.map(r -> aVista(r, resumen, idUsuarioActual));
    }

    @Transactional(readOnly = true)
    public List<RecetaVistaDTO> propias(Integer idUsuario) {
        List<RecetaBd> recetas = recetaRepository.findByAutor_IdUsuarioOrderByFechaCreacionDesc(idUsuario);
        Map<Integer, double[]> resumen = resumenDe(recetas);
        return recetas.stream().map(r -> aVista(r, resumen, idUsuario)).toList();
    }

    // Bandeja de moderacion del administrador.
    @Transactional(readOnly = true)
    public List<RecetaVistaDTO> pendientes() {
        List<RecetaBd> recetas = recetaRepository.findByEstadoOrderByFechaModificacionAsc(EstadoReceta.PENDIENTE);
        Map<Integer, double[]> resumen = resumenDe(recetas);
        return recetas.stream().map(r -> aVista(r, resumen, null)).toList();
    }

    // Una receta la ve su autor, un administrador o cualquiera si esta aprobada.
    @Transactional(readOnly = true)
    public RecetaBd obtenerVisible(Integer idReceta, Integer idUsuario, boolean esAdmin) {
        RecetaBd receta = obtener(idReceta);
        boolean esAutor = receta.getAutor().getIdUsuario().equals(idUsuario);
        if (!esAutor && !esAdmin && receta.getEstado() != EstadoReceta.APROBADA) {
            throw new AccessDeniedException("Esta receta no está publicada");
        }
        receta.getIngredientes().size(); // inicializa la coleccion dentro de la transaccion
        return receta;
    }

    @Transactional(readOnly = true)
    public RecetaVistaDTO vistaDe(RecetaBd receta, Integer idUsuarioActual) {
        return aVista(receta, resumenDe(List.of(receta)), idUsuarioActual);
    }

    @Transactional(readOnly = true)
    public RecetaDTO aFormulario(Integer idReceta, Integer idUsuario) {
        RecetaBd receta = obtenerDelAutor(idReceta, idUsuario);
        RecetaDTO dto = new RecetaDTO();
        dto.setNombre(receta.getNombre());
        dto.setDescripcion(receta.getDescripcion());
        dto.setPasos(receta.getPasos());
        dto.setTiempoMinutos(receta.getTiempoMinutos());
        dto.setIngredientes(receta.getIngredientes().stream()
                .map(i -> new IngredienteDTO(i.getNombre(), i.getCantidad(), i.getUnidad(),
                        Boolean.TRUE.equals(i.getPrincipal())))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
        return dto;
    }

    @Transactional(readOnly = true)
    public Optional<Integer> calificacionDe(Integer idReceta, Integer idUsuario) {
        return calificacionRepository.findByReceta_IdRecetaAndUsuario_IdUsuario(idReceta, idUsuario)
                .map(RecetaCalificacionBd::getPuntuacion);
    }

    // ------------------------------------------------------------ CRUD del autor

    @Transactional
    public RecetaBd crear(Integer idUsuario, RecetaDTO dto) {
        validarIngredientes(dto);
        RecetaBd receta = RecetaBd.builder()
                .autor(usuarioRepository.getReferenceById(idUsuario))
                .estado(EstadoReceta.PRIVADA)
                .build();
        copiarDatos(dto, receta);
        return recetaRepository.save(receta);
    }

    @Transactional
    public RecetaBd actualizar(Integer idReceta, Integer idUsuario, RecetaDTO dto) {
        validarIngredientes(dto);
        RecetaBd receta = obtenerDelAutor(idReceta, idUsuario);
        copiarDatos(dto, receta);
        // Lo que ya reviso el administrador cambio: debe volver a revisarse.
        if (receta.getEstado() == EstadoReceta.APROBADA) {
            receta.setEstado(EstadoReceta.PENDIENTE);
        }
        return recetaRepository.save(receta);
    }

    @Transactional
    public void eliminar(Integer idReceta, Integer idUsuario) {
        recetaRepository.delete(obtenerDelAutor(idReceta, idUsuario));
    }

    // ------------------------------------------------------------ compartir y moderar

    @Transactional
    public RecetaBd compartir(Integer idReceta, Integer idUsuario) {
        RecetaBd receta = obtenerDelAutor(idReceta, idUsuario);
        if (receta.getEstado() == EstadoReceta.PENDIENTE) {
            throw new ConflictoIntegridadException("La receta ya está en revisión");
        }
        if (receta.getEstado() == EstadoReceta.APROBADA) {
            throw new ConflictoIntegridadException("La receta ya está publicada");
        }
        receta.setEstado(EstadoReceta.PENDIENTE);
        return recetaRepository.save(receta);
    }

    @Transactional
    public RecetaBd moderar(Integer idReceta, boolean aprobar) {
        RecetaBd receta = obtener(idReceta);
        if (receta.getEstado() != EstadoReceta.PENDIENTE) {
            throw new ConflictoIntegridadException("La receta no está pendiente de revisión");
        }
        receta.setEstado(aprobar ? EstadoReceta.APROBADA : EstadoReceta.RECHAZADA);
        return recetaRepository.save(receta);
    }

    // ------------------------------------------------------------ calificaciones

    @Transactional
    public void calificar(Integer idReceta, Integer idUsuario, int puntuacion) {
        if (puntuacion < 1 || puntuacion > 5) {
            throw new SolicitudInvalidaException("La puntuación debe estar entre 1 y 5");
        }
        RecetaBd receta = obtener(idReceta);
        if (receta.getAutor().getIdUsuario().equals(idUsuario)) {
            throw new ConflictoIntegridadException("No puedes calificar tu propia receta");
        }
        if (receta.getEstado() != EstadoReceta.APROBADA) {
            throw new ConflictoIntegridadException("Solo se pueden calificar recetas publicadas");
        }
        RecetaCalificacionBd calificacion = calificacionRepository
                .findByReceta_IdRecetaAndUsuario_IdUsuario(idReceta, idUsuario)
                .orElseGet(() -> RecetaCalificacionBd.builder()
                        .receta(receta)
                        .usuario(usuarioRepository.getReferenceById(idUsuario))
                        .build());
        calificacion.setPuntuacion(puntuacion);
        calificacion.setFecha(LocalDateTime.now());
        calificacionRepository.save(calificacion);
    }

    @Transactional
    public void retirarCalificacion(Integer idReceta, Integer idUsuario) {
        calificacionRepository.findByReceta_IdRecetaAndUsuario_IdUsuario(idReceta, idUsuario)
                .ifPresent(calificacionRepository::delete);
    }

    // ------------------------------------------------------------ auxiliares

    private RecetaBd obtener(Integer idReceta) {
        return recetaRepository.findById(idReceta)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una receta con id " + idReceta));
    }

    private RecetaBd obtenerDelAutor(Integer idReceta, Integer idUsuario) {
        RecetaBd receta = obtener(idReceta);
        if (!receta.getAutor().getIdUsuario().equals(idUsuario)) {
            throw new AccessDeniedException("Solo el autor puede modificar esta receta");
        }
        return receta;
    }

    private void validarIngredientes(RecetaDTO dto) {
        if (dto.getIngredientes() == null || dto.getIngredientes().isEmpty()) {
            throw new SolicitudInvalidaException("Agrega al menos un ingrediente");
        }
        if (dto.getIngredientes().stream().noneMatch(IngredienteDTO::isPrincipal)) {
            throw new SolicitudInvalidaException("Marca al menos un ingrediente como principal");
        }
    }

    private void copiarDatos(RecetaDTO dto, RecetaBd receta) {
        receta.setNombre(dto.getNombre().trim());
        receta.setDescripcion(dto.getDescripcion().trim());
        receta.setPasos(dto.getPasos().trim());
        receta.setTiempoMinutos(dto.getTiempoMinutos());
        // orphanRemoval elimina los ingredientes anteriores al vaciar la coleccion.
        receta.getIngredientes().clear();
        for (IngredienteDTO i : dto.getIngredientes()) {
            receta.getIngredientes().add(RecetaIngredienteBd.builder()
                    .receta(receta)
                    .nombre(i.getNombre().trim())
                    .cantidad(i.getCantidad())
                    .unidad(i.getUnidad())
                    .principal(i.isPrincipal())
                    .build());
        }
    }

    // idReceta -> [promedio, total]
    private Map<Integer, double[]> resumenDe(List<RecetaBd> recetas) {
        Map<Integer, double[]> resumen = new HashMap<>();
        if (recetas.isEmpty()) {
            return resumen;
        }
        List<Integer> ids = recetas.stream().map(RecetaBd::getIdReceta).toList();
        for (Object[] fila : calificacionRepository.resumenPorRecetas(ids)) {
            resumen.put((Integer) fila[0],
                    new double[]{((Number) fila[1]).doubleValue(), ((Number) fila[2]).doubleValue()});
        }
        return resumen;
    }

    private RecetaVistaDTO aVista(RecetaBd r, Map<Integer, double[]> resumen, Integer idUsuarioActual) {
        double[] datos = resumen.get(r.getIdReceta());
        Double promedio = datos == null ? null : Math.round(datos[0] * 10) / 10.0;
        long total = datos == null ? 0 : (long) datos[1];
        return new RecetaVistaDTO(
                r.getIdReceta(),
                r.getNombre(),
                r.getAutor().getNombre(),
                r.getDescripcion(),
                r.getTiempoMinutos(),
                r.getEstado().name(),
                promedio,
                total,
                r.getAutor().getIdUsuario().equals(idUsuarioActual));
    }
}
