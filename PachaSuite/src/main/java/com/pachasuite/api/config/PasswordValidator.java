package com.pachasuite.api.config;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class PasswordValidator {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])" +           // al menos un número
                    "(?=.*[a-z])" +            // al menos una minúscula
                    "(?=.*[A-Z])" +            // al menos una mayúscula
                    "(?=.*[@#$%^&+=!?¿¡])" +   // al menos un carácter especial
                    "(?=\\S+$)" +              // sin espacios
                    ".{8,}$"                   // mínimo 8 caracteres
    );

    private static final List<String> COMMON_PASSWORDS = List.of(
            "admin123", "password123", "12345678", "qwerty123",
            "abc123456", "admin1234", "root123", "12345abc"
    );

    public boolean isValid(String password) {
        if (password == null) return false;

        // Longitud mínima
        if (password.length() < 8) return false;

        // Patrón regex
        if (!PASSWORD_PATTERN.matcher(password).matches()) return false;

        // Verificar contra lista de passwords comunes
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) return false;

        return true;
    }

    public String getRequirements() {
        return "La contraseña debe tener:\n" +
                "  - Mínimo 8 caracteres\n" +
                "  - Al menos un número\n" +
                "  - Al menos una letra mayúscula\n" +
                "  - Al menos una letra minúscula\n" +
                "  - Al menos un carácter especial (@#$%^&+=!?¿¡)\n" +
                "  - Sin espacios en blanco";
    }
}