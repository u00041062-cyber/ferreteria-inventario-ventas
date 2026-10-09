package com.ferreteria.repository;

import com.ferreteria.entity.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    Optional<Producto> findByCodigoIgnoreCase(String codigo);

    boolean existsByCategoriaId(Integer idCategoria);

    /** Búsqueda por código o descripción. 'patron' ya viene en minúsculas y con comodines (%texto%). */
    @Query("select p from Producto p where (:soloActivos = false or p.activo = true) " +
           "and (lower(p.codigo) like :patron or lower(p.descripcion) like :patron) " +
           "order by p.descripcion")
    List<Producto> buscar(@Param("patron") String patron, @Param("soloActivos") boolean soloActivos);

    @Query("select p from Producto p where p.activo = true and p.stockActual <= p.stockMinimo " +
           "order by (p.stockActual - p.stockMinimo), p.descripcion")
    List<Producto> alertasStockMinimo();

    /** Bloquea las filas para evitar ventas simultáneas que dejen el stock negativo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id in :ids order by p.id")
    List<Producto> bloquearPorIds(@Param("ids") Collection<Integer> ids);
}
