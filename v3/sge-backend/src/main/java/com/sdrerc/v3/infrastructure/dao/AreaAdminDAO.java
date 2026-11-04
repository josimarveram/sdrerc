package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.domain.AreaAdminDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/** Port literal de {@code com.sdrerc.infrastructure.sdrercapp.dao.AreaDAO} (V2): catálogo `AREA`. */
@Repository
public class AreaAdminDAO {

    private final DataSource dataSource;

    public AreaAdminDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<AreaAdminDTO> listar(Boolean activo) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id_area, codigo, nombre, descripcion, activo FROM area WHERE 1 = 1 ");
        if (activo != null) {
            sql.append("AND activo = ? ");
        }
        sql.append("ORDER BY nombre");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (activo != null) {
                ps.setInt(1, activo ? 1 : 0);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<AreaAdminDTO> areas = new ArrayList<>();
                while (rs.next()) {
                    areas.add(new AreaAdminDTO(
                            getLongOrNull(rs, "id_area"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("descripcion"),
                            rs.getInt("activo") == 1));
                }
                return areas;
            }
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
