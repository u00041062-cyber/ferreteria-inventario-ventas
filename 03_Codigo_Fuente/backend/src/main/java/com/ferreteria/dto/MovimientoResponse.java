package com.ferreteria.dto;
import com.ferreteria.entity.MovimientoInventario;
import java.time.LocalDateTime;
public record MovimientoResponse(Integer id, String tipo, int cantidad, LocalDateTime fecha, String motivo,
                                 Integer idProducto, String producto, String usuario) {
    public static MovimientoResponse de(MovimientoInventario m) {
        return new MovimientoResponse(m.getId(), m.getTipo(), m.getCantidad(), m.getFecha(), m.getMotivo(),
                m.getProducto().getId(), m.getProducto().getDescripcion(), m.getUsuario().getUsuario());
    }
}
