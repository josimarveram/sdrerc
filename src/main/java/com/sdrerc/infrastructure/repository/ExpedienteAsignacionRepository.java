/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author David
 */
public class ExpedienteAsignacionRepository {
    public void registrar(ExpedienteAsignacion asignacion) throws SQLException {

        String sql = "INSERT INTO EXPEDIENTE_ASIGNACION "
                   + "(ID_EXPEDIENTE, ID_TECNICO, FECHA_ASIGNACION) "
                   + "VALUES (?, ?, ?)";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, asignacion.getIdExpediente());
            ps.setInt(2, asignacion.getIdTecnico());
            
            java.sql.Date fechaSql = new java.sql.Date(asignacion.getFechaAsignacion().getTime());
            ps.setDate(3, fechaSql);

            ps.executeUpdate();
        }
    }
}
