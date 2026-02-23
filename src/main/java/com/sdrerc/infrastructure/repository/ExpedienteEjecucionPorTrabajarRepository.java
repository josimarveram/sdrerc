/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author betom
 */
public class ExpedienteEjecucionPorTrabajarRepository 
{
    public List<Expediente> ListarExpedientesEjecucionPorTrabajar(int estadoItem) throws SQLException 
    {        
        List<Expediente> lista = new ArrayList<>();
        
        StringBuilder sqlListaExpediente = new StringBuilder(			
            " SELECT * FROM EXPEDIENTE " +
            " INNER JOIN (SELECT e.ID_EXPEDIENTE AS ID_EXPEDIENTE_DOCUMENTO_VERIFICAR FROM EXPEDIENTE_ANALISIS_ABOGADO_DET_DOC d " +
            "             INNER JOIN EXPEDIENTE_ANALISIS_ABOGADO e ON d.ID_EXPEDIENTE_ANALISIS_ABOGADO = e.ID_EXPEDIENTE_ANALISIS_ABOGADO " +
            "             WHERE d.ID_TIPO_DOCUMENTO_ANALIZADO IN (71,72) AND d.ID_TIPO_DOCUMENTO_ANALIZADO IS NOT NULL AND d.ACTIVE = 1 " +           
            "             ORDER BY d.FECHA_REGISTRO DESC " +
            "             FETCH FIRST 1 ROW ONLY " +
            "            )doc ON doc.ID_EXPEDIENTE_DOCUMENTO_VERIFICAR = EXPEDIENTE.ID_EXPEDIENTE " +
            " INNER JOIN EXPEDIENTE_ANALISIS_ABOGADO eaa on expediente.id_expediente = eaa.id_expediente " +
            " WHERE 1 = 1 AND eaa.ID_ANALISIS = 73 "			
            );
                
        boolean filtrarEstado = estadoItem != 0;
        if(filtrarEstado) 
        {
            sqlListaExpediente.append("AND EXPEDIENTE.estado = ? ");
        }
        
        Connection conn = null;
        try
        {
            conn = OracleConnection.getConnection();
            conn.setAutoCommit(false);             
            try(PreparedStatement psListar = conn.prepareStatement(sqlListaExpediente.toString()))
            {                
                if(filtrarEstado) 
                    psListar.setInt(1, estadoItem);

                ResultSet rs = psListar.executeQuery();
                while (rs.next()) 
                {
                    lista.add(mapRow(rs));
                }
            } 
            return lista;                        
        }
        catch(SQLException ex)
        {
            throw ex;
        }
        finally 
        {
            if(conn != null) 
               conn.setAutoCommit(true); // volver a modo normal
            if(conn != null) 
               conn.close();
        }   
    }
    private Expediente mapRow(ResultSet rs) throws SQLException {
        return new Expediente(
                            rs.getInt("ID_EXPEDIENTE"),
                            rs.getInt("ES_REGISTRO_SDRERC"),
                            rs.getString("HOJA_ENVIO_EXPEDIENTE"),
                            rs.getString("NUMERO_TRAMITE_DOCUMENTO"),
                            rs.getDate("FECHA_RECEPCION"),
                            rs.getDate("FECHA_SOLICITUD"),
                            rs.getInt("TIPO_DOCUMENTO"),
                            rs.getString("NUMERO_DOCUMENTO"),
                            rs.getInt("TIPO_ACTA"),
                            rs.getString("NUMERO_ACTA"),
                            rs.getInt("TIPO_GRUPO_FAMILIAR"),
                            rs.getInt("GRADO_PARENTESCO"),
                            rs.getInt("TIPO_PROCEDIMIENTO_REGISTRAL"),
                            rs.getInt("TIPO_SOLICITUD"),
                            rs.getString("DNI_REMITENTE"),
                            rs.getString("APELLIDO_NOMBRE_REMITENTE"),
                            rs.getInt("UNIDAD_ORGANICA"),
                            rs.getString("DNI_TITULAR"),
                            rs.getString("APELLIDO_NOMBRE_TITULAR"),
                            rs.getInt("DEPARTAMENTO"),
                            rs.getInt("PROVINCIA"),
                            rs.getInt("DISTRITO"),
                            rs.getInt("DIRECCION_DOMICILIARIA"),
                            rs.getString("DOMICILIO"),
                            rs.getString("CORREO_ELECTRONICO"),
                            rs.getString("CELULAR"),
                            rs.getInt("ESTADO"),
                            rs.getInt("ID_USUARIO_CREA"),
                            rs.getDate("FECHA_REGISTRA"),
                            rs.getInt("ID_USUARIO_MODIFICA"),
                            rs.getDate("FECHA_MODIFICA")
        );
    }
}
