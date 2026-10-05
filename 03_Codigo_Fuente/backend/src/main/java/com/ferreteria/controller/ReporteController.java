package com.ferreteria.controller;

import com.ferreteria.dto.FilaInventario;
import com.ferreteria.dto.FilaVenta;
import com.ferreteria.exception.ApiException;
import com.ferreteria.service.ReporteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Solo administrador. Formatos de exportación: pdf | xlsx. */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReporteService service;
    public ReporteController(ReporteService service) { this.service = service; }

    @GetMapping("/ventas")
    public List<FilaVenta> ventas(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                  @RequestParam(required = false) Integer idProducto,
                                  @RequestParam(required = false) Integer idVendedor) {
        return service.ventas(desde, hasta, idProducto, idVendedor);
    }

    @GetMapping("/ventas/exportar")
    public ResponseEntity<byte[]> exportarVentas(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                                 @RequestParam(required = false) Integer idProducto,
                                                 @RequestParam(required = false) Integer idVendedor,
                                                 @RequestParam String formato) {
        List<FilaVenta> f = service.ventas(desde, hasta, idProducto, idVendedor);
        return archivo("reporte-ventas", formato, service.ventasPdf(f, desde, hasta), service.ventasXlsx(f));
    }

    @GetMapping("/inventario")
    public List<FilaInventario> inventario() { return service.inventario(); }

    @GetMapping("/inventario/exportar")
    public ResponseEntity<byte[]> exportarInventario(@RequestParam String formato) {
        List<FilaInventario> f = service.inventario();
        return archivo("reporte-inventario", formato, service.inventarioPdf(f), service.inventarioXlsx(f));
    }

    private ResponseEntity<byte[]> archivo(String nombre, String formato, byte[] pdf, byte[] xlsx) {
        boolean esPdf = "pdf".equalsIgnoreCase(formato);
        if (!esPdf && !"xlsx".equalsIgnoreCase(formato)) throw ApiException.badRequest("Formato no válido: use pdf o xlsx");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombre + (esPdf ? ".pdf" : ".xlsx"))
                .contentType(esPdf ? MediaType.APPLICATION_PDF : XLSX)
                .body(esPdf ? pdf : xlsx);
    }
}
