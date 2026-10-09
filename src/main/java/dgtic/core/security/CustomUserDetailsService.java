package dgtic.core.security;

import dgtic.core.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Recupera usuarios y roles desde la base de datos. Lo usan las dos cadenas de
// seguridad: el formulario web (DaoAuthenticationProvider) y el filtro JWT.
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        String normalizado = correo == null ? "" : correo.trim().toLowerCase();
        return usuarioRepository.findByCorreoAndActivoTrue(normalizado)
                .map(UsuarioPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Correo o contraseña incorrectos"));
    }
}
