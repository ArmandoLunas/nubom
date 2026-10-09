package dgtic.core.security;

import dgtic.core.security.jwt.JwtAccessDeniedHandler;
import dgtic.core.security.jwt.JwtAuthenticationEntryPoint;
import dgtic.core.security.jwt.JwtAuthenticationFilter;
import dgtic.core.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// Dos cadenas de filtros. Spring Security aplica la primera cuyo securityMatcher
// coincide con la ruta:
//   1) /api/**  -> sin estado, autenticacion con JWT, sin CSRF.
//   2) el resto -> aplicacion web Thymeleaf: formulario, sesion y CSRF.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain apiChain(HttpSecurity http,
                                 JwtService jwtService,
                                 CustomUserDetailsService userDetailsService,
                                 JwtAuthenticationEntryPoint entryPoint,
                                 JwtAccessDeniedHandler accessDeniedHandler) throws Exception {
        // El filtro se crea aqui (no es un @Component) para que Spring Boot no lo
        // registre tambien como filtro de servlet de toda la aplicacion.
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        return http.securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Catalogos: cualquiera autenticado los lee, solo ADMIN los modifica.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/tipos-inventario/**", "/api/v1/roles/**").authenticated()
                        .requestMatchers("/api/v1/tipos-inventario/**", "/api/v1/roles/**",
                                "/api/v1/recetas/moderacion/**").hasRole("ADMIN")
                        // El resto se afina por recurso con @PreAuthorize en los controladores.
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/", "/login", "/registro", "/acceso-denegado", "/error",
                                "/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(f -> f
                        .loginPage("/login")
                        .usernameParameter("correo")
                        .passwordParameter("contrasena")
                        .defaultSuccessUrl("/inicio", true)
                        .failureUrl("/login?error"))
                .logout(l -> l
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .sessionManagement(s -> s
                        .sessionFixation(f -> f.migrateSession())
                        .maximumSessions(1))
                .exceptionHandling(e -> e.accessDeniedPage("/acceso-denegado"))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Lo usa AuthRestController para validar credenciales en /api/v1/auth/login.
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Necesario para que maximumSessions(1) se entere de las sesiones que expiran.
    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // CORS solo para la API: la aplicacion web se sirve desde el mismo origen.
    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${nubom.cors.allowed-origins}") List<String> origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenes);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
