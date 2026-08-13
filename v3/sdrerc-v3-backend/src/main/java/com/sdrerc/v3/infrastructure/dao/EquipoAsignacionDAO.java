package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.domain.EquipoAsignacionDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Port literal de com.sdrerc.infrastructure.sdrercapp.dao.EquipoAsignacionDAO (V2), mismo SQL.
 * Único cambio real: la conexión se obtiene de un {@link DataSource} inyectado por Spring.
 */
@Repository
public class EquipoAsignacionDAO {

    private final DataSource dataSource;

    public EquipoAsignacionDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<EquipoAsignacionDTO> listarEquiposActivos() throws SQLException {
        String sql = "SELECT e.id_equipo, e.codigo, e.nombre, a.nombre AS area_nombre "
                + "FROM equipo e "
                + "LEFT JOIN area a ON a.id_area = e.id_area "
                + "WHERE e.activo = 1 "
                + "ORDER BY e.nombre";

        try (Connection conn = dataSource.getConnection();
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
