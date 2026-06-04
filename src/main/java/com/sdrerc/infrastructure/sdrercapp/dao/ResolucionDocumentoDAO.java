package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.ResolucionDocumentoDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ResolucionDocumentoDAO {

    public ResolucionDocumentoDTO obtenerActiva(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return null;
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return obtenerActiva(conn, idExpediente);
        }
    }

    public ResolucionDocumentoDTO obtenerActiva(Connection conn, Long idExpediente) throws SQLException {
        Long id = obtenerIdResolucionActiva(conn, idExpediente);
        if (id == null) {
            return null;
        }
        return obtenerPorId(conn, id);
    }

    public Long obtenerIdResolucionActiva(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT id_expediente_resolucion FROM ("
                + "SELECT id_expediente_resolucion FROM expediente_resolucion "
                + "WHERE id_expediente = ? AND activo = 1 "
                + "ORDER BY creado_en DESC, id_expediente_resolucion DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
    }

    public ResolucionDocumentoDTO obtenerPorId(Connection conn, Long idResolucion) throws SQLException {
        String sql = "SELECT r.id_expediente_resolucion, r.id_expediente, tr.nombre AS tipo_resolucion, "
                + "r.numero_resolucion, r.fecha_resolucion, r.fecha_firma "
                + "FROM expediente_resolucion r "
                + "LEFT JOIN tipo_resolucion tr ON tr.id_tipo_resolucion = r.id_tipo_resolucion "
                + "WHERE r.id_expediente_resolucion = ? AND r.activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idResolucion);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ResolucionDocumentoDTO(
                        getLongOrNull(rs, "id_expediente_resolucion"),
                        getLongOrNull(rs, "id_expediente"),
                        rs.getString("tipo_resolucion"),
                        rs.getString("numero_resolucion"),
                        toLocalDate(rs.getDate("fecha_resolucion")),
                        toLocalDateTime(rs.getTimestamp("fecha_firma")));
            }
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
