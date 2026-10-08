# Guía de instalación

Sistema web de inventario y ventas – Ferretería Industrial Los Olivos S.A.C.
Arquitectura: **React** (puerto 5173) → **Spring Boot** (puerto 8080) → **SQL Server Express** (puerto 1433).

## 1. Requisitos
| Programa | Versión | Uso |
|---|---|---|
| SQL Server Express + SSMS | 2019 o superior | Base de datos |
| JDK | 17 o superior (probado con JDK 21) | Ejecutar la API |
| Maven | 3.9 o superior | Compilar la API |
| Node.js | 20 o superior (incluye npm) | Ejecutar el frontend |
| Git | cualquiera | Clonar el repositorio |

## 2. Obtener el código
```bash
git clone https://github.com/USUARIO/ferreteria-inventario-ventas.git
cd ferreteria-inventario-ventas
```

## 3. Base de datos (SQL Server)
1. Abrir **SQL Server Configuration Manager** → *Protocolos de SQL Server Express* → habilitar **TCP/IP**; en sus propiedades, pestaña *Direcciones IP*, sección **IPAll**: *Puertos TCP* = `1433` y *Puertos dinámicos* en blanco. Reiniciar el servicio **SQL Server (SQLEXPRESS)**.
2. En SSMS, clic derecho en el servidor → *Propiedades* → *Seguridad* → **Modo de autenticación de SQL Server y Windows**. Reiniciar el servicio.
3. Habilitar el inicio de sesión `sa` y asignarle una contraseña (Seguridad → Inicios de sesión → sa → Propiedades).
4. En SSMS abrir y ejecutar, en este orden, con **F5**:
   1. `04_Base_de_Datos/01_crear_base_datos.sql`
   2. `04_Base_de_Datos/02_datos_prueba.sql`

## 4. API (Spring Boot)
```bash
cd 03_Codigo_Fuente/backend
# Windows (PowerShell)
$env:DB_USER="sa"; $env:DB_PASSWORD="TU_CLAVE_DE_SA"; $env:JWT_SECRET="una-clave-larga-de-32-caracteres-o-mas"
# Linux / macOS
# export DB_USER=sa DB_PASSWORD=TU_CLAVE_DE_SA JWT_SECRET=una-clave-larga-de-32-caracteres-o-mas

mvn spring-boot:run
```
La API queda en `http://localhost:8080`. En el primer inicio reemplaza los marcadores de contraseña de los usuarios de prueba por hashes BCrypt.

| Usuario | Contraseña inicial | Rol |
|---|---|---|
| `admin` | `Admin123` | Administrador |
| `vendedor1` | `Vendedor123` | Vendedor |

**Cambie estas contraseñas** (variables `SEED_ADMIN_PASSWORD` y `SEED_VENDEDOR_PASSWORD` antes del primer inicio, o desde la pantalla Usuarios).

## 5. Frontend (React)
```bash
cd 03_Codigo_Fuente/frontend
npm install
npm run dev
```
Abrir `http://localhost:5173`. Si la API está en otra dirección, copie `.env.example` a `.env` y ajuste `VITE_API_URL`.

## 6. Ejecutar las pruebas
```bash
cd 03_Codigo_Fuente/backend
mvn test
```
Las pruebas usan una base H2 en memoria; no tocan SQL Server.

Resultado esperado: al final de la consola debe aparecer `Tests run: 14, Failures: 0, Errors: 0, Skipped: 0` y `BUILD SUCCESS`. En la ejecución del 08/10/2026 (Java 21.0.10) la primera corrida tardó 1 min 40 s: `RendimientoTest` (1 prueba, incluye el arranque de Spring) y `SistemaIntegracionTest` (13 pruebas).

Durante la ejecución aparecen avisos normales que no indican error: "Using generated security password" (contraseña temporal de Spring Security en el perfil de prueba) y "A Java agent has been loaded dynamically" (ByteBuddy, usado por las pruebas).

## 7. Problemas frecuentes
| Síntoma | Causa y solución |
|---|---|
| `Login failed for user 'sa'` | Autenticación mixta o `sa` deshabilitado: ver paso 3. |
| `Connection refused` a `localhost:1433` | TCP/IP no habilitado o servicio sin reiniciar. |
| La pantalla dice "No se pudo conectar con el servidor" | La API no está en ejecución o `VITE_API_URL` es incorrecto. |
| Error de CORS en el navegador | El frontend debe abrirse en `http://localhost:5173` (propiedad `app.cors.origins`). |
| `Invalid or unsupported JWT secret` | `JWT_SECRET` debe tener 32 caracteres o más. |
| Aviso "WARNING: A Java agent has been loaded dynamically" al ejecutar `mvn test` | Es normal (ByteBuddy). No afecta el resultado; si la compilación y las pruebas terminan en BUILD SUCCESS, puede ignorarse. |
| Aviso "Using generated security password" | Es normal en el perfil de prueba; la API real usa los usuarios de la base de datos. |
| `mvn test` tarda más de un minuto | El arranque de Spring y la prueba de 5 000 productos toman tiempo en la primera ejecución; es lo esperado. |
