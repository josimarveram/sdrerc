/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.Departamento;
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
public class DepartamentoRepository {
    public List<Departamento> listarActivos() {
        List<Departamento> lista = new ArrayList<>();

        String sql = "SELECT ID_DEPARTAMENTO, DESCRIPCION FROM DEPARTAMENTO WHERE ACTIVE = 1 ORDER BY DESCRIPCION";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Departamento(
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
