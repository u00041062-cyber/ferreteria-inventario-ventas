package com.ferreteria.dto;
import com.ferreteria.entity.Categoria;
public record CategoriaResponse(Integer id, String nombre, String descripcion) {
    public static CategoriaResponse de(Categoria c) { return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion()); }
}
