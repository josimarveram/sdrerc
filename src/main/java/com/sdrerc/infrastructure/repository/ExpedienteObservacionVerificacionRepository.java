/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;
import com.sdrerc.domain.model.ExpedienteObservacionVerificacion.ExpedienteObservacionVerificacion;
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
public class ExpedienteObservacionVerificacionRepository {
    
    public void insertar(ExpedienteObservacionVerificacion entity) throws SQLException {

        String sql =
            "INSERT INTO EXPEDIENTE_OBSERVACION_VERIFICACION " +
            "(ID_EXPEDIENTE, HOJA_ENVIO, TIENE_OBSERVACION, TIPO_OBSERVACION, DESCRIPCION_OBSERVACION) " +
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
                psupdateExpediente.setInt(1, entity.getIdEstadoExpediente());   // IdEstadoExpediente                
                psupdateExpediente.setInt(2, entity.getUsuarioModificacion());    // id_usuario_modifica    
                psupdateExpediente.setDate(3, new java.sql.Date(System.currentTimeMillis()));  // fecha_modifica                
                psupdateExpediente.setInt(4, entity.getIdExpediente());         // WHERE id_expediente = ?                
                psupdateExpediente.executeUpdate();
            } 
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, entity.getIdExpediente());
                ps.setString(2, entity.getHojaEnvio());
                ps.setInt(3, entity.getTieneObservacion() ? 1 : 0);
                ps.setInt(4, entity.getTipoObservacion());
                ps.setString(5, entity.getDescripcionObservacion());

                ps.executeUpdate();
            }            
            conn.commit();
        } catch (SQLException ex) {
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
