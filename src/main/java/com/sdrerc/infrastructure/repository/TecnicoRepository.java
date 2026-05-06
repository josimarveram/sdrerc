/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.Tecnico;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author David
 */
public class TecnicoRepository {
    public List<Tecnico> listarTecnicos() {
        List<Tecnico> lista = new ArrayList<>();

        String sql = "SELECT DISTINCT t.ID_TECNICO, t.NUMERO_DOCUMENTO, t.APELLIDO_PATERNO, t.APELLIDO_MATERNO, t.NOMBRES, " +
                     "t.APELLIDO_PATERNO || ' ' || t.APELLIDO_MATERNO || ' ' || t.NOMBRES AS NOMBRE_COMPLETO " +
                     "FROM TECNICO t " +
                     "JOIN APP_USERS u ON u.ID_TECNICO = t.ID_TECNICO " +
                     "JOIN APP_USER_ROLES ur ON ur.USER_ID = u.USER_ID " +
                     "JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
                     "WHERE t.ACTIVE = 1 " +
                     "AND UPPER(r.ROLE_NAME) = 'ABOGADO' " +
                     "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO') " +
                     "ORDER BY t.ID_TECNICO ASC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Tecnico(
                        rs.getInt("ID_TECNICO"),
                        rs.getInt("NUMERO_DOCUMENTO"),
                        rs.getString("APELLIDO_PATERNO"),
                        rs.getString("APELLIDO_MATERNO"),
                        rs.getString("NOMBRES"),
                        rs.getString("NOMBRE_COMPLETO")
                ));
            }
        }catch(SQLException ex){
            throw new RuntimeException("Error listando tecnicos" + ex.getMessage());
        }
        return lista;
    }
}
