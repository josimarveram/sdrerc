/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.Provincia;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author David
 */
public class ProvinciaRepository {
    public List<Provincia> listarPorDepartamento(int idDepartamento) {
        List<Provincia> lista = new ArrayList<>();

        String sql = "SELECT ID_PROVINCIA, ID_DEPARTAMENTO, DESCRIPCION FROM PROVINCIA WHERE ACTIVE = 1 AND ID_DEPARTAMENTO = ? ORDER BY DESCRIPCION";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idDepartamento);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Provincia(
                        rs.getInt("ID_PROVINCIA"),
                        rs.getInt("ID_DEPARTAMENTO"),
                        rs.getString("DESCRIPCION")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}
