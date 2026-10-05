package com.ferreteria.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Genera archivos PDF y Excel a partir de un título, encabezados y filas de texto. */
public final class ExportUtil {
    private ExportUtil() {}

    public static byte[] pdf(String titulo, List<String> encabezados, List<List<String>> filas, String pie) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph("Ferretería Industrial Los Olivos S.A.C.", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            doc.add(new Paragraph(titulo, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15)));
            doc.add(new Paragraph("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    FontFactory.getFont(FontFactory.HELVETICA, 9)));
            doc.add(new Paragraph(" "));
            PdfPTable t = new PdfPTable(encabezados.size());
            t.setWidthPercentage(100);
            Font fh = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font fc = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (String h : encabezados) {
                PdfPCell c = new PdfPCell(new Phrase(h, fh));
                c.setBackgroundColor(new java.awt.Color(230, 233, 237));
                t.addCell(c);
            }
            for (List<String> f : filas) for (String v : f) t.addCell(new PdfPCell(new Phrase(v == null ? "" : v, fc)));
            doc.add(t);
            if (pie != null) { doc.add(new Paragraph(" ")); doc.add(new Paragraph(pie, fh)); }
            doc.close();
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        }
    }

    public static byte[] xlsx(String hoja, List<String> encabezados, List<List<String>> filas) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet s = wb.createSheet(hoja);
            CellStyle st = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font f = wb.createFont(); f.setBold(true); st.setFont(f);
            Row r0 = s.createRow(0);
            for (int i = 0; i < encabezados.size(); i++) { Cell c = r0.createCell(i); c.setCellValue(encabezados.get(i)); c.setCellStyle(st); }
            for (int r = 0; r < filas.size(); r++) {
                Row row = s.createRow(r + 1);
                List<String> fila = filas.get(r);
                for (int c = 0; c < fila.size(); c++) {
                    String v = fila.get(c);
                    try { row.createCell(c).setCellValue(Double.parseDouble(v)); }   // números como números
                    catch (NumberFormatException | NullPointerException e) { row.createCell(c).setCellValue(v == null ? "" : v); }
                }
            }
            for (int i = 0; i < encabezados.size(); i++) s.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el Excel", e);
        }
    }
}
