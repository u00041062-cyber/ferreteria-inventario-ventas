package com.ferreteria.dto;
import java.math.BigDecimal;
public record FilaInventario(String codigo, String descripcion, String categoria, BigDecimal precio, int stockActual,
                             int stockMinimo, String ubicacion, boolean requiereReposicion, long unidadesVendidas) {}
