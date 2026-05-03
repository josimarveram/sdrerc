package com.sdrerc.infrastructure.security;

import java.security.SecureRandom;

public final class PasswordGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIAL = "@#$%&*!";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;

    private PasswordGenerator() {
    }

    public static String generateTemporaryPassword(String username) {
        while (true) {
            String password = buildPassword();
            try {
                PasswordPolicy.validateTemporaryPassword(username, password);
                return password;
            } catch (IllegalArgumentException ignored) {
                // Generate another candidate.
            }
        }
    }

    private static String buildPassword() {
        char[] chars = new char[12];
        chars[0] = randomChar(UPPER);
        chars[1] = randomChar(LOWER);
        chars[2] = randomChar(DIGITS);
        chars[3] = randomChar(SPECIAL);
        for (int i = 4; i < chars.length; i++) {
            chars[i] = randomChar(ALL);
        }
        shuffle(chars);
        return new String(chars);
    }

    private static char randomChar(String source) {
        return source.charAt(RANDOM.nextInt(source.length()));
    }

    private static void shuffle(char[] chars) {
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
    }
}
