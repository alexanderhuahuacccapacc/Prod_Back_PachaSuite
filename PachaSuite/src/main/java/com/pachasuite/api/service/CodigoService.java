package com.pachasuite.api.service;

import com.pachasuite.api.dto.VerificacionRequestDTO;
import com.pachasuite.api.entities.CodigoVerificacion;
import com.pachasuite.api.entities.VerificacionContacto;
import com.pachasuite.api.exception.BadRequestException;
import com.pachasuite.api.repository.CodigoVerificacionRepository;
import com.pachasuite.api.repository.VerificacionContactoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CodigoService {

    @Value("${app.mail.expiracion-minutos:10}")
    private int expiracionMinutos;

    private final CodigoVerificacionRepository codigoRepo;
    private final VerificacionContactoRepository contactoRepo;
    private final EmailService emailService;

    private final Random random = new Random();

    public void generarYEnviar(String email, String nombre) {

        String emailNormalizado = email.trim().toLowerCase();

        VerificacionContacto contacto = contactoRepo
                .findByTipoAndValor(VerificacionContacto.TipoContacto.EMAIL, emailNormalizado)
                .orElseGet(() -> contactoRepo.save(
                        VerificacionContacto.builder()
                                .tipo(VerificacionContacto.TipoContacto.EMAIL)
                                .valor(emailNormalizado)
                                .build()
                ));

        LocalDateTime hace5min = LocalDateTime.now(ZoneOffset.UTC).minusMinutes(5);
        long recientes = codigoRepo.countByContactoIdAndCreatedAtAfter(contacto.getId(), hace5min);

        if (recientes >= 3) {
            throw new BadRequestException("Demasiados intentos. Por favor espera 5 minutos.");
        }

        String codigo = String.format("%06d", random.nextInt(1000000));

        CodigoVerificacion cv = CodigoVerificacion.builder()
                .contacto(contacto)
                .codigo(codigo)
                .metodo(CodigoVerificacion.MetodoVerificacion.EMAIL)
                .expiraEn(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(expiracionMinutos))
                .build();

        codigoRepo.save(cv);

        log.info("Código generado para {} → {}", email, codigo);

        boolean enviado = emailService.enviarCodigoVerificacion(email, codigo, nombre != null ? nombre : "");

        if (!enviado) {
            throw new BadRequestException("No se pudo enviar el código de verificación por email.");
        }
    }

    @Transactional(readOnly = true)
    public CodigoVerificacion validar(String contacto, String codigo) {

        String contactoNormalizado = contacto.trim().toLowerCase();

        log.debug("Validando código para: {} | ahora UTC: {}",
                contactoNormalizado, LocalDateTime.now(ZoneOffset.UTC));

        return codigoRepo.findValidCode(
                        contactoNormalizado,
                        codigo,
                        LocalDateTime.now(ZoneOffset.UTC)
                )
                .orElseThrow(() -> new BadRequestException(
                        "Código inválido, expirado o ya usado."));
    }

    @Transactional
    public void marcarUsado(CodigoVerificacion cv) {
        cv.setUsado(true);
        codigoRepo.save(cv);
    }
}