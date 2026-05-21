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
import java.sql.Types;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
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
                                "FECHA_SOLICITUD              AS \"fechaSolicitud\", " +
                                "CANAL_RECEPCION              AS \"canalRecepcion\", " +
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
                            rs.getDate("FECHA_SOLICITUD"),
                            rs.getString("CANAL_RECEPCION"),
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
        boolean soportaSegundoTitular = soportaSegundoTitular();
        String sql = construirSqlInsertExpediente(soportaSegundoTitular);
        

        try (Connection conn = OracleConnection.getConnection()) 
        {
            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE"})) 
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
            
            int index = 1;
            stmt.setInt(index++, expediente.getEsRegistroSdrerc());
            stmt.setString(index++, expediente.getHojaEnvioExpediente());
            stmt.setString(index++, expediente.getNumeroTramiteDocumento());
            stmt.setString(index++, expediente.getCanalRecepcion());
            setNullableDate(stmt, index++, expediente.getFechaSolicitud());
            stmt.setInt(index++, expediente.getTipoDocumento());
            stmt.setString(index++, expediente.getNumeroDocumento());
            stmt.setInt(index++, expediente.getTipoActa());
            stmt.setString(index++, expediente.getNumeroActa());
            stmt.setInt(index++, expediente.getTipoGrupoFamiliar());
            stmt.setInt(index++, expediente.getGradoParentesco());
            stmt.setInt(index++, expediente.getTipoProcedimientoRegistral());
            stmt.setInt(index++, expediente.getTipoSolicitud());
            stmt.setString(index++, expediente.getDniRemitente());
            stmt.setString(index++, expediente.getApellidoNombreRemitente());
            setNullableCatalogId(stmt, index++, expediente.getUnidadOrganica());
            stmt.setString(index++, expediente.getDniTitular());
            stmt.setString(index++, expediente.getApellidoNombreTitular());
            if (soportaSegundoTitular) {
                stmt.setString(index++, expediente.getDniTitular2());
                stmt.setString(index++, expediente.getApellidoNombreTitular2());
            }
            stmt.setInt(index++, expediente.getDepartamento());
            stmt.setInt(index++, expediente.getProvincia());
            stmt.setInt(index++, expediente.getDistrito());
            stmt.setInt(index++, expediente.getDireccionDomiciliaria());
            stmt.setString(index++, expediente.getDomicilio());
            stmt.setString(index++, expediente.getCorreoElectronico());
            stmt.setString(index++, expediente.getCelular());
            stmt.setInt(index++, expediente.getEstado());
            stmt.setInt(index++, expediente.getIdUsuarioCrea());
            stmt.setDate(index, new java.sql.Date(System.currentTimeMillis()));
            
            stmt.executeUpdate();

            // Obtener el ID generado (IDENTITY)
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int idGenerado = rs.getInt(1);
                expediente.setIdExpediente(idGenerado);
            }

            if (expediente.getIdExpediente() > 0) {
                expediente.setNumExpediente(asignarNumeroExpediente(conn, expediente));
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

            ExpedienteResponse response = new ExpedienteResponse(
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
                    expediente.getFechaSolicitud(),
                    expediente.getCanalRecepcion(),
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
            conn.commit();
            return response;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }
        }     
    }
    
    
    public ExpedienteResponse actualizarExpediente(Expediente expediente) throws SQLException 
    {
        boolean soportaSegundoTitular = soportaSegundoTitular();
        String sql = construirSqlUpdateExpediente(soportaSegundoTitular);

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) 
        {
            // Datos para actualizar
            int index = 1;
            stmt.setInt(index++, expediente.getEsRegistroSdrerc());
            stmt.setString(index++, expediente.getHojaEnvioExpediente());
            stmt.setString(index++, expediente.getNumeroTramiteDocumento());
            stmt.setString(index++, expediente.getCanalRecepcion());
            setNullableDate(stmt, index++, expediente.getFechaSolicitud());
            stmt.setInt(index++, expediente.getTipoDocumento());
            stmt.setString(index++, expediente.getNumeroDocumento());
            stmt.setInt(index++, expediente.getTipoActa());
            stmt.setString(index++, expediente.getNumeroActa());
            stmt.setInt(index++, expediente.getTipoGrupoFamiliar());
            stmt.setInt(index++, expediente.getGradoParentesco());
            stmt.setInt(index++, expediente.getTipoProcedimientoRegistral());
            stmt.setInt(index++, expediente.getTipoSolicitud());
            stmt.setString(index++, expediente.getDniRemitente());
            stmt.setString(index++, expediente.getApellidoNombreRemitente());
            setNullableCatalogId(stmt, index++, expediente.getUnidadOrganica());
            stmt.setString(index++, expediente.getDniTitular());
            stmt.setString(index++, expediente.getApellidoNombreTitular());
            if (soportaSegundoTitular) {
                stmt.setString(index++, expediente.getDniTitular2());
                stmt.setString(index++, expediente.getApellidoNombreTitular2());
            }
            stmt.setInt(index++, expediente.getDepartamento());
            stmt.setInt(index++, expediente.getProvincia());
            stmt.setInt(index++, expediente.getDistrito());
            stmt.setInt(index++, expediente.getDireccionDomiciliaria());
            stmt.setString(index++, expediente.getDomicilio());
            stmt.setString(index++, expediente.getCorreoElectronico());
            stmt.setString(index++, expediente.getCelular());
            stmt.setInt(index++, expediente.getEstado());
            stmt.setInt(index++, expediente.getIdUsuarioModifica());
            stmt.setDate(index++, new java.sql.Date(System.currentTimeMillis()));
            stmt.setInt(index, expediente.getIdExpediente());

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
                    expediente.getFechaSolicitud(),
                    expediente.getCanalRecepcion(),
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

        String sql = construirSelectExpedienteConAsignacion()
                + "ORDER BY e.ID_EXPEDIENTE DESC";

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

        String sql = construirSelectExpedienteConAsignacion()
                + "WHERE e.ESTADO = ?";

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
        
        StringBuilder sql = new StringBuilder(construirSelectExpedienteConAsignacion())
                .append("WHERE e.")
                .append(campo)
                .append(" LIKE ? ");

        
        // Si el estado no es "TODOS", agregamos AND
        boolean filtrarEstado = estadoItem != 0;

        if (filtrarEstado) {
            sql.append("AND e.ESTADO = ?");
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

        String sql = construirSelectExpedienteConAsignacion()
                + "WHERE e.ID_EXPEDIENTE = ?";

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

    public Expediente buscarDuplicadoPorActaYTitular(Expediente expediente, boolean validarDosTitulares) throws SQLException {
        String numeroActa = textoSeguro(expediente.getNumeroActa()).trim();
        String dniTitular = textoSeguro(expediente.getDniTitular()).trim();
        String dniTitular2 = textoSeguro(expediente.getDniTitular2()).trim();

        if (numeroActa.isEmpty() || dniTitular.isEmpty()) {
            return null;
        }

        StringBuilder sql = new StringBuilder("SELECT e.*, ci.DESCRIPCION AS ESTADO_DESCRIPCION FROM EXPEDIENTE e ")
                .append("LEFT JOIN CATALOGO_ITEM ci ON ci.ID_CATALOGO_ITEM = e.ESTADO ")
                .append("WHERE TRIM(e.NUMERO_ACTA) = ? ");
        if (expediente.getIdExpediente() > 0) {
            sql.append("AND e.ID_EXPEDIENTE <> ? ");
        }
        sql.append("AND (TRIM(e.DNI_TITULAR) = ? ");
        if (validarDosTitulares) {
            sql.append("OR TRIM(e.DNI_TITULAR_2) = ? ");
        }
        if (validarDosTitulares && !dniTitular2.isEmpty()) {
            sql.append("OR TRIM(e.DNI_TITULAR) = ? OR TRIM(e.DNI_TITULAR_2) = ? ");
        }
        sql.append(") AND ROWNUM = 1");

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            ps.setString(index++, numeroActa);
            if (expediente.getIdExpediente() > 0) {
                ps.setInt(index++, expediente.getIdExpediente());
            }
            ps.setString(index++, dniTitular);
            if (validarDosTitulares) {
                ps.setString(index++, dniTitular);
            }
            if (validarDosTitulares && !dniTitular2.isEmpty()) {
                ps.setString(index++, dniTitular2);
                ps.setString(index, dniTitular2);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Expediente buscarDuplicadoPorActaYNombreTitular(String numeroActa, String titular1, String titular2) throws SQLException {
        String acta = normalizarComparacion(numeroActa);
        String nombreTitular1 = normalizarComparacion(titular1);
        String nombreTitular2 = normalizarComparacion(titular2);

        if (acta.isEmpty() || nombreTitular1.isEmpty()) {
            return null;
        }

        boolean validarSegundoTitular = !nombreTitular2.isEmpty();
        String sql = "SELECT e.*, ci.DESCRIPCION AS ESTADO_DESCRIPCION "
                + "FROM EXPEDIENTE e "
                + "LEFT JOIN CATALOGO_ITEM ci ON ci.ID_CATALOGO_ITEM = e.ESTADO "
                + "WHERE UPPER(TRIM(e.NUMERO_ACTA)) = ? "
                + "AND (UPPER(TRIM(REGEXP_REPLACE(e.APELLIDO_NOMBRE_TITULAR, '\\s+', ' '))) = ? "
                + "OR UPPER(TRIM(REGEXP_REPLACE(e.APELLIDO_NOMBRE_TITULAR_2, '\\s+', ' '))) = ? "
                + (validarSegundoTitular
                    ? "OR UPPER(TRIM(REGEXP_REPLACE(e.APELLIDO_NOMBRE_TITULAR, '\\s+', ' '))) = ? "
                        + "OR UPPER(TRIM(REGEXP_REPLACE(e.APELLIDO_NOMBRE_TITULAR_2, '\\s+', ' '))) = ? "
                    : "")
                + ") AND ROWNUM = 1";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            ps.setString(index++, acta);
            ps.setString(index++, nombreTitular1);
            ps.setString(index++, nombreTitular1);
            if (validarSegundoTitular) {
                ps.setString(index++, nombreTitular2);
                ps.setString(index, nombreTitular2);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Expediente buscarDuplicadoActivoPorActaYNombreTitular(String numeroActa, String titular1, String titular2) throws SQLException {
        Expediente expediente = new Expediente();
        expediente.setIdExpediente(0);
        expediente.setNumeroActa(numeroActa);
        expediente.setApellidoNombreTitular(titular1);
        expediente.setApellidoNombreTitular2(titular2);
        try (Connection conn = OracleConnection.getConnection()) {
            return buscarDuplicadoActivoParaNumeroExpediente(conn, expediente);
        }
    }

    private String construirSelectExpedienteConAsignacion()
    {
        return "SELECT e.*, ea.FECHA_ASIGNACION, "
                + "CASE WHEN t.ID_TECNICO IS NULL THEN au.FULL_NAME "
                + "ELSE TRIM(NVL(t.APELLIDO_PATERNO, '') || ' ' || NVL(t.APELLIDO_MATERNO, '') || ', ' || NVL(t.NOMBRES, '')) END AS ABOGADO_DESIGNADO, "
                + "CASE WHEN ts.ID_TECNICO IS NULL THEN su.FULL_NAME "
                + "ELSE TRIM(NVL(ts.APELLIDO_PATERNO, '') || ' ' || NVL(ts.APELLIDO_MATERNO, '') || ', ' || NVL(ts.NOMBRES, '')) END AS SUPERVISOR_DESIGNADO "
                + "FROM EXPEDIENTE e "
                + "LEFT JOIN EXPEDIENTE_ASIGNACION ea ON e.ID_EXPEDIENTE = ea.ID_EXPEDIENTE "
                + "AND ea.ACTIVE = 1 AND ea.ETAPA_FLUJO IS NULL "
                + "LEFT JOIN TECNICO t ON t.ID_TECNICO = ea.ID_TECNICO "
                + "LEFT JOIN (SELECT ID_TECNICO, MAX(USER_ID) AS USER_ID, MAX(FULL_NAME) AS FULL_NAME "
                + "FROM APP_USERS WHERE ID_TECNICO IS NOT NULL GROUP BY ID_TECNICO) au ON au.ID_TECNICO = ea.ID_TECNICO "
                + "LEFT JOIN (SELECT ABOGADO_ID, MAX(SUPERVISOR_ID) AS SUPERVISOR_ID "
                + "FROM APP_USER_SUPERVISION GROUP BY ABOGADO_ID) aus ON aus.ABOGADO_ID = au.USER_ID "
                + "LEFT JOIN APP_USERS su ON su.USER_ID = aus.SUPERVISOR_ID "
                + "LEFT JOIN TECNICO ts ON ts.ID_TECNICO = su.ID_TECNICO ";
    }
    
    private Expediente mapRow(ResultSet rs) throws SQLException {
        Expediente expediente = new Expediente(
                            rs.getInt("ID_EXPEDIENTE"),
                            rs.getInt("ES_REGISTRO_SDRERC"),
                            rs.getString("HOJA_ENVIO_EXPEDIENTE"),
                            rs.getString("NUMERO_TRAMITE_DOCUMENTO"),
                            rs.getDate("FECHA_SOLICITUD"),
                            rs.getString("CANAL_RECEPCION"),
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
        if (resultSetTieneColumna(rs, "DNI_TITULAR_2")) {
            expediente.setDniTitular2(rs.getString("DNI_TITULAR_2"));
        }
        if (resultSetTieneColumna(rs, "APELLIDO_NOMBRE_TITULAR_2")) {
            expediente.setApellidoNombreTitular2(rs.getString("APELLIDO_NOMBRE_TITULAR_2"));
        }
        if (resultSetTieneColumna(rs, "NUM_EXPEDIENTE")) {
            expediente.setNumExpediente(rs.getString("NUM_EXPEDIENTE"));
        }
        if (resultSetTieneColumna(rs, "ESTADO_DESCRIPCION")) {
            expediente.setEstadoDescripcion(rs.getString("ESTADO_DESCRIPCION"));
        }
        if (resultSetTieneColumna(rs, "ABOGADO_DESIGNADO")) {
            expediente.setAbogadoDesignado(rs.getString("ABOGADO_DESIGNADO"));
        }
        if (resultSetTieneColumna(rs, "SUPERVISOR_DESIGNADO")) {
            expediente.setSupervisorDesignado(rs.getString("SUPERVISOR_DESIGNADO"));
        }
        if (resultSetTieneColumna(rs, "FECHA_ASIGNACION")) {
            expediente.setFechaAsignacion(rs.getDate("FECHA_ASIGNACION"));
        }
        return expediente;
    }

    private String textoSeguro(String value) {
        return value == null ? "" : value;
    }

    private String normalizarComparacion(String value) {
        String normalized = Normalizer.normalize(textoSeguro(value).trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.replaceAll("\\s+", " ").toUpperCase();
    }

    private String asignarNumeroExpediente(Connection conn, Expediente expediente) throws SQLException {
        if (!existeColumnaExpediente(conn, "NUM_EXPEDIENTE")) {
            throw new SQLException("La tabla EXPEDIENTE no tiene la columna NUM_EXPEDIENTE.");
        }

        Expediente principal = buscarDuplicadoActivoParaNumeroExpediente(conn, expediente);
        String numeroExpediente;
        if (principal != null && principal.getIdExpediente() > 0) {
            numeroExpediente = textoSeguro(principal.getNumExpediente()).trim();
            if (numeroExpediente.isEmpty()) {
                numeroExpediente = generarNumeroExpediente(conn);
                actualizarNumeroExpediente(conn, principal.getIdExpediente(), numeroExpediente);
            }
        } else {
            numeroExpediente = generarNumeroExpediente(conn);
        }

        actualizarNumeroExpediente(conn, expediente.getIdExpediente(), numeroExpediente);
        return numeroExpediente;
    }

    private Expediente buscarDuplicadoActivoParaNumeroExpediente(Connection conn, Expediente expediente) throws SQLException {
        String acta = normalizarComparacion(expediente.getNumeroActa());
        String titular1 = normalizarComparacion(expediente.getApellidoNombreTitular());
        String titular2 = normalizarComparacion(expediente.getApellidoNombreTitular2());
        if (acta.isEmpty() || titular1.isEmpty()) {
            return null;
        }

        boolean validarSegundoTitular = !titular2.isEmpty();
        String textoNormalizadoActa = expresionSqlNormalizada("e.NUMERO_ACTA");
        String textoNormalizadoTitular1 = expresionSqlNormalizada("e.APELLIDO_NOMBRE_TITULAR");
        String textoNormalizadoTitular2 = expresionSqlNormalizada("e.APELLIDO_NOMBRE_TITULAR_2");
        String textoNormalizadoEstado = expresionSqlNormalizada("ci.DESCRIPCION");

        String sql = "SELECT * FROM ("
                + "SELECT e.*, ci.DESCRIPCION AS ESTADO_DESCRIPCION "
                + "FROM EXPEDIENTE e "
                + "LEFT JOIN CATALOGO_ITEM ci ON ci.ID_CATALOGO_ITEM = e.ESTADO "
                + "WHERE e.ID_EXPEDIENTE <> ? "
                + "AND " + textoNormalizadoActa + " = ? "
                + "AND (ci.DESCRIPCION IS NULL OR " + textoNormalizadoEstado + " NOT IN (?, ?, ?, ?, ?)) "
                + "AND (" + textoNormalizadoTitular1 + " = ? "
                + (validarSegundoTitular ? "OR " + textoNormalizadoTitular1 + " = ? " : "")
                + "OR " + textoNormalizadoTitular2 + " = ? "
                + (validarSegundoTitular ? "OR " + textoNormalizadoTitular2 + " = ? " : "")
                + ") "
                + "ORDER BY e.FECHA_SOLICITUD ASC NULLS LAST, e.ID_EXPEDIENTE ASC"
                + ") WHERE ROWNUM = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            ps.setInt(index++, expediente.getIdExpediente());
            ps.setString(index++, acta);
            ps.setString(index++, "EJECUTADO");
            ps.setString(index++, "ABANDONO");
            ps.setString(index++, "ARCHIVADO");
            ps.setString(index++, "NOTIFICADO AL CIUDADANO");
            ps.setString(index++, "NOTIFICADO");
            ps.setString(index++, titular1);
            if (validarSegundoTitular) {
                ps.setString(index++, titular2);
            }
            ps.setString(index++, titular1);
            if (validarSegundoTitular) {
                ps.setString(index++, titular2);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    private void actualizarNumeroExpediente(Connection conn, int idExpediente, String numeroExpediente) throws SQLException {
        String sql = "UPDATE EXPEDIENTE SET NUM_EXPEDIENTE = ? WHERE ID_EXPEDIENTE = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroExpediente);
            ps.setInt(2, idExpediente);
            ps.executeUpdate();
        }
    }

    private String generarNumeroExpediente(Connection conn) throws SQLException {
        int anio = obtenerAnioRegistroExpediente();
        int correlativo = obtenerSiguienteCorrelativoExpediente(conn, anio);
        return String.format("SDRERC_EXP_%d_%06d", anio, correlativo);
    }

    private int obtenerAnioRegistroExpediente() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }

    private int obtenerSiguienteCorrelativoExpediente(Connection conn, int anio) throws SQLException {
        bloquearNumeracionExpediente(conn);
        String prefijo = "SDRERC_EXP_" + anio + "_";
        int posicionCorrelativo = prefijo.length() + 1;
        String sql = "SELECT NVL(MAX(CASE "
                + "WHEN REGEXP_LIKE(SUBSTR(NUM_EXPEDIENTE, ?), '^[0-9]+$') "
                + "THEN TO_NUMBER(SUBSTR(NUM_EXPEDIENTE, ?)) "
                + "END), 0) + 1 AS SIGUIENTE "
                + "FROM EXPEDIENTE "
                + "WHERE NUM_EXPEDIENTE LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, posicionCorrelativo);
            ps.setInt(2, posicionCorrelativo);
            ps.setString(3, prefijo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("SIGUIENTE");
                }
            }
        }
        return 1;
    }

    private void bloquearNumeracionExpediente(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("LOCK TABLE EXPEDIENTE IN EXCLUSIVE MODE")) {
            ps.execute();
        }
    }

    private String expresionSqlNormalizada(String columna) {
        return "REGEXP_REPLACE(TRANSLATE(UPPER(TRIM(NVL(" + columna + ", ''))), "
                + "'ÁÀÄÂÉÈËÊÍÌÏÎÓÒÖÔÚÙÜÛÑ', 'AAAAEEEEIIIIOOOOUUUUN'), '\\s+', ' ')";
    }

    private void setNullableDate(PreparedStatement stmt, int index, java.util.Date value) throws SQLException {
        if (value != null) {
            stmt.setDate(index, new java.sql.Date(value.getTime()));
        } else {
            stmt.setNull(index, java.sql.Types.DATE);
        }
    }

    private void setNullableCatalogId(PreparedStatement stmt, int index, int value) throws SQLException {
        if (value <= 0) {
            stmt.setNull(index, Types.NUMERIC);
        } else {
            stmt.setInt(index, value);
        }
    }

    public boolean soportaSegundoTitular() throws SQLException {
        try (Connection conn = OracleConnection.getConnection()) {
            return existeColumnaExpediente(conn, "DNI_TITULAR_2")
                    && existeColumnaExpediente(conn, "APELLIDO_NOMBRE_TITULAR_2");
        }
    }

    private boolean existeColumnaExpediente(Connection conn, String columna) throws SQLException {
        String sql = "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'EXPEDIENTE' AND COLUMN_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, columna);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean resultSetTieneColumna(ResultSet rs, String columna) throws SQLException {
        int total = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= total; i++) {
            if (columna.equalsIgnoreCase(rs.getMetaData().getColumnName(i))) {
                return true;
            }
        }
        return false;
    }

    private String construirSqlInsertExpediente(boolean incluirSegundoTitular) {
        StringBuilder columnas = new StringBuilder("INSERT INTO EXPEDIENTE (")
                .append("ES_REGISTRO_SDRERC, HOJA_ENVIO_EXPEDIENTE, NUMERO_TRAMITE_DOCUMENTO, ")
                .append("CANAL_RECEPCION, FECHA_SOLICITUD, TIPO_DOCUMENTO, NUMERO_DOCUMENTO, ")
                .append("TIPO_ACTA, NUMERO_ACTA, TIPO_GRUPO_FAMILIAR, GRADO_PARENTESCO, ")
                .append("TIPO_PROCEDIMIENTO_REGISTRAL, TIPO_SOLICITUD, DNI_REMITENTE, ")
                .append("APELLIDO_NOMBRE_REMITENTE, UNIDAD_ORGANICA, DNI_TITULAR, ")
                .append("APELLIDO_NOMBRE_TITULAR");
        StringBuilder valores = new StringBuilder(") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?");

        if (incluirSegundoTitular) {
            columnas.append(", DNI_TITULAR_2, APELLIDO_NOMBRE_TITULAR_2");
            valores.append(",?,?");
        }

        columnas.append(", DEPARTAMENTO, PROVINCIA, DISTRITO, DIRECCION_DOMICILIARIA, ")
                .append("DOMICILIO, CORREO_ELECTRONICO, CELULAR, ESTADO, ID_USUARIO_CREA, FECHA_REGISTRA");
        valores.append(",?,?,?,?,?,?,?,?,?,?)");
        return columnas.append(valores).toString();
    }

    private String construirSqlUpdateExpediente(boolean incluirSegundoTitular) {
        StringBuilder sql = new StringBuilder("UPDATE EXPEDIENTE SET ")
                .append("ES_REGISTRO_SDRERC = ?, HOJA_ENVIO_EXPEDIENTE = ?, ")
                .append("NUMERO_TRAMITE_DOCUMENTO = ?, CANAL_RECEPCION = ?, FECHA_SOLICITUD = ?, ")
                .append("TIPO_DOCUMENTO = ?, NUMERO_DOCUMENTO = ?, TIPO_ACTA = ?, NUMERO_ACTA = ?, ")
                .append("TIPO_GRUPO_FAMILIAR = ?, GRADO_PARENTESCO = ?, ")
                .append("TIPO_PROCEDIMIENTO_REGISTRAL = ?, TIPO_SOLICITUD = ?, DNI_REMITENTE = ?, ")
                .append("APELLIDO_NOMBRE_REMITENTE = ?, UNIDAD_ORGANICA = ?, DNI_TITULAR = ?, ")
                .append("APELLIDO_NOMBRE_TITULAR = ?");

        if (incluirSegundoTitular) {
            sql.append(", DNI_TITULAR_2 = ?, APELLIDO_NOMBRE_TITULAR_2 = ?");
        }

        sql.append(", DEPARTAMENTO = ?, PROVINCIA = ?, DISTRITO = ?, DIRECCION_DOMICILIARIA = ?, ")
                .append("DOMICILIO = ?, CORREO_ELECTRONICO = ?, CELULAR = ?, ESTADO = ?, ")
                .append("ID_USUARIO_MODIFICA = ?, FECHA_MODIFICA = ? WHERE ID_EXPEDIENTE = ?");
        return sql.toString();
    }
    
    
}
