package com.pachasuite.api.security;

import com.pachasuite.api.entities.Usuario;
import com.pachasuite.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * IMPORTANTE: si en tu proyecto ya existe una clase equivalente con OTRO nombre
 * (la que hoy inyecta JwtFilter y SecurityConfig), NO agregues esta como una clase
 * paralela — reemplázala o fusiona esta lógica de expiración dentro de la que ya
 * tienes. Tener dos implementaciones de UserDetailsService activas rompe la
 * inyección de dependencias (Spring no sabrá cuál usar).
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return new UsuarioUserDetails(usuario);
    }

    /**
     * Wrapper de Usuario como UserDetails de Spring Security.
     * La pieza clave es isAccountNonExpired(): para un GUEST, se evalúa en VIVO
     * contra expiraEn cada vez que el JwtFilter llama a loadUserByUsername(),
     * es decir, en CADA request autenticado. Esto da expiración inmediata sin
     * depender de que el scheduler de limpieza ya haya corrido.
     */
    public static class UsuarioUserDetails implements UserDetails {

        private final Usuario usuario;

        public UsuarioUserDetails(Usuario usuario) {
            this.usuario = usuario;
        }

        public Usuario getUsuario() {
            return usuario;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority(usuario.getRol().name()));
        }

        @Override
        public String getPassword() {
            return usuario.getPassword();
        }

        @Override
        public String getUsername() {
            return usuario.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            if (usuario.esGuest()) {
                if (usuario.getExpiraEn() == null) return false; // guest sin fecha = inválido por seguridad
                return LocalDateTime.now().isBefore(usuario.getExpiraEn());
            }
            return true; // ADMIN / RECEPCIONISTA no expiran por tiempo
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return Boolean.TRUE.equals(usuario.getActivo());
        }
    }
}