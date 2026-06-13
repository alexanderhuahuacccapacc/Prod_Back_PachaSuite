package com.pachasuite.api.controller;

import com.pachasuite.api.service.EmailService;
import com.pachasuite.api.service.PdfService;
import com.pachasuite.api.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaPdfController {

    private final ReservaService  reservaService;
    private final PdfService      pdfService;
    private final EmailService    emailService;

    @PostMapping("/{codigo}/enviar-pdf")
    public ResponseEntity<Map<String, String>> enviarPdf(
            @PathVariable String codigo,
            @RequestParam String email) {

        var reserva = reservaService.findByCodigo(codigo);
        byte[] pdf  = pdfService.generarReservaPdf(reserva);
        emailService.enviarReservaPdf(email, codigo, pdf);

        return ResponseEntity.ok(Map.of(
                "message", "PDF enviado a " + email
        ));
    }
}