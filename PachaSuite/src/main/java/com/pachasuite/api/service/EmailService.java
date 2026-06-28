package com.pachasuite.api.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name:Pacha Suite}")
    private String fromName;

    @Value("${app.mail.expiracion-minutos:10}")
    private int expiracionMinutos;


    public boolean enviarCodigoVerificacion(String destinatario, String codigo, String nombre) {
        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(destinatario);
            helper.setSubject("🔐 Tu código de verificación - Pacha Suite");

            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto'>"
                    + "<h2 style='background:#3B1F0E;color:#E8A265;padding:20px;margin:0'>Pacha Suite – Verificación</h2>"
                    + "<div style='padding:24px;border:1px solid #ddd'>"
                    + "<p>Hola <b>" + StringEscapeUtils.escapeHtml4(nombre != null && !nombre.isEmpty() ? nombre : "usuario") + "</b>,</p>"
                    + "<p>Tu código de verificación es:</p>"
                    + "<h1 style='letter-spacing:8px;text-align:center;color:#3B1F0E'>" + StringEscapeUtils.escapeHtml4(codigo) + "</h1>"
                    + "<p style='color:#999;font-size:13px'>Este código expira en " + expiracionMinutos + " minutos. No lo compartas con nadie.</p>"
                    + "</div>"
                    + "<p style='text-align:center;font-size:12px;color:#999'>Pacha Suite · Puno, Perú</p>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(mail);
            log.info("Código de verificación enviado a {}", destinatario);
            return true;

        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            log.error("Error enviando código de verificación: {}", e.getMessage());
            return false;
        }
    }


    public boolean enviarNotificacionContacto(String destinatario, String nombre,
                                              String emailRemitente, String telefono,
                                              String asunto, String mensaje) {
        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true, "UTF-8");

            helper.setFrom(fromAddress, nombre + " via Pacha Suite");
            helper.setReplyTo(emailRemitente);
            helper.setTo(destinatario);
            helper.setSubject("📩 Nuevo contacto de " + nombre + ": " + asunto);

            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto'>"
                    + "<h2 style='background:#3B1F0E;color:#E8A265;padding:20px;margin:0'>Pacha Suite – Nuevo mensaje</h2>"
                    + "<div style='padding:24px;border:1px solid #ddd'>"
                    + "<p><b>Nombre:</b> "   + StringEscapeUtils.escapeHtml4(nombre) + "</p>"
                    + "<p><b>Email:</b> "    + StringEscapeUtils.escapeHtml4(emailRemitente) + "</p>"
                    + "<p><b>Teléfono:</b> " + StringEscapeUtils.escapeHtml4(telefono != null ? telefono : "No indicado") + "</p>"
                    + "<p><b>Asunto:</b> "   + StringEscapeUtils.escapeHtml4(asunto) + "</p>"
                    + "<p style='background:#f5f5f5;padding:12px;border-radius:6px'>" + StringEscapeUtils.escapeHtml4(mensaje) + "</p>"
                    + "</div>"
                    + "<p style='text-align:center;font-size:12px;color:#999'>Pacha Suite · Puno, Perú</p>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(mail);
            log.info("Notificación de contacto enviada a {}", destinatario);
            return true;

        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            log.error("Error enviando notificación contacto: {}", e.getMessage());
            return false;
        }
    }


    public boolean enviarRespuestaContacto(String destinatario, String nombre, String asuntoOriginal, String cuerpo) {
        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(destinatario);
            helper.setSubject("Re: " + asuntoOriginal + " – Pacha Suite");

            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto'>"
                    + "<h2 style='background:#3B1F0E;color:#E8A265;padding:20px;margin:0'>Pacha Suite – Respuesta</h2>"
                    + "<div style='padding:24px;border:1px solid #ddd'>"
                    + "<p>Hola <b>" + StringEscapeUtils.escapeHtml4(nombre != null && !nombre.isEmpty() ? nombre : "usuario") + "</b>,</p>"
                    + "<p>Gracias por contactarnos. A continuación nuestra respuesta:</p>"
                    + "<p style='background:#f5f5f5;padding:12px;border-radius:6px'>" + StringEscapeUtils.escapeHtml4(cuerpo) + "</p>"
                    + "<p style='color:#999;font-size:13px;margin-top:24px'>Si tienes más consultas, no dudes en escribirnos.</p>"
                    + "</div>"
                    + "<p style='text-align:center;font-size:12px;color:#999'>Pacha Suite · Puno, Perú</p>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(mail);
            log.info("Respuesta de contacto enviada a {}", destinatario);
            return true;

        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            log.error("Error enviando respuesta de contacto: {}", e.getMessage());
            return false;
        }
    }
    public void enviarReservaPdf(String destinatario, String codigo, byte[] pdf) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);   // ✅ esta línea faltaba
            helper.setTo(destinatario);
            helper.setSubject("Tu reserva " + codigo + " — Pacha Suite");
            helper.setText("Adjuntamos el comprobante de tu reserva <b>" + codigo + "</b>.", true);
            helper.addAttachment("reserva-" + codigo + ".pdf",
                    new ByteArrayResource(pdf), "application/pdf");
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Error enviando PDF", e);
        }
    }

    /**
     * Envía las credenciales de acceso temporal al portal de huésped.
     * Se dispara cuando una reserva pasa a estado "confirmada"
     * (ver GuestCreationService). La password se manda en texto plano
     * SOLO por este correo; nunca se devuelve en ninguna respuesta de la API.
     */
    public boolean enviarCredencialesGuest(String destinatario, String nombre,
                                           String passwordPlano, String codigoReserva,
                                           java.time.LocalDate checkOut) {
        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(destinatario);
            helper.setSubject("🔑 Acceso a tu reserva " + codigoReserva + " - Pacha Suite");

            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto'>"
                    + "<h2 style='background:#3B1F0E;color:#E8A265;padding:20px;margin:0'>Pacha Suite – Acceso de huésped</h2>"
                    + "<div style='padding:24px;border:1px solid #ddd'>"
                    + "<p>Hola <b>" + StringEscapeUtils.escapeHtml4(nombre != null && !nombre.isEmpty() ? nombre : "huésped") + "</b>,</p>"
                    + "<p>Tu reserva <b>" + StringEscapeUtils.escapeHtml4(codigoReserva) + "</b> ha sido confirmada. "
                    + "Hemos creado un acceso temporal para que puedas ver tu reserva y registrar tu vehículo en cochera.</p>"
                    + "<p><b>Usuario:</b> " + StringEscapeUtils.escapeHtml4(destinatario) + "</p>"
                    + "<p><b>Contraseña:</b> "
                    + "<span style='letter-spacing:2px;font-size:18px;color:#3B1F0E'>"
                    + StringEscapeUtils.escapeHtml4(passwordPlano) + "</span></p>"
                    + "<p style='color:#999;font-size:13px'>Este acceso es válido únicamente hasta el "
                    + checkOut + " (fecha de check-out). Después de esa fecha la cuenta se elimina automáticamente.</p>"
                    + "</div>"
                    + "<p style='text-align:center;font-size:12px;color:#999'>Pacha Suite · Puno, Perú</p>"
                    + "</div>";

            helper.setText(html, true);
            mailSender.send(mail);
            log.info("Credenciales de guest enviadas a {} (reserva {})", destinatario, codigoReserva);
            return true;

        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            log.error("Error enviando credenciales de guest a {}: {}", destinatario, e.getMessage());
            return false;
        }
    }
}