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
                + "fecha_registro, fecha_ultimo_movimiento, fecha_vencimiento, "
                + "requiere_publicacion, expediente_digital_completo, total_documentos, "
                + "observaciones_pendientes, total_notificaciones, total_cargos "
                + "FROM vw_expediente_consola "
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
