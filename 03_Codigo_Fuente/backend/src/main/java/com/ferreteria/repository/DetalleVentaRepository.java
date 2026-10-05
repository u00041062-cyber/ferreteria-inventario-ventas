package com.ferreteria.repository;

import com.ferreteria.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {
    /** Unidades vendidas por producto: [idProducto, suma]. */
    @Query("select d.producto.id, sum(d.cantidad) from DetalleVenta d group by d.producto.id")
    List<Object[]> unidadesVendidasPorProducto();
}
