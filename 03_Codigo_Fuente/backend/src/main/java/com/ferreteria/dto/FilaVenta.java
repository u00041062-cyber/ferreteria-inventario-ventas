package com.ferreteria.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record FilaVenta(Integer idVenta, LocalDateTime fecha, String vendedor, String codigo, String producto,
                        int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}
