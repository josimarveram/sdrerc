package com.sdrerc.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class SdrercAppConnection {

    private static final String DEFAULT_URL = "jdbc:oracle:thin:@localhost:1521/xe";
    private static final String DEFAULT_USER = "SDRERC_APP";
    private static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";

    private static final String ENV_URL = "SDRERC_APP_DB_URL";
    private static final String ENV_USER = "SDRERC_APP_DB_USER";
    private static final String ENV_PASSWORD = "SDRERC_APP_DB_PASSWORD";

    private SdrercAppConnection() {
    }

    public static Connection getConnection() throws SQLException {
        cargarDriverOracle();
        String url = getEnvOrDefault(ENV_URL, DEFAULT_URL);
        String user = getEnvOrDefault(ENV_USER, DEFAULT_USER);
        String password = System.getenv(ENV_PASSWORD);

        // TODO: externalizar a properties/env de forma centralizada para todos los ambientes.
        if (password == null || password.trim().isEmpty()) {
            throw new SQLException("Debe configurar la variable de entorno " + ENV_PASSWORD + " para conectar a SDRERC_APP.");
        }

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

    private static String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
}
