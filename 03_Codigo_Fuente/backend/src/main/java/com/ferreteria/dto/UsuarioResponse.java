package com.ferreteria.dto;
import com.ferreteria.entity.Usuario;
public record UsuarioResponse(Integer id, String nombre, String usuario, boolean estado, Integer idRol, String rol) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getUsuario(), u.isEstado(), u.getRol().getId(), u.getRol().getNombre());
    }
}
