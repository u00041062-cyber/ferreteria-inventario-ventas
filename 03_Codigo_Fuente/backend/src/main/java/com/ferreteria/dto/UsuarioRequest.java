package com.ferreteria.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public record UsuarioRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotBlank @Size(max = 30) String usuario,
        @Size(min = 6, max = 60, message = "debe tener entre 6 y 60 caracteres") String contrasena,
        @NotNull Integer idRol,
        Boolean estado) {}
