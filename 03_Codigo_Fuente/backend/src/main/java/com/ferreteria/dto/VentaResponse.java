package com.ferreteria.dto;
import com.ferreteria.entity.Venta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record VentaResponse(Integer id, LocalDateTime fecha, String vendedor, BigDecimal total, List<Detalle> detalles) {
    public record Detalle(Integer idProducto, String codigo, String producto, int cantidad,
                          BigDecimal precioUnitario, BigDecimal subtotal) {}
    public static VentaResponse de(Venta v) {
        List<Detalle> ds = v.getDetalles().stream().map(d -> new Detalle(d.getProducto().getId(),
                d.getProducto().getCodigo(), d.getProducto().getDescripcion(), d.getCantidad(),
                d.getPrecioUnitario(), d.getSubtotal())).toList();
        return new VentaResponse(v.getId(), v.getFecha(), v.getUsuario().getUsuario(), v.getTotal(), ds);
    }
}
