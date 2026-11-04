package com.sdrerc.v3.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Port literal de com.sdrerc.infrastructure.security.PasswordEncoder (V2).
 * Sin cambios de logica: mismo BCrypt, misma libreria/version (jbcrypt 0.4).
 */
public final class PasswordEncoder {

    private PasswordEncoder() {
    }

    public static String hash(String pass) {
        return BCrypt.hashpw(pass, BCrypt.gensalt());
    }

    public static boolean matches(String pass, String hash) {
        return BCrypt.checkpw(pass, hash);
    }
}
