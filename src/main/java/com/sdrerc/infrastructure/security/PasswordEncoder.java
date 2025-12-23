/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.security;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author David
 */
public class PasswordEncoder {
    private PasswordEncoder() {
        // Evita instanciación
    }

    public static String hash(String pass) {
        return BCrypt.hashpw(pass, BCrypt.gensalt());
    }

    public static boolean matches(String pass, String hash) {
        return BCrypt.checkpw(pass, hash);
    }
}
