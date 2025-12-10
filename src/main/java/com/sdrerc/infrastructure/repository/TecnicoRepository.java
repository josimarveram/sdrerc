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

        String sql = "SELECT ID_TECNICO, APELLIDO_PATERNO, APELLIDO_MATERNO, NOMBRES " +
                     "FROM TECNICO WHERE ACTIVE = 1 ORDER BY APELLIDO_PATERNO";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Tecnico(
                        rs.getInt("ID_TECNICO"),
                        rs.getString("APELLIDO_PATERNO"),
                        rs.getString("APELLIDO_MATERNO"),
                        rs.getString("NOMBRES")
                ));
            }
        }catch(SQLException ex){
            throw new RuntimeException("Error listando tecnicos" + ex.getMessage());
        }
        return lista;
    }
}
