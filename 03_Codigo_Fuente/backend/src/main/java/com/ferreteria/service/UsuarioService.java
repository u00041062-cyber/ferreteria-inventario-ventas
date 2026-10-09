package com.ferreteria.service;

import com.ferreteria.dto.UsuarioRequest;
import com.ferreteria.dto.UsuarioResponse;
import com.ferreteria.entity.Usuario;
import com.ferreteria.exception.ApiException;
import com.ferreteria.repository.RolRepository;
import com.ferreteria.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;
    private final UsuarioActual actual;

    public UsuarioService(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder, UsuarioActual actual) {
        this.usuarios = usuarios; this.roles = roles; this.encoder = encoder; this.actual = actual;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() { return usuarios.findAll().stream().map(UsuarioResponse::de).toList(); }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest r) {
        if (r.contrasena() == null || r.contrasena().isBlank()) throw ApiException.badRequest("La contraseña es obligatoria");
        if (usuarios.existsByUsuario(r.usuario().trim())) throw ApiException.conflict("El nombre de usuario ya existe");
        Usuario u = new Usuario();
        aplicar(u, r);
        u.setContrasena(encoder.encode(r.contrasena()));
        return UsuarioResponse.de(usuarios.save(u));
    }

    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioRequest r) {
        Usuario u = usuarios.findById(id).orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));
        usuarios.findByUsuario(r.usuario().trim()).filter(x -> !x.getId().equals(id))
                .ifPresent(x -> { throw ApiException.conflict("El nombre de usuario ya existe"); });
        aplicar(u, r);
        if (r.contrasena() != null && !r.contrasena().isBlank()) u.setContrasena(encoder.encode(r.contrasena()));
        return UsuarioResponse.de(usuarios.save(u));
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Integer id, boolean estado) {
        Usuario u = usuarios.findById(id).orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));
        if (!estado && u.getId().equals(actual.obtener().getId()))
            throw ApiException.badRequest("No puedes desactivar tu propia cuenta");
        u.setEstado(estado);
        return UsuarioResponse.de(usuarios.save(u));
    }

    private void aplicar(Usuario u, UsuarioRequest r) {
        u.setNombre(r.nombre().trim());
        u.setUsuario(r.usuario().trim());
        u.setRol(roles.findById(r.idRol()).orElseThrow(() -> ApiException.badRequest("Rol no válido")));
        if (r.estado() != null) u.setEstado(r.estado());
    }
}
