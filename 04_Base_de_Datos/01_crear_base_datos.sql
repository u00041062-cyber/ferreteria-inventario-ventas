/* ============================================================================
   PROYECTO  : Sistema web de inventario y ventas para una ferretería
   EMPRESA   : Ferretería Industrial Los Olivos S.A.C. (caso de estudio)
   GRUPO     : EFSRT G08 - Cibertec
   MOTOR     : SQL Server Express (ejecutar en SQL Server Management Studio)
   CONTENIDO : Creación de la base de datos, tablas, restricciones, índices,
               vistas y procedimientos de apoyo.
   MODELO    : ROL, USUARIO, CATEGORIA, PRODUCTO, MOVIMIENTO_INVENTARIO,
               VENTA, DETALLE_VENTA   (según el informe, sección 3.2)
   ============================================================================ */

USE master;
GO

IF DB_ID(N'FerreteriaLosOlivos') IS NOT NULL
BEGIN
    ALTER DATABASE FerreteriaLosOlivos SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE FerreteriaLosOlivos;
END
GO

CREATE DATABASE FerreteriaLosOlivos
    COLLATE Modern_Spanish_CI_AS;   -- español: no distingue mayúsculas/minúsculas, sí distingue tildes
GO

USE FerreteriaLosOlivos;
GO

/* ----------------------------------------------------------------------------
   1. ROL
   Un rol agrupa a varios usuarios (Administrador / Vendedor).
   ---------------------------------------------------------------------------- */
CREATE TABLE dbo.ROL (
    id_rol      INT IDENTITY(1,1)  NOT NULL,
    nombre      VARCHAR(30)        NOT NULL,
    CONSTRAINT PK_ROL        PRIMARY KEY (id_rol),
    CONSTRAINT UQ_ROL_nombre UNIQUE (nombre)
);
GO

/* ----------------------------------------------------------------------------
   2. USUARIO
   La contraseña se guarda ENCRIPTADA (hash BCrypt generado por Spring Security),
   nunca en texto plano. Longitud 60 para BCrypt; se deja 100 por holgura.
   ---------------------------------------------------------------------------- */
CREATE TABLE dbo.USUARIO (
    id_usuario  INT IDENTITY(1,1)  NOT NULL,
    nombre      VARCHAR(100)       NOT NULL,
    usuario     VARCHAR(30)        NOT NULL,
    contrasena  VARCHAR(100)       NOT NULL,          -- hash BCrypt
    estado      BIT                NOT NULL CONSTRAINT DF_USUARIO_estado DEFAULT (1),  -- 1 activo, 0 inactivo
    id_rol      INT                NOT NULL,
    CONSTRAINT PK_USUARIO         PRIMARY KEY (id_usuario),
    CONSTRAINT UQ_USUARIO_usuario UNIQUE (usuario),
    CONSTRAINT FK_USUARIO_ROL     FOREIGN KEY (id_rol) REFERENCES dbo.ROL (id_rol)
);
GO

/* ----------------------------------------------------------------------------
   3. CATEGORIA
   Una categoría agrupa varios productos.
   ---------------------------------------------------------------------------- */
CREATE TABLE dbo.CATEGORIA (
    id_categoria INT IDENTITY(1,1) NOT NULL,
    nombre       VARCHAR(60)       NOT NULL,
    descripcion  VARCHAR(200)      NULL,
    CONSTRAINT PK_CATEGORIA        PRIMARY KEY (id_categoria),
    CONSTRAINT UQ_CATEGORIA_nombre UNIQUE (nombre)
);
GO

/* ----------------------------------------------------------------------------
   4. PRODUCTO
   El código es único (caso de prueba CP05: rechazo de código duplicado).
   El stock nunca puede ser negativo (CP09: stock insuficiente).
   "activo" permite dar de baja un producto sin perder su historial.
   ---------------------------------------------------------------------------- */
CREATE TABLE dbo.PRODUCTO (
    id_producto  INT IDENTITY(1,1) NOT NULL,
    codigo       VARCHAR(20)       NOT NULL,
    descripcion  VARCHAR(150)      NOT NULL,
    precio       DECIMAL(10,2)     NOT NULL,
    stock_actual INT               NOT NULL CONSTRAINT DF_PRODUCTO_stock  DEFAULT (0),
    stock_minimo INT               NOT NULL CONSTRAINT DF_PRODUCTO_minimo DEFAULT (0),
    ubicacion    VARCHAR(50)       NULL,                -- ej. "Estante A-2"
    activo       BIT               NOT NULL CONSTRAINT DF_PRODUCTO_activo DEFAULT (1),
    id_categoria INT               NOT NULL,
    CONSTRAINT PK_PRODUCTO        PRIMARY KEY (id_producto),
    CONSTRAINT UQ_PRODUCTO_codigo UNIQUE (codigo),
    CONSTRAINT FK_PRODUCTO_CATEGORIA FOREIGN KEY (id_categoria) REFERENCES dbo.CATEGORIA (id_categoria),
    CONSTRAINT CK_PRODUCTO_precio  CHECK (precio > 0),
    CONSTRAINT CK_PRODUCTO_stock   CHECK (stock_actual >= 0),
    CONSTRAINT CK_PRODUCTO_minimo  CHECK (stock_minimo >= 0)
);
GO

/* ----------------------------------------------------------------------------
   5. MOVIMIENTO_INVENTARIO
   Entradas (ingreso de mercadería) y salidas (venta o ajuste). Cada movimiento
   afecta el stock de un producto.
   ---------------------------------------------------------------------------- */
CREATE TABLE dbo.MOVIMIENTO_INVENTARIO (
    id_movimiento INT IDENTITY(1,1) NOT NULL,
    tipo          VARCHAR(7)        NOT NULL,           -- 'ENTRADA' | 'SALIDA'
    cantidad      INT               NOT NULL,
    fecha         DATETIME2(0)      NOT NULL CONSTRAINT DF_MOV_fecha DEFAULT (SYSDATETIME()),
    motivo        VARCHAR(100)      NOT NULL,           -- ej. 'Compra a proveedor', 'Venta', 'Ajuste'
    id_producto   INT               NOT NULL,
    id_usuario    INT               NOT NULL,
    CONSTRAINT PK_MOVIMIENTO         PRIMARY KEY (id_movimiento),
    CONSTRAINT FK_MOV_PRODUCTO       FOREIGN KEY (id_producto) REFERENCES dbo.PRODUCTO (id_producto),
    CONSTRAINT FK_MOV_USUARIO        FOREIGN KEY (id_usuario)  REFERENCES dbo.USUARIO  (id_usuario),
    CONSTRAINT CK_MOV_tipo           CHECK (tipo IN ('ENTRADA','SALIDA')),
    CONSTRAINT CK_MOV_cantidad       CHECK (cantidad > 0)
);
GO

/* ----------------------------------------------------------------------------
   6. VENTA
   Cabecera de la venta. Contiene varios detalles.
   ---------------------------------------------------------------------------- */
CREATE TABLE dbo.VENTA (
    id_venta   INT IDENTITY(1,1) NOT NULL,
    fecha      DATETIME2(0)      NOT NULL CONSTRAINT DF_VENTA_fecha DEFAULT (SYSDATETIME()),
    total      DECIMAL(12,2)     NOT NULL CONSTRAINT DF_VENTA_total DEFAULT (0),
    id_usuario INT               NOT NULL,
    CONSTRAINT PK_VENTA         PRIMARY KEY (id_venta),
    CONSTRAINT FK_VENTA_USUARIO FOREIGN KEY (id_usuario) REFERENCES dbo.USUARIO (id_usuario),
    CONSTRAINT CK_VENTA_total   CHECK (total >= 0)
);
GO

/* ----------------------------------------------------------------------------
   7. DETALLE_VENTA
   Relaciona una venta con los productos vendidos. El precio unitario se guarda
   en el detalle para conservar el precio vigente al momento de la venta.
   El subtotal es una columna calculada y persistida (cantidad x precio).
   ---------------------------------------------------------------------------- */
CREATE TABLE dbo.DETALLE_VENTA (
    id_detalle      INT IDENTITY(1,1) NOT NULL,
    id_venta        INT               NOT NULL,
    id_producto     INT               NOT NULL,
    cantidad        INT               NOT NULL,
    precio_unitario DECIMAL(10,2)     NOT NULL,
    subtotal        AS (CONVERT(DECIMAL(12,2), cantidad * precio_unitario)) PERSISTED,
    CONSTRAINT PK_DETALLE_VENTA PRIMARY KEY (id_detalle),
    CONSTRAINT UQ_DETALLE_venta_producto UNIQUE (id_venta, id_producto),
    CONSTRAINT FK_DETALLE_VENTA    FOREIGN KEY (id_venta)    REFERENCES dbo.VENTA    (id_venta) ON DELETE CASCADE,
    CONSTRAINT FK_DETALLE_PRODUCTO FOREIGN KEY (id_producto) REFERENCES dbo.PRODUCTO (id_producto),
    CONSTRAINT CK_DETALLE_cantidad CHECK (cantidad > 0),
    CONSTRAINT CK_DETALLE_precio   CHECK (precio_unitario > 0)
);
GO

/* ----------------------------------------------------------------------------
   ÍNDICES de apoyo (consulta rápida de stock y reportes por periodo)
   ---------------------------------------------------------------------------- */
CREATE INDEX IX_PRODUCTO_descripcion ON dbo.PRODUCTO (descripcion);
CREATE INDEX IX_PRODUCTO_categoria   ON dbo.PRODUCTO (id_categoria);
CREATE INDEX IX_MOV_producto_fecha   ON dbo.MOVIMIENTO_INVENTARIO (id_producto, fecha);
CREATE INDEX IX_VENTA_fecha          ON dbo.VENTA (fecha);
CREATE INDEX IX_VENTA_usuario        ON dbo.VENTA (id_usuario);
CREATE INDEX IX_DETALLE_producto     ON dbo.DETALLE_VENTA (id_producto);
GO

/* ----------------------------------------------------------------------------
   VISTAS para los módulos de consulta de stock y reportes
   ---------------------------------------------------------------------------- */

-- Consulta de stock en tiempo real + alerta de reposición (CP06, módulo Control de inventario)
CREATE VIEW dbo.vw_stock_productos
AS
SELECT  p.id_producto,
        p.codigo,
        p.descripcion,
        c.nombre        AS categoria,
        p.precio,
        p.stock_actual,
        p.stock_minimo,
        p.ubicacion,
        p.activo,
        CASE WHEN p.stock_actual <= p.stock_minimo THEN 1 ELSE 0 END AS requiere_reposicion
FROM    dbo.PRODUCTO  p
JOIN    dbo.CATEGORIA c ON c.id_categoria = p.id_categoria;
GO

-- Reporte de ventas: una fila por producto vendido (filtrar por fecha, producto o vendedor)
CREATE VIEW dbo.vw_reporte_ventas
AS
SELECT  v.id_venta,
        v.fecha,
        u.usuario       AS vendedor,
        p.codigo,
        p.descripcion   AS producto,
        d.cantidad,
        d.precio_unitario,
        d.subtotal,
        v.total         AS total_venta
FROM    dbo.VENTA         v
JOIN    dbo.DETALLE_VENTA d ON d.id_venta    = v.id_venta
JOIN    dbo.PRODUCTO      p ON p.id_producto = d.id_producto
JOIN    dbo.USUARIO       u ON u.id_usuario  = v.id_usuario;
GO

-- Rotación de productos: unidades vendidas (mayor y menor rotación)
CREATE VIEW dbo.vw_rotacion_productos
AS
SELECT  p.id_producto,
        p.codigo,
        p.descripcion,
        ISNULL(SUM(d.cantidad), 0) AS unidades_vendidas
FROM    dbo.PRODUCTO p
LEFT JOIN dbo.DETALLE_VENTA d ON d.id_producto = p.id_producto
GROUP BY p.id_producto, p.codigo, p.descripcion;
GO

/* ----------------------------------------------------------------------------
   PROCEDIMIENTO: registrar un movimiento de inventario (entrada o salida)
   Actualiza el stock en la misma transacción. Si la salida supera el stock,
   falla con un mensaje claro (CP07 y CP09).
   Nota: la API Spring Boot también valida; este procedimiento garantiza la
   consistencia aunque se use la base de datos directamente.
   ---------------------------------------------------------------------------- */
CREATE PROCEDURE dbo.sp_registrar_movimiento
    @tipo        VARCHAR(7),
    @cantidad    INT,
    @motivo      VARCHAR(100),
    @id_producto INT,
    @id_usuario  INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @tipo NOT IN ('ENTRADA','SALIDA') OR @cantidad <= 0
        THROW 50001, 'Tipo de movimiento o cantidad no válidos.', 1;

    BEGIN TRANSACTION;

        DECLARE @stock INT;
        SELECT @stock = stock_actual
        FROM   dbo.PRODUCTO WITH (UPDLOCK, ROWLOCK)
        WHERE  id_producto = @id_producto AND activo = 1;

        IF @stock IS NULL
            THROW 50002, 'El producto no existe o está dado de baja.', 1;

        IF @tipo = 'SALIDA' AND @cantidad > @stock
            THROW 50003, 'Stock insuficiente para registrar la salida.', 1;

        UPDATE dbo.PRODUCTO
        SET    stock_actual = CASE WHEN @tipo = 'ENTRADA'
                                   THEN stock_actual + @cantidad
                                   ELSE stock_actual - @cantidad END
        WHERE  id_producto = @id_producto;

        INSERT INTO dbo.MOVIMIENTO_INVENTARIO (tipo, cantidad, motivo, id_producto, id_usuario)
        VALUES (@tipo, @cantidad, @motivo, @id_producto, @id_usuario);

    COMMIT TRANSACTION;
END
GO

/* ----------------------------------------------------------------------------
   PROCEDIMIENTO: registrar una venta con un solo producto por llamada
   (la API puede llamarlo por cada línea dentro de su propia transacción, o
   implementar la venta completa en Java). Aquí se deja el flujo completo:
   cabecera + detalle + descuento de stock + movimiento de salida + total.
   ---------------------------------------------------------------------------- */
CREATE TYPE dbo.tipo_detalle_venta AS TABLE (
    id_producto INT NOT NULL,
    cantidad    INT NOT NULL CHECK (cantidad > 0)
);
GO

CREATE PROCEDURE dbo.sp_registrar_venta
    @id_usuario INT,
    @detalle    dbo.tipo_detalle_venta READONLY,
    @id_venta   INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF NOT EXISTS (SELECT 1 FROM @detalle)
        THROW 50010, 'La venta no tiene productos.', 1;

    BEGIN TRANSACTION;

        -- Bloquea y valida stock de todas las líneas
        IF EXISTS (
            SELECT 1
            FROM   @detalle d
            JOIN   dbo.PRODUCTO p WITH (UPDLOCK, ROWLOCK) ON p.id_producto = d.id_producto
            WHERE  p.activo = 0 OR d.cantidad > p.stock_actual
        )
            THROW 50003, 'Stock insuficiente o producto dado de baja.', 1;

        INSERT INTO dbo.VENTA (id_usuario, total) VALUES (@id_usuario, 0);
        SET @id_venta = SCOPE_IDENTITY();

        INSERT INTO dbo.DETALLE_VENTA (id_venta, id_producto, cantidad, precio_unitario)
        SELECT @id_venta, d.id_producto, d.cantidad, p.precio
        FROM   @detalle d
        JOIN   dbo.PRODUCTO p ON p.id_producto = d.id_producto;

        UPDATE p
        SET    p.stock_actual = p.stock_actual - d.cantidad
        FROM   dbo.PRODUCTO p
        JOIN   @detalle d ON d.id_producto = p.id_producto;

        INSERT INTO dbo.MOVIMIENTO_INVENTARIO (tipo, cantidad, motivo, id_producto, id_usuario)
        SELECT 'SALIDA', d.cantidad, 'Venta N° ' + CAST(@id_venta AS VARCHAR(10)), d.id_producto, @id_usuario
        FROM   @detalle d;

        UPDATE dbo.VENTA
        SET    total = (SELECT SUM(subtotal) FROM dbo.DETALLE_VENTA WHERE id_venta = @id_venta)
        WHERE  id_venta = @id_venta;

    COMMIT TRANSACTION;
END
GO

PRINT 'Base de datos FerreteriaLosOlivos creada correctamente.';
GO
