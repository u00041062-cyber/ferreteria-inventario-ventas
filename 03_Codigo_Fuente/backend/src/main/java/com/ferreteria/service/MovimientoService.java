package com.ferreteria.service;

import com.ferreteria.dto.MovimientoRequest;
import com.ferreteria.dto.MovimientoResponse;
import com.ferreteria.entity.MovimientoInventario;
import com.ferreteria.entity.Producto;
import com.ferreteria.exception.ApiException;
import com.ferreteria.repository.MovimientoRepository;
import com.ferreteria.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimientoService {
    private final MovimientoRepository movimientos;
    private final ProductoRepository productos;
    private final UsuarioActual actual;

    public MovimientoService(MovimientoRepository movimientos, ProductoRepository productos, UsuarioActual actual) {
        this.movimientos = movimientos; this.productos = productos; this.actual = actual;
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> recientes() {
        return movimientos.findTop200ByOrderByFechaDescIdDesc().stream().map(MovimientoResponse::de).toList();
    }

    /** Entrada: suma stock. Salida: resta stock y se rechaza si supera lo disponible. */
    @Transactional
    public MovimientoResponse registrar(MovimientoRequest r) {
        Producto p = productos.bloquearPorIds(List.of(r.idProducto())).stream().findFirst()
                .orElseThrow(() -> ApiException.notFound("Producto no encontrado"));
        if (!p.isActivo()) throw ApiException.badRequest("El producto está dado de baja");

        if (MovimientoInventario.ENTRADA.equals(r.tipo())) {
            p.setStockActual(p.getStockActual() + r.cantidad());
        } else {
            if (r.cantidad() > p.getStockActual())
                throw ApiException.conflict("Stock insuficiente: disponible " + p.getStockActual() + " unidades");
            p.setStockActual(p.getStockActual() - r.cantidad());
        }
        MovimientoInventario m = new MovimientoInventario();
        m.setTipo(r.tipo()); m.setCantidad(r.cantidad()); m.setFecha(LocalDateTime.now());
        m.setMotivo(r.motivo().trim()); m.setProducto(p); m.setUsuario(actual.obtener());
        productos.save(p);
        return MovimientoResponse.de(movimientos.save(m));
    }
}
