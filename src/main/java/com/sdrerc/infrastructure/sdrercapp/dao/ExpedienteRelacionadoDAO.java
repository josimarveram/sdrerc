package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CalendarioLaboralService;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionResultadoDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ExpedienteRelacionadoDAO {

    private static final String TIPO_RELACION_DOCUMENTO_DUPLICADO_ASOCIADO = "DOCUMENTO_DUPLICADO_ASOCIADO";
    private static final String TIPO_RELACION_MISMA_ACTA_TITULAR = "MISMA_ACTA_TITULAR";
    private static final String MOVIMIENTO_ASOCIACION_DUPLICADO = "ASOCIACION_DUPLICADO";
    private static final String MOVIMIENTO_ASIGNACION_ABOGADO = "ASIGNACION_ABOGADO";
    private static final String CODIGO_ETAPA_ASIGNACION = "ASIGNACION";
    private static final String CODIGO_ETAPA_ANALISIS = "ANALISIS";
    private static final String CODIGO_ESTADO_ASIGNADO = "ASIGNADO";
    private static final String MOTIVO_MISMA_ACTA_TITULAR = "Misma acta y titular";
    private static final String MOTIVO_DOCUMENTO_DUPLICADO = "Documento duplicado asociado al expediente principal por misma acta y titular";

    private final CatalogoLookupDAO catalogoLookupDAO;
    private final CalendarioLaboralService calendarioLaboralService;
    private final ExpedienteAlertaDAO expedienteAlertaDAO;

    public ExpedienteRelacionadoDAO() {
        this(new CatalogoLookupDAO(), new CalendarioLaboralService(), new ExpedienteAlertaDAO());
    }

    public ExpedienteRelacionadoDAO(CatalogoLookupDAO catalogoLookupDAO) {
        this(catalogoLookupDAO, new CalendarioLaboralService(), new ExpedienteAlertaDAO());
    }

    public ExpedienteRelacionadoDAO(CatalogoLookupDAO catalogoLookupDAO, CalendarioLaboralService calendarioLaboralService) {
        this(catalogoLookupDAO, calendarioLaboralService, new ExpedienteAlertaDAO());
    }

    public ExpedienteRelacionadoDAO(
            CatalogoLookupDAO catalogoLookupDAO,
            CalendarioLaboralService calendarioLaboralService,
            ExpedienteAlertaDAO expedienteAlertaDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.calendarioLaboralService = calendarioLaboralService;
        this.expedienteAlertaDAO = expedienteAlertaDAO;
    }

    public List<ExpedienteRelacionadoDTO> listarPosiblesRelacionados(Long idExpediente) throws SQLException {
        List<ExpedienteRelacionadoDTO> relacionados = new ArrayList<>();
        if (idExpediente == null) {
            return relacionados;
        }

        String sql = "WITH base AS ( "
                + "SELECT e.id_expediente, ea.id_tipo_acta, UPPER(TRIM(ea.numero_acta)) AS numero_acta_norm, "
                + normalizarPersona("p") + " AS titular_norm "
                + "FROM expediente e "
                + "JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1 "
                + "JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                + "JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                + "WHERE e.id_expediente = ? AND e.activo = 1 "
                + ") "
                + "SELECT DISTINCT e.id_expediente, e.numero_expediente, e.numero_tramite_documentario, "
                + "esol.numero_expediente_sgd, "
                + numeroDocumentoRelacionadoSql("e") + " AS numero_documento, "
                + "ta.nombre AS tipo_acta, ea.numero_acta, "
                + nombrePersona("p") + " AS titular, esol.asunto AS procedimiento, "
                + nombrePersona("ps") + " AS solicitante, "
                + "eqr.nombre AS equipo_asignado, e.id_equipo_responsable_actual AS id_equipo_responsable, "
                + "NVL(ur.nombre_completo, (SELECT MAX(ua.nombre_completo) FROM expediente_asignacion axa "
                + " JOIN usuario ua ON ua.id_usuario = axa.id_usuario_asignado "
                + " WHERE axa.id_expediente = e.id_expediente AND axa.activa = 1 AND axa.activo = 1)) AS abogado_asignado, "
                + "NVL(e.id_usuario_responsable_actual, (SELECT MAX(axa.id_usuario_asignado) "
                + " FROM expediente_asignacion axa WHERE axa.id_expediente = e.id_expediente "
                + " AND axa.activa = 1 AND axa.activo = 1)) AS id_abogado_responsable, "
                + "et.codigo AS etapa_codigo, est.codigo AS estado_codigo, esol.fecha_recepcion, e.fecha_vencimiento, "
                + "? AS motivo_coincidencia, "
                + resumenAlertasSql("e") + " AS alerta_ingreso "
                + "FROM base b "
                + "JOIN expediente_acta ea ON UPPER(TRIM(ea.numero_acta)) = b.numero_acta_norm "
                + " AND ea.activo = 1 "
                + "JOIN expediente e ON e.id_expediente = ea.id_expediente AND e.activo = 1 AND e.id_expediente <> b.id_expediente "
                + "JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                + "JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta "
                + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                + "LEFT JOIN persona ps ON ps.id_persona = esol.id_persona_solicitante AND ps.activo = 1 "
                + "LEFT JOIN equipo eqr ON eqr.id_equipo = e.id_equipo_responsable_actual "
                + "LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual "
                + "WHERE b.numero_acta_norm IS NOT NULL "
                + "AND b.titular_norm IS NOT NULL "
                + "AND " + normalizarPersona("p") + " = b.titular_norm "
                + "AND NOT EXISTS ("
                + "SELECT 1 FROM expediente_relacion r "
                + "WHERE r.activo = 1 AND r.id_expediente_relacionado = e.id_expediente"
                + ") "
                + "AND NOT EXISTS ("
                + "SELECT 1 FROM expediente_relacion r "
                + "WHERE r.activo = 1 AND ("
                + "(r.id_expediente_principal = b.id_expediente AND r.id_expediente_relacionado = e.id_expediente) "
                + "OR (r.id_expediente_principal = e.id_expediente AND r.id_expediente_relacionado = b.id_expediente)"
                + ")) "
                + "ORDER BY e.numero_expediente";

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, MOTIVO_MISMA_ACTA_TITULAR);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    relacionados.add(mapPosible(rs));
                }
            }
        }
        return relacionados;
    }

    public int contarPosiblesRelacionados(Connection conn, Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM ("
                + "WITH base AS ( "
                + "SELECT e.id_expediente, ea.id_tipo_acta, UPPER(TRIM(ea.numero_acta)) AS numero_acta_norm, "
                + normalizarPersona("p") + " AS titular_norm "
                + "FROM expediente e "
                + "JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1 "
                + "JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                + "JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                + "WHERE e.id_expediente = ? AND e.activo = 1 "
                + ") "
                + "SELECT DISTINCT e.id_expediente "
                + "FROM base b "
                + "JOIN expediente_acta ea ON UPPER(TRIM(ea.numero_acta)) = b.numero_acta_norm "
                + " AND ea.activo = 1 "
                + "JOIN expediente e ON e.id_expediente = ea.id_expediente AND e.activo = 1 AND e.id_expediente <> b.id_expediente "
                + "JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                + "JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                + "WHERE b.numero_acta_norm IS NOT NULL "
                + "AND b.titular_norm IS NOT NULL "
                + "AND " + normalizarPersona("p") + " = b.titular_norm "
                + "AND NOT EXISTS ("
                + "SELECT 1 FROM expediente_relacion r "
                + "WHERE r.activo = 1 AND r.id_expediente_relacionado = e.id_expediente"
                + ") "
                + "AND NOT EXISTS ("
                + "SELECT 1 FROM expediente_relacion r "
                + "WHERE r.activo = 1 AND ("
                + "(r.id_expediente_principal = b.id_expediente AND r.id_expediente_relacionado = e.id_expediente) "
                + "OR (r.id_expediente_principal = e.id_expediente AND r.id_expediente_relacionado = b.id_expediente)"
                + "))"
                + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<ExpedienteRelacionadoDTO> listarAsociadosConfirmados(Long idExpediente) throws SQLException {
        List<ExpedienteRelacionadoDTO> asociados = new ArrayList<>();
        if (idExpediente == null) {
            return asociados;
        }

        String sql = "SELECT e.id_expediente, e.numero_expediente, e.numero_tramite_documentario, "
                + "esol.numero_expediente_sgd, "
                + numeroDocumentoRelacionadoSql("e") + " AS numero_documento, "
                + "ta.nombre AS tipo_acta, ea.numero_acta, "
                + nombrePersona("p") + " AS titular, esol.asunto AS procedimiento, "
                + nombrePersona("ps") + " AS solicitante, "
                + "eqr.nombre AS equipo_asignado, e.id_equipo_responsable_actual AS id_equipo_responsable, "
                + "NVL(ur.nombre_completo, (SELECT MAX(ua.nombre_completo) FROM expediente_asignacion axa "
                + " JOIN usuario ua ON ua.id_usuario = axa.id_usuario_asignado "
                + " WHERE axa.id_expediente = e.id_expediente AND axa.activa = 1 AND axa.activo = 1)) AS abogado_asignado, "
                + "NVL(e.id_usuario_responsable_actual, (SELECT MAX(axa.id_usuario_asignado) "
                + " FROM expediente_asignacion axa WHERE axa.id_expediente = e.id_expediente "
                + " AND axa.activa = 1 AND axa.activo = 1)) AS id_abogado_responsable, "
                + "et.codigo AS etapa_codigo, est.codigo AS estado_codigo, esol.fecha_recepcion, e.fecha_vencimiento, "
                + resumenAlertasSql("e") + " AS alerta_ingreso, "
                + "r.tipo_relacion, r.descripcion, r.creado_en AS fecha_asociacion, u.nombre_completo AS usuario_relacion "
                + "FROM expediente_relacion r "
                + "JOIN expediente e ON e.id_expediente = CASE "
                + "WHEN r.id_expediente_principal = ? THEN r.id_expediente_relacionado "
                + "ELSE r.id_expediente_principal END "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "LEFT JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1 "
                + "LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta "
                + "LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                + "LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                + "LEFT JOIN persona ps ON ps.id_persona = esol.id_persona_solicitante AND ps.activo = 1 "
                + "LEFT JOIN usuario u ON u.id_usuario = r.creado_por "
                + "LEFT JOIN equipo eqr ON eqr.id_equipo = e.id_equipo_responsable_actual "
                + "LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual "
                + "WHERE r.activo = 1 "
                + "AND (r.id_expediente_principal = ? OR r.id_expediente_relacionado = ?) "
                + "ORDER BY r.creado_en DESC";

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idExpediente);
            ps.setLong(3, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    asociados.add(mapAsociado(conn, rs));
                }
            }
        }
        return asociados;
    }

    public ExpedienteRelacionadoDTO obtenerExpedientePrincipalAsociado(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return null;
        }

        String sql = "SELECT e.id_expediente, e.numero_expediente, e.numero_tramite_documentario, "
                + "esol.numero_expediente_sgd, "
                + numeroDocumentoRelacionadoSql("e") + " AS numero_documento, "
                + "ta.nombre AS tipo_acta, ea.numero_acta, "
                + nombrePersona("p") + " AS titular, esol.asunto AS procedimiento, "
                + nombrePersona("ps") + " AS solicitante, "
                + "eqr.nombre AS equipo_asignado, e.id_equipo_responsable_actual AS id_equipo_responsable, "
                + "NVL(ur.nombre_completo, (SELECT MAX(ua.nombre_completo) FROM expediente_asignacion axa "
                + " JOIN usuario ua ON ua.id_usuario = axa.id_usuario_asignado "
                + " WHERE axa.id_expediente = e.id_expediente AND axa.activa = 1 AND axa.activo = 1)) AS abogado_asignado, "
                + "NVL(e.id_usuario_responsable_actual, (SELECT MAX(axa.id_usuario_asignado) "
                + " FROM expediente_asignacion axa WHERE axa.id_expediente = e.id_expediente "
                + " AND axa.activa = 1 AND axa.activo = 1)) AS id_abogado_responsable, "
                + "et.codigo AS etapa_codigo, est.codigo AS estado_codigo, esol.fecha_recepcion, e.fecha_vencimiento, "
                + resumenAlertasSql("e") + " AS alerta_ingreso, "
                + "r.tipo_relacion, r.descripcion, r.creado_en AS fecha_asociacion, u.nombre_completo AS usuario_relacion "
                + "FROM expediente_relacion r "
                + "JOIN expediente e ON e.id_expediente = r.id_expediente_principal AND e.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "LEFT JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1 "
                + "LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta "
                + "LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                + "LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                + "LEFT JOIN persona ps ON ps.id_persona = esol.id_persona_solicitante AND ps.activo = 1 "
                + "LEFT JOIN usuario u ON u.id_usuario = r.creado_por "
                + "LEFT JOIN equipo eqr ON eqr.id_equipo = e.id_equipo_responsable_actual "
                + "LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual "
                + "WHERE r.activo = 1 "
                + "AND r.id_expediente_relacionado = ? "
                + "AND UPPER(r.tipo_relacion) IN (?, ?) "
                + "ORDER BY r.creado_en DESC";

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, TIPO_RELACION_DOCUMENTO_DUPLICADO_ASOCIADO);
            ps.setString(3, TIPO_RELACION_MISMA_ACTA_TITULAR);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAsociado(conn, rs);
                }
            }
        }
        return null;
    }

    public ExpedienteRelacionResultadoDTO asociarRelacionados(
            Long idExpedientePrincipal,
            List<Long> idsRelacionados,
            Long idUsuarioCreador,
            String descripcion) throws SQLException {
        if (idExpedientePrincipal == null) {
            throw new IllegalArgumentException("Seleccione el expediente principal para asociar.");
        }
        if (idsRelacionados == null || idsRelacionados.isEmpty()) {
            throw new IllegalArgumentException("Seleccione al menos un expediente relacionado.");
        }

        int asociados = 0;
        int yaAsociados = 0;
        int omitidos = 0;
        Set<Long> idsUnicos = new LinkedHashSet<>(idsRelacionados);
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, MOVIMIENTO_ASOCIACION_DUPLICADO);
                Long idMovimientoAsignacion = catalogoLookupDAO.obtenerTipoMovimientoId(
                        conn,
                        MOVIMIENTO_ASIGNACION_ABOGADO);
                for (Long idSeleccionado : idsUnicos) {
                    if (idSeleccionado == null || idSeleccionado.equals(idExpedientePrincipal)) {
                        omitidos++;
                        continue;
                    }
                    if (!existeExpedienteActivo(conn, idExpedientePrincipal) || !existeExpedienteActivo(conn, idSeleccionado)) {
                        throw new SQLException("Uno de los expedientes seleccionados ya no existe o no está activo.");
                    }
                    if (!coincidenPorActaYTitular(conn, idExpedientePrincipal, idSeleccionado)) {
                        omitidos++;
                        continue;
                    }
                    if (estaRelacionadoComoHijoEnOtroPrincipal(conn, idSeleccionado, idExpedientePrincipal)) {
                        throw new SQLException("El expediente " + idSeleccionado + " ya está asociado a otro expediente principal. Desasócielo antes de volver a relacionarlo.");
                    }
                    OrientacionRelacion orientacion = resolverOrientacionRelacion(
                            conn,
                            idExpedientePrincipal,
                            idSeleccionado);
                    Date fechaVencimientoPrincipal = resolverFechaVencimientoPrincipal(
                            conn,
                            orientacion.idPrincipal,
                            idUsuarioCreador);
                    sincronizarNumeroExpedienteRelacionado(
                            conn,
                            orientacion.idPrincipal,
                            orientacion.idRelacionado,
                            idUsuarioCreador);
                    if (existeRelacionActiva(conn, orientacion.idPrincipal, orientacion.idRelacionado)) {
                        sincronizarFechaVencimientoRelacionado(
                                conn,
                                orientacion.idRelacionado,
                                fechaVencimientoPrincipal,
                                idUsuarioCreador);
                        sincronizarAsignacionDesdePrincipal(
                                conn,
                                orientacion.idPrincipal,
                                orientacion.idRelacionado,
                                idUsuarioCreador,
                                idMovimientoAsignacion,
                                true);
                        expedienteAlertaDAO.marcarAtendidas(
                                conn,
                                orientacion.idRelacionado,
                                Collections.singletonList("Potencial duplicado"),
                                idUsuarioCreador);
                        yaAsociados++;
                        continue;
                    }
                    Long idRelacion = insertarRelacion(
                            conn,
                            orientacion.idPrincipal,
                            orientacion.idRelacionado,
                            idUsuarioCreador,
                            descripcion);
                    sincronizarFechaVencimientoRelacionado(
                            conn,
                            orientacion.idRelacionado,
                            fechaVencimientoPrincipal,
                            idUsuarioCreador);
                    sincronizarAsignacionDesdePrincipal(
                            conn,
                            orientacion.idPrincipal,
                            orientacion.idRelacionado,
                            idUsuarioCreador,
                            idMovimientoAsignacion,
                            true);
                    expedienteAlertaDAO.marcarAtendidas(
                            conn,
                            orientacion.idRelacionado,
                            Collections.singletonList("Potencial duplicado"),
                            idUsuarioCreador);
                    if (idMovimiento != null) {
                        insertarHistorialRelacion(
                                conn,
                                orientacion.idPrincipal,
                                idMovimiento,
                                idUsuarioCreador,
                                idRelacion,
                                descripcion);
                        insertarHistorialRelacion(
                                conn,
                                orientacion.idRelacionado,
                                idMovimiento,
                                idUsuarioCreador,
                                idRelacion,
                                descripcion);
                    }
                    asociados++;
                }
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            }
        }
        String mensaje = asociados + " documento(s) duplicado(s) asociado(s) al expediente principal.";
        if (yaAsociados > 0) {
            mensaje += " " + yaAsociados + " documento(s) ya se encontraban asociados al expediente principal.";
        }
        if (omitidos > 0) {
            mensaje += " " + omitidos + " expediente(s) omitido(s) porque no coinciden por número de acta y titular o la selección no es válida.";
        }
        return new ExpedienteRelacionResultadoDTO(
                idsRelacionados.size(),
                asociados,
                yaAsociados,
                omitidos,
                mensaje);
    }

    public String desasociarRelacionado(
            Long idExpedientePrincipal,
            Long idExpedienteRelacionado,
            Long idUsuarioCreador,
            String descripcion) throws SQLException {
        if (idExpedientePrincipal == null || idExpedienteRelacionado == null) {
            throw new IllegalArgumentException("Seleccione el expediente principal y el expediente relacionado.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                if (!existeExpedienteActivo(conn, idExpedientePrincipal) || !existeExpedienteActivo(conn, idExpedienteRelacionado)) {
                    throw new SQLException("Uno de los expedientes seleccionados ya no existe o no está activo.");
                }
                if (!existeRelacionActiva(conn, idExpedientePrincipal, idExpedienteRelacionado)) {
                    throw new SQLException("El expediente ya no está asociado al expediente principal.");
                }
                Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, MOVIMIENTO_ASOCIACION_DUPLICADO);
                Date fechaVencimientoRelacionado = resolverFechaVencimientoPrincipal(
                        conn,
                        idExpedienteRelacionado,
                        idUsuarioCreador);
                desactivarRelacionActiva(conn, idExpedientePrincipal, idExpedienteRelacionado, idUsuarioCreador);
                limpiarNumeroExpedienteRelacionado(conn, idExpedienteRelacionado, idUsuarioCreador);
                sincronizarFechaVencimientoRelacionado(
                        conn,
                        idExpedienteRelacionado,
                        fechaVencimientoRelacionado,
                        idUsuarioCreador);
                if (idMovimiento != null) {
                    String comentario = descripcion == null || descripcion.trim().isEmpty()
                            ? "Asociación eliminada del expediente principal."
                            : descripcion.trim();
                    insertarHistorialRelacion(
                            conn,
                            idExpedientePrincipal,
                            idMovimiento,
                            idUsuarioCreador,
                            null,
                            comentario);
                    insertarHistorialRelacion(
                            conn,
                            idExpedienteRelacionado,
                            idMovimiento,
                            idUsuarioCreador,
                            null,
                            comentario);
                }
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return "Se eliminó la asociación del expediente relacionado. El expediente volvió a mostrarse de forma independiente.";
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            }
        }
    }

    public int sincronizarAsignacionAsociados(
            Connection conn,
            Long idExpedientePrincipal,
            Long idUsuarioModificador) throws SQLException {
        if (conn == null || idExpedientePrincipal == null) {
            return 0;
        }
        Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, MOVIMIENTO_ASIGNACION_ABOGADO);
        String sql = "SELECT id_expediente_relacionado "
                + "FROM expediente_relacion "
                + "WHERE id_expediente_principal = ? AND activo = 1 "
                + "AND UPPER(tipo_relacion) IN (?, ?)";
        int sincronizados = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpedientePrincipal);
            ps.setString(2, TIPO_RELACION_DOCUMENTO_DUPLICADO_ASOCIADO);
            ps.setString(3, TIPO_RELACION_MISMA_ACTA_TITULAR);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (sincronizarAsignacionDesdePrincipal(
                            conn,
                            idExpedientePrincipal,
                            getLongOrNull(rs, "id_expediente_relacionado"),
                            idUsuarioModificador,
                            idMovimiento,
                            true)) {
                        sincronizados++;
                    }
                }
            }
        }
        return sincronizados;
    }

    private boolean sincronizarAsignacionDesdePrincipal(
            Connection conn,
            Long idPrincipal,
            Long idRelacionado,
            Long idUsuarioModificador,
            Long idMovimiento,
            boolean sincronizarEstadoOperativo) throws SQLException {
        if (idPrincipal == null || idRelacionado == null) {
            return false;
        }
        AsignacionActual asignacionPrincipal = obtenerAsignacionActual(conn, idPrincipal);
        if (asignacionPrincipal == null) {
            return false;
        }
        AsignacionActual asignacionDestino = prepararAsignacionDestino(
                conn,
                asignacionPrincipal,
                sincronizarEstadoOperativo);
        boolean actualizarEstado = asignacionDestino != asignacionPrincipal;
        if (sincronizarEstadoOperativo && asignacionPrincipal.esAsignado()) {
            actualizarEstado = true;
        }
        if (tieneAsignacionCoincidente(conn, idRelacionado, asignacionPrincipal)) {
            actualizarResponsablesRelacionado(conn, idRelacionado, asignacionDestino, idUsuarioModificador, actualizarEstado);
            return false;
        }
        if (idMovimiento == null) {
            throw new SQLException("No se encontró el movimiento ASIGNACION_ABOGADO para registrar la sincronización.");
        }

        desactivarAsignacionesActuales(conn, idRelacionado, idUsuarioModificador);
        Long idAsignacion = insertarAsignacionAsociada(
                conn,
                idRelacionado,
                asignacionPrincipal,
                idUsuarioModificador);
        actualizarResponsablesRelacionado(conn, idRelacionado, asignacionDestino, idUsuarioModificador, actualizarEstado);
        insertarHistorialAsignacionAsociada(
                conn,
                idRelacionado,
                asignacionPrincipal,
                idMovimiento,
                idAsignacion,
                idUsuarioModificador);
        return true;
    }

    private AsignacionActual prepararAsignacionDestino(
            Connection conn,
            AsignacionActual asignacionPrincipal,
            boolean sincronizarEstadoOperativo) throws SQLException {
        if (!sincronizarEstadoOperativo) {
            return asignacionPrincipal;
        }
        Long idEtapaAsignacion = catalogoLookupDAO.obtenerEtapaId(conn, CODIGO_ETAPA_ASIGNACION);
        Long idEstadoAsignado = catalogoLookupDAO.obtenerEstadoId(conn, CODIGO_ESTADO_ASIGNADO);
        if (idEtapaAsignacion == null || idEstadoAsignado == null) {
            throw new SQLException("No se encontró la etapa/estado requerida para preparar el documento asociado.");
        }
        return new AsignacionActual(
                asignacionPrincipal.idUsuario,
                asignacionPrincipal.idEquipo,
                asignacionPrincipal.idEtapa,
                idEtapaAsignacion,
                idEstadoAsignado,
                CODIGO_ETAPA_ASIGNACION,
                CODIGO_ESTADO_ASIGNADO);
    }

    private AsignacionActual obtenerAsignacionActual(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT id_usuario_asignado, id_equipo_asignado, id_etapa "
                + "FROM (SELECT id_usuario_asignado, id_equipo_asignado, id_etapa "
                + "FROM expediente_asignacion "
                + "WHERE id_expediente = ? AND activa = 1 AND activo = 1 "
                + "ORDER BY fecha_asignacion DESC, id_expediente_asignacion DESC) "
                + "WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ExpedienteEstadoActual estadoActual = obtenerEstadoActual(conn, idExpediente);
                return new AsignacionActual(
                        getLongOrNull(rs, "id_usuario_asignado"),
                        getLongOrNull(rs, "id_equipo_asignado"),
                        getLongOrNull(rs, "id_etapa"),
                        estadoActual.idEtapa,
                        estadoActual.idEstado,
                        estadoActual.etapaCodigo,
                        estadoActual.estadoCodigo);
            }
        }
    }

    private ExpedienteEstadoActual obtenerEstadoActual(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT e.id_etapa_actual, e.id_estado_actual, et.codigo AS etapa_codigo, est.codigo AS estado_codigo "
                + "FROM expediente e "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "WHERE e.id_expediente = ? AND e.activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El expediente principal seleccionado ya no está disponible.");
                }
                return new ExpedienteEstadoActual(
                        getLongOrNull(rs, "id_etapa_actual"),
                        getLongOrNull(rs, "id_estado_actual"),
                        rs.getString("etapa_codigo"),
                        rs.getString("estado_codigo"));
            }
        }
    }

    private boolean tieneAsignacionCoincidente(
            Connection conn,
            Long idExpediente,
            AsignacionActual asignacion) throws SQLException {
        String sql = "SELECT 1 FROM expediente_asignacion "
                + "WHERE id_expediente = ? AND activa = 1 AND activo = 1 "
                + "AND id_usuario_asignado = ? "
                + "AND NVL(id_equipo_asignado, -1) = NVL(?, -1) "
                + "AND id_etapa = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, asignacion.idUsuario);
            setLongOrNull(ps, 3, asignacion.idEquipo);
            ps.setLong(4, asignacion.idEtapa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void desactivarAsignacionesActuales(
            Connection conn,
            Long idExpediente,
            Long idUsuarioModificador) throws SQLException {
        String sql = "UPDATE expediente_asignacion "
                + "SET activa = 0, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activa = 1 AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setLongOrNull(ps, 1, idUsuarioModificador);
            ps.setLong(2, idExpediente);
            ps.executeUpdate();
        }
    }

    private Long insertarAsignacionAsociada(
            Connection conn,
            Long idExpediente,
            AsignacionActual asignacion,
            Long idUsuarioModificador) throws SQLException {
        String sql = "INSERT INTO expediente_asignacion ("
                + "id_expediente, id_usuario_asignado, id_equipo_asignado, id_etapa, fecha_asignacion, "
                + "activa, es_abogado_principal, es_reasignacion_excepcional, motivo, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, SYSTIMESTAMP, 1, 0, 0, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_ASIGNACION"})) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, asignacion.idUsuario);
            setLongOrNull(ps, 3, asignacion.idEquipo);
            ps.setLong(4, asignacion.idEtapa);
            ps.setString(5, "Asignación sincronizada desde el expediente principal asociado.");
            setLongOrNull(ps, 6, idUsuarioModificador);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "expediente_asignacion");
        }
    }

    private void actualizarResponsablesRelacionado(
            Connection conn,
            Long idExpediente,
            AsignacionActual asignacion,
            Long idUsuarioModificador,
            boolean actualizarEstadoOperativo) throws SQLException {
        String sql = "UPDATE expediente SET "
                + "id_usuario_responsable_actual = ?, "
                + "id_usuario_abogado_inicial = NVL(id_usuario_abogado_inicial, ?), "
                + "id_equipo_responsable_actual = ?, "
                + (actualizarEstadoOperativo ? "id_etapa_actual = ?, id_estado_actual = ?, fecha_ultimo_movimiento = SYSTIMESTAMP, " : "")
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, asignacion.idUsuario);
            ps.setLong(2, asignacion.idUsuario);
            setLongOrNull(ps, 3, asignacion.idEquipo);
            int index = 4;
            if (actualizarEstadoOperativo) {
                ps.setLong(index++, asignacion.idEtapaExpediente);
                ps.setLong(index++, asignacion.idEstadoExpediente);
            }
            setLongOrNull(ps, index++, idUsuarioModificador);
            ps.setLong(index, idExpediente);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo sincronizar el equipo y abogado del documento asociado.");
            }
        }
    }

    private void insertarHistorialAsignacionAsociada(
            Connection conn,
            Long idExpediente,
            AsignacionActual asignacion,
            Long idMovimiento,
            Long idAsignacion,
            Long idUsuarioModificador) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_usuario_origen, "
                + "id_usuario_destino, id_equipo_destino, tabla_relacionada, id_registro_relacionado, "
                + "comentario, motivo, activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, ?, 'EXPEDIENTE_ASIGNACION', ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            setLongOrNull(ps, 3, idUsuarioModificador);
            ps.setLong(4, asignacion.idUsuario);
            setLongOrNull(ps, 5, asignacion.idEquipo);
            ps.setLong(6, idAsignacion);
            ps.setString(7, "Equipo y abogado sincronizados desde el expediente principal asociado.");
            ps.setString(8, MOVIMIENTO_ASIGNACION_ABOGADO);
            setLongOrNull(ps, 9, idUsuarioModificador);
            ps.executeUpdate();
        }
    }

    private boolean coincidenPorActaYTitular(Connection conn, Long idExpedienteA, Long idExpedienteB) throws SQLException {
        String sql = "SELECT 1 "
                + "FROM expediente_acta a1 "
                + "JOIN expediente_persona ep1 ON ep1.id_expediente = a1.id_expediente "
                + " AND ep1.activo = 1 AND UPPER(ep1.tipo_relacion_persona) = 'TITULAR' "
                + "JOIN persona p1 ON p1.id_persona = ep1.id_persona AND p1.activo = 1 "
                + "JOIN expediente_acta a2 ON UPPER(TRIM(a2.numero_acta)) = UPPER(TRIM(a1.numero_acta)) "
                + " AND a2.activo = 1 "
                + "JOIN expediente_persona ep2 ON ep2.id_expediente = a2.id_expediente "
                + " AND ep2.activo = 1 AND UPPER(ep2.tipo_relacion_persona) = 'TITULAR' "
                + "JOIN persona p2 ON p2.id_persona = ep2.id_persona AND p2.activo = 1 "
                + "WHERE a1.id_expediente = ? AND a1.activo = 1 "
                + "AND a2.id_expediente = ? "
                + "AND TRIM(a1.numero_acta) IS NOT NULL "
                + "AND " + normalizarPersona("p1") + " IS NOT NULL "
                + "AND " + normalizarPersona("p1") + " = " + normalizarPersona("p2") + " "
                + "AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpedienteA);
            ps.setLong(2, idExpedienteB);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private OrientacionRelacion resolverOrientacionRelacion(
            Connection conn,
            Long idExpedienteA,
            Long idExpedienteB) throws SQLException {
        ExpedienteReferencia referenciaA = obtenerReferenciaExpediente(conn, idExpedienteA);
        ExpedienteReferencia referenciaB = obtenerReferenciaExpediente(conn, idExpedienteB);

        if (referenciaA.tieneNumero() != referenciaB.tieneNumero()) {
            return referenciaA.tieneNumero()
                    ? new OrientacionRelacion(idExpedienteA, idExpedienteB)
                    : new OrientacionRelacion(idExpedienteB, idExpedienteA);
        }
        int comparacionFecha = referenciaA.fechaRegistro.compareTo(referenciaB.fechaRegistro);
        if (comparacionFecha < 0 || (comparacionFecha == 0 && idExpedienteA < idExpedienteB)) {
            return new OrientacionRelacion(idExpedienteA, idExpedienteB);
        }
        return new OrientacionRelacion(idExpedienteB, idExpedienteA);
    }

    private ExpedienteReferencia obtenerReferenciaExpediente(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT numero_expediente, fecha_registro "
                + "FROM expediente WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El expediente seleccionado ya no está disponible.");
                }
                Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
                return new ExpedienteReferencia(
                        rs.getString("numero_expediente"),
                        fechaRegistro == null ? LocalDateTime.MAX : fechaRegistro.toLocalDateTime());
            }
        }
    }

    private Long insertarRelacion(
            Connection conn,
            Long idPrincipal,
            Long idRelacionado,
            Long idUsuarioCreador,
            String descripcion) throws SQLException {
        String sql = "INSERT INTO expediente_relacion ("
                + "id_expediente_principal, id_expediente_relacionado, tipo_relacion, descripcion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_RELACION"})) {
            ps.setLong(1, idPrincipal);
            ps.setLong(2, idRelacionado);
            ps.setString(3, TIPO_RELACION_DOCUMENTO_DUPLICADO_ASOCIADO);
            ps.setString(4, descripcion == null || descripcion.trim().isEmpty() ? MOTIVO_DOCUMENTO_DUPLICADO : descripcion.trim());
            setLongOrNull(ps, 5, idUsuarioCreador);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "expediente_relacion");
        }
    }

    private void insertarHistorialRelacion(
            Connection conn,
            Long idExpediente,
            Long idMovimiento,
            Long idUsuarioCreador,
            Long idRelacion,
            String descripcion) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_usuario_origen, "
                + "tabla_relacionada, id_registro_relacionado, comentario, motivo, activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, 'EXPEDIENTE_RELACION', ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            setLongOrNull(ps, 3, idUsuarioCreador);
            setLongOrNull(ps, 4, idRelacion);
            ps.setString(5, descripcion == null || descripcion.trim().isEmpty()
                    ? "Documento duplicado asociado al expediente principal. Será evaluado en Análisis si corresponde."
                    : descripcion.trim());
            ps.setString(6, TIPO_RELACION_DOCUMENTO_DUPLICADO_ASOCIADO);
            setLongOrNull(ps, 7, idUsuarioCreador);
            ps.executeUpdate();
        }
    }

    private void desactivarRelacionActiva(
            Connection conn,
            Long idExpedientePrincipal,
            Long idExpedienteRelacionado,
            Long idUsuarioCreador) throws SQLException {
        String sql = "UPDATE expediente_relacion "
                + "SET activo = 0, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE activo = 1 AND ("
                + "(id_expediente_principal = ? AND id_expediente_relacionado = ?) "
                + "OR (id_expediente_principal = ? AND id_expediente_relacionado = ?))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setLongOrNull(ps, 1, idUsuarioCreador);
            ps.setLong(2, idExpedientePrincipal);
            ps.setLong(3, idExpedienteRelacionado);
            ps.setLong(4, idExpedienteRelacionado);
            ps.setLong(5, idExpedientePrincipal);
            if (ps.executeUpdate() < 1) {
                throw new SQLException("No se pudo eliminar la relación del expediente seleccionado.");
            }
        }
    }

    private void limpiarNumeroExpedienteRelacionado(
            Connection conn,
            Long idExpedienteRelacionado,
            Long idUsuarioCreador) throws SQLException {
        if (idExpedienteRelacionado == null) {
            return;
        }
        String sql = "UPDATE expediente "
                + "SET numero_expediente = NULL, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setLongOrNull(ps, 1, idUsuarioCreador);
            ps.setLong(2, idExpedienteRelacionado);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo limpiar el número del expediente relacionado.");
            }
        }
    }

    private Date resolverFechaVencimientoPrincipal(Connection conn, Long idExpedientePrincipal, Long idUsuarioCreador) throws SQLException {
        String sql = "SELECT e.fecha_vencimiento, esol.fecha_recepcion "
                + "FROM expediente e "
                + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                + "WHERE e.id_expediente = ? AND e.activo = 1 "
                + "ORDER BY esol.fecha_recepcion ASC NULLS LAST";
        Date fechaVencimiento = null;
        Date fechaSolicitud = null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpedientePrincipal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    fechaVencimiento = rs.getDate("fecha_vencimiento");
                    fechaSolicitud = rs.getDate("fecha_recepcion");
                }
            }
        }
        if (fechaVencimiento != null) {
            return fechaVencimiento;
        }
        if (fechaSolicitud == null) {
            return null;
        }
        Date calculada = Date.valueOf(
                calendarioLaboralService.calcularFechaVencimientoSolicitud(conn, fechaSolicitud.toLocalDate()));
        sincronizarFechaVencimientoRelacionado(conn, idExpedientePrincipal, calculada, idUsuarioCreador);
        return calculada;
    }

    private void sincronizarFechaVencimientoRelacionado(
            Connection conn,
            Long idExpediente,
            Date fechaVencimiento,
            Long idUsuarioModificador) throws SQLException {
        if (idExpediente == null || fechaVencimiento == null) {
            return;
        }
        String sql = "UPDATE expediente SET fecha_vencimiento = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, fechaVencimiento);
            setLongOrNull(ps, 2, idUsuarioModificador);
            ps.setLong(3, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar la fecha de vencimiento del expediente relacionado.");
            }
        }
    }

    private void sincronizarNumeroExpedienteRelacionado(
            Connection conn,
            Long idPrincipal,
            Long idRelacionado,
            Long idUsuarioModificador) throws SQLException {
        if (idPrincipal == null || idRelacionado == null) {
            return;
        }
        String sqlPrincipal = "SELECT numero_expediente FROM expediente WHERE id_expediente = ? AND activo = 1";
        String numeroPrincipal = null;
        try (PreparedStatement ps = conn.prepareStatement(sqlPrincipal)) {
            ps.setLong(1, idPrincipal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    numeroPrincipal = rs.getString("numero_expediente");
                }
            }
        }
        if (numeroPrincipal == null || numeroPrincipal.trim().isEmpty()) {
            return;
        }
        String sqlUpdate = "UPDATE expediente "
                + "SET numero_expediente = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
            ps.setString(1, numeroPrincipal.trim());
            setLongOrNull(ps, 2, idUsuarioModificador);
            ps.setLong(3, idRelacionado);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo sincronizar el número del expediente relacionado.");
            }
        }
    }

    private boolean estaRelacionadoComoHijoEnOtroPrincipal(Connection conn, Long idRelacionado, Long idPrincipalPermitido) throws SQLException {
        if (idRelacionado == null) {
            return false;
        }
        String sql = "SELECT id_expediente_principal "
                + "FROM expediente_relacion "
                + "WHERE activo = 1 AND id_expediente_relacionado = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idRelacionado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long idPrincipal = getLongOrNull(rs, "id_expediente_principal");
                    if (idPrincipal != null && !idPrincipal.equals(idPrincipalPermitido)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean existeExpedienteActivo(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT 1 FROM expediente WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean existeRelacionActiva(Connection conn, Long idPrincipal, Long idRelacionado) throws SQLException {
        String sql = "SELECT 1 FROM expediente_relacion "
                + "WHERE activo = 1 AND ("
                + "(id_expediente_principal = ? AND id_expediente_relacionado = ?) "
                + "OR (id_expediente_principal = ? AND id_expediente_relacionado = ?))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idPrincipal);
            ps.setLong(2, idRelacionado);
            ps.setLong(3, idRelacionado);
            ps.setLong(4, idPrincipal);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private ExpedienteRelacionadoDTO mapPosible(ResultSet rs) throws SQLException {
        return new ExpedienteRelacionadoDTO(
                getLongOrNull(rs, "id_expediente"),
                rs.getString("numero_expediente"),
                rs.getString("numero_expediente_sgd"),
                rs.getString("numero_tramite_documentario"),
                rs.getString("numero_documento"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                rs.getString("procedimiento"),
                rs.getString("solicitante"),
                rs.getString("equipo_asignado"),
                getLongOrNull(rs, "id_equipo_responsable"),
                rs.getString("abogado_asignado"),
                getLongOrNull(rs, "id_abogado_responsable"),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                toLocalDate(rs.getDate("fecha_vencimiento")),
                null,
                rs.getString("motivo_coincidencia"),
                rs.getString("alerta_ingreso"),
                "",
                "",
                null,
                "");
    }

    private ExpedienteRelacionadoDTO mapAsociado(Connection conn, ResultSet rs) throws SQLException {
        java.time.LocalDate fechaVencimiento = toLocalDate(rs.getDate("fecha_vencimiento"));
        return new ExpedienteRelacionadoDTO(
                getLongOrNull(rs, "id_expediente"),
                rs.getString("numero_expediente"),
                rs.getString("numero_expediente_sgd"),
                rs.getString("numero_tramite_documentario"),
                rs.getString("numero_documento"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                rs.getString("procedimiento"),
                rs.getString("solicitante"),
                rs.getString("equipo_asignado"),
                getLongOrNull(rs, "id_equipo_responsable"),
                rs.getString("abogado_asignado"),
                getLongOrNull(rs, "id_abogado_responsable"),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                fechaVencimiento,
                fechaVencimiento == null ? null : calendarioLaboralService.calcularDiasHabilesRestantes(conn, rs.getDate("fecha_vencimiento")),
                "",
                rs.getString("alerta_ingreso"),
                rs.getString("tipo_relacion"),
                rs.getString("descripcion"),
                toLocalDateTime(rs.getTimestamp("fecha_asociacion")),
                rs.getString("usuario_relacion"));
    }

    private static String normalizarPersona(String alias) {
        return "NULLIF(UPPER(TRIM(REGEXP_REPLACE(NVL(" + alias + ".razon_social, "
                + "TRIM(NVL(" + alias + ".nombres, '') || ' ' || NVL(" + alias + ".apellidos, ''))), "
                + "'[[:space:]]+', ' '))), '')";
    }

    private static String nombrePersona(String alias) {
        return "TRIM(NVL(" + alias + ".razon_social, TRIM(NVL(" + alias + ".nombres, '') || ' ' || NVL(" + alias + ".apellidos, ''))))";
    }

    private static String numeroDocumentoRelacionadoSql(String expedienteAlias) {
        return "(SELECT MIN(ed.numero_documento) KEEP (DENSE_RANK FIRST ORDER BY ed.id_expediente_documento) "
                + "FROM expediente_documento ed "
                + "WHERE ed.id_expediente = " + expedienteAlias + ".id_expediente "
                + "AND ed.activo = 1 "
                + "AND TRIM(ed.numero_documento) IS NOT NULL)";
    }

    private static String resumenAlertasSql(String expedienteAlias) {
        return "(SELECT LISTAGG(a.mensaje, ' / ') WITHIN GROUP (ORDER BY a.creado_en, a.id_expediente_alerta) "
                + "FROM expediente_alerta a "
                + "WHERE a.id_expediente = " + expedienteAlias + ".id_expediente "
                + "AND a.activo = 1 AND a.atendida = 0)";
    }

    private Long obtenerGeneratedKey(PreparedStatement ps, String entidad) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
        throw new SQLException("No se pudo obtener el identificador generado de " + entidad + ".");
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // El error original se reporta al usuario; el rollback fallido no debe ocultarlo.
        }
    }

    private static final class OrientacionRelacion {
        private final Long idPrincipal;
        private final Long idRelacionado;

        private OrientacionRelacion(Long idPrincipal, Long idRelacionado) {
            this.idPrincipal = idPrincipal;
            this.idRelacionado = idRelacionado;
        }
    }

    private static final class ExpedienteReferencia {
        private final String numeroExpediente;
        private final LocalDateTime fechaRegistro;

        private ExpedienteReferencia(String numeroExpediente, LocalDateTime fechaRegistro) {
            this.numeroExpediente = numeroExpediente;
            this.fechaRegistro = fechaRegistro;
        }

        private boolean tieneNumero() {
            return numeroExpediente != null && !numeroExpediente.trim().isEmpty();
        }
    }

    private static final class AsignacionActual {
        private final Long idUsuario;
        private final Long idEquipo;
        private final Long idEtapa;
        private final Long idEtapaExpediente;
        private final Long idEstadoExpediente;
        private final String etapaCodigo;
        private final String estadoCodigo;

        private AsignacionActual(
                Long idUsuario,
                Long idEquipo,
                Long idEtapa,
                Long idEtapaExpediente,
                Long idEstadoExpediente,
                String etapaCodigo,
                String estadoCodigo) throws SQLException {
            if (idUsuario == null || idEtapa == null) {
                throw new SQLException("La asignación vigente del expediente principal está incompleta.");
            }
            this.idUsuario = idUsuario;
            this.idEquipo = idEquipo;
            this.idEtapa = idEtapa;
            this.idEtapaExpediente = idEtapaExpediente;
            this.idEstadoExpediente = idEstadoExpediente;
            this.etapaCodigo = etapaCodigo == null ? "" : etapaCodigo;
            this.estadoCodigo = estadoCodigo == null ? "" : estadoCodigo;
        }

        private boolean esAsignado() {
            return CODIGO_ETAPA_ASIGNACION.equalsIgnoreCase(etapaCodigo)
                    && CODIGO_ESTADO_ASIGNADO.equalsIgnoreCase(estadoCodigo)
                    && idEtapaExpediente != null
                    && idEstadoExpediente != null;
        }
    }

    private static final class ExpedienteEstadoActual {
        private final Long idEtapa;
        private final Long idEstado;
        private final String etapaCodigo;
        private final String estadoCodigo;

        private ExpedienteEstadoActual(Long idEtapa, Long idEstado, String etapaCodigo, String estadoCodigo) {
            this.idEtapa = idEtapa;
            this.idEstado = idEstado;
            this.etapaCodigo = etapaCodigo;
            this.estadoCodigo = estadoCodigo;
        }
    }
}
