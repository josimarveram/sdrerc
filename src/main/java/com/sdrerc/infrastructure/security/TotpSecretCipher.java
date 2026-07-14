package com.sdrerc.infrastructure.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Cifra/descifra el secreto TOTP con AES-256-GCM antes de persistirlo en
 * {@code usuario.totp_secret}. El secreto TOTP debe ser reversible (no se
 * puede hashear como una contraseña) para poder generar y comparar codigos,
 * por eso se cifra en vez de guardarse en claro.
 *
 * <p>La clave se resuelve con el mismo patron de configuracion externa ya
 * usado por {@code SdrercAppConnection} para las credenciales de BD:
 * propiedad {@code security.totp.key} en {@code config/sdrerc-app.properties},
 * o variable de entorno {@code SDRERC_APP_TOTP_KEY}. Nunca hardcodeada en el
 * fuente. Cualquier passphrase configurada se deriva a una clave AES-256 via
 * SHA-256, para no exigir un formato exacto de clave.</p>
 */
public final class TotpSecretCipher {

    private static final String CONFIG_FILE_NAME = "sdrerc-app.properties";
    private static final String PROP_KEY = "security.totp.key";
    private static final String PROP_KEY_ALIAS = "sdrerc.totp.key";
    private static final String ENV_KEY = "SDRERC_APP_TOTP_KEY";
    private static final String ENV_KEY_ALIAS = "SDRERC_TOTP_KEY";
    private static final String SYS_CONFIG_FILE = "sdrerc.app.config";
    private static final String SYS_CONFIG_DIR = "sdrerc.config.dir";

    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;

    private TotpSecretCipher() {
    }

    public static String cifrar(String textoPlano) {
        try {
            byte[] iv = new byte[GCM_IV_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, resolveClaveAes(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] textoCifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + textoCifrado.length);
            buffer.put(iv);
            buffer.put(textoCifrado);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo cifrar el secreto TOTP.", ex);
        }
    }

    public static String descifrar(String valorCifrado) {
        try {
            byte[] datos = Base64.getDecoder().decode(valorCifrado);
            ByteBuffer buffer = ByteBuffer.wrap(datos);
            byte[] iv = new byte[GCM_IV_BYTES];
            buffer.get(iv);
            byte[] textoCifrado = new byte[buffer.remaining()];
            buffer.get(textoCifrado);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, resolveClaveAes(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(textoCifrado), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo descifrar el secreto TOTP.", ex);
        }
    }

    public static boolean estaConfigurado() {
        String passphrase = resolvePassphrase();
        return passphrase != null && !passphrase.trim().isEmpty();
    }

    public static void requerirConfiguracion() {
        if (!estaConfigurado()) {
            throw new IllegalStateException(
                    "Debe configurar " + PROP_KEY + " (o " + PROP_KEY_ALIAS + ") en config/" + CONFIG_FILE_NAME
                            + ", o la variable de entorno " + ENV_KEY + " / " + ENV_KEY_ALIAS
                            + " para cifrar/descifrar secretos TOTP.");
        }
    }

    private static SecretKeySpec resolveClaveAes() {
        requerirConfiguracion();
        String passphrase = resolvePassphrase();
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] claveDerivada = sha256.digest(passphrase.trim().getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(claveDerivada, AES_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No se pudo derivar la clave de cifrado TOTP.", ex);
        }
    }

    private static String resolvePassphrase() {
        String value = trimToNull(System.getProperty(PROP_KEY));
        if (value == null) {
            value = trimToNull(System.getProperty(PROP_KEY_ALIAS));
        }
        if (value != null) {
            return value;
        }

        value = trimToNull(System.getenv(ENV_KEY));
        if (value == null) {
            value = trimToNull(System.getenv(ENV_KEY_ALIAS));
        }
        if (value != null) {
            return value;
        }

        Properties config = cargarConfiguracionExterna();
        value = trimToNull(config.getProperty(PROP_KEY));
        if (value != null) {
            return value;
        }
        return trimToNull(config.getProperty(PROP_KEY_ALIAS));
    }

    private static Properties cargarConfiguracionExterna() {
        Properties properties = new Properties();
        File configFile = resolveConfigFile();
        if (configFile == null || !configFile.isFile()) {
            return properties;
        }
        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo leer la configuracion externa: " + configFile.getAbsolutePath(), ex);
        }
        return properties;
    }

    private static File resolveConfigFile() {
        String explicitFile = trimToNull(System.getProperty(SYS_CONFIG_FILE));
        if (explicitFile == null) {
            explicitFile = trimToNull(System.getenv("SDRERC_APP_CONFIG"));
        }
        if (explicitFile != null) {
            return new File(explicitFile);
        }

        String configDir = trimToNull(System.getProperty(SYS_CONFIG_DIR));
        if (configDir != null) {
            return new File(configDir, CONFIG_FILE_NAME);
        }

        return new File("config", CONFIG_FILE_NAME);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
