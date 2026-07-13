package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.PermisoDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermisoDAO {

    /**
     * Códigos de permiso activos de un usuario, resueltos vía usuario -&gt; usuario_rol -&gt; rol_permiso -&gt; permiso.
     */
    public Set<String> listarCodigosPermisoPorUsuario(Long idUsuario) throws SQLException {
        Set<String> codigos = new HashSet<String>();
        if (idUsuario == null) {
            return codigos;
        }
        String sql = "SELECT DISTINCT p.codigo "
                + "FROM permiso p "
                + "JOIN rol_permiso rp ON rp.id_permiso = p.id_permiso AND rp.activo = 1 "
                + "JOIN rol r ON r.id_rol = rp.id_rol AND r.activo = 1 "
                + "JOIN usuario_rol ur ON ur.id_rol = r.id_rol AND ur.activo = 1 "
                + "WHERE p.activo = 1 AND ur.id_usuario = ?";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String codigo = rs.getString("codigo");
                    if (codigo != null) {
                        codigos.add(codigo.trim().toUpperCase(java.util.Locale.ROOT));
                    }
                }
            }
        }
        return codigos;
    }

    public List<PermisoDTO> listarPermisosPorRol(Long idRol) throws SQLException {
        String sql = "SELECT p.id_permiso, p.codigo, p.nombre, p.modulo, p.activo, "
                + "CASE WHEN rp.id_permiso IS NULL THEN 0 ELSE 1 END AS asignado "
                + "FROM permiso p "
                + "LEFT JOIN ("
                + "  SELECT DISTINCT id_permiso FROM rol_permiso WHERE id_rol = ? AND activo = 1"
                + ") rp ON rp.id_permiso = p.id_permiso "
                + "WHERE p.activo = 1 "
                + "ORDER BY p.modulo ASC, p.nombre ASC, p.codigo ASC";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idRol == null) {
                ps.setNull(1, java.sql.Types.NUMERIC);
            } else {
                ps.setLong(1, idRol);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<PermisoDTO> permisos = new ArrayList<>();
                while (rs.next()) {
                    permisos.add(new PermisoDTO(
                            getLongOrNull(rs, "id_permiso"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("modulo"),
                            rs.getInt("activo") == 1,
                            rs.getInt("asignado") == 1));
                }
                return permisos;
            }
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
