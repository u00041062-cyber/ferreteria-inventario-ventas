package com.ferreteria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity @Table(name = "MOVIMIENTO_INVENTARIO")
@Getter @Setter @NoArgsConstructor
public class MovimientoInventario {
    public static final String ENTRADA = "ENTRADA";
    public static final String SALIDA = "SALIDA";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer id;

    @Column(name = "tipo", nullable = false, length = 7)
    private String tipo;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "motivo", nullable = false, length = 100)
    private String motivo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}
