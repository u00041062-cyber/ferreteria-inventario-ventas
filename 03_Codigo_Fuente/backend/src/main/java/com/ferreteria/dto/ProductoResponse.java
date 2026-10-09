package com.ferreteria.dto;
import com.ferreteria.entity.Producto;
import java.math.BigDecimal;
public record ProductoResponse(Integer id, String codigo, String descripcion, BigDecimal precio, int stockActual,
                               int stockMinimo, String ubicacion, boolean activo, Integer idCategoria,
                               String categoria, boolean requiereReposicion) {
    public static ProductoResponse de(Producto p) {
        return new ProductoResponse(p.getId(), p.getCodigo(), p.getDescripcion(), p.getPrecio(), p.getStockActual(),
                p.getStockMinimo(), p.getUbicacion(), p.isActivo(), p.getCategoria().getId(),
                p.getCategoria().getNombre(), p.requiereReposicion());
    }
}
