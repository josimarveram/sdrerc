package com.sdrerc.v3.security;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Port literal de com.sdrerc.infrastructure.security.EmailOtpMailer (V2), mismos nombres de
 * propiedad/variable de entorno ({@code mail.smtp.*} / {@code SDRERC_APP_SMTP_*}) para poder
 * reutilizar la misma configuracion SMTP ya operativa en V2. Unico cambio: logger SLF4J en vez
 * de Log4j2 directo (Spring Boot ya trae SLF4J+Logback por defecto, sin agregar una dependencia
 * de logging nueva solo para esta clase).
 */
public final class EmailOtpMailer {

    private static final Logger LOG = LoggerFactory.getLogger(EmailOtpMailer.class);

    private static final String CONFIG_FILE_NAME = "sdrerc-app.properties";

    private static final String PROP_HOST = "mail.smtp.host";
    private static final String PROP_PORT = "mail.smtp.port";
    private static final String PROP_USER = "mail.smtp.user";
    private static final String PROP_PASSWORD = "mail.smtp.password";
    private static final String PROP_FROM = "mail.smtp.from";

    private static final String ENV_HOST = "SDRERC_APP_SMTP_HOST";
    private static final String ENV_PORT = "SDRERC_APP_SMTP_PORT";
    private static final String ENV_USER = "SDRERC_APP_SMTP_USER";
    private static final String ENV_PASSWORD = "SDRERC_APP_SMTP_PASSWORD";
    private static final String ENV_FROM = "SDRERC_APP_SMTP_FROM";

    private static final String SYS_CONFIG_FILE = "sdrerc.app.config";
    private static final String SYS_CONFIG_DIR = "sdrerc.config.dir";

    private static final String ASUNTO = "Código de verificación SDRERC";

    private EmailOtpMailer() {
    }

    public static boolean estaConfigurado() {
        Properties config = cargarConfiguracionExterna();
        return trimToNull(resolveValue(config, PROP_HOST, ENV_HOST)) != null
                && trimToNull(resolveValue(config, PROP_USER, ENV_USER)) != null
                && trimToNull(resolveValue(config, PROP_PASSWORD, ENV_PASSWORD)) != null;
    }

    public static void requerirConfiguracion() {
        if (!estaConfigurado()) {
            throw new IllegalStateException(
                    "Debe configurar " + PROP_HOST + ", " + PROP_USER + " y " + PROP_PASSWORD
                            + " en config/" + CONFIG_FILE_NAME + " (o las variables de entorno "
                            + ENV_HOST + " / " + ENV_USER + " / " + ENV_PASSWORD
                            + ") para enviar el código de verificación por correo.");
        }
    }

    public static void enviarCodigo(String correoDestino, String nombreDestino, String codigo) {
        requerirConfiguracion();
        Properties config = cargarConfiguracionExterna();
        String host = resolveValue(config, PROP_HOST, ENV_HOST);
        String puerto = resolveValueConDefecto(config, PROP_PORT, ENV_PORT, "587");
        String usuario = resolveValue(config, PROP_USER, ENV_USER);
        String password = quitarEspacios(resolveValue(config, PROP_PASSWORD, ENV_PASSWORD));
        String remitente = resolveValue(config, PROP_FROM, ENV_FROM);
        if (remitente == null) {
            remitente = usuario;
        }

        Properties propiedadesSmtp = new Properties();
        propiedadesSmtp.put("mail.smtp.auth", "true");
        propiedadesSmtp.put("mail.smtp.starttls.enable", "true");
        propiedadesSmtp.put("mail.smtp.host", host);
        propiedadesSmtp.put("mail.smtp.port", puerto);
        propiedadesSmtp.put("mail.smtp.connectiontimeout", "10000");
        propiedadesSmtp.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(propiedadesSmtp, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(usuario, password);
            }
        });

        try {
            MimeMessage mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(remitente, "SDRERC"));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoDestino));
            mensaje.setSubject(ASUNTO);
            mensaje.setText(construirCuerpo(nombreDestino, codigo));
            Transport.send(mensaje);
        } catch (MessagingException | java.io.UnsupportedEncodingException ex) {
            LOG.error("Fallo el envio del codigo de verificacion por correo (host={}, puerto={}, usuario={})",
                    host, puerto, usuario, ex);
            throw new IllegalStateException(
                    "No se pudo enviar el código de verificación al correo registrado. "
                            + "Verifique la configuración SMTP o intente con otro método.", ex);
        }
    }

    private static String construirCuerpo(String nombreDestino, String codigo) {
        String saludo = (nombreDestino == null || nombreDestino.trim().isEmpty())
                ? "Hola"
                : "Hola, " + nombreDestino.trim();
        return saludo + ":\n\n"
                + "Tu código de verificación para ingresar a SDRERC es: " + codigo + "\n\n"
                + "Este código vence en 10 minutos y solo puede usarse una vez. "
                + "Si no intentaste iniciar sesión, ignora este mensaje.\n";
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
                    "No se pudo leer la configuración externa: " + configFile.getAbsolutePath(), ex);
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

    private static String resolveValue(Properties config, String propertyName, String envName) {
        String value = trimToNull(System.getProperty(propertyName));
        if (value != null) {
            return value;
        }
        value = trimToNull(System.getenv(envName));
        if (value != null) {
            return value;
        }
        return trimToNull(config.getProperty(propertyName));
    }

    private static String resolveValueConDefecto(Properties config, String propertyName, String envName, String defecto) {
        String value = resolveValue(config, propertyName, envName);
        return value != null ? value : defecto;
    }

    /**
     * Las contraseñas de aplicación de Gmail se muestran agrupadas en bloques de 4 caracteres
     * separados por espacios (ej. "abcd efgh ijkl mnop") solo para que se lean fácil; el valor
     * real no tiene espacios. Se quitan todos (no solo los de los extremos) para que la
     * autenticación SMTP no falle si se pegó tal cual la muestra Google.
     */
    private static String quitarEspacios(String value) {
        return value == null ? null : value.replaceAll("\\s+", "");
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
