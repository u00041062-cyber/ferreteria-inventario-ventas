# Manual de usuario

Sistema de inventario y ventas – Ferretería Industrial Los Olivos S.A.C.
Se usa desde el navegador (Chrome, Edge o Firefox) en la dirección que indique el equipo del proyecto.

## 1. Ingresar al sistema
1. Escriba su **usuario** y **contraseña** y pulse **Ingresar**.
2. Si los datos son incorrectos aparece un mensaje de error. Si su cuenta fue desactivada, tampoco podrá ingresar.
3. Para salir, pulse **Salir** (arriba a la derecha).

Según su rol verá más o menos opciones:

| Opción | Vendedor | Administrador |
|---|:-:|:-:|
| Stock (consulta) | ✔ | ✔ |
| Ventas | ✔ | ✔ |
| Productos y categorías | | ✔ |
| Almacén (entradas/salidas) | | ✔ |
| Reportes | | ✔ |
| Usuarios | | ✔ |

## 2. Consultar stock (vendedor y administrador)
1. En **Stock**, escriba parte del código o de la descripción y pulse **Buscar**.
2. Verá el precio, el stock y la **ubicación** del producto, sin ir al almacén.
3. Las filas en rojo son productos con stock igual o menor al mínimo. Abajo se listan todos los productos por reponer.

## 3. Registrar una venta
1. Entre a **Ventas**, busque el producto y pulse **Agregar**. Repita para cada producto.
2. Ajuste las cantidades en el detalle. No puede vender más unidades que el stock disponible.
3. Revise el **TOTAL** y pulse **Registrar venta**.
4. El stock se descuenta automáticamente. Pulse **Descargar comprobante** para obtener el PDF.

## 4. Productos y categorías (administrador)
- **Nuevo producto:** complete código (no se puede repetir), descripción, precio, stock mínimo, ubicación, categoría y stock inicial; pulse **Guardar**.
- **Editar:** pulse **Editar** en la fila, cambie los datos y guarde. El stock no se cambia aquí.
- **Baja:** retira el producto del catálogo conservando su historial.
- **Categorías:** al final de la pantalla puede agregar, editar o eliminar categorías (no se elimina una que tenga productos).

## 5. Entradas y salidas de almacén (administrador)
1. En **Almacén** elija **Entrada** (llegó mercadería) o **Salida** (ajuste, merma).
2. Seleccione el producto, la cantidad y escriba el motivo; pulse **Registrar**.
3. El stock se actualiza y el movimiento queda en el historial. Una salida mayor al stock se rechaza.

## 6. Reportes (administrador)
1. En **Reportes** elija **Ventas** o **Inventario**.
2. Para ventas, indique el periodo y, si desea, producto o vendedor. Pulse **Generar**.
3. El reporte de inventario muestra el stock, las alertas de reposición y las unidades vendidas (rotación).
4. Pulse **Exportar PDF** o **Exportar Excel** para descargar el archivo.

## 7. Usuarios y roles (administrador)
- **Crear:** nombre, usuario, contraseña (mínimo 6 caracteres) y rol (Administrador o Vendedor).
- **Editar:** puede cambiar nombre, rol o dejar una nueva contraseña (si deja el campo vacío se mantiene la actual).
- **Desactivar / Activar:** un usuario desactivado no puede ingresar. No puede desactivar su propia cuenta.

## 8. Mensajes frecuentes
| Mensaje | Qué hacer |
|---|---|
| Usuario o contraseña incorrectos | Verifique los datos o pida al administrador restablecer su contraseña. |
| Stock insuficiente | Reduzca la cantidad o registre una entrada de mercadería. |
| Ya existe un producto con el código… | Use otro código o edite el producto existente. |
| No tiene permiso para esta acción | Esa opción es solo para el administrador. |
| No se pudo conectar con el servidor | Avise al equipo del proyecto: el sistema no está en ejecución. |
