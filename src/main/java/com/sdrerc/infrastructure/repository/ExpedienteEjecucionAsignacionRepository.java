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
import java.sql.SQLException;

/**
 *
 * @author betom
 */
public class ExpedienteEjecucionAsignacionRepository 
{
    public void registrarExpedienteEjecucionAsignacion(ExpedienteAsignacion asignacion, Expediente expediente) throws SQLException 
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
}
