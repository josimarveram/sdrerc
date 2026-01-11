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
 * @author David
 */
public class ExpedienteAsignacionRepository {
    
    public void registrar(ExpedienteAsignacion asignacion, Expediente expediente) throws SQLException 
    {        
        // Primero, actualizar la tabla EXPEDIENTE con los nuevos valores
        String updateExpedienteSql =
            "UPDATE EXPEDIENTE SET "
            + "FECHA_SOLICITUD = ?, "
            + "NUMERO_TRAMITE_DOCUMENTO = ?, "
            + "TIPO_SOLICITUD = ?, "
            + "TIPO_DOCUMENTO = ?, "
            + "DNI_REMITENTE = ?, "
            + "APELLIDO_NOMBRE_REMITENTE = ?, "
            + "TIPO_PROCEDIMIENTO_REGISTRAL = ?, "
            + "TIPO_ACTA = ?, "
            + "NUMERO_ACTA = ?, "
            + "TIPO_GRUPO_FAMILIAR = ?, "
            + "DNI_TITULAR = ?, "
            + "APELLIDO_NOMBRE_TITULAR = ?, "
            + "ESTADO = ?, "
            + "ID_USUARIO_MODIFICA = ?, "
            + "FECHA_MODIFICA = ? ,"
                
                
            + "FECHA_RECEPCION = ? ,"
            + "CORREO_ELECTRONICO = ? ,"
            + "CELULAR = ? ,"
            + "DOMICILIO = ? ,"
            + "DIRECCION_DOMICILIARIA = ? ,"
            + "GRADO_PARENTESCO = ? ,"
            + "UNIDAD_ORGANICA = ? "                
                
            + "WHERE ID_EXPEDIENTE = ?";
        
        // Luego, insertar en la tabla EXPEDIENTE_ASIGNACION
        String insertAsignacionSql = "INSERT INTO EXPEDIENTE_ASIGNACION "
                + "(ID_EXPEDIENTE, ID_TECNICO, FECHA_ASIGNACION, HOJA_ENVIO_ASIGNACION) "
                + "VALUES (?, ?, ?, ?)";
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
                psUpdate.setInt(7, expediente.getTipoProcedimientoRegistral());
                psUpdate.setInt(8, expediente.getTipoActa());
                psUpdate.setString(9, expediente.getNumeroActa());
                psUpdate.setInt(10, expediente.getTipoGrupoFamiliar());
                psUpdate.setString(11, expediente.getDniTitular());
                psUpdate.setString(12, expediente.getApellidoNombreTitular());
                psUpdate.setInt(13, expediente.getEstado());
                psUpdate.setInt(14, expediente.getIdUsuarioModifica());
                psUpdate.setDate(15, new java.sql.Date(System.currentTimeMillis()));
                
                psUpdate.setDate(16, new java.sql.Date(expediente.getFechaRecepcion().getTime()));
                psUpdate.setString(17, expediente.getCorreoElectronico());
                psUpdate.setString(18, expediente.getCelular());
                psUpdate.setString(19, expediente.getDomicilio());
                psUpdate.setInt(20, expediente.getDireccionDomiciliaria());
                psUpdate.setInt(21, expediente.getGradoParentesco());
                psUpdate.setInt(22, expediente.getUnidadOrganica());                

                psUpdate.setInt(23, expediente.getIdExpediente()); // WHERE

                psUpdate.executeUpdate();
            }
            
            // 2️⃣ INSERT EXPEDIENTE_ASIGNACION
            try (PreparedStatement psInsert = conn.prepareStatement(insertAsignacionSql)) {

                psInsert.setInt(1, asignacion.getIdExpediente());
                psInsert.setInt(2, asignacion.getIdTecnico());

                java.sql.Date fechaSql = new java.sql.Date(asignacion.getFechaAsignacion().getTime());
                psInsert.setDate(3, fechaSql);
                psInsert.setString(4, asignacion.getHojaEnvioAsignacion());

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
                psupdateExpediente.setInt(1, oExpedienteAsignacion.getIdEstadoExpediente());   // IdEstadoExpediente                
                psupdateExpediente.setInt(2, oExpedienteAsignacion.getIdUsuarioModifica());    // id_usuario_modifica    
                psupdateExpediente.setDate(3, new java.sql.Date(System.currentTimeMillis()));  // fecha_modifica                
                psupdateExpediente.setInt(4, oExpedienteAsignacion.getIdExpediente());         // WHERE id_expediente = ?                
                psupdateExpediente.executeUpdate();
            } 
            
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
      
    
    public List<Expediente> ListarExpedientesAsignadosPorTrabajador(int idTecnico, int aceptaRecepcion,int estadoItem) throws SQLException 
    {        
        List<Expediente> lista = new ArrayList<>();
        
        StringBuilder sqlListaExpediente = new StringBuilder("	SELECT * FROM EXPEDIENTE INNER JOIN EXPEDIENTE_ASIGNACION ON EXPEDIENTE.ID_EXPEDIENTE = EXPEDIENTE_ASIGNACION.ID_EXPEDIENTE WHERE EXPEDIENTE_ASIGNACION.id_tecnico = ?");
        
        // Si el estado no es "TODOS", agregamos AND
        boolean filtrarEstado = estadoItem != 0;
        boolean filtrarAceptaRecepcion = aceptaRecepcion != 0;

        if(filtrarEstado) 
        {
            sqlListaExpediente.append("AND EXPEDIENTE.estado = ?");
        }
        if(filtrarAceptaRecepcion) 
        {
            sqlListaExpediente.append("AND expediente_asignacion.acepta_recepcion = ?");
        }
        
        Connection conn = null;
        try
        {
            conn = OracleConnection.getConnection();
            conn.setAutoCommit(false);             
            try(PreparedStatement psListar = conn.prepareStatement(sqlListaExpediente.toString()))
            {
                psListar.setInt(1, idTecnico);
                
                if(filtrarEstado) 
                    psListar.setInt(2, estadoItem);
                
                if(filtrarAceptaRecepcion) 
                    psListar.setInt(3, aceptaRecepcion);

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
