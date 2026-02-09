/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.ExpedienteObservacionEjecucion.ExpedienteObservacionEjecucion;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author David
 */
public class ExpedienteObservacionEjecucionRepository {
    public void insertar(ExpedienteObservacionEjecucion o) throws SQLException {

        String sql =
            "INSERT INTO EXPEDIENTE_OBSERVACION_EJECUCION " +
            "(ID_EXPEDIENTE,ID_ESTADO_EJECUCION, TIENE_OBSERVACION, DESCRIPCION_OBSERVACION, FECHA_EJECUCION) " +
            "VALUES (?, ?, ?, ?, ?)";
        
        String updateExpedienteSql = "UPDATE EXPEDIENTE SET " +
                    " ESTADO = ?, " +
                    " id_usuario_modifica = ?, " +
                    " fecha_modifica      = ? " +
                    " WHERE id_expediente = ?";

        Connection conn = null;
        try {
            conn = OracleConnection.getConnection();
            conn.setAutoCommit(false);
            
            try(PreparedStatement psupdateExpediente = conn.prepareStatement(updateExpedienteSql))
            {
                // Datos para actualizar
                psupdateExpediente.setInt(1, o.getIdEstadoExpediente());   // IdEstadoExpediente                
                psupdateExpediente.setInt(2, o.getUsuarioModificacion());    // id_usuario_modifica    
                psupdateExpediente.setDate(3, new java.sql.Date(System.currentTimeMillis()));  // fecha_modifica                
                psupdateExpediente.setInt(4, o.getIdExpediente());         // WHERE id_expediente = ?                
                psupdateExpediente.executeUpdate();
            } 
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, o.getIdExpediente());
                ps.setInt(2, o.getIdEstadoEjecucion());
                ps.setInt(3, o.getTieneObservacion() ? 1 : 0);
                ps.setString(4, o.getDescripcionObservacion());
                ps.setDate(5, new java.sql.Date(System.currentTimeMillis()));
                ps.executeUpdate();
            }            
            conn.commit();   
        }catch (SQLException ex) {
            if (conn != null) {
                conn.rollback(); 
            }
            throw ex;

        } finally {
            if (conn != null) conn.setAutoCommit(true); // volver a modo normal
            if (conn != null) conn.close();
        }
    }
}
