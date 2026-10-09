package com.ferreteria.repository;

import com.ferreteria.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<MovimientoInventario, Integer> {
    List<MovimientoInventario> findTop200ByOrderByFechaDescIdDesc();
}
