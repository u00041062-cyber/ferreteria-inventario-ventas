package com.ferreteria.service;

import com.ferreteria.dto.FilaInventario;
import com.ferreteria.dto.FilaVenta;
import com.ferreteria.dto.VentaResponse;
import com.ferreteria.entity.Producto;
import com.ferreteria.entity.Venta;
import com.ferreteria.exception.ApiException;
import com.ferreteria.repository.DetalleVentaRepository;
import com.ferreteria.repository.ProductoRepository;
import com.ferreteria.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReporteService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VentaRepository ventas;
    private final ProductoRepository productos;
    private final DetalleVentaRepository detalles;

    public ReporteService(VentaRepository ventas, ProductoRepository productos, DetalleVentaRepository detalles) {
        this.ventas = ventas; this.productos = productos; this.detalles = detalles;
    }

    // ---------------- Ventas ----------------
    @Transactional(readOnly = true)
    public List<FilaVenta> ventas(LocalDate desde, LocalDate hasta, Integer idProducto, Integer idVendedor) {
        if (desde == null || hasta == null) throw ApiException.badRequest("Indique el periodo (desde y hasta)");
        if (hasta.isBefore(desde)) throw ApiException.badRequest("La fecha 'hasta' no puede ser anterior a 'desde'");
        List<FilaVenta> filas = new ArrayList<>();
        for (Venta v : ventas.findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaAscIdAsc(
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay())) {
            if (idVendedor != null && !v.getUsuario().getId().equals(idVendedor)) continue;
            v.getDetalles().stream()
                    .filter(d -> idProducto == null || d.getProducto().getId().equals(idProducto))
                    .forEach(d -> filas.add(new FilaVenta(v.getId(), v.getFecha(), v.getUsuario().getUsuario(),
                            d.getProducto().getCodigo(), d.getProducto().getDescripcion(), d.getCantidad(),
                            d.getPrecioUnitario(), d.getSubtotal())));
        }
        return filas;
    }

    public byte[] ventasPdf(List<FilaVenta> f, LocalDate desde, LocalDate hasta) {
        BigDecimal total = f.stream().map(FilaVenta::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return ExportUtil.pdf("Reporte de ventas del " + desde + " al " + hasta, ENC_VENTAS, filasVenta(f),
                "TOTAL VENDIDO: S/ " + total.setScale(2, RoundingMode.HALF_UP));
    }

    public byte[] ventasXlsx(List<FilaVenta> f) { return ExportUtil.xlsx("Ventas", ENC_VENTAS, filasVenta(f)); }

    private static final List<String> ENC_VENTAS =
            List.of("N° venta", "Fecha", "Vendedor", "Código", "Producto", "Cantidad", "P. unitario", "Subtotal");

    private static List<List<String>> filasVenta(List<FilaVenta> f) {
        return f.stream().map(x -> List.of(String.valueOf(x.idVenta()), x.fecha().format(FMT), x.vendedor(), x.codigo(),
                x.producto(), String.valueOf(x.cantidad()), x.precioUnitario().toPlainString(), x.subtotal().toPlainString())).toList();
    }

    // ---------------- Inventario ----------------
    @Transactional(readOnly = true)
    public List<FilaInventario> inventario() {
        Map<Integer, Long> vendidas = new HashMap<>();
        for (Object[] o : detalles.unidadesVendidasPorProducto()) vendidas.put((Integer) o[0], ((Number) o[1]).longValue());
        List<FilaInventario> filas = new ArrayList<>();
        for (Producto p : productos.buscar("%", true))
            filas.add(new FilaInventario(p.getCodigo(), p.getDescripcion(), p.getCategoria().getNombre(), p.getPrecio(),
                    p.getStockActual(), p.getStockMinimo(), p.getUbicacion(), p.requiereReposicion(),
                    vendidas.getOrDefault(p.getId(), 0L)));
        filas.sort(Comparator.comparingLong(FilaInventario::unidadesVendidas).reversed());   // mayor rotación primero
        return filas;
    }

    private static final List<String> ENC_INV =
            List.of("Código", "Descripción", "Categoría", "Precio", "Stock", "Mínimo", "Ubicación", "Reposición", "Unid. vendidas");

    private static List<List<String>> filasInv(List<FilaInventario> f) {
        return f.stream().map(x -> List.of(x.codigo(), x.descripcion(), x.categoria(), x.precio().toPlainString(),
                String.valueOf(x.stockActual()), String.valueOf(x.stockMinimo()), x.ubicacion() == null ? "" : x.ubicacion(),
                x.requiereReposicion() ? "REPONER" : "OK", String.valueOf(x.unidadesVendidas()))).toList();
    }

    public byte[] inventarioPdf(List<FilaInventario> f) {
        long alertas = f.stream().filter(FilaInventario::requiereReposicion).count();
        return ExportUtil.pdf("Reporte de inventario (ordenado por rotación)", ENC_INV, filasInv(f),
                "Productos por reponer: " + alertas);
    }

    public byte[] inventarioXlsx(List<FilaInventario> f) { return ExportUtil.xlsx("Inventario", ENC_INV, filasInv(f)); }

    // ---------------- Comprobante ----------------
    public byte[] comprobantePdf(VentaResponse v) {
        List<List<String>> filas = v.detalles().stream().map(d -> List.of(d.codigo(), d.producto(),
                String.valueOf(d.cantidad()), d.precioUnitario().toPlainString(), d.subtotal().toPlainString())).toList();
        return ExportUtil.pdf("Comprobante de venta N° " + v.id() + " – " + v.fecha().format(FMT) + " – Vendedor: " + v.vendedor(),
                List.of("Código", "Producto", "Cantidad", "P. unitario", "Subtotal"), filas,
                "TOTAL: S/ " + v.total().setScale(2, RoundingMode.HALF_UP));
    }
}
