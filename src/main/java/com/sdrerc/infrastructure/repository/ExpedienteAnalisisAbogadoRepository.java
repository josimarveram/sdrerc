/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.ExpedienteAnalisAbogadoDetDoc.ExpedienteAnalisisAbogadoDetDoc;
import com.sdrerc.domain.model.ExpedienteAnalisisAbogado.ExpedienteAnalisisAbogado;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author betom
 */
public class ExpedienteAnalisisAbogadoRepository 
{
    public boolean InsertarAnalisisAbogado(ExpedienteAnalisisAbogado oExpedienteAnalisisAbogado) throws SQLException 
    {
        String insertAnalisisAbogadoSql = "INSERT INTO EXPEDIENTE_ANALISIS_ABOGADO " 
                   + "(ID_EXPEDIENTE,ID_ABOGADO,ID_ANALISIS,FECHA_ATENCION,FECHA_REGISTRO,USUARIO_REGISTRO)"
                   + "VALUES (?, ?, ?, ?, ?, ?) " 
				   + "RETURNING ID_EXPEDIENTE_ANALISIS_ABOGADO INTO ?";
        
        String insertAnalisisAbogadoDetDocSql = "INSERT INTO EXPEDIENTE_ANALISIS_ABOGADO_DET_DOC " 
                   + "(ID_EXPEDIENTE_ANALISIS_ABOGADO,ID_TIPO_DOCUMENTO_ANALIZADO,DESC_DOCUMENTO,FECHA_REGISTRO,USUARIO_REGISTRO)"
                   + "VALUES (?, ?, ?, ?, ?)";
        
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
                psupdateExpediente.setInt(1, oExpedienteAnalisisAbogado.getIdEstadoExpediente());   // IdEstadoExpediente                
                psupdateExpediente.setInt(2, oExpedienteAnalisisAbogado.getUsuarioModificacion());    // id_usuario_modifica    
                psupdateExpediente.setDate(3, new java.sql.Date(System.currentTimeMillis()));  // fecha_modifica                
                psupdateExpediente.setInt(4, oExpedienteAnalisisAbogado.getIdExpediente());         // WHERE id_expediente = ?                
                psupdateExpediente.executeUpdate();
            } 
            
	    int idAnalisisAbogado;
            
            try (OraclePreparedStatement psInsert = (OraclePreparedStatement) conn.prepareStatement(insertAnalisisAbogadoSql)) 
            {
                psInsert.setInt(1, oExpedienteAnalisisAbogado.getIdExpediente());
                psInsert.setInt(2, oExpedienteAnalisisAbogado.getIdAbogado());
                psInsert.setInt(3, oExpedienteAnalisisAbogado.getIdAnalisis());
                psInsert.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                psInsert.setDate(5, new java.sql.Date(System.currentTimeMillis()));
                psInsert.setInt(6, oExpedienteAnalisisAbogado.getUsuarioRegistro());

                psInsert.registerReturnParameter(7, OracleTypes.NUMBER);
                psInsert.executeUpdate();

                try (ResultSet rs = psInsert.getReturnResultSet()) 
                {
                    if (rs.next()) 
                        idAnalisisAbogado = rs.getInt(1);
                    else 
                        throw new SQLException("No se pudo obtener ID generado");
                }
            }		
			
            try (PreparedStatement psDet = conn.prepareStatement(insertAnalisisAbogadoDetDocSql)) 
            {
                for (ExpedienteAnalisisAbogadoDetDoc det :
                oExpedienteAnalisisAbogado.getExpedienteAnalisisAbogadoDetDoc()) 
                {
                    psDet.setInt(1, idAnalisisAbogado);
                    psDet.setInt(2, det.getIdTipoDocumentoAnalizado());
                    psDet.setString(3, det.getDescDocumento());
                    psDet.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                    psDet.setInt(5, det.getUsuarioRegistro());
                    psDet.addBatch();
                }
                psDet.executeBatch();
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
