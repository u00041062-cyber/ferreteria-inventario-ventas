package com.ferreteria;

import com.ferreteria.entity.Categoria;
import com.ferreteria.entity.Producto;
import com.ferreteria.repository.CategoriaRepository;
import com.ferreteria.repository.ProductoRepository;
import com.ferreteria.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** CP11 – consulta de stock con un catálogo amplio: respuesta en menos de 3 segundos. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RendimientoTest {
    @Autowired ProductoService service;
    @Autowired ProductoRepository productos;
    @Autowired CategoriaRepository categorias;

    @Test
    void cp11_consultaStockCatalogoAmplio() {
        Categoria c = new Categoria(); c.setNombre("Carga masiva");
        c = categorias.save(c);
        List<Producto> lote = new ArrayList<>();
        for (int i = 0; i < 5000; i++) {
            Producto p = new Producto();
            p.setCodigo("PRF-" + i); p.setDescripcion("Producto de prueba " + i);
            p.setPrecio(new BigDecimal("10.00")); p.setStockActual(100); p.setStockMinimo(10); p.setCategoria(c);
            lote.add(p);
        }
        productos.saveAll(lote);
        productos.flush();

        long ini = System.nanoTime();
        var resultado = service.buscar("prueba 49", null, true);
        long ms = (System.nanoTime() - ini) / 1_000_000;

        assertThat(resultado).isNotEmpty();
        assertThat(ms).as("tiempo de consulta en ms").isLessThan(3000);
        System.out.println("CP11: consulta sobre 5000 productos en " + ms + " ms");
    }
}
