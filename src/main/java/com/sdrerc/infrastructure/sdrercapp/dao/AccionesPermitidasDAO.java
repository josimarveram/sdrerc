package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.AccionPermitidaDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccionesPermitidasDAO {

    public List<AccionPermitidaDTO> listarPorExpediente(Long idExpediente) throws SQLException {
        List<AccionPermitidaDTO> acciones = new ArrayList<>();
        if (idExpediente == null) {
            return acciones;
        }

        String sql = "SELECT id_expediente, codigo_accion, nombre_accion, requiere_comentario, "
                + "requiere_documento, etapa_destino_codigo, estado_destino_codigo "
                + "FROM vw_expediente_acciones_permitidas "
                + "WHERE id_expediente = ? "
                + "ORDER BY codigo_accion";

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    acciones.add(map(rs));
                }
            }
        }
        return acciones;
    }

    private AccionPermitidaDTO map(ResultSet rs) throws SQLException {
        return new AccionPermitidaDTO(
                getLongOrNull(rs, "id_expediente"),
                rs.getString("codigo_accion"),
                rs.getString("nombre_accion"),
                getBooleanFromNumber(rs, "requiere_comentario"),
                getBooleanFromNumber(rs, "requiere_documento"),
                rs.getString("etapa_destino_codigo"),
                rs.getString("estado_destino_codigo")
        );
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static boolean getBooleanFromNumber(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return !rs.wasNull() && value == 1;
    }
}
