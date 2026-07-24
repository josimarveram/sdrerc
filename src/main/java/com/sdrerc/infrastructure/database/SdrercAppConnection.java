package com.sdrerc.infrastructure.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fuente unica de conexiones JDBC a SDRERC_APP para toda la aplicacion. Desde esta clase se
 * obtiene siempre una conexion ya administrada por un pool ({@link HikariDataSource}), nunca una
 * conexion cruda de {@link java.sql.DriverManager}: abrir/cerrar una conexion fisica por cada
 * consulta (una por cada {@code try (Connection conn = SdrercAppConnection.getConnection())} de
 * cada DAO, que es el patron usado en todo el proyecto) es costoso bajo uso concurrente real
 * (handshake TCP + autenticacion + creacion de sesion Oracle en cada llamada) y agota rapido el
 * limite de sesiones concurrentes de Oracle. El pool reutiliza conexiones ya abiertas entre
 * llamadas, sin que ningun DAO existente tenga que cambiar: {@code Connection.close()} sobre una
 * conexion del pool simplemente la devuelve al pool en vez de cerrarla fisicamente.
 *
 * <p>Tamaño de pool pensado para un cliente de escritorio individual (esta app es cliente-servidor
 * LAN, cada usuario corre su propia instancia del JAR, no hay un servidor de aplicaciones
 * compartido) — no para un pool compartido entre todos los usuarios de la LAN.</p>
 */
public final class SdrercAppConnection {

    private static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
    private static final String CONFIG_FILE_NAME = "sdrerc-app.properties";

    private static final String ENV_URL = "SDRERC_APP_DB_URL";
    private static final String ENV_USER = "SDRERC_APP_DB_USER";
    private static final String ENV_PASSWORD = "SDRERC_APP_DB_PASSWORD";
    private static final String ENV_URL_ALIAS = "SDRERC_APP_URL";
    private static final String ENV_USER_ALIAS = "SDRERC_APP_USER";
    private static final String ENV_PASSWORD_ALIAS = "SDRERC_APP_PASSWORD";
    private static final String ENV_CONFIG_FILE = "SDRERC_APP_CONFIG";

    private static final String SYS_CONFIG_FILE = "sdrerc.app.config";
    private static final String SYS_CONFIG_DIR = "sdrerc.config.dir";

    private static final String PROP_URL = "db.url";
    private static final String PROP_USER = "db.user";
    private static final String PROP_PASSWORD = "db.password";
    private static final String PROP_URL_ALIAS = "sdrerc.db.url";
    private static final String PROP_USER_ALIAS = "sdrerc.db.user";
    private static final String PROP_PASSWORD_ALIAS = "sdrerc.db.password";

    private static final String PROP_POOL_MAX_SIZE = "db.pool.maxSize";
    private static final String PROP_POOL_MIN_IDLE = "db.pool.minIdle";
    private static final String ENV_POOL_MAX_SIZE = "SDRERC_APP_DB_POOL_MAX_SIZE";
    private static final String ENV_POOL_MIN_IDLE = "SDRERC_APP_DB_POOL_MIN_IDLE";
    private static final int DEFAULT_POOL_MAX_SIZE = 5;
    private static final int DEFAULT_POOL_MIN_IDLE = 1;

    private static final Pattern ORACLE_THIN_URL = Pattern.compile(
            "jdbc:oracle:thin:@(?://)?([^:/]+):(\\d+)/(\\S+)",
            Pattern.CASE_INSENSITIVE);

    /**
     * {@code volatile} + double-checked locking en vez del idiom "static holder": si Oracle no
     * esta disponible en el primer intento y la creacion del pool falla, {@code dataSource} se
     * queda en {@code null} y la siguiente llamada a {@link #getConnection()} vuelve a intentar
     * crearlo. Con un holder estatico, esa primera falla dejaria la clase marcada como
     * inicializada-con-error para siempre (JVM lanza {@code NoClassDefFoundError} en todo acceso
     * posterior), impidiendo conectar por el resto de la sesion aunque Oracle vuelva a estar
     * disponible despues.
     */
    private static volatile HikariDataSource dataSource;

    private SdrercAppConnection() {
    }

    public static Connection getConnection() throws SQLException {
        HikariDataSource ds = obtenerDataSource();
        try {
            return ds.getConnection();
        } catch (SQLException ex) {
            throw traducirErrorConexion(ds.getJdbcUrl(), ex);
        }
    }

    private static HikariDataSource obtenerDataSource() throws SQLException {
        HikariDataSource ds = dataSource;
        if (ds != null) {
            return ds;
        }
        synchronized (SdrercAppConnection.class) {
            ds = dataSource;
            if (ds == null) {
                ds = crearDataSource();
                dataSource = ds;
            }
            return ds;
        }
    }

    private static HikariDataSource crearDataSource() throws SQLException {
        cargarDriverOracle();
        Properties config = cargarConfiguracionExterna();
        String url = resolveValue(config, PROP_URL, PROP_URL_ALIAS, ENV_URL, ENV_URL_ALIAS);
        String user = resolveValue(config, PROP_USER, PROP_USER_ALIAS, ENV_USER, ENV_USER_ALIAS);
        String password = resolveValue(config, PROP_PASSWORD, PROP_PASSWORD_ALIAS, ENV_PASSWORD, ENV_PASSWORD_ALIAS);

        requireConfigured(PROP_URL, PROP_URL_ALIAS, ENV_URL, ENV_URL_ALIAS, url);
        requireConfigured(PROP_USER, PROP_USER_ALIAS, ENV_USER, ENV_USER_ALIAS, user);
        requireConfigured(PROP_PASSWORD, PROP_PASSWORD_ALIAS, ENV_PASSWORD, ENV_PASSWORD_ALIAS, password);

        int poolMaxSize = resolveIntValue(config, PROP_POOL_MAX_SIZE, ENV_POOL_MAX_SIZE, DEFAULT_POOL_MAX_SIZE);
        int poolMinIdle = resolveIntValue(config, PROP_POOL_MIN_IDLE, ENV_POOL_MIN_IDLE, DEFAULT_POOL_MIN_IDLE);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("SdrercAppPool");
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.setDriverClassName(ORACLE_DRIVER);
        hikariConfig.setMaximumPoolSize(poolMaxSize);
        hikariConfig.setMinimumIdle(poolMinIdle);
        // No falla al crear el pool si Oracle no esta disponible en ese instante (ej. app recien
        // abierta antes de que el servidor levante); el error real se reporta recien en el primer
        // getConnection(), igual que el comportamiento anterior con DriverManager.
        hikariConfig.setInitializationFailTimeout(-1);

        try {
            HikariDataSource ds = new HikariDataSource(hikariConfig);
            Runtime.getRuntime().addShutdownHook(new Thread(ds::close, "SdrercAppPool-shutdown"));
            return ds;
        } catch (RuntimeException ex) {
            SQLException causaSql = buscarCausaSql(ex);
            if (causaSql != null) {
                throw causaSql;
            }
            throw new SQLException("No se pudo inicializar el pool de conexiones a SDRERC_APP.", ex);
        }
    }

    private static SQLException buscarCausaSql(Throwable ex) {
        Throwable actual = ex;
        while (actual != null) {
            if (actual instanceof SQLException) {
                return (SQLException) actual;
            }
            actual = actual.getCause();
        }
        return null;
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

    private static SQLException traducirErrorConexion(String url, SQLException ex) {
        if (!esErrorRedOracle(ex)) {
            return ex;
        }

        String destino = describirDestino(url);
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("No se pudo establecer conexion con Oracle SDRERC_APP");
        if (destino != null) {
            mensaje.append(" en ").append(destino);
        }
        mensaje.append(". Verifique que el servidor Oracle este encendido, que el listener escuche el puerto 1521, ")
                .append("que la IP configurada sea la IP actual del servidor y que el firewall permita la conexion. ")
                .append("Configuracion usada: ").append(resolveConfigFile().getPath()).append(". ")
                .append("Prueba sugerida: Test-NetConnection <IP_SERVIDOR> -Port 1521.");
        return new SQLException(mensaje.toString(), ex.getSQLState(), ex.getErrorCode(), ex);
    }

    private static boolean esErrorRedOracle(SQLException ex) {
        SQLException current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (current instanceof SQLRecoverableException) {
                return true;
            }
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("network adapter")
                        || lower.contains("io error")
                        || lower.contains("error de e/s")
                        || lower.contains("connection refused")
                        || lower.contains("the network adapter could not establish the connection")) {
                    return true;
                }
            }
            current = current.getNextException();
        }
        return false;
    }

    private static String describirDestino(String url) {
        if (url == null) {
            return null;
        }
        Matcher matcher = ORACLE_THIN_URL.matcher(url.trim());
        if (!matcher.matches()) {
            return url;
        }
        return matcher.group(1) + ":" + matcher.group(2) + "/" + matcher.group(3);
    }

    private static String resolveValue(Properties config, String propertyName, String propertyAlias, String envName, String envAlias) {
        String value = trimToNull(System.getProperty(propertyName));
        if (value == null) {
            value = trimToNull(System.getProperty(propertyAlias));
        }
        if (value != null) {
            return value;
        }

        value = trimToNull(System.getenv(envName));
        if (value == null) {
            value = trimToNull(System.getenv(envAlias));
        }
        if (value != null) {
            return value;
        }

        value = trimToNull(config.getProperty(propertyName));
        if (value != null) {
            return value;
        }

        return trimToNull(config.getProperty(propertyAlias));
    }

    private static int resolveIntValue(Properties config, String propertyName, String envName, int valorPorDefecto) {
        String value = trimToNull(System.getProperty(propertyName));
        if (value == null) {
            value = trimToNull(System.getenv(envName));
        }
        if (value == null) {
            value = trimToNull(config.getProperty(propertyName));
        }
        if (value == null) {
            return valorPorDefecto;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : valorPorDefecto;
        } catch (NumberFormatException ex) {
            return valorPorDefecto;
        }
    }

    private static void requireConfigured(String propertyName, String propertyAlias, String envName, String envAlias, String value) throws SQLException {
        if (value == null) {
            throw new SQLException(
                    "Debe configurar " + propertyName + " o " + propertyAlias + " en config/" + CONFIG_FILE_NAME
                            + ", o la variable de entorno " + envName + " / " + envAlias + " para conectar a SDRERC_APP.");
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
