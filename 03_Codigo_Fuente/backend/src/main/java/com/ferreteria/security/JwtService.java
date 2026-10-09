package com.ferreteria.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long minutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = minutes * 60_000L;
    }

    public String generar(String usuario, String rol) {
        long ahora = System.currentTimeMillis();
        return Jwts.builder().subject(usuario).claim("rol", rol)
                .issuedAt(new Date(ahora)).expiration(new Date(ahora + expirationMs))
                .signWith(key).compact();
    }

    /** Devuelve el nombre de usuario si el token es válido y no ha expirado. */
    public Optional<String> usuarioDe(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.ofNullable(c.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
