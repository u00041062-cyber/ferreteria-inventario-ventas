# Plan de pruebas – Sistema de inventario y ventas (Ferretería Los Olivos)

Tipos de prueba: **funcional**, **de integración** (API REST ↔ lógica de negocio ↔ base de datos; la conexión con el frontend y con SQL Server se comprueba en la prueba manual) y **de rendimiento**.
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
| CP10 | Reportes | Generar y exportar el reporte de ventas de un periodo | Archivo PDF o Excel generado; el PDF muestra el total vendido y el Excel el subtotal de cada línea | Funcional | `cp10_reporteVentasExportable` | ✔ Aprobado (mvn test, 08/10/2026) |
| CP11 | Consulta de stock | Consultar el stock con un catálogo de prueba amplio (5 000 productos) | Respuesta en menos de 3 segundos | Rendimiento | `RendimientoTest.cp11_consultaStockCatalogoAmplio` | ✔ Aprobado: 415 ms (mvn test, 08/10/2026) |
| CP12 | Flujo completo | Inicio de sesión, entrada de mercadería, venta y reporte | Datos consistentes entre todos los módulos | Integración | `cp12_flujoCompleto` | ✔ Aprobado (mvn test, 08/10/2026) |

Pruebas adicionales (también aprobadas): `sinTokenNoAccede` (acceso sin token, 401) y `alertaStockMinimo` (alerta de stock mínimo).

## Resumen de la ejecución automática (mvn test)
| Dato | Valor |
|---|---|
| Fecha de ejecución | 08/10/2026, 08:42 – 08:43 (hora de Lima) |
| Comando | `mvn test` (carpeta `03_Codigo_Fuente/backend`) |
| Entorno | Windows, Java 21.0.10, Spring Boot 3.3.5, Hibernate 6.5.3, Maven Surefire 3.2.5 (JUnit Platform) |
| Base de datos de prueba | H2 en memoria (perfil "test"); no se usó SQL Server |
| RendimientoTest | 1 prueba, 0 fallos, 0 errores (44,09 s) |
| SistemaIntegracionTest | 13 pruebas, 0 fallos, 0 errores (18,15 s) |
| Total | 14 pruebas: 0 fallos, 0 errores, 0 omitidas |
| Resultado final | BUILD SUCCESS (tiempo total: 1 min 40 s) |
| CP11 – rendimiento | Consulta sobre 5 000 productos en 415 ms (meta: menos de 3 000 ms) |

Las 12 pruebas planificadas (CP01–CP12) y las 2 adicionales quedaron aprobadas. En el registro de consola aparecen avisos esperados que no afectan el resultado: la contraseña generada por Spring Security (solo para desarrollo) y la advertencia de carga dinámica del agente Java de ByteBuddy.

El proyecto compila con Java 17 (`pom.xml`); la ejecución se hizo con JDK 21, compatible con esa versión.

Segunda ejecución de verificación (08/10/2026, 18:53 – 18:54, hora de Lima; Windows, Java 21.0.10): 14 pruebas, 0 fallos, 0 errores, 0 omitidas; BUILD SUCCESS en 2 min 39 s (RendimientoTest 48,48 s; SistemaIntegracionTest 40,26 s). CP11: consulta sobre 5 000 productos en 525 ms. Los tiempos varían según el equipo y la corrida; el criterio de aprobación (menos de 3 000 ms) se cumple en ambas ejecuciones.

Alcance de las pruebas automáticas: validan la API REST, la lógica de negocio y la persistencia sobre H2 en memoria con el esquema generado por Hibernate; no ejecutan los scripts de 04_Base_de_Datos ni usan SQL Server o el frontend. CP08 verifica el descuento de stock y el total de la venta; CP10 verifica el subtotal del reporte de ventas (JSON), que los archivos PDF y Excel se generen con el formato correcto y que el reporte de inventario sea exclusivo del administrador (el vendedor recibe acceso denegado). CP11 mide el tiempo de la búsqueda de productos en la capa de servicio (`ProductoService.buscar`, con un filtro de texto sobre 5 000 productos) y no incluye red ni frontend. La descarga y revisión visual del comprobante y de los reportes exportados (en el PDF de ventas, el total vendido; en el Excel, el subtotal de cada línea, sin fila de total), el registro en SQL Server (objetivo 5) y la medición de CP11 en los equipos del local se comprueban en la prueba manual.

## Prueba manual en el navegador (evidencia para el informe)
1. Ingresar como `admin` → ver menú completo. Ingresar como `vendedor1` → solo Stock y Ventas.
2. Registrar un producto, una entrada de mercadería y una venta; descargar el comprobante PDF, verificar el descuento de stock en Stock y comprobar en SSMS que la venta y los movimientos quedaron en SQL Server.
3. Generar el reporte de ventas del día, exportar PDF y Excel y revisar que el total vendido del PDF coincida con el TOTAL de la pantalla y que la suma de los subtotales del Excel dé el mismo monto (el Excel no incluye fila de total).
4. Tomar capturas de pantalla de cada módulo (Captura 1 a 7 del informe).

## Medición de indicadores (objetivos específicos)
| Objetivo | Cómo medir | Fórmula | Meta |
|---|---|---|---|
| 1 | Cronometrar 10 atenciones con el procedimiento manual y 10 con el sistema | I = ((Tsin − Tcon)/Tsin)×100 | Tcon ≤ 9 min (I ≥ 40 %) |
| 2 | Contar errores en 20 ventas/movimientos manuales vs. con el sistema | I = ((Esin − Econ)/Esin)×100 | Econ ≤ 5 % (I ≥ 50 %) |
| 3 | Cronometrar la generación de un reporte manual vs. el reporte del sistema | I = ((Rsin − Rcon)/Rsin)×100 | Rcon ≤ 16 min (I ≥ 60 %) |
| 4 | Probar el acceso con un usuario de cada rol (CP01–CP03) | I = (Uaut/Utot)×100 | 100 % |
| 5 | Verificar que ventas y movimientos queden en SQL Server | I = (Creg/Ctot)×100 | 100 % |

> La columna "Resultado obtenido" corresponde a la ejecución automática del 08/10/2026. Las mediciones de los objetivos 1 a 3 (cronometraje y conteo de errores) y la verificación del objetivo 5 en SQL Server se completan con la prueba manual; no se incluyen valores inventados. Para el objetivo 4, las pruebas CP01–CP03 aprobadas dan la evidencia automática del control de acceso por rol.
