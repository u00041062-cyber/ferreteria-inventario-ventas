package com.ferreteria.service;

import com.ferreteria.dto.ProductoRequest;
import com.ferreteria.dto.ProductoResponse;
import com.ferreteria.entity.MovimientoInventario;
import com.ferreteria.entity.Producto;
import com.ferreteria.exception.ApiException;
import com.ferreteria.repository.CategoriaRepository;
import com.ferreteria.repository.MovimientoRepository;
import com.ferreteria.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class ProductoService {
    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final MovimientoRepository movimientos;
    private final UsuarioActual actual;

    public ProductoService(ProductoRepository productos, CategoriaRepository categorias,
                           MovimientoRepository movimientos, UsuarioActual actual) {
        this.productos = productos; this.categorias = categorias; this.movimientos = movimientos; this.actual = actual;
    }

    /** Consulta de stock: por código o descripción, opcionalmente por categoría. */
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscar(String q, Integer idCategoria, boolean soloActivos) {
        String patron = "%" + (q == null ? "" : q.trim().toLowerCase(Locale.ROOT)) + "%";
        return productos.buscar(patron, soloActivos).stream()
                .filter(p -> idCategoria == null || p.getCategoria().getId().equals(idCategoria))
                .map(ProductoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> alertas() {
        return productos.alertasStockMinimo().stream().map(ProductoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Integer id) {
        return ProductoResponse.de(productos.findById(id).orElseThrow(() -> ApiException.notFound("Producto no encontrado")));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest r) {
        productos.findByCodigoIgnoreCase(r.codigo().trim()).ifPresent(p -> {
            throw ApiException.conflict("Ya existe un producto con el código " + r.codigo().trim());
        });
        Producto p = new Producto();
        aplicar(p, r);
        int inicial = r.stockActual() == null ? 0 : r.stockActual();
        p.setStockActual(inicial);
        p = productos.save(p);
        if (inicial > 0) {   // deja historial del stock inicial
            MovimientoInventario m = new MovimientoInventario();
            m.setTipo(MovimientoInventario.ENTRADA); m.setCantidad(inicial); m.setFecha(LocalDateTime.now());
            m.setMotivo("Stock inicial"); m.setProducto(p); m.setUsuario(actual.obtener());
            movimientos.save(m);
        }
        return ProductoResponse.de(p);
    }

    @Transactional
    public ProductoResponse actualizar(Integer id, ProductoRequest r) {
        Producto p = productos.findById(id).orElseThrow(() -> ApiException.notFound("Producto no encontrado"));
        productos.findByCodigoIgnoreCase(r.codigo().trim()).filter(x -> !x.getId().equals(id)).ifPresent(x -> {
            throw ApiException.conflict("Ya existe un producto con el código " + r.codigo().trim());
        });
        aplicar(p, r);   // el stock NO se modifica aquí: solo mediante movimientos o ventas
        return ProductoResponse.de(productos.save(p));
    }

    /** Baja lógica: se conserva el historial de ventas y movimientos. */
    @Transactional
    public void darDeBaja(Integer id) {
        Producto p = productos.findById(id).orElseThrow(() -> ApiException.notFound("Producto no encontrado"));
        p.setActivo(false);
        productos.save(p);
    }

    private void aplicar(Producto p, ProductoRequest r) {
        p.setCodigo(r.codigo().trim());
        p.setDescripcion(r.descripcion().trim());
        p.setPrecio(r.precio());
        p.setStockMinimo(r.stockMinimo());
        p.setUbicacion(r.ubicacion());
        p.setCategoria(categorias.findById(r.idCategoria()).orElseThrow(() -> ApiException.badRequest("Categoría no válida")));
    }
}
