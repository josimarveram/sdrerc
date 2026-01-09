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
        String sql = "SELECT " +
                                "ID_EXPEDIENTE                AS \"idExpediente\", " +
                                "ES_REGISTRO_SDRERC           AS \"esRegistroSdrerc\", " +
                                "HOJA_ENVIO_EXPEDIENTE        AS \"hojaEnvioExpediente\", " +
                                "NUMERO_TRAMITE_DOCUMENTO     AS \"numeroTramiteDocumento\", " +
                                "FECHA_RECEPCION              AS \"fechaRecepcion\", " +
                                "FECHA_SOLICITUD              AS \"fechaSolicitud\", " +
                                "TIPO_DOCUMENTO               AS \"tipoDocumento\", " +
                                "NUMERO_DOCUMENTO             AS \"numeroDocumento\", " +
                                "TIPO_ACTA                    AS \"tipoActa\", " +
                                "NUMERO_ACTA                  AS \"numeroActa\", " +
                                "TIPO_GRUPO_FAMILIAR          AS \"tipoGrupoFamiliar\", " +
                                "GRADO_PARENTESCO             AS \"gradoParentesco\", " +
                                "TIPO_PROCEDIMIENTO_REGISTRAL AS \"tipoProcedimientoRegistral\", " +
                                "TIPO_SOLICITUD               AS \"tipoSolicitud\", " +
                                "DNI_REMITENTE                AS \"dniRemitente\", " +
                                "APELLIDO_NOMBRE_REMITENTE    AS \"apellidoNombreRemitente\", " +
                                "UNIDAD_ORGANICA              AS \"unidadOrganica\", " +
                                "DNI_TITULAR                  AS \"dniTitular\", " +
                                "APELLIDO_NOMBRE_TITULAR      AS \"apellidoNombreTitular\", " +
                                "DEPARTAMENTO                 AS \"departamento\", " +
                                "PROVINCIA                    AS \"provincia\", " +
                                "DISTRITO                     AS \"distrito\", " +
                                "DIRECCION_DOMICILIARIA       AS \"direccionDomiciliaria\", " +
                                "DOMICILIO                    AS \"domicilio\", " +
                                "CORREO_ELECTRONICO           AS \"correoElectronico\", " +
                                "CELULAR                      AS \"celular\", " +
                                "ESTADO                       AS \"estado\", " +
                                "ID_USUARIO_CREA              AS \"idUsuarioCrea\", " +
                                "FECHA_REGISTRA               AS \"fechaRegistra\", " +
                                "ID_USUARIO_MODIFICA          AS \"idUsuarioModifica\", " +
                                "FECHA_MODIFICA               AS \"fechaModifica\" " +
                                "FROM EXPEDIENTE";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) 
            {
                return new ExpedienteResponse(
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
        return null; 
    }
    
    public ExpedienteResponse agregarExpediente(Expediente expediente) throws SQLException 
    {        
        String sql = "INSERT INTO EXPEDIENTE ( " +
			"ES_REGISTRO_SDRERC, " +
			"HOJA_ENVIO_EXPEDIENTE, " +
			"NUMERO_TRAMITE_DOCUMENTO, " +
			"FECHA_RECEPCION, " +
			"FECHA_SOLICITUD, " +
			"TIPO_DOCUMENTO, " +
			"NUMERO_DOCUMENTO, " +
			"TIPO_ACTA, " +
			"NUMERO_ACTA, " +
			"TIPO_GRUPO_FAMILIAR, " +
			"GRADO_PARENTESCO, " +
			"TIPO_PROCEDIMIENTO_REGISTRAL, " +
			"TIPO_SOLICITUD, " +
			"DNI_REMITENTE, " +
			"APELLIDO_NOMBRE_REMITENTE, " +
			"UNIDAD_ORGANICA, " +
			"DNI_TITULAR, " +
			"APELLIDO_NOMBRE_TITULAR, " +
			"DEPARTAMENTO, " +
			"PROVINCIA, " +
			"DISTRITO, " +
			"DIRECCION_DOMICILIARIA, " +
			"DOMICILIO, " +
			"CORREO_ELECTRONICO, " +
			"CELULAR, " +
			"ESTADO, " +
			"ID_USUARIO_CREA, " +
			"FECHA_REGISTRA " +
			") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE"})) 
        {   
            /*
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
            */
            
            stmt.setInt(1, expediente.getEsRegistroSdrerc());
            stmt.setString(2, expediente.getHojaEnvioExpediente());
            stmt.setString(3, expediente.getNumeroTramiteDocumento());
            stmt.setDate(4, new java.sql.Date(System.currentTimeMillis())); //stmt.setDate(4, new java.sql.Date(expediente.getFechaRecepcion().getTime()));
            stmt.setDate(5, new java.sql.Date(System.currentTimeMillis())); //stmt.setDate(5, new java.sql.Date(expediente.getFechaSolicitud().getTime()));
            stmt.setInt(6, expediente.getTipoDocumento());
            stmt.setString(7, expediente.getNumeroDocumento());
            stmt.setInt(8, expediente.getTipoActa());
            stmt.setString(9, expediente.getNumeroActa());
            stmt.setInt(10, expediente.getTipoGrupoFamiliar());
            stmt.setInt(11, expediente.getGradoParentesco());
            stmt.setInt(12, expediente.getTipoProcedimientoRegistral());
            stmt.setInt(13, expediente.getTipoSolicitud());
            stmt.setString(14, expediente.getDniRemitente());
            stmt.setString(15, expediente.getApellidoNombreRemitente());
            stmt.setInt(16, expediente.getUnidadOrganica());
            stmt.setString(17, expediente.getDniTitular());
            stmt.setString(18, expediente.getApellidoNombreTitular());
            stmt.setInt(19, expediente.getDepartamento());
            stmt.setInt(20, expediente.getProvincia());
            stmt.setInt(21, expediente.getDistrito());
            stmt.setInt(22, expediente.getDireccionDomiciliaria());
            stmt.setString(23, expediente.getDomicilio());
            stmt.setString(24, expediente.getCorreoElectronico());
            stmt.setString(25, expediente.getCelular());
            stmt.setInt(26, expediente.getEstado());
            stmt.setInt(27, expediente.getIdUsuarioCrea());
            stmt.setDate(28, new java.sql.Date(System.currentTimeMillis())); //stmt.setDate(28, new java.sql.Date(expediente.getFechaRegistra().getTime()));           
            
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
                    sqlFechaModifica                    
            );
        }     
    }
    
    
    public ExpedienteResponse actualizarExpediente(Expediente expediente) throws SQLException 
    {
        String sql = "UPDATE EXPEDIENTE SET " +
                    "ES_REGISTRO_SDRERC = ?, " +
                    "HOJA_ENVIO_EXPEDIENTE = ?, " +
                    "NUMERO_TRAMITE_DOCUMENTO = ?, " +
                    "FECHA_RECEPCION = ?, " +
                    "FECHA_SOLICITUD = ?, " +
                    "TIPO_DOCUMENTO = ?, " +
                    "NUMERO_DOCUMENTO = ?, " +
                    "TIPO_ACTA = ?, " +
                    "NUMERO_ACTA = ?, " +
                    "TIPO_GRUPO_FAMILIAR = ?, " +
                    "GRADO_PARENTESCO = ?, " +
                    "TIPO_PROCEDIMIENTO_REGISTRAL = ?, " +
                    "TIPO_SOLICITUD = ?, " +
                    "DNI_REMITENTE = ?, " +
                    "APELLIDO_NOMBRE_REMITENTE = ?, " +
                    "UNIDAD_ORGANICA = ?, " +
                    "DNI_TITULAR = ?, " +
                    "APELLIDO_NOMBRE_TITULAR = ?, " +
                    "DEPARTAMENTO = ?, " +
                    "PROVINCIA = ?, " +
                    "DISTRITO = ?, " +
                    "DIRECCION_DOMICILIARIA = ?, " +
                    "DOMICILIO = ?, " +
                    "CORREO_ELECTRONICO = ?, " +
                    "CELULAR = ?, " +
                    "ESTADO = ?, " +
                    "ID_USUARIO_MODIFICA = ?, " +
                    "FECHA_MODIFICA = ? " +
                    "WHERE ID_EXPEDIENTE = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            // Datos para actualizar
            stmt.setInt(1, expediente.getEsRegistroSdrerc());
            stmt.setString(2, expediente.getHojaEnvioExpediente());
            stmt.setString(3, expediente.getNumeroTramiteDocumento());
            stmt.setDate(4, new java.sql.Date(expediente.getFechaRecepcion().getTime()));
            stmt.setDate(5, new java.sql.Date(expediente.getFechaSolicitud().getTime()));
            stmt.setInt(6, expediente.getTipoDocumento());
            stmt.setString(7, expediente.getNumeroDocumento());
            stmt.setInt(8, expediente.getTipoActa());
            stmt.setString(9, expediente.getNumeroActa());
            stmt.setInt(10, expediente.getTipoGrupoFamiliar());
            stmt.setInt(11, expediente.getGradoParentesco());
            stmt.setInt(12, expediente.getTipoProcedimientoRegistral());
            stmt.setInt(13, expediente.getTipoSolicitud());
            stmt.setString(14, expediente.getDniRemitente());
            stmt.setString(15, expediente.getApellidoNombreRemitente());
            stmt.setInt(16, expediente.getUnidadOrganica());
            stmt.setString(17, expediente.getDniTitular());
            stmt.setString(18, expediente.getApellidoNombreTitular());
            stmt.setInt(19, expediente.getDepartamento());
            stmt.setInt(20, expediente.getProvincia());
            stmt.setInt(21, expediente.getDistrito());
            stmt.setInt(22, expediente.getDireccionDomiciliaria());
            stmt.setString(23, expediente.getDomicilio());
            stmt.setString(24, expediente.getCorreoElectronico());
            stmt.setString(25, expediente.getCelular());
            stmt.setInt(26, expediente.getEstado());
            stmt.setInt(27, expediente.getIdUsuarioModifica());
            stmt.setDate(28, new java.sql.Date(expediente.getFechaModifica().getTime()));
            // WHERE
            stmt.setInt(29, expediente.getIdExpediente());

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
