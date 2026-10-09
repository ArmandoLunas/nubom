package dgtic.core.security.jwt;

import dgtic.core.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

// Filtro propio de la cadena de la API. Si la peticion trae un JWT valido en
// "Authorization: Bearer ...", deja al usuario autenticado en el SecurityContext.
// Si no hay token o no es valido, no hace nada: la regla de autorizacion decide
// y, si la ruta lo exige, JwtAuthenticationEntryPoint responde 401.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Optional<String> correo = extraerToken(request).flatMap(jwtService::extraerCorreo);

        if (correo.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Se recarga el usuario para respetar bajas y cambios de rol posteriores a la emision.
                UserDetails usuario = userDetailsService.loadUserByUsername(correo.get());
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (UsernameNotFoundException ex) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    private Optional<String> extraerToken(HttpServletRequest request) {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            return Optional.of(cabecera.substring(PREFIJO.length()).trim());
        }
        return Optional.empty();
    }
}
