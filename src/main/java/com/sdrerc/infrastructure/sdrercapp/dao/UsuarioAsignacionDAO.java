package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioAsignacionDAO {

    public List<UsuarioAsignableDTO> listarAbogadosAsignables(Long idEquipo) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT u.id_usuario, u.username, u.nombre_completo, ");
        sql.append("eu.id_equipo, eq.nombre AS equipo_nombre, r.codigo AS rol_codigo, ");
        sql.append("sup.nombre_completo AS supervisor_nombre ");
        sql.append("FROM usuario u ");
        sql.append("JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario AND ur.activo = 1 ");
        sql.append("JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1 ");
        sql.append("LEFT JOIN equipo_usuario eu ON eu.id_usuario = u.id_usuario AND eu.activo = 1 ");
        sql.append("LEFT JOIN equipo eq ON eq.id_equipo = eu.id_equipo AND eq.activo = 1 ");
        sql.append("LEFT JOIN usuario_supervision us ON us.id_abogado = u.id_usuario AND us.activo = 1 ");
        sql.append("LEFT JOIN usuario sup ON sup.id_usuario = us.id_supervisor AND sup.activo = 1 ");
        sql.append("WHERE u.activo = 1 ");
        sql.append("AND UPPER(u.estado) = 'ACTIVO' ");
        sql.append("AND UPPER(r.codigo) IN ('ABOGADO', 'ANALISTA') ");
        if (idEquipo != null) {
            sql.append("AND eu.id_equipo = ? ");
            params.add(idEquipo);
        }
        sql.append("ORDER BY u.nombre_completo");

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<UsuarioAsignableDTO> usuarios = new ArrayList<>();
                while (rs.next()) {
                    usuarios.add(new UsuarioAsignableDTO(
                            getLongOrNull(rs, "id_usuario"),
                            rs.getString("username"),
                            rs.getString("nombre_completo"),
                            getLongOrNull(rs, "id_equipo"),
                            rs.getString("equipo_nombre"),
                            rs.getString("rol_codigo"),
                            rs.getString("supervisor_nombre")));
                }
                return usuarios;
            }
        }
    }

    public Long obtenerIdUsuarioActivoPorUsername(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT id_usuario FROM usuario "
                + "WHERE UPPER(username) = ? AND activo = 1 AND UPPER(estado) = 'ACTIVO'";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long value = rs.getLong("id_usuario");
                return rs.wasNull() ? null : value;
            }
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
