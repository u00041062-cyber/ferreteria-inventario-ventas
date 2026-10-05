package com.ferreteria.controller;

import com.ferreteria.dto.ProductoRequest;
import com.ferreteria.dto.ProductoResponse;
import com.ferreteria.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService service;
    public ProductoController(ProductoService service) { this.service = service; }

    /** Consulta de stock en tiempo real (vendedor y administrador). */
    @GetMapping
    public List<ProductoResponse> buscar(@RequestParam(required = false) String q,
                                         @RequestParam(required = false) Integer idCategoria,
                                         @RequestParam(defaultValue = "true") boolean soloActivos) {
        return service.buscar(q, idCategoria, soloActivos);
    }

    @GetMapping("/alertas")
    public List<ProductoResponse> alertas() { return service.alertas(); }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Integer id) { return service.obtener(id); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest r) { return service.crear(r); }

    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ProductoRequest r) { return service.actualizar(id, r); }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void baja(@PathVariable Integer id) { service.darDeBaja(id); }
}
