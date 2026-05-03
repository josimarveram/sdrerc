package com.sdrerc.infrastructure.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class PasswordPolicy {

    private static final Set<String> WEAK_PASSWORDS = new HashSet<>(Arrays.asList(
            "123456",
            "12345678",
            "password",
            "admin",
            "usuario",
            "qwerty",
            "reniec",
            "reniec@2026"
    ));

    private PasswordPolicy() {
    }

    public static void validateTemporaryPassword(String username, String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (!containsUppercase(password)) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos una mayúscula.");
        }
        if (!containsLowercase(password)) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos una minúscula.");
        }
        if (!containsDigit(password)) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos un número.");
        }
        if (!containsSpecial(password)) {
            throw new IllegalArgumentException("La contraseña debe incluir al menos un carácter especial.");
        }

        String normalizedPassword = normalize(password);
        String normalizedUsername = normalize(username);

        if (!normalizedUsername.isEmpty() && normalizedPassword.equals(normalizedUsername)) {
            throw new IllegalArgumentException("La contraseña no puede ser igual al usuario.");
        }
        if (!normalizedUsername.isEmpty() && normalizedPassword.contains(normalizedUsername)) {
            throw new IllegalArgumentException("La contraseña no puede contener el usuario.");
        }
        if (WEAK_PASSWORDS.contains(normalizedPassword)) {
            throw new IllegalArgumentException("La contraseña no cumple la política de seguridad.");
        }
    }

    private static boolean containsUppercase(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isUpperCase(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsLowercase(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isLowerCase(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsDigit(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsSpecial(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
