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
    private static final String PASSWORD = "123456789";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✔ Conexión exitosa a Oracle 23c Free");
        } catch (SQLException e) {
            System.out.println("❌ Error de conexión: " + e.getMessage());
        }
    }
}
