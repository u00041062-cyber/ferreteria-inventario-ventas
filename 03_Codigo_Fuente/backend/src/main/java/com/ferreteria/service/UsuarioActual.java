package com.ferreteria.service;

import com.ferreteria.entity.Usuario;
import com.ferreteria.exception.ApiException;
import com.ferreteria.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Obtiene el usuario autenticado de la solicitud en curso. */
@Component
public class UsuarioActual {
    private final UsuarioRepository usuarios;
    public UsuarioActual(UsuarioRepository usuarios) { this.usuarios = usuarios; }

    public Usuario obtener() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getName() == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "No autenticado");
        return usuarios.findByUsuario(a.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));
    }
}
