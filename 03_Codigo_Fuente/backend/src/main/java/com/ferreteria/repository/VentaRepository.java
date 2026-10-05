package com.ferreteria.repository;

import com.ferreteria.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    List<Venta> findTop100ByOrderByFechaDescIdDesc();
    List<Venta> findByFechaGreaterThanEqualAndFechaLessThanOrderByFechaAscIdAsc(LocalDateTime desde, LocalDateTime hasta);
}
