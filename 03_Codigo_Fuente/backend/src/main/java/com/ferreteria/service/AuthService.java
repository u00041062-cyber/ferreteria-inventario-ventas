package com.ferreteria.service;

import com.ferreteria.dto.LoginRequest;
import com.ferreteria.dto.LoginResponse;
import com.ferreteria.entity.Usuario;
import com.ferreteria.exception.ApiException;
import com.ferreteria.repository.UsuarioRepository;
import com.ferreteria.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios; this.encoder = encoder; this.jwt = jwt;
    }

    public LoginResponse login(LoginRequest r) {
        Usuario u = usuarios.findByUsuario(r.usuario().trim())
                .filter(Usuario::isEstado)
                .filter(x -> encoder.matches(r.contrasena(), x.getContrasena()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));
        return new LoginResponse(jwt.generar(u.getUsuario(), u.getRol().getNombre()),
                u.getUsuario(), u.getNombre(), u.getRol().getNombre());
    }
}
