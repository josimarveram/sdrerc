package com.sdrerc.infrastructure.sdrercapp.dao;

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
import java.util.List;

public class ExpedienteRelacionadoDAO {

    private static final String TIPO_RELACION_MISMA_ACTA_TITULAR = "MISMA_ACTA_TITULAR";
    private static final String MOVIMIENTO_ASOCIACION_DUPLICADO = "ASOCIACION_DUPLICADO";
    private static final String MOTIVO_MISMA_ACTA_TITULAR = "Misma acta y titular";
    private static final int DIAS_PLAZO_INICIAL = 30;

    private final CatalogoLookupDAO catalogoLookupDAO;

    public ExpedienteRelacionadoDAO() {
        this(new CatalogoLookupDAO());
    }

    public ExpedienteRelacionadoDAO(CatalogoLookupDAO catalogoLookupDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
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
                + "SELECT DISTINCT e.id_expediente, e.numero_expediente, ta.nombre AS tipo_acta, ea.numero_acta, "
                + nombrePersona("p") + " AS titular, esol.asunto AS procedimiento, "
                + "et.codigo AS etapa_codigo, est.codigo AS estado_codigo, esol.fecha_recepcion, "
                + "? AS motivo_coincidencia "
                + "FROM base b "
                + "JOIN expediente_acta ea ON UPPER(TRIM(ea.numero_acta)) = b.numero_acta_norm "
                + " AND NVL(ea.id_tipo_acta, -1) = NVL(b.id_tipo_acta, -1) AND ea.activo = 1 "
                + "JOIN expediente e ON e.id_expediente = ea.id_expediente AND e.activo = 1 AND e.id_expediente <> b.id_expediente "
                + "JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                + "JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta "
                + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                + "WHERE b.numero_acta_norm IS NOT NULL "
                + "AND b.titular_norm IS NOT NULL "
                + "AND " + normalizarPersona("p") + " = b.titular_norm "
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
                + " AND NVL(ea.id_tipo_acta, -1) = NVL(b.id_tipo_acta, -1) AND ea.activo = 1 "
                + "JOIN expediente e ON e.id_expediente = ea.id_expediente AND e.activo = 1 AND e.id_expediente <> b.id_expediente "
                + "JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                + "JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                + "WHERE b.numero_acta_norm IS NOT NULL "
                + "AND b.titular_norm IS NOT NULL "
                + "AND " + normalizarPersona("p") + " = b.titular_norm"
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

        String sql = "SELECT e.id_expediente, e.numero_expediente, ta.nombre AS tipo_acta, ea.numero_acta, "
                + nombrePersona("p") + " AS titular, esol.asunto AS procedimiento, "
                + "et.codigo AS etapa_codigo, est.codigo AS estado_codigo, esol.fecha_recepcion, "
                + "r.tipo_relacion, r.descripcion, r.creado_en, u.nombre_completo AS usuario_relacion "
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
                + "LEFT JOIN usuario u ON u.id_usuario = r.creado_por "
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
                    asociados.add(mapAsociado(rs));
                }
            }
        }
        return asociados;
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
        int omitidos = 0;
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, MOVIMIENTO_ASOCIACION_DUPLICADO);
                Date fechaVencimientoPrincipal = resolverFechaVencimientoPrincipal(conn, idExpedientePrincipal, idUsuarioCreador);
                for (Long idRelacionado : idsRelacionados) {
                    if (idRelacionado == null || idRelacionado.equals(idExpedientePrincipal)) {
                        omitidos++;
                        continue;
                    }
                    if (!existeExpedienteActivo(conn, idExpedientePrincipal) || !existeExpedienteActivo(conn, idRelacionado)) {
                        throw new SQLException("Uno de los expedientes seleccionados ya no existe o no está activo.");
                    }
                    if (existeRelacionActiva(conn, idExpedientePrincipal, idRelacionado)) {
                        omitidos++;
                        continue;
                    }
                    Long idRelacion = insertarRelacion(conn, idExpedientePrincipal, idRelacionado, idUsuarioCreador, descripcion);
                    sincronizarFechaVencimientoRelacionado(conn, idRelacionado, fechaVencimientoPrincipal, idUsuarioCreador);
                    if (idMovimiento != null) {
                        insertarHistorialRelacion(conn, idExpedientePrincipal, idMovimiento, idUsuarioCreador, idRelacion, descripcion);
                        insertarHistorialRelacion(conn, idRelacionado, idMovimiento, idUsuarioCreador, idRelacion, descripcion);
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
        String mensaje = asociados + " relación(es) confirmada(s).";
        if (omitidos > 0) {
            mensaje += " " + omitidos + " expediente(s) omitido(s) por duplicado o selección inválida.";
        }
        return new ExpedienteRelacionResultadoDTO(idsRelacionados.size(), asociados, omitidos, mensaje);
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
            ps.setString(3, TIPO_RELACION_MISMA_ACTA_TITULAR);
            ps.setString(4, descripcion == null || descripcion.trim().isEmpty() ? MOTIVO_MISMA_ACTA_TITULAR : descripcion.trim());
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
            ps.setLong(4, idRelacion);
            ps.setString(5, descripcion == null || descripcion.trim().isEmpty()
                    ? "Relación confirmada por misma acta y titular."
                    : descripcion.trim());
            ps.setString(6, TIPO_RELACION_MISMA_ACTA_TITULAR);
            setLongOrNull(ps, 7, idUsuarioCreador);
            ps.executeUpdate();
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
        Date calculada = Date.valueOf(fechaSolicitud.toLocalDate().plusDays(DIAS_PLAZO_INICIAL));
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
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                rs.getString("procedimiento"),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                rs.getString("motivo_coincidencia"),
                "",
                "",
                null,
                "");
    }

    private ExpedienteRelacionadoDTO mapAsociado(ResultSet rs) throws SQLException {
        return new ExpedienteRelacionadoDTO(
                getLongOrNull(rs, "id_expediente"),
                rs.getString("numero_expediente"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                rs.getString("procedimiento"),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                "",
                rs.getString("tipo_relacion"),
                rs.getString("descripcion"),
                toLocalDateTime(rs.getTimestamp("creado_en")),
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
}
