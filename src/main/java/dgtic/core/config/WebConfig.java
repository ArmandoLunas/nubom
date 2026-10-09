package dgtic.core.config;

import dgtic.core.interceptor.TiempoRespuestaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// El control de acceso ya no se hace con un interceptor: lo resuelve Spring
// Security (ver SecurityConfig). Aqui solo queda el interceptor de medicion.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TiempoRespuestaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/favicon.ico");
    }
}
