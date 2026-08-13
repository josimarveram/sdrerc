package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.domain.UsuarioAsignableDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Subconjunto de com.sdrerc.infrastructure.sdrercapp.dao.UsuarioAsignacionDAO (V2): solo
 * {@code listarAbogadosAsignables}, que es lo que usa el combo "Abogado responsable" del bloque
 * "Asignación de abogado" en la Bandeja Asignación. El resto de métodos de V2 (carga laboral,
 * resolución de usuario actual por username, ids de equipo del usuario, etc.) se agregan cuando
 * el módulo que los necesita (Carga Abogados, visibilidad por asignación) se porte; en V3 no
 * hace falta resolver "usuario actual por username" porque {@code CurrentUser} ya trae el id
 * directamente desde el JWT.
 */
@Repository
public class UsuarioAsignacionDAO {

    private final DataSource dataSource;

    public UsuarioAsignacionDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<UsuarioAsignableDTO> listarAbogadosAsignables(Long idEquipo) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.id_usuario, u.username, u.nombre_completo, ");
        sql.append("eu.id_equipo, eq.nombre AS equipo_nombre, ");
        sql.append("(SELECT LISTAGG(r.codigo, ', ') WITHIN GROUP (ORDER BY r.codigo) ");
        sql.append("   FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1 ");
        sql.append("  WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1) AS rol_codigo, ");
        sql.append("(SELECT MAX(sup.nombre_completo) FROM usuario_supervision us ");
        sql.append("   JOIN usuario sup ON sup.id_usuario = us.id_supervisor AND sup.activo = 1 ");
        sql.append("  WHERE us.id_abogado = u.id_usuario AND us.activo = 1) AS supervisor_nombre ");
        sql.append("FROM usuario u ");
        sql.append("LEFT JOIN equipo_usuario eu ON eu.id_usuario = u.id_usuario AND eu.activo = 1 ");
        sql.append("LEFT JOIN equipo eq ON eq.id_equipo = eu.id_equipo AND eq.activo = 1 ");
        sql.append("WHERE u.activo = 1 ");
        sql.append("AND UPPER(u.estado) = 'ACTIVO' ");
        sql.append("AND EXISTS (SELECT 1 FROM usuario_rol ur2 ");
        sql.append("  JOIN rol r2 ON r2.id_rol = ur2.id_rol AND r2.activo = 1 ");
        sql.append(" WHERE ur2.id_usuario = u.id_usuario AND ur2.activo = 1 ");
        sql.append("   AND UPPER(r2.codigo) IN ('ABOGADO', 'ANALISTA')) ");
        if (idEquipo != null) {
            sql.append("AND eu.id_equipo = ? ");
            params.add(idEquipo);
        }
        sql.append("ORDER BY u.nombre_completo");

        try (Connection conn = dataSource.getConnection();
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

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
