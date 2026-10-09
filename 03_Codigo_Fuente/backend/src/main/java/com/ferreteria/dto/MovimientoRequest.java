package com.ferreteria.dto;
import jakarta.validation.constraints.*;
public record MovimientoRequest(
        @NotBlank @Pattern(regexp = "ENTRADA|SALIDA", message = "debe ser ENTRADA o SALIDA") String tipo,
        @NotNull @Min(1) Integer cantidad,
        @NotBlank @Size(max = 100) String motivo,
        @NotNull Integer idProducto) {}
