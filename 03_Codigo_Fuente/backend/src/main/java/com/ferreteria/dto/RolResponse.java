package com.ferreteria.dto;
import com.ferreteria.entity.Rol;
public record RolResponse(Integer id, String nombre) {
    public static RolResponse de(Rol r) { return new RolResponse(r.getId(), r.getNombre()); }
}
