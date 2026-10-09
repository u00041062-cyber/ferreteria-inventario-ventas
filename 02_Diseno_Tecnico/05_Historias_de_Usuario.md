# Historias de usuario – Product Backlog

| ID | Historia de usuario | Prioridad | Sprint |
|---|---|---|---|
| HU01 | Como usuario quiero iniciar sesión con mi usuario y contraseña para acceder solo a las funciones de mi perfil. | Alta | Sprint 2 |
| HU02 | Como administrador quiero crear, editar y desactivar usuarios y asignarles un rol para controlar sus permisos. | Alta | Sprint 2 |
| HU03 | Como administrador quiero registrar y mantener productos y categorías para tener el catálogo actualizado. | Alta | Sprint 2 |
| HU04 | Como vendedor quiero consultar el stock y el precio de un producto desde mi computadora para atender sin ir al almacén. | Alta | Sprint 2 |
| HU05 | Como administrador quiero registrar entradas y salidas de almacén para que el stock se actualice automáticamente. | Alta | Sprint 3 |
| HU06 | Como vendedor quiero registrar una venta para que se descuente el stock y se genere el comprobante. | Alta | Sprint 3 |
| HU07 | Como administrador o vendedor quiero ver alertas de stock mínimo para planificar la reposición de productos. | Media | Sprint 3 |
| HU08 | Como administrador quiero generar reportes de inventario y de ventas por periodo, producto o vendedor para analizar el negocio. | Media | Sprint 3 |
| HU09 | Como administrador quiero exportar los reportes a PDF y Excel para imprimirlos o analizarlos externamente. | Media | Sprint 3 |
| HU10 | Como equipo quiero ejecutar pruebas funcionales, de integración y de rendimiento para verificar la calidad de la solución. | Alta | Sprint 3 |

## Criterios de aceptación
- **HU01:** credenciales válidas → acceso con las opciones del rol; contraseña incorrecta → mensaje de error y acceso denegado.
- **HU02:** solo el administrador accede a la administración de usuarios; el usuario desactivado no puede ingresar.
- **HU03:** el código de producto no se repite; un producto dado de baja conserva su historial.
- **HU04:** la búsqueda muestra stock, precio y ubicación en menos de 3 segundos.
- **HU05:** una entrada aumenta el stock; una salida mayor al stock disponible se rechaza.
- **HU06:** al registrar la venta el stock se descuenta y se genera el comprobante; no se vende más de lo disponible.
- **HU07:** se listan los productos con stock actual menor o igual al mínimo, visibles para administrador y vendedor en la pantalla de Stock.
- **HU08:** los reportes se filtran por periodo, producto y vendedor.
- **HU09:** los reportes se descargan en PDF y Excel con totales correctos.
- **HU10:** se ejecutan los casos CP01–CP12 del plan de pruebas.
