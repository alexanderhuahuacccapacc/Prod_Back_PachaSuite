package com.pachasuite.api.config;

import com.pachasuite.api.entities.Usuario;
import com.pachasuite.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;

    private static final String ADMIN_EMAIL  = "admin@pachasuite.com";
    private static final String ADMIN_NOMBRE = "Administrador Pacha";

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        upsertAdmin();
    }

    private void upsertAdmin() {
        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException("Variable ADMIN_PASSWORD no configurada");
        }

        usuarioRepository.findByEmail(ADMIN_EMAIL).ifPresentOrElse(
                admin -> {
                    String nuevoHash = passwordEncoder.encode(adminPassword);
                    admin.setPassword(nuevoHash);
                    admin.setRol(Usuario.UsuarioRol.ROLE_ADMIN);
                    admin.setActivo(true);
                    usuarioRepository.save(admin);
                    log.info("Admin actualizado: {}", ADMIN_EMAIL);
                },
                () -> {
                    Usuario admin = Usuario.builder()
                            .email(ADMIN_EMAIL)
                            .password(passwordEncoder.encode(adminPassword))
                            .nombre(ADMIN_NOMBRE)
                            .rol(Usuario.UsuarioRol.ROLE_ADMIN)
                            .activo(true)
                            .build();
                    usuarioRepository.save(admin);
                    log.info("Admin creado: {}", ADMIN_EMAIL);
                }
        );
    }
}