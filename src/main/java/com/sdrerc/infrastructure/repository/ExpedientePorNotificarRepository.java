/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.Expediente.Expediente;
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
public class ExpedientePorNotificarRepository 
{
    public List<Expediente> ListarExpedientesPorNotificar(int estadoItem) throws SQLException 
    {        
        List<Expediente> lista = new ArrayList<>();
        
        StringBuilder sqlListaExpediente = new StringBuilder(			
                " SELECT * FROM EXPEDIENTE exp " +
                " inner join EXPEDIENTE_ASIGNACION expAsi on expAsi.ID_EXPEDIENTE = exp.ID_EXPEDIENTE and expAsi.etapa_flujo = 90 " +
                " WHERE expasi.active = 1 "	
            );
                
        boolean filtrarEstado = estadoItem != 0;
        if(filtrarEstado) 
        {
            sqlListaExpediente.append("AND exp.estado = ? ");
        }
        
        Connection conn = null;
        try
        {
            conn = OracleConnection.getConnection();
            conn.setAutoCommit(false);             
            try(PreparedStatement psListar = conn.prepareStatement(sqlListaExpediente.toString()))
            {                
                if(filtrarEstado) 
                    psListar.setInt(2, estadoItem);

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
