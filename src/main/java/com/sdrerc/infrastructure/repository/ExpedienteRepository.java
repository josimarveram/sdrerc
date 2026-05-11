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

    public Expediente buscarDuplicadoPorActaYTitular(Expediente expediente, boolean validarDosTitulares) throws SQLException {
        String numeroActa = textoSeguro(expediente.getNumeroActa()).trim();
        String dniTitular = textoSeguro(expediente.getDniTitular()).trim();
        String dniTitular2 = textoSeguro(expediente.getDniTitular2()).trim();

        if (numeroActa.isEmpty() || dniTitular.isEmpty()) {
            return null;
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM EXPEDIENTE WHERE TRIM(NUMERO_ACTA) = ? ");
        if (expediente.getIdExpediente() > 0) {
            sql.append("AND ID_EXPEDIENTE <> ? ");
        }
        sql.append("AND (TRIM(DNI_TITULAR) = ? ");
        if (validarDosTitulares) {
            sql.append("OR TRIM(DNI_TITULAR_2) = ? ");
        }
        if (validarDosTitulares && !dniTitular2.isEmpty()) {
            sql.append("OR TRIM(DNI_TITULAR) = ? OR TRIM(DNI_TITULAR_2) = ? ");
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
        return expediente;
    }

    private String textoSeguro(String value) {
        return value == null ? "" : value;
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
