package dgtic.core.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

// Mide cuanto tarda cada peticion y deja en bitacora las que superan el umbral,
// para vigilar el requerimiento de tiempo de respuesta.
@Slf4j
public class TiempoRespuestaInterceptor implements HandlerInterceptor {

    private static final String ATRIBUTO_INICIO = TiempoRespuestaInterceptor.class.getName() + ".inicio";
    private static final long UMBRAL_MS = 2000;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATRIBUTO_INICIO, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Object inicio = request.getAttribute(ATRIBUTO_INICIO);
        if (inicio instanceof Long milis) {
            long duracion = System.currentTimeMillis() - milis;
            if (duracion > UMBRAL_MS) {
                log.warn("Petición lenta: {} {} tardó {} ms (estado {})",
                        request.getMethod(), request.getRequestURI(), duracion, response.getStatus());
            }
        }
    }
}
