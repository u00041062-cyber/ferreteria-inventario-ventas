package com.ferreteria.controller;

import com.ferreteria.dto.RolResponse;
import com.ferreteria.dto.UsuarioRequest;
import com.ferreteria.dto.UsuarioResponse;
import com.ferreteria.repository.RolRepository;
import com.ferreteria.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Solo administrador (ver SecurityConfig). */
@RestController
public class UsuarioController {
    private final UsuarioService service;
    private final RolRepository roles;

    public UsuarioController(UsuarioService service, RolRepository roles) { this.service = service; this.roles = roles; }

    @GetMapping("/api/roles")
    public List<RolResponse> roles() { return roles.findAll().stream().map(RolResponse::de).toList(); }

    @GetMapping("/api/usuarios")
    public List<UsuarioResponse> listar() { return service.listar(); }

    @PostMapping("/api/usuarios") @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest r) { return service.crear(r); }

    @PutMapping("/api/usuarios/{id}")
    public UsuarioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioRequest r) { return service.actualizar(id, r); }

    @PatchMapping("/api/usuarios/{id}/estado")
    public UsuarioResponse estado(@PathVariable Integer id, @RequestBody Map<String, Boolean> body) {
        return service.cambiarEstado(id, Boolean.TRUE.equals(body.get("estado")));
    }
}
