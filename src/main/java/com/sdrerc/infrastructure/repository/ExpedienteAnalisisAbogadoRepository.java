/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.ExpedienteAnalisisAbogado.ExpedienteAnalisisAbogado;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author betom
 */
public class ExpedienteAnalisisAbogadoRepository 
{
    public boolean actualizarAnalisisAbogado(ExpedienteAnalisisAbogado oExpedienteAnalisisAbogado) throws SQLException 
    {
        String insertAnalisisAbogadoSql = "INSERT INTO EXPEDIENTE_ANALISIS_ABOGADO " 
                   + "(ID_EXPEDIENTE,ID_ABOGADO,ID_ANALISIS,FECHA_ATENCION,FECHA_REGISTRO,USUARIO_REGISTRO)"
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        
        String updateExpedienteSql = "UPDATE EXPEDIENTE SET " +
                    " ESTADO = ?, " +
                    " id_usuario_modifica = ?, " +
                    " fecha_modifica      = ? " +
                    " WHERE id_expediente = ?";

        Connection conn = null;        
        try 
        {
            conn = OracleConnection.getConnection();
            conn.setAutoCommit(false);             
            
            try(PreparedStatement psupdateExpediente = conn.prepareStatement(updateExpedienteSql))
            {
                // Datos para actualizar
                psupdateExpediente.setInt(1, 59);   // IdEstadoExpediente                
                psupdateExpediente.setInt(2, oExpedienteAnalisisAbogado.getUsuarioModificacion());    // id_usuario_modifica    
                psupdateExpediente.setDate(3, new java.sql.Date(System.currentTimeMillis()));  // fecha_modifica                
                psupdateExpediente.setInt(4, oExpedienteAnalisisAbogado.getIdExpediente());         // WHERE id_expediente = ?                
                psupdateExpediente.executeUpdate();
            } 
            
            try (PreparedStatement psInsert = conn.prepareStatement(insertAnalisisAbogadoSql)) 
            {
                psInsert.setInt(1, oExpedienteAnalisisAbogado.getIdExpediente());
                psInsert.setInt(2, oExpedienteAnalisisAbogado.getIdAbogado());
                psInsert.setInt(3, oExpedienteAnalisisAbogado.getIdAnalisis());
                psInsert.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                psInsert.setDate(5, new java.sql.Date(System.currentTimeMillis()));
                psInsert.setInt(6, oExpedienteAnalisisAbogado.getIdAbogado());
                psInsert.executeUpdate();
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
