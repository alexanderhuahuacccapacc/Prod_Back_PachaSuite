package com.pachasuite.api.controller;

import com.pachasuite.api.dto.HabitacionDTO;
import com.pachasuite.api.dto.MessageDTO;
import com.pachasuite.api.dto.VerificacionRequestDTO;
import com.pachasuite.api.service.CodigoService;
import com.pachasuite.api.service.HabitacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "Público", description = "Endpoints accesibles sin autenticación")
public class PublicController {

    private final HabitacionService habitacionService;
    private final CodigoService codigoService;

    @GetMapping("/habitaciones")
    public ResponseEntity<List<HabitacionDTO>> getAllHabitaciones() {
        List<HabitacionDTO> habitaciones = habitacionService.findAll();
        return ResponseEntity.ok(habitaciones);
    }

    @Value("${recaptcha.secret-key:DESACTIVADO}")
    private String recaptchaSecretKey;

    private boolean verificarCaptcha(String token) {
        if (recaptchaSecretKey.equals("DESACTIVADO") || token == null || token.isBlank()) {
            return true;
        }
        try {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.ofSeconds(3))
                    .setResponseTimeout(Timeout.ofSeconds(5))
                    .build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .build();

            HttpComponentsClientHttpRequestFactory factory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);

            RestTemplate rt = new RestTemplate(factory);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret",   recaptchaSecretKey);
            params.add("response", token);

            Map response = rt.postForObject(
                    "https://www.google.com/recaptcha/api/siteverify",
                    params,
                    Map.class
            );

            boolean success = (boolean) response.get("success");
            double score = ((Number) response.get("score")).doubleValue();
            return success && score >= 0.5;

        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/habitaciones/disponibles")
    @Operation(summary = "Buscar habitaciones disponibles")
    public ResponseEntity<List<HabitacionDTO>> disponibles(
            @Parameter(description = "Fecha de check-in (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @Parameter(description = "Fecha de check-out (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @Parameter(description = "Número de adultos (mínimo 1)")
            @RequestParam(defaultValue = "1") int adultos,
            @Parameter(description = "Número de niños")
            @RequestParam(defaultValue = "0") int ninos
    ) {
        return ResponseEntity.ok(habitacionService.buscarDisponibles(checkIn, checkOut, adultos, ninos));
    }

    // ==================== VERIFICACIÓN SOLO EMAIL ====================
    @PostMapping("/verificacion/enviar")
    @Operation(summary = "Enviar código de verificación por Email")
    public ResponseEntity<MessageDTO> enviarCodigoEmail(
            @Valid @RequestBody VerificacionRequestDTO dto) {

        try {
            if (!verificarCaptcha(dto.getCaptchaToken())) {
                return ResponseEntity.badRequest()
                        .body(MessageDTO.of("Verificación de seguridad fallida. Recarga la página e intenta de nuevo."));
            }

            codigoService.generarYEnviar(dto.getContacto(), dto.getNombre());

            return ResponseEntity.ok(
                    MessageDTO.of("Código de verificación enviado correctamente por email")
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    MessageDTO.of("Ha ocurrido un error. Intente más tarde")
            );
        }
    }

}