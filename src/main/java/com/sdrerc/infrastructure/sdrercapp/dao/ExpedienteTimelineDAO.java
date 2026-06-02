package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.ExpedienteTimelineDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExpedienteTimelineDAO {

    public List<ExpedienteTimelineDTO> listarPorExpediente(Long idExpediente) throws SQLException {
        List<ExpedienteTimelineDTO> timeline = new ArrayList<>();
        if (idExpediente == null) {
            return timeline;
        }

        String sql = "SELECT id_expediente_historial, id_expediente, fecha_movimiento, movimiento, "
                + "etapa_origen, etapa_destino, estado_origen, estado_destino, "
                + "usuario_origen, usuario_destino, comentario, motivo "
                + "FROM vw_expediente_timeline "
                + "WHERE id_expediente = ? "
                + "ORDER BY fecha_movimiento DESC NULLS LAST, id_expediente_historial DESC";

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    timeline.add(map(rs));
                }
            }
        }
        return timeline;
    }

    private ExpedienteTimelineDTO map(ResultSet rs) throws SQLException {
        return new ExpedienteTimelineDTO(
                getLongOrNull(rs, "id_expediente_historial"),
                getLongOrNull(rs, "id_expediente"),
                toLocalDateTime(rs.getTimestamp("fecha_movimiento")),
                rs.getString("movimiento"),
                rs.getString("etapa_origen"),
                rs.getString("etapa_destino"),
                rs.getString("estado_origen"),
                rs.getString("estado_destino"),
                rs.getString("usuario_origen"),
                rs.getString("usuario_destino"),
                rs.getString("comentario"),
                rs.getString("motivo")
        );
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
