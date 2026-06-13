package com.pachasuite.api.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.pachasuite.api.dto.ReservaResponseDTO;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] generarReservaPdf(ReservaResponseDTO r) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        // Título
        doc.add(new Paragraph("PACHA SUITE")
                .setBold().setFontSize(22)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph("Confirmación de Reserva")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));

        doc.add(new Paragraph(" "));

        // Código
        doc.add(new Paragraph("Código: " + r.getCodigo())
                .setBold().setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(" "));

        // Tabla de detalles
        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .setWidth(UnitValue.createPercentValue(100));

        agregarFila(table, "Habitación", r.getHabitacionNombre());
        agregarFila(table, "Check-in",   r.getCheckIn().toString());
        agregarFila(table, "Check-out",  r.getCheckOut().toString());
        agregarFila(table, "Noches",     String.valueOf(r.getNoches()));
        agregarFila(table, "Huéspedes", r.getAdultos() + " adultos" +
                (r.getNinos() > 0 ? ", " + r.getNinos() + " niños" : ""));
        agregarFila(table, "Estado",     r.getEstado());
        agregarFila(table, "Subtotal",   "$" + r.getSubtotal());
        agregarFila(table, "IGV (18%)",  "$" + r.getImpuestos());
        agregarFila(table, "Total",      "$" + r.getTotal());

        doc.add(table);

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Presenta este código al hacer check-in.")
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return out.toByteArray();
    }

    private void agregarFila(Table table, String label, String value) {
        table.addCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(label).setBold()));
        table.addCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(value)));
    }
}