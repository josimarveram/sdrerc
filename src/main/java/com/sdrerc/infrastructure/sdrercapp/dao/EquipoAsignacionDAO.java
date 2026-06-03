package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EquipoAsignacionDAO {

    public List<EquipoAsignacionDTO> listarEquiposActivos() throws SQLException {
        String sql = "SELECT e.id_equipo, e.codigo, e.nombre, a.nombre AS area_nombre "
                + "FROM equipo e "
                + "LEFT JOIN area a ON a.id_area = e.id_area "
                + "WHERE e.activo = 1 "
                + "ORDER BY e.nombre";

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<EquipoAsignacionDTO> equipos = new ArrayList<>();
            while (rs.next()) {
                equipos.add(new EquipoAsignacionDTO(
                        getLongOrNull(rs, "id_equipo"),
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("area_nombre")));
            }
            return equipos;
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
