package dgtic.core.service;

import dgtic.core.model.dto.HogarDTO;
import dgtic.core.model.dto.HogarResumenDTO;
import dgtic.core.model.dto.MiembroHogarDTO;

import java.util.List;
import java.util.Optional;

public interface HogarService {

    // Vacio si el usuario todavia no pertenece a ningun hogar
    Optional<HogarResumenDTO> obtenerResumenDeUsuario(Integer idUsuario);

    List<MiembroHogarDTO> obtenerMiembros(Integer idHogar);

    HogarResumenDTO crearHogar(Integer idUsuario, HogarDTO dto);

    // Lanza IllegalStateException si el usuario no es propietario del hogar
    void renombrarHogar(Integer idHogar, Integer idUsuarioSolicitante, HogarDTO dto);

    // Lanza IllegalStateException si el usuario no es propietario del hogar.
    // Elimina en cascada sus inventarios y productos.
    void eliminarHogar(Integer idHogar, Integer idUsuarioSolicitante);

    // Incorpora como FAMILIAR a un usuario ya registrado, identificado por su correo.
    void agregarMiembro(Integer idHogar, Integer idUsuarioSolicitante, String correo);

    // Retira a un integrante del hogar (el propietario no puede retirarse a si mismo).
    void quitarMiembro(Integer idHogar, Integer idUsuarioSolicitante, Integer idUsuarioMiembro);
}
