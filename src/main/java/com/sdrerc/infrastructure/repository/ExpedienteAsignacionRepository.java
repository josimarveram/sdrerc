/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.infrastructure.database.OracleConnection;
import com.sdrerc.shared.constants.FlujoExpedienteConstants.DocumentoAnalizado;
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
                
                
            + "CORREO_ELECTRONICO = ? ,"
            + "CELULAR = ? ,"
            + "DOMICILIO = ? ,"
            + "DIRECCION_DOMICILIARIA = ? ,"
            + "GRADO_PARENTESCO = ? ,"
            + "UNIDAD_ORGANICA = ? "                
                
            + "WHERE ID_EXPEDIENTE = ?";
        
        // Luego, insertar en la tabla EXPEDIENTE_ASIGNACION
        String insertAsignacionSql = "INSERT INTO EXPEDIENTE_ASIGNACION "
                + "(ID_EXPEDIENTE, ID_TECNICO, FECHA_ASIGNACION, HOJA_ENVIO_ASIGNACION, ID_TIPO_PERSONAL_ASIGNACION, TIPO_PERSONAL_ASIGNACION) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        /*
        String sql = "INSERT INTO EXPEDIENTE_ASIGNACION "
                   + "(ID_EXPEDIENTE, ID_TECNICO, FECHA_ASIGNACION) "
                   + "VALUES (?, ?, ?)";
        */
        Connection conn = null;
        try {
             
            conn = OracleConnection.getConnection();
            conn.setAutoCommit(false); 

            if (existeAsignacionActivaPorEtapa(conn, asignacion.getIdExpediente(), null)) {
                throw new SQLException("El expediente ya cuenta con una asignación activa para esta etapa. No es posible asignarlo nuevamente.");
            }
                
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
                
                psUpdate.setString(16, expediente.getCorreoElectronico());
                psUpdate.setString(17, expediente.getCelular());
                psUpdate.setString(18, expediente.getDomicilio());
                psUpdate.setInt(19, expediente.getDireccionDomiciliaria());
                psUpdate.setInt(20, expediente.getGradoParentesco());
                psUpdate.setInt(21, expediente.getUnidadOrganica());                

                psUpdate.setInt(22, expediente.getIdExpediente()); // WHERE

                psUpdate.executeUpdate();
            }
            
            // 2️⃣ INSERT EXPEDIENTE_ASIGNACION
            TipoPersonalAsignacion tipoPersonal = obtenerTipoPersonalAsignacion(conn, asignacion.getIdTecnico());
            try (PreparedStatement psInsert = conn.prepareStatement(insertAsignacionSql)) {

                psInsert.setInt(1, asignacion.getIdExpediente());
                psInsert.setInt(2, asignacion.getIdTecnico());

                java.sql.Date fechaSql = new java.sql.Date(asignacion.getFechaAsignacion().getTime());
                psInsert.setDate(3, fechaSql);
                psInsert.setString(4, asignacion.getHojaEnvioAsignacion());
                if (tipoPersonal.idTipoPersonal == null) {
                    psInsert.setNull(5, java.sql.Types.NUMERIC);
                } else {
                    psInsert.setInt(5, tipoPersonal.idTipoPersonal);
                }
                psInsert.setString(6, tipoPersonal.descripcion);

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
    
    
    public boolean RegistrarAsigancionExpedienteTO(ExpedienteAsignacion oExpedienteAsignacion) throws SQLException 
    {
        String insertAsignacionSql = "INSERT INTO EXPEDIENTE_ASIGNACION "
                + "(ID_EXPEDIENTE, ID_TECNICO, FECHA_ASIGNACION, HOJA_ENVIO_ASIGNACION, ETAPA_FLUJO, TIPO_PROCEDIMIENTO_REGISTRAL, NUMERO_RESOLUCION, TIPO_ACTA, ID_TIPO_PERSONAL_ASIGNACION, TIPO_PERSONAL_ASIGNACION) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
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

            if (existeAsignacionActivaPorEtapa(conn, oExpedienteAsignacion.getIdExpediente(), oExpedienteAsignacion.getEtapaFlujo())) {
                throw new SQLException("El expediente ya cuenta con una asignación activa para esta etapa. No es posible asignarlo nuevamente.");
            }
            
            try(PreparedStatement psupdateExpediente = conn.prepareStatement(updateExpedienteSql))
            {
                // Datos para actualizar
                psupdateExpediente.setInt(1, oExpedienteAsignacion.getEtapaFlujo());   // IdEstadoExpediente                
                psupdateExpediente.setInt(2, oExpedienteAsignacion.getIdUsuarioModifica());    // id_usuario_modifica    
                psupdateExpediente.setDate(3, new java.sql.Date(System.currentTimeMillis()));  // fecha_modifica                
                psupdateExpediente.setInt(4, oExpedienteAsignacion.getIdExpediente());         // WHERE id_expediente = ?                
                psupdateExpediente.executeUpdate();
            } 
            
            // 2️⃣ INSERT EXPEDIENTE_ASIGNACION
            TipoPersonalAsignacion tipoPersonal = obtenerTipoPersonalAsignacion(conn, oExpedienteAsignacion.getIdTecnico());
            try (PreparedStatement psInsert = conn.prepareStatement(insertAsignacionSql)) 
            {
                psInsert.setInt(1, oExpedienteAsignacion.getIdExpediente());
                psInsert.setInt(2, oExpedienteAsignacion.getIdTecnico());

                java.sql.Date fechaSql = new java.sql.Date(oExpedienteAsignacion.getFechaAsignacion().getTime());
                psInsert.setDate(3, fechaSql);
                psInsert.setString(4, oExpedienteAsignacion.getHojaEnvioAsignacion());
                
                psInsert.setInt(5, oExpedienteAsignacion.getEtapaFlujo());
                psInsert.setInt(6, oExpedienteAsignacion.getTipoProcedimientoRegistral());
                psInsert.setString(7, oExpedienteAsignacion.getNumeroResolucion());
                psInsert.setInt(8, oExpedienteAsignacion.getTipoActa());
                if (tipoPersonal.idTipoPersonal == null) {
                    psInsert.setNull(9, java.sql.Types.NUMERIC);
                } else {
                    psInsert.setInt(9, tipoPersonal.idTipoPersonal);
                }
                psInsert.setString(10, tipoPersonal.descripcion);
                
                psInsert.executeUpdate();
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
            throw ex;
        } 
        finally 
        {
            if (conn != null) conn.setAutoCommit(true); // volver a modo normal
            if (conn != null) conn.close();
        }               
    }

    private boolean existeAsignacionActivaPorEtapa(Connection conn, int idExpediente, Integer etapaFlujo) throws SQLException {
        String sql;
        if (etapaFlujo == null) {
            sql = "SELECT COUNT(1) FROM EXPEDIENTE_ASIGNACION "
                    + "WHERE ID_EXPEDIENTE = ? AND ACTIVE = 1 AND ETAPA_FLUJO IS NULL";
        } else {
            sql = "SELECT COUNT(1) FROM EXPEDIENTE_ASIGNACION "
                    + "WHERE ID_EXPEDIENTE = ? AND ACTIVE = 1 AND ETAPA_FLUJO = ?";
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idExpediente);
            if (etapaFlujo != null) {
                ps.setInt(2, etapaFlujo);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private TipoPersonalAsignacion obtenerTipoPersonalAsignacion(Connection conn, int idTecnico) throws SQLException {
        String sql =
            "SELECT t.ID_TIPO_PERSONAL, ci.DESCRIPCION AS TIPO_PERSONAL " +
            "FROM TECNICO t " +
            "LEFT JOIN CATALOGO_ITEM ci ON ci.ID_CATALOGO_ITEM = t.ID_TIPO_PERSONAL " +
            "WHERE t.ID_TECNICO = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTecnico);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idTipoPersonal = rs.getInt("ID_TIPO_PERSONAL");
                    return new TipoPersonalAsignacion(
                            rs.wasNull() ? null : idTipoPersonal,
                            rs.getString("TIPO_PERSONAL")
                    );
                }
            }
        }
        return new TipoPersonalAsignacion(null, null);
    }

    private static class TipoPersonalAsignacion {
        private final Integer idTipoPersonal;
        private final String descripcion;

        private TipoPersonalAsignacion(Integer idTipoPersonal, String descripcion) {
            this.idTipoPersonal = idTipoPersonal;
            this.descripcion = descripcion;
        }
    }
    
    
    public boolean actualizarRecepcionExpediente(ExpedienteAsignacion oExpedienteAsignacion) throws SQLException 
    {
        String updateExpedienteAsignacionSql = "UPDATE EXPEDIENTE_ASIGNACION SET " +
                    " acepta_recepcion = ?, " +
                    " fecha_recepcion = ?, " +
                    " usuario_modificacion = ?, " +
                    " fecha_modificacion = ? " +
                    " WHERE id_expediente = ? " +
                    " AND id_tecnico = ? " +
                    " AND active = 1 " +
                    " AND etapa_flujo IS NULL";
        
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
                psupdateExpediente.setInt(1, oExpedienteAsignacion.getEtapaFlujo());   // IdEstadoExpediente                
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
                psUpdate.setInt(6, oExpedienteAsignacion.getIdTecnico());            // AND id_tecnico = ?
                int filasActualizadas = psUpdate.executeUpdate();
                if (filasActualizadas == 0) {
                    throw new SQLException("No se encontro una asignacion inicial activa para recepcionar el expediente.");
                }
                if (filasActualizadas > 1) {
                    throw new SQLException("Se encontro mas de una asignacion inicial activa para el expediente. Revise el duplicado antes de recepcionar.");
                }
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
            throw ex;
        } 
        finally 
        {
            if (conn != null) conn.setAutoCommit(true); // volver a modo normal
            if (conn != null) conn.close();
        }               
    }  
      
    
    public List<Expediente> ListarExpedientesAsignadosPorTrabajador(int idTecnico, int aceptaRecepcion,int estadoItem,int esPorVerificar, int esPorNotificar) throws SQLException 
    {        
        List<Expediente> lista = new ArrayList<>();
        
        StringBuilder sqlListaExpediente = new StringBuilder("	SELECT EXPEDIENTE.*, "
                + "CASE WHEN TECNICO.ID_TECNICO IS NULL THEN NULL "
                + "ELSE TRIM(NVL(TECNICO.APELLIDO_PATERNO, '') || ' ' || NVL(TECNICO.APELLIDO_MATERNO, '') || ', ' || NVL(TECNICO.NOMBRES, '')) END AS ABOGADO_DESIGNADO "
                + "FROM EXPEDIENTE "
                + "INNER JOIN EXPEDIENTE_ASIGNACION ON EXPEDIENTE.ID_EXPEDIENTE = EXPEDIENTE_ASIGNACION.ID_EXPEDIENTE "
                + "LEFT JOIN TECNICO ON TECNICO.ID_TECNICO = EXPEDIENTE_ASIGNACION.ID_TECNICO "
                + "LEFT JOIN (\n" +
                    "    SELECT DISTINCT e.ID_EXPEDIENTE AS ID_EXPEDIENTE_DOCUMENTO_VERIFICAR\n" +
                    "    FROM EXPEDIENTE_ANALISIS_ABOGADO_DET_DOC d\n" +
                    "    INNER JOIN EXPEDIENTE_ANALISIS_ABOGADO e ON d.ID_EXPEDIENTE_ANALISIS_ABOGADO = e.ID_EXPEDIENTE_ANALISIS_ABOGADO\n" +
                    "    WHERE d.ID_TIPO_DOCUMENTO_ANALIZADO IN (" + DocumentoAnalizado.RESOLUCIONES + "," + DocumentoAnalizado.INFORMES + ")\n" +
                    "    AND d.ACTIVE = 1\n" +
                    ") doc\n" +
                    "ON doc.ID_EXPEDIENTE_DOCUMENTO_VERIFICAR = EXPEDIENTE.ID_EXPEDIENTE "
                + "WHERE 1 = 1 ");
        
        // Si el estado no es "TODOS", agregamos AND
        boolean filtrarTecnico = idTecnico != 0;
        boolean filtrarEstado = estadoItem != 0;
        boolean filtrarAceptaRecepcion = aceptaRecepcion != 0;
        boolean filtrarVerificacion = esPorVerificar != 0;
        boolean filtrarNotificacion = esPorNotificar != 0;

        if(filtrarTecnico)
        {
            sqlListaExpediente.append("AND EXPEDIENTE_ASIGNACION.id_tecnico = ? ");
        }
        if(filtrarEstado) 
        {
            sqlListaExpediente.append("AND EXPEDIENTE.estado = ? ");
        }
        if(filtrarAceptaRecepcion) 
        {
            sqlListaExpediente.append("AND expediente_asignacion.acepta_recepcion = ? ");
        }
        
        if(filtrarVerificacion) 
        {
            sqlListaExpediente.append("AND ID_EXPEDIENTE_DOCUMENTO_VERIFICAR IS NOT NULL ");
        }
        
        
        Connection conn = null;
        try
        {
            conn = OracleConnection.getConnection();
            conn.setAutoCommit(false);             
            try(PreparedStatement psListar = conn.prepareStatement(sqlListaExpediente.toString()))
            {
                int paramIndex = 1;
                if(filtrarTecnico)
                    psListar.setInt(paramIndex++, idTecnico);
                
                if(filtrarEstado) 
                    psListar.setInt(paramIndex++, estadoItem);
                
                if(filtrarAceptaRecepcion) 
                    psListar.setInt(paramIndex++, aceptaRecepcion);

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

    public List<Expediente> listarExpedientesAsignados(String campo, String valor, int estadoItem, int idTecnico) throws SQLException
    {
        List<Expediente> lista = new ArrayList<>();
        String filtroCampo = resolverFiltroCampoAsignado(campo);
        boolean filtrarTexto = valor != null && !valor.trim().isEmpty() && filtroCampo != null;
        boolean filtrarEstado = estadoItem != 0;
        boolean filtrarTecnico = idTecnico > 0;

        StringBuilder sql = new StringBuilder(
                "SELECT e.*, "
                + "CASE WHEN t.ID_TECNICO IS NULL THEN NULL "
                + "ELSE TRIM(NVL(t.APELLIDO_PATERNO, '') || ' ' || NVL(t.APELLIDO_MATERNO, '') || ', ' || NVL(t.NOMBRES, '')) END AS ABOGADO_DESIGNADO "
                + "FROM EXPEDIENTE e "
                + "INNER JOIN EXPEDIENTE_ASIGNACION ea ON e.ID_EXPEDIENTE = ea.ID_EXPEDIENTE "
                + "LEFT JOIN TECNICO t ON t.ID_TECNICO = ea.ID_TECNICO "
                + "WHERE ea.ACTIVE = 1 "
                + "AND ea.ETAPA_FLUJO IS NULL ");

        if (filtrarTecnico) {
            sql.append("AND ea.ID_TECNICO = ? ");
        }
        if (filtrarEstado) {
            sql.append("AND e.ESTADO = ? ");
        }
        if (filtrarTexto) {
            sql.append("AND UPPER(").append(filtroCampo).append(") LIKE ? ");
        }
        sql.append("ORDER BY e.FECHA_SOLICITUD DESC, e.ID_EXPEDIENTE DESC");

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (filtrarTecnico) {
                ps.setInt(paramIndex++, idTecnico);
            }
            if (filtrarEstado) {
                ps.setInt(paramIndex++, estadoItem);
            }
            if (filtrarTexto) {
                ps.setString(paramIndex++, "%" + valor.trim().toUpperCase() + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }
        }
        return lista;
    }

    private String resolverFiltroCampoAsignado(String campo)
    {
        if (campo == null) {
            return null;
        }

        String normalizado = campo.trim().toUpperCase();
        switch (normalizado) {
            case "NUMERO_TRAMITE_DOCUMENTO":
                return "NVL(e.NUMERO_TRAMITE_DOCUMENTO, '')";
            case "TIPO_SOLICITUD":
                return "TO_CHAR(e.TIPO_SOLICITUD)";
            case "DNI_REMITENTE":
                return "NVL(e.DNI_REMITENTE, '')";
            case "APELLIDO_NOMBRE_REMITENTE":
                return "NVL(e.APELLIDO_NOMBRE_REMITENTE, '')";
            case "TIPO_PROCEDIMIENTO_REGISTRAL":
                return "TO_CHAR(e.TIPO_PROCEDIMIENTO_REGISTRAL)";
            case "ABOGADO_DESIGNADO":
                return "NVL(t.APELLIDO_PATERNO, '') || ' ' || NVL(t.APELLIDO_MATERNO, '') || ', ' || NVL(t.NOMBRES, '')";
            default:
                return null;
        }
    }

    public ExpedienteAsignacion buscarAsignacionInicialActivaPorExpediente(int idExpediente) throws SQLException
    {
        String sql =
                "SELECT ea.ID_EXPEDIENTE_ASIGNACION, ea.ID_EXPEDIENTE, ea.ID_TECNICO, "
                + "ea.FECHA_ASIGNACION, ea.HOJA_ENVIO_ASIGNACION, "
                + "CASE WHEN t.ID_TECNICO IS NULL THEN NULL "
                + "ELSE TRIM(NVL(t.APELLIDO_PATERNO, '') || ' ' || NVL(t.APELLIDO_MATERNO, '') || ', ' || NVL(t.NOMBRES, '')) END AS NOMBRE_TECNICO "
                + "FROM EXPEDIENTE_ASIGNACION ea "
                + "LEFT JOIN TECNICO t ON t.ID_TECNICO = ea.ID_TECNICO "
                + "WHERE ea.ID_EXPEDIENTE = ? "
                + "AND ea.ACTIVE = 1 "
                + "AND ea.ETAPA_FLUJO IS NULL";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ExpedienteAsignacion asignacion = new ExpedienteAsignacion();
                    asignacion.setIdExpedienteAsignacion(rs.getInt("ID_EXPEDIENTE_ASIGNACION"));
                    asignacion.setIdExpediente(rs.getInt("ID_EXPEDIENTE"));
                    asignacion.setIdTecnico(rs.getInt("ID_TECNICO"));
                    asignacion.setFechaAsignacion(rs.getDate("FECHA_ASIGNACION"));
                    asignacion.setHojaEnvioAsignacion(rs.getString("HOJA_ENVIO_ASIGNACION"));
                    asignacion.setNombreTecnico(rs.getString("NOMBRE_TECNICO"));
                    return asignacion;
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
        expediente.setAbogadoDesignado(obtenerStringSiExiste(rs, "ABOGADO_DESIGNADO"));
        return expediente;
    }

    private String obtenerStringSiExiste(ResultSet rs, String columnName) throws SQLException
    {
        try {
            return rs.getString(columnName);
        } catch (SQLException ex) {
            return null;
        }
    }
    
}
