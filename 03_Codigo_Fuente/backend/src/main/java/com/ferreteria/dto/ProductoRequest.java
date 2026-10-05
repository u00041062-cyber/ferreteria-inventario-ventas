package com.ferreteria.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ProductoRequest(
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank @Size(max = 150) String descripcion,
        @NotNull @DecimalMin(value = "0.01", message = "debe ser mayor que 0") @Digits(integer = 8, fraction = 2, message = "máximo 2 decimales") BigDecimal precio,
        @Min(0) Integer stockActual,            // solo se usa al crear; luego el stock cambia con movimientos
        @NotNull @Min(0) Integer stockMinimo,
        @Size(max = 50) String ubicacion,
        @NotNull Integer idCategoria) {}
