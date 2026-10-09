package com.ferreteria.controller;

import com.ferreteria.dto.MovimientoRequest;
import com.ferreteria.dto.MovimientoResponse;
import com.ferreteria.service.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {
    private final MovimientoService service;
    public MovimientoController(MovimientoService service) { this.service = service; }

    @GetMapping public List<MovimientoResponse> recientes() { return service.recientes(); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse registrar(@Valid @RequestBody MovimientoRequest r) { return service.registrar(r); }
}
