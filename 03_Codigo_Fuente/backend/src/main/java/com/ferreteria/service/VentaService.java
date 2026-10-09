package com.ferreteria.service;

import com.ferreteria.dto.VentaRequest;
import com.ferreteria.dto.VentaResponse;
import com.ferreteria.entity.DetalleVenta;
import com.ferreteria.entity.MovimientoInventario;
import com.ferreteria.entity.Producto;
import com.ferreteria.entity.Venta;
import com.ferreteria.exception.ApiException;
import com.ferreteria.repository.MovimientoRepository;
import com.ferreteria.repository.ProductoRepository;
import com.ferreteria.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VentaService {
    private final VentaRepository ventas;
    private final ProductoRepository productos;
    private final MovimientoRepository movimientos;
    private final UsuarioActual actual;

    public VentaService(VentaRepository ventas, ProductoRepository productos,
                        MovimientoRepository movimientos, UsuarioActual actual) {
        this.ventas = ventas; this.productos = productos; this.movimientos = movimientos; this.actual = actual;
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> recientes() {
        return ventas.findTop100ByOrderByFechaDescIdDesc().stream().map(VentaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public VentaResponse obtener(Integer id) {
        return VentaResponse.de(ventas.findById(id).orElseThrow(() -> ApiException.notFound("Venta no encontrada")));
    }

    /** Registra la venta, descuenta el stock y deja la salida en el historial, todo en una transacción. */
    @Transactional
    public VentaResponse registrar(VentaRequest r) {
        // Une líneas repetidas del mismo producto
        Map<Integer, Integer> pedido = new TreeMap<>();
        r.items().forEach(i -> pedido.merge(i.idProducto(), i.cantidad(), Integer::sum));

        List<Producto> lista = productos.bloquearPorIds(pedido.keySet());
        if (lista.size() != pedido.size()) throw ApiException.notFound("Alguno de los productos no existe");

        Venta v = new Venta();
        v.setFecha(LocalDateTime.now());
        v.setUsuario(actual.obtener());
        BigDecimal total = BigDecimal.ZERO;

        for (Producto p : lista) {
            int cant = pedido.get(p.getId());
            if (!p.isActivo()) throw ApiException.badRequest("El producto " + p.getCodigo() + " está dado de baja");
            if (cant > p.getStockActual())
                throw ApiException.conflict("Stock insuficiente de " + p.getDescripcion() + " (disponible " + p.getStockActual() + ")");
            DetalleVenta d = new DetalleVenta();
            d.setVenta(v); d.setProducto(p); d.setCantidad(cant); d.setPrecioUnitario(p.getPrecio());
            v.getDetalles().add(d);
            total = total.add(d.getSubtotal());
            p.setStockActual(p.getStockActual() - cant);
        }
        v.setTotal(total);
        v = ventas.save(v);

        for (DetalleVenta d : v.getDetalles()) {
            MovimientoInventario m = new MovimientoInventario();
            m.setTipo(MovimientoInventario.SALIDA); m.setCantidad(d.getCantidad()); m.setFecha(v.getFecha());
            m.setMotivo("Venta N° " + v.getId()); m.setProducto(d.getProducto()); m.setUsuario(v.getUsuario());
            movimientos.save(m);
        }
        return VentaResponse.de(v);
    }
}
