/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.domain.model.Expediente.ExpedienteResponse;
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
    public void registrar(ExpedienteAsignacion asignacion, Expediente expediente) throws SQLException {
        
        // Primero, actualizar la tabla EXPEDIENTE con los nuevos valores
        String updateExpedienteSql =
            "UPDATE EXPEDIENTE SET "
            + "FECHA_SOLICITUD = ?, "
            + "NUMERO_TRAMITE_DOCUMENTO = ?, "
            + "TIPO_SOLICITUD = ?, "
            + "TIPO_DOCUMENTO = ?, "
            + "DNI_REMITENTE = ?, "
            + "APELLIDO_NOMBRE_REMITENTE = ?, "
            + "DNI_SOLICITANTE = ?, "
            + "APELLIDO_NOMBRE_SOLICITANTE = ?, "
            + "TIPO_PROCEDIMIENTO_REGISTRAL = ?, "
            + "TIPO_ACTA = ?, "
            + "NUMERO_ACTA = ?, "
            + "TIPO_GRUPO_FAMILIAR = ?, "
            + "NUMERO_GRUPO_FAMILIAR = ?, "
            + "DNI_TITULAR = ?, "
            + "APELLIDO_NOMBRE_TITULAR = ?, "
            + "ESTADO = ?, "
            + "ID_USUARIO_MODIFICA = ?, "
            + "FECHA_MODIFICA = ? "
            + "WHERE ID_EXPEDIENTE = ?";
        
        // Luego, insertar en la tabla EXPEDIENTE_ASIGNACION
        String insertAsignacionSql = "INSERT INTO EXPEDIENTE_ASIGNACION "
                + "(ID_EXPEDIENTE, ID_TECNICO, FECHA_ASIGNACION) "
                + "VALUES (?, ?, ?)";
        /*
        String sql = "INSERT INTO EXPEDIENTE_ASIGNACION "
                   + "(ID_EXPEDIENTE, ID_TECNICO, FECHA_ASIGNACION) "
                   + "VALUES (?, ?, ?)";
        */
        Connection conn = null;
        try {
             
            conn = OracleConnection.getConnection();
            conn.setAutoCommit(false); 
                
            try (PreparedStatement psUpdate = conn.prepareStatement(updateExpedienteSql)) {

                psUpdate.setDate(1, new java.sql.Date(expediente.getFechaSolicitud().getTime()));
                psUpdate.setString(2, expediente.getNumeroTramiteDocumento());
                psUpdate.setInt(3, expediente.getTipoSolicitud());
                psUpdate.setInt(4, expediente.getTipoDocumento());
                psUpdate.setString(5, expediente.getDniRemitente());
                psUpdate.setString(6, expediente.getApellidoNombreRemitente());
                psUpdate.setString(7, expediente.getDniSolicitante());
                psUpdate.setString(8, expediente.getApellidoNombreSolicitante());
                psUpdate.setInt(9, expediente.getTipoProcedimientoRegistral());
                psUpdate.setInt(10, expediente.getTipoActa());
                psUpdate.setString(11, expediente.getNumeroActa());
                psUpdate.setInt(12, expediente.getTipoGrupoFamiliar());
                psUpdate.setString(13, expediente.getNumeroGrupoFamiliar());
                psUpdate.setString(14, expediente.getDniTitular());
                psUpdate.setString(15, expediente.getApellidoNombreTitular());
                psUpdate.setInt(16, expediente.getEstado());
                psUpdate.setInt(17, expediente.getIdUsuarioModifica());
                psUpdate.setDate(18, new java.sql.Date(expediente.getFechaModifica().getTime()));

                psUpdate.setInt(19, expediente.getIdExpediente()); // WHERE

                psUpdate.executeUpdate();
            }
            
            // 2️⃣ INSERT EXPEDIENTE_ASIGNACION
            try (PreparedStatement psInsert = conn.prepareStatement(insertAsignacionSql)) {

                psInsert.setInt(1, asignacion.getIdExpediente());
                psInsert.setInt(2, asignacion.getIdTecnico());

                java.sql.Date fechaSql = new java.sql.Date(asignacion.getFechaAsignacion().getTime());
                psInsert.setDate(3, fechaSql);

                psInsert.executeUpdate();
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
    
    public boolean actualizarRecepcionExpediente(ExpedienteAsignacion oExpedienteAsignacion) throws SQLException 
    {
        String updateExpedienteAsignacionSql = "UPDATE EXPEDIENTE_ASIGNACION SET " +
                    " acepta_recepcion = ?, " +
                    " fecha_recepcion = ?, " +
                    " usuario_modificacion = ?, " +
                    " fecha_modificacion = ? " +
                    " WHERE id_expediente = ?";

        Connection conn = null;        
        try 
        {
            conn = OracleConnection.getConnection();
            conn.setAutoCommit(false);             
            try(PreparedStatement psUpdate = conn.prepareStatement(updateExpedienteAsignacionSql))
            {
                // Datos para actualizar
                psUpdate.setInt(1, 1);                                               // acepta_recepcion
                psUpdate.setDate(2, new java.sql.Date(System.currentTimeMillis()));  // fecha_recepcion
                psUpdate.setInt(3, oExpedienteAsignacion.getIdUsuarioModifica());    // id_usuario_modifica    
                psUpdate.setDate(4, new java.sql.Date(System.currentTimeMillis()));  // fecha_modifica
                
                psUpdate.setInt(5, oExpedienteAsignacion.getIdExpediente());         // WHERE id_expediente = ?                
                psUpdate.executeUpdate();
            } 
            conn.commit(); 
            return true;   
        }
        catch (SQLException ex) 
        {
            if (conn != null) 
            {
                conn.rollback(); 
            }
            return false;   
        } 
        finally 
        {
            if (conn != null) conn.setAutoCommit(true); // volver a modo normal
            if (conn != null) conn.close();
        }               
    }  
      
}
