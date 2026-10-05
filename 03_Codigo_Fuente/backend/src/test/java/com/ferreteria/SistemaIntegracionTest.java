package com.ferreteria;

import com.ferreteria.entity.*;
import com.ferreteria.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas funcionales y de integración (API REST + lógica + base de datos H2 en memoria).
 * Corresponden a los casos CP01 a CP12 del plan de pruebas (ver 05_Pruebas/Plan_de_Pruebas.md).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SistemaIntegracionTest {

    @Autowired MockMvc mvc;
    @Autowired RolRepository roles;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired ProductoRepository productos;
    @Autowired PasswordEncoder encoder;

    Integer idProducto;
    Integer idCategoria;

    @BeforeEach
    void datos() {
        Rol admin = roles.save(new Rol("Administrador"));
        Rol vend = roles.save(new Rol("Vendedor"));
        crearUsuario("admin", "Admin123", admin);
        crearUsuario("vendedor1", "Vendedor123", vend);
        Categoria c = new Categoria(); c.setNombre("Herramientas");
        idCategoria = categorias.save(c).getId();
        Producto p = new Producto();
        p.setCodigo("HER-001"); p.setDescripcion("Martillo 16 oz"); p.setPrecio(new BigDecimal("25.00"));
        p.setStockActual(10); p.setStockMinimo(3); p.setUbicacion("Estante A-1"); p.setCategoria(c);
        idProducto = productos.save(p).getId();
    }

    private void crearUsuario(String user, String clave, Rol rol) {
        Usuario u = new Usuario(); u.setNombre(user); u.setUsuario(user);
        u.setContrasena(encoder.encode(clave)); u.setRol(rol); u.setEstado(true);
        usuarios.save(u);
    }

    private String login(String user, String clave) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":\"" + user + "\",\"contrasena\":\"" + clave + "\"}"))
                .andExpect(status().isOk()).andReturn();
        String body = r.getResponse().getContentAsString();
        return "Bearer " + body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    // CP01
    @Test void cp01_loginValido() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":\"admin\",\"contrasena\":\"Admin123\"}"))
           .andExpect(status().isOk()).andExpect(jsonPath("$.rol").value("Administrador"))
           .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // CP02
    @Test void cp02_contrasenaIncorrecta() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":\"admin\",\"contrasena\":\"mala\"}"))
           .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.mensaje").exists());
    }

    @Test void sinTokenNoAccede() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
    }

    // CP03
    @Test void cp03_vendedorNoAdministraUsuarios() throws Exception {
        mvc.perform(get("/api/usuarios").header("Authorization", login("vendedor1", "Vendedor123")))
           .andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios").header("Authorization", login("admin", "Admin123")))
           .andExpect(status().isOk());
    }

    // CP04
    @Test void cp04_registrarProducto() throws Exception {
        String t = login("admin", "Admin123");
        mvc.perform(post("/api/productos").header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"HER-009\",\"descripcion\":\"Destornillador\",\"precio\":9.5,\"stockActual\":5,\"stockMinimo\":2,\"ubicacion\":\"A-3\",\"idCategoria\":" + idCategoria + "}"))
           .andExpect(status().isCreated());
        mvc.perform(get("/api/productos?q=destorn").header("Authorization", t))
           .andExpect(status().isOk()).andExpect(jsonPath("$[0].codigo").value("HER-009"));
    }

    // CP05
    @Test void cp05_codigoDuplicado() throws Exception {
        mvc.perform(post("/api/productos").header("Authorization", login("admin", "Admin123")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"HER-001\",\"descripcion\":\"Otro\",\"precio\":9.5,\"stockMinimo\":2,\"idCategoria\":" + idCategoria + "}"))
           .andExpect(status().isConflict());
    }

    // CP06
    @Test void cp06_consultaStock() throws Exception {
        mvc.perform(get("/api/productos?q=martillo").header("Authorization", login("vendedor1", "Vendedor123")))
           .andExpect(status().isOk()).andExpect(jsonPath("$[0].stockActual").value(10))
           .andExpect(jsonPath("$[0].precio").value(25.0)).andExpect(jsonPath("$[0].ubicacion").value("Estante A-1"));
    }

    // CP07
    @Test void cp07_entradaAumentaStock() throws Exception {
        mvc.perform(post("/api/movimientos").header("Authorization", login("admin", "Admin123")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"ENTRADA\",\"cantidad\":15,\"motivo\":\"Compra a proveedor\",\"idProducto\":" + idProducto + "}"))
           .andExpect(status().isCreated());
        assertThat(productos.findById(idProducto).orElseThrow().getStockActual()).isEqualTo(25);
    }

    // CP08
    @Test void cp08_ventaDescuentaStockYGeneraComprobante() throws Exception {
        String t = login("vendedor1", "Vendedor123");
        mvc.perform(post("/api/ventas").header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"idProducto\":" + idProducto + ",\"cantidad\":4}]}"))
           .andExpect(status().isCreated()).andExpect(jsonPath("$.total").value(100.0));
        assertThat(productos.findById(idProducto).orElseThrow().getStockActual()).isEqualTo(6);
        mvc.perform(get("/api/ventas").header("Authorization", t)).andExpect(status().isOk());
    }

    // CP09
    @Test void cp09_ventaMayorAlStock() throws Exception {
        mvc.perform(post("/api/ventas").header("Authorization", login("vendedor1", "Vendedor123")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"idProducto\":" + idProducto + ",\"cantidad\":11}]}"))
           .andExpect(status().isConflict()).andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("Stock insuficiente")));
        assertThat(productos.findById(idProducto).orElseThrow().getStockActual()).isEqualTo(10);
    }

    // CP10
    @Test void cp10_reporteVentasExportable() throws Exception {
        String ta = login("admin", "Admin123");
        mvc.perform(post("/api/ventas").header("Authorization", ta).contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"idProducto\":" + idProducto + ",\"cantidad\":2}]}")).andExpect(status().isCreated());
        String hoy = java.time.LocalDate.now().toString();
        mvc.perform(get("/api/reportes/ventas?desde=" + hoy + "&hasta=" + hoy).header("Authorization", ta))
           .andExpect(status().isOk()).andExpect(jsonPath("$[0].subtotal").value(50.0));
        byte[] pdf = mvc.perform(get("/api/reportes/ventas/exportar?desde=" + hoy + "&hasta=" + hoy + "&formato=pdf").header("Authorization", ta))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
        byte[] xlsx = mvc.perform(get("/api/reportes/ventas/exportar?desde=" + hoy + "&hasta=" + hoy + "&formato=xlsx").header("Authorization", ta))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertThat(xlsx[0]).isEqualTo((byte) 'P');   // un .xlsx es un ZIP ("PK")
        mvc.perform(get("/api/reportes/inventario").header("Authorization", login("vendedor1", "Vendedor123")))
           .andExpect(status().isForbidden());
    }

    // CP12 – flujo completo: login, entrada de mercadería, venta y reporte coherentes
    @Test void cp12_flujoCompleto() throws Exception {
        String t = login("admin", "Admin123");
        mvc.perform(post("/api/movimientos").header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"ENTRADA\",\"cantidad\":10,\"motivo\":\"Compra\",\"idProducto\":" + idProducto + "}")).andExpect(status().isCreated());
        mvc.perform(post("/api/ventas").header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
                .content("{\"items\":[{\"idProducto\":" + idProducto + ",\"cantidad\":5}]}")).andExpect(status().isCreated());
        assertThat(productos.findById(idProducto).orElseThrow().getStockActual()).isEqualTo(15);
        mvc.perform(get("/api/reportes/inventario").header("Authorization", t))
           .andExpect(status().isOk()).andExpect(jsonPath("$[0].stockActual").value(15))
           .andExpect(jsonPath("$[0].unidadesVendidas").value(5));
        mvc.perform(get("/api/movimientos").header("Authorization", t))
           .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test void alertaStockMinimo() throws Exception {
        String t = login("admin", "Admin123");
        mvc.perform(post("/api/movimientos").header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\":\"SALIDA\",\"cantidad\":8,\"motivo\":\"Ajuste\",\"idProducto\":" + idProducto + "}")).andExpect(status().isCreated());
        mvc.perform(get("/api/productos/alertas").header("Authorization", t))
           .andExpect(status().isOk()).andExpect(jsonPath("$[0].codigo").value("HER-001"));
    }
}
