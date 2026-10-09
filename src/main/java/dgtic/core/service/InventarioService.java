package dgtic.core.service;

import dgtic.core.model.entity.InventarioBd;

import java.util.List;

public interface InventarioService {

    // Cada hogar tiene exactamente 2 inventarios (Refrigerador y Alacena),
    // creados automaticamente al crear el hogar. Ya no se pueden crear mas.
    void crearInventariosIniciales(Integer idHogar);

    List<InventarioBd> listarPorHogar(Integer idHogar);

    // Lanza IllegalArgumentException si el inventario no pertenece a ese hogar
    InventarioBd obtenerDeHogar(Integer idHogar, Integer idInventario);

    // Tamaños permitidos: 20, 40 u 80 objetos.
    // Lanza IllegalArgumentException si el inventario no es del hogar o el valor no es válido.
    // Lanza IllegalStateException si la nueva capacidad es menor a los productos ya guardados.
    InventarioBd actualizarCapacidad(Integer idHogar, Integer idInventario, Integer nuevaCapacidad);

    // Cambia el estilo visual (clasico, moderno o retro) con el que se dibuja el inventario.
    InventarioBd actualizarEstilo(Integer idHogar, Integer idInventario, String estilo);
}
