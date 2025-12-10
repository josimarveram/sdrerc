/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.domain.model.Expediente.ExpedienteResponse;
import com.sdrerc.domain.model.User;
import com.sdrerc.infrastructure.database.OracleConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author usuario
 */
public class ExpedienteRepository 
{
    public ExpedienteResponse ListarExpediente(String username, String password) throws SQLException 
    {
        String sql = "SELECT                      " +
                     " id_expediente                AS \"idExpediente\", " +
                     " fecha_solicitud              AS \"fechaSolicitud\", " +
                     " numero_tramite_documento     AS \"numeroTramiteDocumento\", " +
                     " tipo_solicitud               AS \"tipoSolicitud\", " +
                     " tipo_documento               AS \"tipoDocumento\", " +
                     " dni_remitente                AS \"dniRemitente\", " +
                     " apellido_nombre_remitente    AS \"apellidoNombreRemitente\", " +
                     " dni_solicitante              AS \"dniSolicitante\", " +
                     " apellido_nombre_solicitante  AS \"apellidoNombreSolicitante\", " +
                     " tipo_procedimiento_registral AS \"tipoProcedimientoRegistral\", " +
                     " tipo_acta                    AS \"tipoActa\", " +
                     " numero_acta                  AS \"numeroActa\", " +
                     " tipo_grupo_familiar          AS \"tipoGrupoFamiliar\", " +
                     " numero_grupo_familiar        AS \"numeroGrupoFamiliar\", " +
                     " dni_titular                  AS \"dniTitular\", " +
                     " apellido_nombre_titular      AS \"apellidoNombreTitular\", " +
                     " estado, " +
                     " id_usuario_crea              AS \"idUsuarioCrea\", " +
                     " fecha_registra               AS \"fechaRegistra\", " +
                     " id_usuario_modifica          AS \"idUsuarioModifica\", " +
                     " fecha_modifica               AS \"fechaModifica\" " +
                     " FROM trs_expediente ";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) 
            {
                return new ExpedienteResponse(
                        rs.getInt("idExpediente"),
                        rs.getDate("fechaSolicitud"),
                        rs.getString("numeroTramiteDocumento"),
                        rs.getInt("tipoSolicitud"),
                        rs.getInt("tipoDocumento"),
                        rs.getString("dniRemitente"),
                        rs.getString("apellidoNombreRemitente"),
                        rs.getString("dniSolicitante"),
                        rs.getString("apellidoNombreSolicitante"),
                        rs.getInt("tipoProcedimientoRegistral"),
                        rs.getInt("tipoActa"),
                        rs.getString("numeroActa"),
                        rs.getInt("tipoGrupoFamiliar"),
                        rs.getString("numeroGrupoFamiliar"),
                        rs.getString("dniTitular"),
                        rs.getString("apellidoNombreTitular"),
                        rs.getString("estado"),
                        rs.getInt("idUsuarioCrea"),
                        rs.getDate("fechaRegistra"),
                        rs.getInt("idUsuarioModifica"),
                        rs.getDate("fechaModifica")
                );
            }
        }
        return null; 
    }
    
    public ExpedienteResponse agregarExpediente(Expediente expediente) throws SQLException 
    {

        String sql = "INSERT INTO trs_expediente (" +
                " fecha_solicitud, numero_tramite_documento, tipo_solicitud, tipo_documento, " +
                " dni_remitente, apellido_nombre_remitente, dni_solicitante, apellido_nombre_solicitante, " +
                " tipo_procedimiento_registral, tipo_acta, numero_acta, tipo_grupo_familiar, " +
                " numero_grupo_familiar, dni_titular, apellido_nombre_titular, estado, " +
                " id_usuario_crea, fecha_registra" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE"})) 
        {
            //stmt.setDate(1, expediente.getFechaSolicitud());
            stmt.setDate(1, new java.sql.Date(System.currentTimeMillis()));
            stmt.setString(2, expediente.getNumeroTramiteDocumento());
            stmt.setInt(3, expediente.getTipoSolicitud());
            stmt.setInt(4, expediente.getTipoDocumento());
            stmt.setString(5, expediente.getDniRemitente());
            stmt.setString(6, expediente.getApellidoNombreRemitente());
            stmt.setString(7, expediente.getDniSolicitante());
            stmt.setString(8, expediente.getApellidoNombreSolicitante());
            stmt.setInt(9, expediente.getTipoProcedimientoRegistral());
            stmt.setInt(10, expediente.getTipoActa());
            stmt.setString(11, expediente.getNumeroActa());
            stmt.setInt(12, expediente.getTipoGrupoFamiliar());
            stmt.setString(13, expediente.getNumeroGrupoFamiliar());
            stmt.setString(14, expediente.getDniTitular());
            stmt.setString(15, expediente.getApellidoNombreTitular());
            stmt.setString(16, expediente.getEstado());
            stmt.setInt(17, expediente.getIdUsuarioCrea());
            stmt.setDate(18, new java.sql.Date(System.currentTimeMillis()));

            stmt.executeUpdate();

            // Obtener el ID generado (IDENTITY)
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int idGenerado = rs.getInt(1);
                expediente.setIdExpediente(idGenerado);
            }

            return new ExpedienteResponse(
                    expediente.getIdExpediente(),
                    expediente.getFechaSolicitud(),
                    expediente.getNumeroTramiteDocumento(),
                    expediente.getTipoSolicitud(),
                    expediente.getTipoDocumento(),
                    expediente.getDniRemitente(),
                    expediente.getApellidoNombreRemitente(),
                    expediente.getDniSolicitante(),
                    expediente.getApellidoNombreSolicitante(),
                    expediente.getTipoProcedimientoRegistral(),
                    expediente.getTipoActa(),
                    expediente.getNumeroActa(),
                    expediente.getTipoGrupoFamiliar(),
                    expediente.getNumeroGrupoFamiliar(),
                    expediente.getDniTitular(),
                    expediente.getApellidoNombreTitular(),
                    expediente.getEstado(),
                    expediente.getIdUsuarioCrea(),
                    expediente.getFechaRegistra(),
                    expediente.getIdUsuarioModifica(),
                    expediente.getFechaModifica()
            );
        }     
    }
    
    
}
