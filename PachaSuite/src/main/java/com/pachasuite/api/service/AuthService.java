package com.pachasuite.api.service;

import com.pachasuite.api.dto.JwtDTO;
import com.pachasuite.api.dto.LoginDTO;
import com.pachasuite.api.dto.RegisterDTO;
import com.pachasuite.api.entities.Usuario;
import com.pachasuite.api.exception.BadRequestException;
import com.pachasuite.api.repository.UsuarioRepository;
import com.pachasuite.api.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtProvider jwtProvider;

    @Transactional
    public void register(RegisterDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("El email ya está registrado: " + dto.getEmail());
        }

        Usuario usuario = Usuario.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nombre(dto.getNombre())
                .rol(Usuario.UsuarioRol.ROLE_CLIENT)
                .activo(true)
                .build();

        usuarioRepository.save(usuario);
        log.info("Usuario registrado: {}", dto.getEmail());
    }

    @Transactional(readOnly = true)
    public JwtDTO login(LoginDTO dto) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String token = jwtProvider.generateToken(auth);

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow();

        log.info("Login exitoso: {}", dto.getEmail());
        return JwtDTO.of(token, usuario.getEmail(), usuario.getNombre(), usuario.getRol().name());
    }
}