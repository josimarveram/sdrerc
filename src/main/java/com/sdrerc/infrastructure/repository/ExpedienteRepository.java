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
import java.util.ArrayList;
import java.util.List;
import java.sql.Date;

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
                     " FROM EXPEDIENTE ";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) 
            {
                return new ExpedienteResponse(
                            rs.getInt("ID_EXPEDIENTE"),
                            rs.getString("ES_REGISTRO_SDRERC"),
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
        return null; 
    }
    
    public ExpedienteResponse agregarExpediente(Expediente expediente) throws SQLException 
    {

        String sql = "INSERT INTO EXPEDIENTE (" +
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
            stmt.setInt(9, expediente.getTipoProcedimientoRegistral());
            stmt.setInt(10, expediente.getTipoActa());
            stmt.setString(11, expediente.getNumeroActa());
            stmt.setInt(12, expediente.getTipoGrupoFamiliar());
            stmt.setString(14, expediente.getDniTitular());
            stmt.setString(15, expediente.getApellidoNombreTitular());
            stmt.setInt(16, expediente.getEstado());
            stmt.setInt(17, expediente.getIdUsuarioCrea());
            stmt.setDate(18, new java.sql.Date(System.currentTimeMillis()));

            stmt.executeUpdate();

            // Obtener el ID generado (IDENTITY)
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int idGenerado = rs.getInt(1);
                expediente.setIdExpediente(idGenerado);
            }
            
            // convertir fechaSolicitud (puede ser null)
            java.sql.Date sqlFechaSolicitud = null;
            if (expediente.getFechaSolicitud() != null) {
                sqlFechaSolicitud = new java.sql.Date(expediente.getFechaSolicitud().getTime());
            }
            
            // fecha_registra (si quieres usar la fecha que viene en el objeto)
            java.sql.Date sqlFechaRegistra = null;
            if (expediente.getFechaRegistra() != null) {
                sqlFechaRegistra = new java.sql.Date(expediente.getFechaRegistra().getTime());
            } else {
                sqlFechaRegistra = new java.sql.Date(System.currentTimeMillis());
            }
            // fecha_registra (si quieres usar la fecha que viene en el objeto)
            java.sql.Date sqlFechaModifica = null;
            if (expediente.getFechaModifica()!= null) {
                sqlFechaModifica = new java.sql.Date(expediente.getFechaModifica().getTime());
            } else {
                sqlFechaModifica = new java.sql.Date(System.currentTimeMillis());
            }

            return new ExpedienteResponse(
                    /*
                    expediente.getIdExpediente(),
                    sqlFechaSolicitud,
                    expediente.getNumeroTramiteDocumento(),
                    expediente.getTipoSolicitud(),
                    expediente.getTipoDocumento(),
                    expediente.getDniRemitente(),
                    expediente.getApellidoNombreRemitente(),
                    expediente.getTipoProcedimientoRegistral(),
                    expediente.getTipoActa(),
                    expediente.getNumeroActa(),
                    expediente.getTipoGrupoFamiliar(),
                    expediente.getDniTitular(),
                    expediente.getApellidoNombreTitular(),
                    expediente.getEstado(),
                    expediente.getIdUsuarioCrea(),
                    sqlFechaRegistra,
                    expediente.getIdUsuarioModifica(),
                    sqlFechaModifica
                    */
                    
                    expediente.getIdExpediente(),
                    expediente.getEsRegistroSdrerc(),
                    expediente.getHojaEnvioExpediente(),
                    expediente.getNumeroTramiteDocumento(),
                    expediente.getFechaRecepcion(),
                    expediente.getFechaSolicitud(),
                    expediente.getTipoDocumento(),
                    expediente.getNumeroDocumento(),
                    expediente.getTipoActa(),
                    expediente.getNumeroActa(),
                    expediente.getTipoGrupoFamiliar(),
                    expediente.getGradoParentesco(),
                    expediente.getTipoProcedimientoRegistral(),
                    expediente.getTipoSolicitud(),
                    expediente.getDniRemitente(),
                    expediente.getApellidoNombreRemitente(),
                    expediente.getUnidadOrganica(),
                    expediente.getDniTitular(),
                    expediente.getApellidoNombreTitular(),
                    expediente.getDepartamento(),
                    expediente.getProvincia(),
                    expediente.getDistrito(),
                    expediente.getDireccionDomiciliaria(),
                    expediente.getDomicilio(),
                    expediente.getCorreoElectronico(),
                    expediente.getCelular(),
                    expediente.getEstado(),
                    expediente.getIdUsuarioCrea(),
                    expediente.getFechaRegistra(),
                    expediente.getIdUsuarioModifica(),
                    expediente.getFechaModifica()
                    
            );
        }     
    }
    
    
    public ExpedienteResponse actualizarExpediente(Expediente expediente) throws SQLException 
    {
        String sql = "UPDATE EXPEDIENTE SET " +
                    " fecha_solicitud = ?, " +
                    " numero_tramite_documento = ?, " +
                    " tipo_solicitud = ?, " +
                    " tipo_documento = ?, " +
                    " dni_remitente = ?, " +
                    " apellido_nombre_remitente = ?, " +
                    " dni_solicitante = ?, " +
                    " apellido_nombre_solicitante = ?, " +
                    " tipo_procedimiento_registral = ?, " +
                    " tipo_acta = ?, " +
                    " numero_acta = ?, " +
                    " tipo_grupo_familiar = ?, " +
                    " numero_grupo_familiar = ?, " +
                    " dni_titular = ?, " +
                    " apellido_nombre_titular = ?, " +
                    " estado = ?, " +
                    " id_usuario_modifica = ?, " +
                    " fecha_modifica = ? " +
                    " WHERE id_expediente = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            // Datos para actualizar
            stmt.setDate(1, new java.sql.Date(System.currentTimeMillis())); // fecha_solicitud
            stmt.setString(2, expediente.getNumeroTramiteDocumento());
            stmt.setInt(3, expediente.getTipoSolicitud());
            stmt.setInt(4, expediente.getTipoDocumento());
            stmt.setString(5, expediente.getDniRemitente());
            stmt.setString(6, expediente.getApellidoNombreRemitente());
            stmt.setInt(9, expediente.getTipoProcedimientoRegistral());
            stmt.setInt(10, expediente.getTipoActa());
            stmt.setString(11, expediente.getNumeroActa());
            stmt.setInt(12, expediente.getTipoGrupoFamiliar());
            stmt.setString(14, expediente.getDniTitular());
            stmt.setString(15, expediente.getApellidoNombreTitular());
            stmt.setInt(16, expediente.getEstado());
            stmt.setInt(17, expediente.getIdUsuarioModifica());
            stmt.setDate(18, new java.sql.Date(System.currentTimeMillis())); // fecha_modifica

            // WHERE id_expediente = ?
            stmt.setInt(19, expediente.getIdExpediente());

            int rows = stmt.executeUpdate();

            if (rows == 0) 
            {
                throw new SQLException("No se encontró el expediente con ID: " + expediente.getIdExpediente());
            }
            
            java.sql.Date sqlFechaSolicitud = null;
            if (expediente.getFechaSolicitud() != null) {
                sqlFechaSolicitud = new java.sql.Date(expediente.getFechaSolicitud().getTime());
            } else {
                sqlFechaSolicitud = new java.sql.Date(System.currentTimeMillis());
            }

            java.sql.Date sqlFechaModifica = null;
            if (expediente.getFechaModifica() != null) {
                sqlFechaModifica = new java.sql.Date(expediente.getFechaModifica().getTime());
            } else {
                sqlFechaModifica = new java.sql.Date(System.currentTimeMillis());
            }
            
            java.sql.Date sqlFechaRegistra = null;
            if (expediente.getFechaRegistra()!= null) {
                sqlFechaRegistra = new java.sql.Date(expediente.getFechaRegistra().getTime());
            } else {
                sqlFechaRegistra = new java.sql.Date(System.currentTimeMillis());
            }
            
            // Devuelves la respuesta igual que en el INSERT
            return new ExpedienteResponse(
                    /*
                    expediente.getIdExpediente(),
                    //expediente.getFechaSolicitud(),
                    sqlFechaSolicitud,
                    expediente.getNumeroTramiteDocumento(),
                    expediente.getTipoSolicitud(),
                    expediente.getTipoDocumento(),
                    expediente.getDniRemitente(),
                    expediente.getApellidoNombreRemitente(),
                    expediente.getTipoProcedimientoRegistral(),
                    expediente.getTipoActa(),
                    expediente.getNumeroActa(),
                    expediente.getTipoGrupoFamiliar(),
                    expediente.getDniTitular(),
                    expediente.getApellidoNombreTitular(),
                    expediente.getEstado(),
                    expediente.getIdUsuarioCrea(),
                    //expediente.getFechaRegistra(),
                    sqlFechaRegistra,
                    expediente.getIdUsuarioModifica(),
                    //expediente.getFechaModifica()
                    sqlFechaModifica
                    */
                    expediente.getIdExpediente(),
                    expediente.getEsRegistroSdrerc(),
                    expediente.getHojaEnvioExpediente(),
                    expediente.getNumeroTramiteDocumento(),
                    expediente.getFechaRecepcion(),
                    expediente.getFechaSolicitud(),
                    expediente.getTipoDocumento(),
                    expediente.getNumeroDocumento(),
                    expediente.getTipoActa(),
                    expediente.getNumeroActa(),
                    expediente.getTipoGrupoFamiliar(),
                    expediente.getGradoParentesco(),
                    expediente.getTipoProcedimientoRegistral(),
                    expediente.getTipoSolicitud(),
                    expediente.getDniRemitente(),
                    expediente.getApellidoNombreRemitente(),
                    expediente.getUnidadOrganica(),
                    expediente.getDniTitular(),
                    expediente.getApellidoNombreTitular(),
                    expediente.getDepartamento(),
                    expediente.getProvincia(),
                    expediente.getDistrito(),
                    expediente.getDireccionDomiciliaria(),
                    expediente.getDomicilio(),
                    expediente.getCorreoElectronico(),
                    expediente.getCelular(),
                    expediente.getEstado(),
                    expediente.getIdUsuarioCrea(),
                    expediente.getFechaRegistra(),
                    expediente.getIdUsuarioModifica(),
                    expediente.getFechaModifica()
                    
            );
        }
    }
    
    
    
    public List<Expediente> listar() throws SQLException {
        List<Expediente> lista = new ArrayList<>();

        String sql = "SELECT * FROM EXPEDIENTE ORDER BY ID_EXPEDIENTE DESC";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }
    
    public List<Expediente> buscarPorEstado(int estado) throws SQLException {
        List<Expediente> lista = new ArrayList<>();

        String sql = "SELECT * FROM EXPEDIENTE WHERE ESTADO = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, estado);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }
    
    public List<Expediente> buscarPorCampo(String campo, String valor,int estadoItem) throws SQLException {
        List<Expediente> lista = new ArrayList<>();

        //String sql = "SELECT * FROM EXPEDIENTE WHERE " + campo + " LIKE ?";
        
        StringBuilder sql = new StringBuilder("SELECT * FROM EXPEDIENTE WHERE " + campo + " LIKE ? ");

        
        // Si el estado no es "TODOS", agregamos AND
        boolean filtrarEstado = estadoItem != 0;

        if (filtrarEstado) {
            sql.append("AND ESTADO = ?");
        }

        
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            ps.setString(1, "%" + valor + "%");
            
            if (filtrarEstado) {
                ps.setInt(2, estadoItem);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }
    
    public Expediente buscarPorId(int id) throws SQLException {
        Expediente lista = new Expediente();

        String sql = "SELECT * FROM EXPEDIENTE WHERE ID_EXPEDIENTE = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista = mapRow(rs); 
            }
        }
        return lista;
    }
    
    private Expediente mapRow(ResultSet rs) throws SQLException {
        return new Expediente(
                            rs.getInt("ID_EXPEDIENTE"),
                            rs.getString("ES_REGISTRO_SDRERC"),
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
