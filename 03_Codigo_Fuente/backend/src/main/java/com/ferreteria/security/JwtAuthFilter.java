package com.ferreteria.security;

import com.ferreteria.entity.Usuario;
import com.ferreteria.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UsuarioRepository usuarios;

    public JwtAuthFilter(JwtService jwt, UsuarioRepository usuarios) { this.jwt = jwt; this.usuarios = usuarios; }

    /** "Administrador" -> ROLE_ADMINISTRADOR ; "Vendedor" -> ROLE_VENDEDOR */
    public static String autoridad(String rol) { return "ROLE_" + rol.toUpperCase(); }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            jwt.usuarioDe(h.substring(7)).flatMap(usuarios::findByUsuario).filter(Usuario::isEstado).ifPresent(u -> {
                var auth = new UsuarioAuth(u.getUsuario(), List.of(new SimpleGrantedAuthority(autoridad(u.getRol().getNombre()))));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        chain.doFilter(req, res);
    }

    /** Autenticación sencilla ya validada (token firmado + usuario activo en BD). */
    static class UsuarioAuth extends UsernamePasswordAuthenticationToken {
        UsuarioAuth(String usuario, List<SimpleGrantedAuthority> roles) { super(usuario, null, roles); }
    }
}
