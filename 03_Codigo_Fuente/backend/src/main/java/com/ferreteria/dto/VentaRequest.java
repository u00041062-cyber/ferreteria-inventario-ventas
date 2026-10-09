package com.ferreteria.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record VentaRequest(@NotEmpty(message = "la venta debe tener al menos un producto") @Valid List<Item> items) {
    public record Item(@NotNull Integer idProducto, @NotNull @Min(1) Integer cantidad) {}
}
