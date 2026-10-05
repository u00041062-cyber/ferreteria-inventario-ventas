package com.ferreteria.controller;

import com.ferreteria.dto.LoginRequest;
import com.ferreteria.dto.LoginResponse;
import com.ferreteria.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r) { return auth.login(r); }
}
