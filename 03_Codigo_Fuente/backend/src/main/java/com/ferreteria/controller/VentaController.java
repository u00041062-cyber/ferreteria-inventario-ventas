package com.ferreteria.controller;

import com.ferreteria.dto.VentaRequest;
import com.ferreteria.dto.VentaResponse;
import com.ferreteria.service.ReporteService;
import com.ferreteria.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    private final VentaService service;
    private final ReporteService reportes;

    public VentaController(VentaService service, ReporteService reportes) { this.service = service; this.reportes = reportes; }

    @GetMapping public List<VentaResponse> recientes() { return service.recientes(); }

    @GetMapping("/{id}") public VentaResponse obtener(@PathVariable Integer id) { return service.obtener(id); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public VentaResponse registrar(@Valid @RequestBody VentaRequest r) { return service.registrar(r); }

    /** Comprobante de la venta en PDF. */
    @GetMapping("/{id}/comprobante")
    public ResponseEntity<byte[]> comprobante(@PathVariable Integer id) {
        byte[] pdf = reportes.comprobantePdf(service.obtener(id));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comprobante-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}
