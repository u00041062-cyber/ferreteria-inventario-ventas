# Plan de pruebas – Sistema de inventario y ventas (Ferretería Los Olivos)

Tipos de prueba: **funcional**, **de integración** (frontend ↔ API REST ↔ base de datos) y **de rendimiento**.
Las pruebas automáticas están en `03_Codigo_Fuente/backend/src/test/java/com/ferreteria/` y se ejecutan con:

```bash
cd 03_Codigo_Fuente/backend
mvn test
```

## Casos de prueba

| ID | Módulo | Caso de prueba | Resultado esperado | Tipo | Prueba automática | Resultado obtenido |
|---|---|---|---|---|---|---|
| CP01 | Inicio de sesión | Ingresar con credenciales válidas | Acceso al sistema con las opciones del rol | Funcional | `cp01_loginValido` | ✔ Aprobado (mvn test, 08/10/2026) |
| CP02 | Inicio de sesión | Ingresar con una contraseña incorrecta | Mensaje de error y acceso denegado | Funcional | `cp02_contrasenaIncorrecta` | ✔ Aprobado (mvn test, 08/10/2026) |
| CP03 | Usuarios y roles | Acceder con rol de vendedor a la administración de usuarios | Acceso denegado | Funcional | `cp03_vendedorNoAdministraUsuarios` | ✔ Aprobado (mvn test, 08/10/2026) |
| CP04 | Productos | Registrar un producto con datos completos | Producto visible en el catálogo | Funcional | `cp04_registrarProducto` | ✔ Aprobado (mvn test, 08/10/2026) |
| CP05 | Productos | Registrar un producto con código duplicado | Rechazo con mensaje de validación | Funcional | `cp05_codigoDuplicado` | ✔ Aprobado (mvn test, 08/10/2026) |
| CP06 | Consulta de stock | Consultar un producto existente | Se muestran stock, precio y ubicación | Funcional | `cp06_consultaStock` | ✔ Aprobado (mvn test, 08/10/2026) |
| CP07 | Inventario | Registrar una entrada de mercadería | El stock aumenta en la cantidad ingresada | Funcional | `cp07_entradaAumentaStock` | ✔ Aprobado (mvn test, 08/10/2026) |
| CP08 | Ventas | Registrar la venta de un producto con stock | El stock se descuenta y se genera el comprobante | Integración | `cp08_ventaDescuentaStockYGeneraComprobante` | ✔ Aprobado (mvn test, 08/10/2026) |
| CP09 | Ventas | Vender más unidades que las disponibles | Bloqueo con mensaje de stock insuficiente | Funcional | `cp09_ventaMayorAlStock` | ✔ Aprobado (mvn test, 08/10/2026) |
| CP10 | Reportes | Generar y exportar el reporte de ventas de un periodo | Archivo PDF o Excel con totales correctos | Funcional | `cp10_reporteVentasExportable` | ✔ Aprobado (mvn test, 08/10/2026) |
| CP11 | Consulta de stock | Consultar el stock con un catálogo de prueba amplio (5 000 productos) | Respuesta en menos de 3 segundos | Rendimiento | `RendimientoTest.cp11_consultaStockCatalogoAmplio` | ✔ Aprobado: 415 ms (mvn test, 08/10/2026) |
| CP12 | Flujo completo | Inicio de sesión, entrada de mercadería, venta y reporte | Datos consistentes entre todos los módulos | Integración | `cp12_flujoCompleto` | ✔ Aprobado (mvn test, 08/10/2026) |

Pruebas adicionales: acceso sin token (401) y alerta de stock mínimo.

## Prueba manual en el navegador (evidencia para el informe)
1. Ingresar como `admin` → ver menú completo. Ingresar como `vendedor1` → solo Stock y Ventas.
2. Registrar un producto, una entrada de mercadería y una venta; verificar el descuento de stock en Stock.
3. Generar el reporte de ventas del día y exportar PDF y Excel.
4. Tomar capturas de pantalla de cada módulo (Captura 1 a 7 del informe).

## Medición de indicadores (objetivos específicos)
| Objetivo | Cómo medir | Fórmula | Meta |
|---|---|---|---|
| 1 | Cronometrar 10 atenciones con el procedimiento manual y 10 con el sistema | I = ((Tsin − Tcon)/Tsin)×100 | Tcon ≤ 9 min (I ≥ 40 %) |
| 2 | Contar errores en 20 ventas/movimientos manuales vs. con el sistema | I = ((Esin − Econ)/Esin)×100 | Econ ≤ 5 % (I ≥ 50 %) |
| 3 | Cronometrar la generación de un reporte manual vs. el reporte del sistema | I = ((Rsin − Rcon)/Rsin)×100 | Rcon ≤ 16 min (I ≥ 60 %) |
| 4 | Probar el acceso con un usuario de cada rol (CP01–CP03) | I = (Uaut/Utot)×100 | 100 % |
| 5 | Verificar que ventas y movimientos queden en SQL Server | I = (Creg/Ctot)×100 | 100 % |

> Las columnas "Resultado obtenido" y las mediciones se completan al ejecutar las pruebas; no se incluyen valores inventados.

## Prueba de integración
Verifica que React, la API REST y SQL Server funcionen de forma coordinada: el flujo CP12 ejecutado desde la interfaz
(login → entrada → venta → reporte) debe dejar los mismos datos en la pantalla de Stock y en la tabla `MOVIMIENTO_INVENTARIO`.
