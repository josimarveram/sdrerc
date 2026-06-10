package com.sdrerc.infrastructure.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class SdrercAppConnection {

    private static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
    private static final String CONFIG_FILE_NAME = "sdrerc-app.properties";

    private static final String ENV_URL = "SDRERC_APP_DB_URL";
    private static final String ENV_USER = "SDRERC_APP_DB_USER";
    private static final String ENV_PASSWORD = "SDRERC_APP_DB_PASSWORD";
    private static final String ENV_CONFIG_FILE = "SDRERC_APP_CONFIG";

    private static final String SYS_CONFIG_FILE = "sdrerc.app.config";
    private static final String SYS_CONFIG_DIR = "sdrerc.config.dir";

    private static final String PROP_URL = "db.url";
    private static final String PROP_USER = "db.user";
    private static final String PROP_PASSWORD = "db.password";

    private SdrercAppConnection() {
    }

    public static Connection getConnection() throws SQLException {
        cargarDriverOracle();
        Properties config = cargarConfiguracionExterna();
        String url = resolveValue(config, PROP_URL, ENV_URL);
        String user = resolveValue(config, PROP_USER, ENV_USER);
        String password = resolveValue(config, PROP_PASSWORD, ENV_PASSWORD);

        requireConfigured(PROP_URL, ENV_URL, url);
        requireConfigured(PROP_USER, ENV_USER, user);
        requireConfigured(PROP_PASSWORD, ENV_PASSWORD, password);

        return DriverManager.getConnection(url, user, password);
    }

    private static void cargarDriverOracle() throws SQLException {
        try {
            Class.forName(ORACLE_DRIVER);
        } catch (ClassNotFoundException ex) {
            throw new SQLException(
                    "No se encontro el driver JDBC de Oracle en el classpath. Verifique que ojdbc este incluido.",
                    ex);
        }
    }

    private static Properties cargarConfiguracionExterna() throws SQLException {
        Properties properties = new Properties();
        File configFile = resolveConfigFile();
        if (configFile == null || !configFile.isFile()) {
            return properties;
        }

        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
            return properties;
        } catch (IOException ex) {
            throw new SQLException(
                    "No se pudo leer la configuracion externa de SDRERC_APP: " + configFile.getAbsolutePath(),
                    ex);
        }
    }

    private static File resolveConfigFile() {
        String explicitFile = firstValue(System.getProperty(SYS_CONFIG_FILE), System.getenv(ENV_CONFIG_FILE));
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

    private static void requireConfigured(String propertyName, String envName, String value) throws SQLException {
        if (value == null) {
            throw new SQLException(
                    "Debe configurar " + propertyName + " en config/" + CONFIG_FILE_NAME
                            + " o la variable de entorno " + envName + " para conectar a SDRERC_APP.");
        }
    }

    private static String firstValue(String first, String second) {
        String value = trimToNull(first);
        if (value != null) {
            return value;
        }
        return trimToNull(second);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
