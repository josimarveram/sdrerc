package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.ExpedienteConsolaDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpedienteConsolaDAO {

    public ExpedienteConsolaDTO obtenerPorExpediente(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return null;
        }

        String sql = "SELECT id_expediente, numero_expediente, numero_tramite_documentario, "
                + "etapa_codigo, estado_codigo, abogado_inicial, responsable_actual, equipo_actual, "
                + "titular, titular_documento, remitente, remitente_documento, procedimiento, canal_recepcion, fecha_recepcion, "
                + "tipo_documento, numero_documento, tipo_acta, numero_acta, anio_acta, oficina_registral, "
                + "tipo_resolucion, numero_resolucion, fecha_resolucion, fecha_firma, "
                + "tipo_notificacion, estado_notificacion, resultado_notificacion, estado_cargo_acuse, "
                + "estado_publicacion, medio_publicacion, numero_publicacion, ruta_carpeta_digital, enlace_carpeta_digital, "
                + "fecha_registro, fecha_ultimo_movimiento, fecha_vencimiento, "
                + "requiere_publicacion, expediente_digital_completo, total_documentos, "
                + "observaciones_pendientes, total_notificaciones, total_cargos "
                + "FROM (SELECT c.*, "
                + personSubquery("TITULAR", "titular")
                + ", " + personDocumentSubquery("TITULAR", "titular_documento")
                + ", " + personSubquery("REMITENTE", "remitente")
                + ", " + personDocumentSubquery("REMITENTE", "remitente_documento")
                + ", (SELECT procedimiento FROM (SELECT s.asunto AS procedimiento FROM expediente_solicitud s WHERE s.id_expediente = c.id_expediente AND s.activo = 1 ORDER BY s.creado_en DESC) WHERE ROWNUM = 1) AS procedimiento "
                + ", (SELECT canal FROM (SELECT cr.nombre AS canal FROM expediente_solicitud s LEFT JOIN canal_recepcion cr ON cr.id_canal_recepcion = s.id_canal_recepcion WHERE s.id_expediente = c.id_expediente AND s.activo = 1 ORDER BY s.creado_en DESC) WHERE ROWNUM = 1) AS canal_recepcion "
                + ", (SELECT fecha_recepcion FROM (SELECT s.fecha_recepcion FROM expediente_solicitud s WHERE s.id_expediente = c.id_expediente AND s.activo = 1 ORDER BY s.creado_en DESC) WHERE ROWNUM = 1) AS fecha_recepcion "
                + ", (SELECT tipo_documento FROM (SELECT d.nombre_documento AS tipo_documento FROM expediente_documento d WHERE d.id_expediente = c.id_expediente AND d.activo = 1 ORDER BY d.creado_en DESC) WHERE ROWNUM = 1) AS tipo_documento "
                + ", (SELECT numero_documento FROM (SELECT d.numero_documento FROM expediente_documento d WHERE d.id_expediente = c.id_expediente AND d.activo = 1 ORDER BY d.creado_en DESC) WHERE ROWNUM = 1) AS numero_documento "
                + ", (SELECT tipo_acta FROM (SELECT ta.nombre AS tipo_acta FROM expediente_acta a LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = a.id_tipo_acta WHERE a.id_expediente = c.id_expediente AND a.activo = 1 ORDER BY a.creado_en DESC) WHERE ROWNUM = 1) AS tipo_acta "
                + ", (SELECT numero_acta FROM (SELECT a.numero_acta FROM expediente_acta a WHERE a.id_expediente = c.id_expediente AND a.activo = 1 ORDER BY a.creado_en DESC) WHERE ROWNUM = 1) AS numero_acta "
                + ", (SELECT anio_acta FROM (SELECT a.anio_acta FROM expediente_acta a WHERE a.id_expediente = c.id_expediente AND a.activo = 1 ORDER BY a.creado_en DESC) WHERE ROWNUM = 1) AS anio_acta "
                + ", (SELECT oficina_registral FROM (SELECT a.oficina_registral FROM expediente_acta a WHERE a.id_expediente = c.id_expediente AND a.activo = 1 ORDER BY a.creado_en DESC) WHERE ROWNUM = 1) AS oficina_registral "
                + ", (SELECT tipo_resolucion FROM (SELECT tr.nombre AS tipo_resolucion FROM expediente_resolucion r LEFT JOIN tipo_resolucion tr ON tr.id_tipo_resolucion = r.id_tipo_resolucion WHERE r.id_expediente = c.id_expediente AND r.activo = 1 ORDER BY r.creado_en DESC) WHERE ROWNUM = 1) AS tipo_resolucion "
                + ", (SELECT numero_resolucion FROM (SELECT r.numero_resolucion FROM expediente_resolucion r WHERE r.id_expediente = c.id_expediente AND r.activo = 1 ORDER BY r.creado_en DESC) WHERE ROWNUM = 1) AS numero_resolucion "
                + ", (SELECT fecha_resolucion FROM (SELECT r.fecha_resolucion FROM expediente_resolucion r WHERE r.id_expediente = c.id_expediente AND r.activo = 1 ORDER BY r.creado_en DESC) WHERE ROWNUM = 1) AS fecha_resolucion "
                + ", (SELECT fecha_firma FROM (SELECT r.fecha_firma FROM expediente_resolucion r WHERE r.id_expediente = c.id_expediente AND r.activo = 1 ORDER BY r.creado_en DESC) WHERE ROWNUM = 1) AS fecha_firma "
                + ", (SELECT tipo_notificacion FROM (SELECT tn.nombre AS tipo_notificacion FROM expediente_notificacion n JOIN tipo_notificacion tn ON tn.id_tipo_notificacion = n.id_tipo_notificacion WHERE n.id_expediente = c.id_expediente AND n.activo = 1 ORDER BY n.creado_en DESC) WHERE ROWNUM = 1) AS tipo_notificacion "
                + ", (SELECT estado_notificacion FROM (SELECT en.nombre AS estado_notificacion FROM expediente_notificacion n JOIN estado_notificacion en ON en.id_estado_notificacion = n.id_estado_notificacion WHERE n.id_expediente = c.id_expediente AND n.activo = 1 ORDER BY n.creado_en DESC) WHERE ROWNUM = 1) AS estado_notificacion "
                + ", (SELECT resultado FROM (SELECT n.resultado FROM expediente_notificacion n WHERE n.id_expediente = c.id_expediente AND n.activo = 1 ORDER BY n.creado_en DESC) WHERE ROWNUM = 1) AS resultado_notificacion "
                + ", (SELECT estado_cargo FROM (SELECT ec.nombre AS estado_cargo FROM expediente_cargo_acuse ca JOIN estado_cargo_acuse ec ON ec.id_estado_cargo_acuse = ca.id_estado_cargo_acuse WHERE ca.id_expediente = c.id_expediente AND ca.activo = 1 ORDER BY ca.creado_en DESC) WHERE ROWNUM = 1) AS estado_cargo_acuse "
                + ", (SELECT estado_publicacion FROM (SELECT p.estado_publicacion FROM expediente_publicacion p WHERE p.id_expediente = c.id_expediente AND p.activo = 1 ORDER BY p.creado_en DESC) WHERE ROWNUM = 1) AS estado_publicacion "
                + ", (SELECT medio_publicacion FROM (SELECT p.medio_publicacion FROM expediente_publicacion p WHERE p.id_expediente = c.id_expediente AND p.activo = 1 ORDER BY p.creado_en DESC) WHERE ROWNUM = 1) AS medio_publicacion "
                + ", (SELECT numero_publicacion FROM (SELECT p.numero_publicacion FROM expediente_publicacion p WHERE p.id_expediente = c.id_expediente AND p.activo = 1 ORDER BY p.creado_en DESC) WHERE ROWNUM = 1) AS numero_publicacion "
                + ", (SELECT ruta_carpeta FROM (SELECT d.ruta_carpeta FROM expediente_digital d WHERE d.id_expediente = c.id_expediente AND d.activo = 1 ORDER BY d.creado_en DESC) WHERE ROWNUM = 1) AS ruta_carpeta_digital "
                + ", (SELECT enlace_carpeta FROM (SELECT d.enlace_carpeta FROM expediente_digital d WHERE d.id_expediente = c.id_expediente AND d.activo = 1 ORDER BY d.creado_en DESC) WHERE ROWNUM = 1) AS enlace_carpeta_digital "
                + "FROM vw_expediente_consola c) "
                + "WHERE id_expediente = ?";

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return map(rs);
            }
        }
    }

    private ExpedienteConsolaDTO map(ResultSet rs) throws SQLException {
        return new ExpedienteConsolaDTO(
                getLongOrNull(rs, "id_expediente"),
                rs.getString("numero_expediente"),
                rs.getString("numero_tramite_documentario"),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getString("abogado_inicial"),
                rs.getString("responsable_actual"),
                rs.getString("equipo_actual"),
                rs.getString("titular"),
                rs.getString("titular_documento"),
                rs.getString("remitente"),
                rs.getString("remitente_documento"),
                rs.getString("procedimiento"),
                rs.getString("canal_recepcion"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                rs.getString("tipo_documento"),
                rs.getString("numero_documento"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                getIntegerOrNull(rs, "anio_acta"),
                rs.getString("oficina_registral"),
                rs.getString("tipo_resolucion"),
                rs.getString("numero_resolucion"),
                toLocalDate(rs.getDate("fecha_resolucion")),
                toLocalDateTime(rs.getTimestamp("fecha_firma")),
                rs.getString("tipo_notificacion"),
                rs.getString("estado_notificacion"),
                rs.getString("resultado_notificacion"),
                rs.getString("estado_cargo_acuse"),
                rs.getString("estado_publicacion"),
                rs.getString("medio_publicacion"),
                rs.getString("numero_publicacion"),
                rs.getString("ruta_carpeta_digital"),
                rs.getString("enlace_carpeta_digital"),
                toLocalDateTime(rs.getTimestamp("fecha_registro")),
                toLocalDateTime(rs.getTimestamp("fecha_ultimo_movimiento")),
                toLocalDate(rs.getDate("fecha_vencimiento")),
                getBooleanFromNumber(rs, "requiere_publicacion"),
                getBooleanFromNumber(rs, "expediente_digital_completo"),
                getIntegerOrNull(rs, "total_documentos"),
                getIntegerOrNull(rs, "observaciones_pendientes"),
                getIntegerOrNull(rs, "total_notificaciones"),
                getIntegerOrNull(rs, "total_cargos")
        );
    }

    private static String personSubquery(String tipoRelacion, String alias) {
        return "(SELECT nombre_persona FROM (SELECT COALESCE(NULLIF(TRIM(p.razon_social), ''), "
                + "NULLIF(TRIM(TRIM(NVL(p.nombres, '')) || ' ' || TRIM(NVL(p.apellidos, ''))), ''), "
                + "p.numero_documento) AS nombre_persona "
                + "FROM expediente_persona ep JOIN persona p ON p.id_persona = ep.id_persona "
                + "WHERE ep.id_expediente = c.id_expediente AND ep.activo = 1 AND ep.tipo_relacion_persona = '" + tipoRelacion + "' "
                + "ORDER BY ep.creado_en DESC) WHERE ROWNUM = 1) AS " + alias;
    }

    private static String personDocumentSubquery(String tipoRelacion, String alias) {
        return "(SELECT documento_persona FROM (SELECT TRIM(NVL(p.tipo_documento, '') || ' ' || NVL(p.numero_documento, '')) AS documento_persona "
                + "FROM expediente_persona ep JOIN persona p ON p.id_persona = ep.id_persona "
                + "WHERE ep.id_expediente = c.id_expediente AND ep.activo = 1 AND ep.tipo_relacion_persona = '" + tipoRelacion + "' "
                + "ORDER BY ep.creado_en DESC) WHERE ROWNUM = 1) AS " + alias;
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Integer getIntegerOrNull(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static boolean getBooleanFromNumber(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return !rs.wasNull() && value == 1;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
