package com.ferreteria.config;

import com.ferreteria.entity.Usuario;
import com.ferreteria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * El script 02_datos_prueba.sql crea los usuarios con un marcador (REEMPLAZAR_HASH_BCRYPT_*).
 * Al iniciar la API se reemplaza por el hash BCrypt de la contraseña inicial configurada.
 */
@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {
    private static final String MARCADOR = "REEMPLAZAR_HASH_BCRYPT";
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String claveAdmin;
    private final String claveVendedor;

    public DataInitializer(UsuarioRepository usuarios, PasswordEncoder encoder,
                           @Value("${app.seed.admin-password}") String claveAdmin,
                           @Value("${app.seed.vendedor-password}") String claveVendedor) {
        this.usuarios = usuarios; this.encoder = encoder; this.claveAdmin = claveAdmin; this.claveVendedor = claveVendedor;
    }

    @Override
    public void run(String... args) {
        for (Usuario u : usuarios.findAll()) {
            if (u.getContrasena().startsWith(MARCADOR)) {
                String clave = "Administrador".equalsIgnoreCase(u.getRol().getNombre()) ? claveAdmin : claveVendedor;
                u.setContrasena(encoder.encode(clave));
                usuarios.save(u);
            }
        }
    }
}
