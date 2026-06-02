package com.sdrerc.infrastructure.sdrercapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CatalogoLookupDAO {

    public Long obtenerEtapaId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "etapa_expediente", "id_etapa", codigo);
    }

    public Long obtenerEstadoId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "estado_expediente", "id_estado", codigo);
    }

    public Long obtenerTipoMovimientoId(Connection conn, String codigo) throws SQLException {
        return obtenerIdPorCodigo(conn, "tipo_movimiento", "id_tipo_movimiento", codigo);
    }

    private Long obtenerIdPorCodigo(Connection conn, String tabla, String columnaId, String codigo) throws SQLException {
        String sql = "SELECT " + columnaId + " FROM " + tabla + " WHERE UPPER(codigo) = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo == null ? "" : codigo.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
    }
}
