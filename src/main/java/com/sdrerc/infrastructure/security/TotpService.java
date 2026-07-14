package com.sdrerc.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Genera y valida codigos TOTP (RFC 6238) usando unicamente JDK estandar
 * (HMAC-SHA1 via javax.crypto.Mac), sin depender de librerias externas.
 */
public final class TotpService {

    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int SECRET_BYTES = 20;
    private static final int STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final int WINDOW_TOLERANCE_STEPS = 1;
    private static final char[] BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    private TotpService() {
    }

    public static String generarSecreto() {
        byte[] buffer = new byte[SECRET_BYTES];
        new SecureRandom().nextBytes(buffer);
        return base32Encode(buffer);
    }

    public static String construirUriEnrolamiento(String secretoBase32, String username, String emisor) {
        String label = urlEncode(emisor) + ":" + urlEncode(username);
        return "otpauth://totp/" + label
                + "?secret=" + secretoBase32
                + "&issuer=" + urlEncode(emisor)
                + "&algorithm=SHA1"
                + "&digits=" + CODE_DIGITS
                + "&period=" + STEP_SECONDS;
    }

    public static boolean validarCodigo(String secretoBase32, String codigoIngresado) {
        if (secretoBase32 == null || codigoIngresado == null) {
            return false;
        }
        String codigoNormalizado = codigoIngresado.trim();
        if (!codigoNormalizado.matches("\\d{" + CODE_DIGITS + "}")) {
            return false;
        }
        long pasoActual = System.currentTimeMillis() / 1000L / STEP_SECONDS;
        byte[] secreto = base32Decode(secretoBase32);
        for (int desvio = -WINDOW_TOLERANCE_STEPS; desvio <= WINDOW_TOLERANCE_STEPS; desvio++) {
            String codigoEsperado = generarCodigo(secreto, pasoActual + desvio);
            if (codigoEsperado.equals(codigoNormalizado)) {
                return true;
            }
        }
        return false;
    }

    private static String generarCodigo(byte[] secreto, long contadorPaso) {
        byte[] data = new byte[8];
        long valor = contadorPaso;
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (valor & 0xFF);
            valor >>= 8;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secreto, HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binario = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int codigo = binario % (int) Math.pow(10, CODE_DIGITS);
            return String.format("%0" + CODE_DIGITS + "d", codigo);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("No se pudo calcular el codigo TOTP.", ex);
        }
    }

    private static String urlEncode(String value) {
        StringBuilder sb = new StringBuilder();
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            char c = (char) (b & 0xFF);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                    || c == '-' || c == '_' || c == '.' || c == '~') {
                sb.append(c);
            } else {
                sb.append('%').append(String.format("%02X", b & 0xFF));
            }
        }
        return sb.toString();
    }

    private static String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int bitBuffer = 0;
        int bitsEnBuffer = 0;
        for (byte b : data) {
            bitBuffer = (bitBuffer << 8) | (b & 0xFF);
            bitsEnBuffer += 8;
            while (bitsEnBuffer >= 5) {
                int indice = (bitBuffer >> (bitsEnBuffer - 5)) & 0x1F;
                sb.append(BASE32_ALPHABET[indice]);
                bitsEnBuffer -= 5;
            }
        }
        if (bitsEnBuffer > 0) {
            int indice = (bitBuffer << (5 - bitsEnBuffer)) & 0x1F;
            sb.append(BASE32_ALPHABET[indice]);
        }
        return sb.toString();
    }

    private static byte[] base32Decode(String encoded) {
        String limpio = encoded.trim().toUpperCase(java.util.Locale.ROOT).replace("=", "");
        int bitBuffer = 0;
        int bitsEnBuffer = 0;
        java.io.ByteArrayOutputStream salida = new java.io.ByteArrayOutputStream();
        for (char c : limpio.toCharArray()) {
            int indice = indexOfBase32(c);
            if (indice < 0) {
                continue;
            }
            bitBuffer = (bitBuffer << 5) | indice;
            bitsEnBuffer += 5;
            if (bitsEnBuffer >= 8) {
                salida.write((bitBuffer >> (bitsEnBuffer - 8)) & 0xFF);
                bitsEnBuffer -= 8;
            }
        }
        return salida.toByteArray();
    }

    private static int indexOfBase32(char c) {
        for (int i = 0; i < BASE32_ALPHABET.length; i++) {
            if (BASE32_ALPHABET[i] == c) {
                return i;
            }
        }
        return -1;
    }
}
