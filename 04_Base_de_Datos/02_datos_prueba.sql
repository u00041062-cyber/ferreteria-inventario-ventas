/* ============================================================================
   DATOS DE PRUEBA - FerreteriaLosOlivos
   Ejecutar DESPUÉS de 01_crear_base_datos.sql
   ============================================================================ */
USE FerreteriaLosOlivos;
GO

/* ---- Roles ---- */
INSERT INTO dbo.ROL (nombre) VALUES ('Administrador'), ('Vendedor');
GO

/* ---- Usuarios ----
   IMPORTANTE: la columna contrasena debe contener un hash BCrypt.
   Los valores 'REEMPLAZAR_HASH_BCRYPT_*' son marcadores: genera el hash real
   con Spring Security (BCryptPasswordEncoder) o con un generador BCrypt y
   actualízalo:
       UPDATE dbo.USUARIO SET contrasena = '<hash>' WHERE usuario = 'admin';
   Usuarios sugeridos para pruebas:  admin / vendedor1  (define tus claves). */
INSERT INTO dbo.USUARIO (nombre, usuario, contrasena, estado, id_rol) VALUES
('Dueño de la ferretería', 'admin',     'REEMPLAZAR_HASH_BCRYPT_ADMIN',    1, (SELECT id_rol FROM dbo.ROL WHERE nombre = 'Administrador')),
('Vendedor de mostrador',  'vendedor1', 'REEMPLAZAR_HASH_BCRYPT_VENDEDOR', 1, (SELECT id_rol FROM dbo.ROL WHERE nombre = 'Vendedor'));
GO

/* ---- Categorías ---- */
INSERT INTO dbo.CATEGORIA (nombre, descripcion) VALUES
('Herramientas',               'Herramientas manuales y eléctricas'),
('Materiales de construcción', 'Cemento, fierro, ladrillos y afines'),
('Acabados',                   'Pinturas, barnices y accesorios de acabado'),
('Electricidad',               'Cables, interruptores y accesorios eléctricos'),
('Gasfitería',                 'Tuberías, llaves y accesorios de agua'),
('Fijaciones',                 'Clavos, tornillos, pernos y tarugos');
GO

/* ---- Productos ---- */
INSERT INTO dbo.PRODUCTO (codigo, descripcion, precio, stock_actual, stock_minimo, ubicacion, id_categoria) VALUES
('HER-001', 'Martillo de acero 16 oz',            25.00,  30,  8, 'Estante A-1', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Herramientas')),
('HER-002', 'Alicate universal 8"',               18.50,  25,  6, 'Estante A-1', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Herramientas')),
('HER-003', 'Taladro percutor 650 W',            189.90,   8,  3, 'Estante A-2', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Herramientas')),
('HER-004', 'Flexómetro 5 m',                     12.00,  40, 10, 'Estante A-1', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Herramientas')),
('CON-001', 'Cemento Portland tipo I (bolsa 42.5 kg)', 28.50, 120, 40, 'Almacén B-1', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Materiales de construcción')),
('CON-002', 'Fierro corrugado 3/8" x 9 m',       32.00,  80, 25, 'Almacén B-2', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Materiales de construcción')),
('CON-003', 'Ladrillo King Kong 18 huecos (unidad)', 1.10, 1500, 500, 'Patio C-1', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Materiales de construcción')),
('ACA-001', 'Pintura látex blanco 1 galón',       55.00,  20,  6, 'Estante D-1', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Acabados')),
('ACA-002', 'Thinner acrílico 1 litro',           14.00,  35, 10, 'Estante D-2', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Acabados')),
('ACA-003', 'Brocha de cerda 3"',                  9.50,  50, 12, 'Estante D-1', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Acabados')),
('ELE-001', 'Cable THW 14 AWG (metro)',            1.80, 600, 150, 'Estante E-1', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Electricidad')),
('ELE-002', 'Interruptor simple empotrable',       6.50,  45, 12, 'Estante E-2', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Electricidad')),
('GAS-001', 'Tubo PVC agua 1/2" x 5 m',           11.00,  60, 15, 'Almacén F-1', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Gasfitería')),
('GAS-002', 'Llave de paso 1/2"',                 16.00,  28,  8, 'Estante F-2', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Gasfitería')),
('FIJ-001', 'Clavos de 3" (kg)',                   7.50,   4, 10, 'Estante G-1', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Fijaciones')),  -- bajo el mínimo: genera alerta
('FIJ-002', 'Tornillo autorroscante 1" (caja x100)', 8.00, 70, 20, 'Estante G-1', (SELECT id_categoria FROM dbo.CATEGORIA WHERE nombre='Fijaciones'));
GO

/* ---- Movimientos iniciales de inventario (ingreso de mercadería) ---- */
INSERT INTO dbo.MOVIMIENTO_INVENTARIO (tipo, cantidad, fecha, motivo, id_producto, id_usuario)
SELECT 'ENTRADA', p.stock_actual, DATEADD(DAY, -10, SYSDATETIME()), 'Inventario inicial',
       p.id_producto, (SELECT id_usuario FROM dbo.USUARIO WHERE usuario = 'admin')
FROM   dbo.PRODUCTO p;
GO

/* ---- Ventas de ejemplo (usan el procedimiento, así descuentan stock) ---- */
DECLARE @vendedor INT = (SELECT id_usuario FROM dbo.USUARIO WHERE usuario = 'vendedor1');
DECLARE @id INT, @d dbo.tipo_detalle_venta;

INSERT INTO @d VALUES
  ((SELECT id_producto FROM dbo.PRODUCTO WHERE codigo='HER-001'), 2),
  ((SELECT id_producto FROM dbo.PRODUCTO WHERE codigo='FIJ-002'), 3);
EXEC dbo.sp_registrar_venta @vendedor, @d, @id OUTPUT;

DELETE FROM @d;
INSERT INTO @d VALUES
  ((SELECT id_producto FROM dbo.PRODUCTO WHERE codigo='CON-001'), 10),
  ((SELECT id_producto FROM dbo.PRODUCTO WHERE codigo='CON-002'), 6);
EXEC dbo.sp_registrar_venta @vendedor, @d, @id OUTPUT;

DELETE FROM @d;
INSERT INTO @d VALUES
  ((SELECT id_producto FROM dbo.PRODUCTO WHERE codigo='ACA-001'), 2),
  ((SELECT id_producto FROM dbo.PRODUCTO WHERE codigo='ACA-003'), 2);
EXEC dbo.sp_registrar_venta @vendedor, @d, @id OUTPUT;
GO

/* ---- Verificación rápida ---- */
SELECT * FROM dbo.vw_stock_productos ORDER BY requiere_reposicion DESC, codigo;
SELECT * FROM dbo.vw_reporte_ventas  ORDER BY id_venta;
GO
