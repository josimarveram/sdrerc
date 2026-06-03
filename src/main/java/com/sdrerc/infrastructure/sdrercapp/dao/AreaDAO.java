package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.AreaDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AreaDAO {

    public List<AreaDTO> listar(Boolean activo) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id_area, codigo, nombre, descripcion, activo ");
        sql.append("FROM area WHERE 1 = 1 ");
        if (activo != null) {
            sql.append("AND activo = ? ");
        }
        sql.append("ORDER BY nombre");

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (activo != null) {
                ps.setInt(1, activo ? 1 : 0);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<AreaDTO> areas = new ArrayList<>();
                while (rs.next()) {
                    areas.add(mapArea(rs));
                }
                return areas;
            }
        }
    }

    private AreaDTO mapArea(ResultSet rs) throws SQLException {
        return new AreaDTO(
                getLongOrNull(rs, "id_area"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getInt("activo") == 1);
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
