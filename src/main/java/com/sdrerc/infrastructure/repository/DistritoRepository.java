/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.Distrito;
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
public class DistritoRepository {
    public List<Distrito> listarPorProvincia(int idProvincia) {
        List<Distrito> lista = new ArrayList<>();

        String sql = "SELECT ID_DISTRITO, ID_PROVINCIA, DESCRIPCION FROM DISTRITO WHERE ACTIVE = 1 AND ID_PROVINCIA = ? ORDER BY DESCRIPCION";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idProvincia);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Distrito(
                        rs.getInt("ID_DISTRITO"),
                        rs.getInt("ID_PROVINCIA"),
                        rs.getString("DESCRIPCION")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}
