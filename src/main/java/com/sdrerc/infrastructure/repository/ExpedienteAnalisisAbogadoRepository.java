/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.ExpedienteAnalisAbogadoDetDoc.ExpedienteAnalisisAbogadoDetDoc;
import com.sdrerc.domain.model.ExpedienteAnalisisAbogado.ExpedienteAnalisisAbogado;
import com.sdrerc.domain.model.ExpedienteAnalisisAbogado.ExpedienteAnalisisAbogadoResponse;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
	
    public ExpedienteAnalisisAbogadoResponse ObtenerExpedientesPorNotificarXidExpediente(int idExpediente) throws SQLException 
    {
        String sql =
        " SELECT " +
        " ea.ID_EXPEDIENTE_ANALISIS_ABOGADO, ea.ID_EXPEDIENTE, ea.ID_ABOGADO, ea.ID_ANALISIS, " +
        " ea.DESC_FUNDAMENTO, ea.FECHA_ATENCION, ea.FECHA_REGISTRO, ea.USUARIO_REGISTRO, " +
        " ea.FECHA_MODIFICACION, ea.USUARIO_MODIFICACION, " +
        " d.ID_EXPEDIENTE_ANALISIS_ABOGADO_DET_DOC, d.ID_TIPO_DOCUMENTO_ANALIZADO, " +
        " d.DESC_DOCUMENTO, d.ACTIVE AS ACTIVE_DOC, d.FECHA_REGISTRO AS FECHA_REGISTRO_DOC, " +
        " d.USUARIO_REGISTRO AS USUARIO_REGISTRO_DOC, d.FECHA_MODIFICACION AS FECHA_MODIFICACION_DOC, " +
        " d.USUARIO_MODIFICACION AS USUARIO_MODIFICACION_DOC, c.descripcion AS descTipoDocumentoAnalizado " +
        " FROM EXPEDIENTE_ANALISIS_ABOGADO ea " +
        " LEFT JOIN EXPEDIENTE_ANALISIS_ABOGADO_DET_DOC d " + " ON ea.ID_EXPEDIENTE_ANALISIS_ABOGADO = d.ID_EXPEDIENTE_ANALISIS_ABOGADO " + " AND d.ACTIVE = 1 " +
        " INNER JOIN CATALOGO_ITEM C ON d.ID_TIPO_DOCUMENTO_ANALIZADO = c.ID_CATALOGO_ITEM " +
        " WHERE ea.ACTIVE = 1 " +
        " AND ea.ID_EXPEDIENTE = ? " +
        " ORDER BY ea.ID_EXPEDIENTE_ANALISIS_ABOGADO, d.FECHA_REGISTRO ";
        
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) 
        {
            ps.setInt(1, idExpediente);
            ResultSet rs = ps.executeQuery();            
            
            ExpedienteAnalisisAbogadoResponse padre = null;

            while(rs.next()) 
            {
               if (padre == null) 
               {
                padre = new ExpedienteAnalisisAbogadoResponse(
                rs.getInt("ID_EXPEDIENTE_ANALISIS_ABOGADO"),
                rs.getInt("ID_EXPEDIENTE"),
                rs.getInt("ID_ABOGADO"),
                rs.getInt("ID_ANALISIS"),
                rs.getString("DESC_FUNDAMENTO"),
                rs.getDate("FECHA_ATENCION"),
                rs.getDate("FECHA_REGISTRO"),
                0, // usuarioRegistro no aplica como int
                rs.getDate("FECHA_MODIFICACION"),
                0, // usuarioModificacion no aplica como int
                1 // idEstadoExpediente → no existe, valor fijo o eliminar del modelo
                );
                //mapa.put(idPadre, padre);
               }
               int idDetalle = rs.getInt("ID_EXPEDIENTE_ANALISIS_ABOGADO_DET_DOC");
               if (!rs.wasNull()) 
               {
                  ExpedienteAnalisisAbogadoDetDoc doc = new ExpedienteAnalisisAbogadoDetDoc(
                        idDetalle,
                        padre.getIdExpedienteAnalisisAbogado(),
                        rs.getInt("ID_TIPO_DOCUMENTO_ANALIZADO"),
                        rs.getString("descTipoDocumentoAnalizado"),
                        rs.getString("DESC_DOCUMENTO"),
                        rs.getInt("ACTIVE_DOC"),
                        rs.getDate("FECHA_REGISTRO_DOC"),
                        0,
                        rs.getDate("FECHA_MODIFICACION_DOC"),
                        0
                    );
                    padre.getExpedienteAnalisisAbogadoDetDoc().add(doc);
                }
            }

            rs.close();
            ps.close();
            return padre;
        }
    }
}
