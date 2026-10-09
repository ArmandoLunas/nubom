package dgtic.core.service;

import dgtic.core.model.dto.CambiarContrasenaDTO;
import dgtic.core.model.dto.PerfilUsuarioDTO;
import dgtic.core.model.dto.RegistroUsuarioDTO;
import dgtic.core.model.entity.UsuarioBd;

public interface UsuarioService {

    // Da de alta la cuenta con la contrasena cifrada (BCrypt) y rol USUARIO.
    UsuarioBd registrar(RegistroUsuarioDTO dto);

    UsuarioBd obtenerPorId(Integer idUsuario);

    void actualizarPerfil(Integer idUsuario, PerfilUsuarioDTO dto);

    void cambiarContrasena(Integer idUsuario, CambiarContrasenaDTO dto);

    boolean puedeEliminarCuenta(Integer idUsuario);

    void eliminarCuenta(Integer idUsuario);
}
