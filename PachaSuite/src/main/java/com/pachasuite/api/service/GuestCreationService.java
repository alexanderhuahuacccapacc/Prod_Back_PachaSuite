package com.pachasuite.api.service;

import com.pachasuite.api.entities.Reserva;
import com.pachasuite.api.entities.Usuario;
import com.pachasuite.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Crea la cuenta temporal (ROLE_GUEST) cuando una reserva pasa a "confirmada".
 *
 * Reglas (confirmadas con el usuario):
 * - El "usuario" para login es el gmail del titular de la reserva.
 * - La password se genera al azar y se envía por correo (nunca se devuelve en la API).
 * - La cuenta expira al checkOut de la reserva (fin del día de checkOut).
 * - Si el mismo email vuelve a confirmar otra reserva mientras aún tiene cuenta
 *   guest activa: se REEMPLAZA la password y se reenvía el correo, y la cuenta
 *   queda asociada a la nueva reserva (la expiración se actualiza al nuevo checkOut).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GuestCreationService {

    private static final String CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789"; // sin caracteres ambiguos
    private static final int PASSWORD_LENGTH = 10;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Punto de entrada: se llama desde ReservaService.confirmar().
     * email: el gmail del titular (lo provee el caller, ver nota en ReservaService).
     */
    @Transactional
    public void crearOActualizarGuest(Reserva reserva, String emailTitular, String nombreTitular) {
        if (emailTitular == null || emailTitular.isBlank()) {
            log.warn("Reserva {} confirmada sin email de titular; no se crea cuenta guest.",
                    reserva.getCodigo());
            return;
        }

        String passwordPlano = generarPasswordAleatoria();
        LocalDateTime expiracion = reserva.getCheckOut().atTime(LocalTime.MAX); // fin del día de checkout

        Optional<Usuario> existente = usuarioRepository.findByEmail(emailTitular);

        Usuario usuario;
        boolean esNuevo = existente.isEmpty();

        if (esNuevo) {
            usuario = Usuario.builder()
                    .email(emailTitular)
                    .nombre(nombreTitular != null ? nombreTitular : "Huésped")
                    .password(passwordEncoder.encode(passwordPlano))
                    .rol(Usuario.UsuarioRol.ROLE_GUEST)
                    .activo(true)
                    .expiraEn(expiracion)
                    .reserva(reserva)
                    .build();
        } else {
            usuario = existente.get();

            // Si el email ya existe pero es ADMIN/RECEPCIONISTA, no lo tocamos:
            // esto evita que alguien reserve con el correo de un usuario de staff
            // y termine pisando su cuenta.
            if (!usuario.esGuest()) {
                log.warn("Email {} ya pertenece a una cuenta de staff ({}). " +
                                "No se crea/sobreescribe cuenta guest para reserva {}.",
                        emailTitular, usuario.getRol(), reserva.getCodigo());
                return;
            }

            // Reemplazar password + reenviar correo + re-asociar a la nueva reserva
            // (regla confirmada: "reemplazar password y reenviar correo")
            usuario.setPassword(passwordEncoder.encode(passwordPlano));
            usuario.setExpiraEn(expiracion);
            usuario.setReserva(reserva);
            usuario.setActivo(true);
        }

        usuarioRepository.save(usuario);

        boolean enviado = emailService.enviarCredencialesGuest(
                emailTitular, nombreTitular, passwordPlano, reserva.getCodigo(), reserva.getCheckOut());

        if (!enviado) {
            // No revertimos la transacción por un fallo de correo: la cuenta ya quedó
            // creada/actualizada en BD. Si el correo falla, hay que loguearlo fuerte
            // para que alguien pueda regenerar/reenviar manualmente.
            log.error("Cuenta guest {} para reserva {} pero el correo de credenciales FALLÓ.",
                    esNuevo ? "creada" : "actualizada", reserva.getCodigo());
        }

        log.info("Cuenta GUEST {} para {} | reserva {} | expira {}",
                esNuevo ? "creada" : "actualizada", emailTitular, reserva.getCodigo(), expiracion);
    }

    private String generarPasswordAleatoria() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(CHARS.charAt(secureRandom.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}