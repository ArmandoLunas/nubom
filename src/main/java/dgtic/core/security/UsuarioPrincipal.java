package dgtic.core.security;

import dgtic.core.model.entity.RolSistema;
import dgtic.core.model.entity.UsuarioBd;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

// Usuario autenticado tal como lo ve Spring Security. Ademas de lo que pide
// UserDetails guarda el id y el nombre, para que controladores y vistas no
// tengan que volver a consultar la base de datos en cada peticion.
public class UsuarioPrincipal implements UserDetails, Serializable {

    private final Integer idUsuario;
    private String nombre;
    private final String correo;
    private final String contrasena;
    private final RolSistema rolSistema;
    private final boolean activo;

    public UsuarioPrincipal(UsuarioBd usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.nombre = usuario.getNombre();
        this.correo = usuario.getCorreo();
        this.contrasena = usuario.getContrasena();
        this.rolSistema = usuario.getRolSistema() != null ? usuario.getRolSistema() : RolSistema.USUARIO;
        this.activo = Boolean.TRUE.equals(usuario.getActivo());
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    // Se actualiza cuando el usuario edita su perfil, para que el menu muestre el nombre nuevo.
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public RolSistema getRolSistema() {
        return rolSistema;
    }

    public boolean esAdmin() {
        return rolSistema == RolSistema.ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rolSistema.name()));
    }

    @Override
    public String getPassword() {
        return contrasena;
    }

    @Override
    public String getUsername() {
        return correo;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    // equals/hashCode por id: el control de sesiones concurrentes compara principales.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioPrincipal otro)) return false;
        return Objects.equals(idUsuario, otro.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario);
    }
}
