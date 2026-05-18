/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author David
 */
public class OracleConnection {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/xe";
    private static final String USER = "system";
    private static final String PASSWORD = "satrapa12345";
    private static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";

    public static Connection getConnection() throws SQLException {
        cargarDriverOracle();
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static void cargarDriverOracle() throws SQLException {
        try {
            Class.forName(ORACLE_DRIVER);
        } catch (ClassNotFoundException ex) {
            throw new SQLException(
                    "No se encontró el driver JDBC de Oracle en el classpath. "
                    + "Verifique que ojdbc11 esté incluido al ejecutar la aplicación.",
                    ex);
        }
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✔ Conexión exitosa a Oracle 23c Free");
        } catch (SQLException e) {
            System.out.println("❌ Error de conexión: " + e.getMessage());
        }
    }
}
