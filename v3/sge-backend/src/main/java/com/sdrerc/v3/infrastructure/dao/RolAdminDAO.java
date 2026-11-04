package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.domain.RolAdminDTO;
import com.sdrerc.v3.domain.RolResumenDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Port de {@code com.sdrerc.infrastructure.sdrercapp.dao.RolDAO} (V2), Administración &gt; Roles.
 * {@link #listarActivos()} se conserva tal cual (ya la usaba Administración &gt; Usuarios desde el
 * incremento anterior, catálogo mínimo para el multi-select "Roles y equipos") — el resto del CRUD
 * es nuevo en este incremento.
 */
@Repository
public class RolAdminDAO {

    private static final int DEFAULT_LIMIT = 300;
    private static final int MAX_LIMIT = 1000;

    private final DataSource dataSource;

    public RolAdminDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<RolResumenDTO> listarActivos() throws SQLException {
        String sql = "SELECT id_rol, codigo, nombre FROM rol WHERE activo = 1 ORDER BY nombre";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<RolResumenDTO> roles = new ArrayList<>();
            while (rs.next()) {
                roles.add(new RolResumenDTO(rs.getLong("id_rol"), rs.getString("codigo"), rs.getString("nombre")));
            }
            return roles;
        }
    }

    public List<RolAdminDTO> buscar(String texto, Boolean activo, Integer limite) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM (");
        sql.append(selectRolesBase());
        sql.append("WHERE 1 = 1 ");
        if (hasText(texto)) {
            sql.append("AND (UPPER(NVL(r.codigo, '')) LIKE ? OR UPPER(NVL(r.nombre, '')) LIKE ? "
                    + "OR UPPER(NVL(r.descripcion, '')) LIKE ?) ");
            String pattern = "%" + texto.trim().toUpperCase(Locale.ROOT) + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (activo != null) {
            sql.append("AND r.activo = ? ");
            params.add(activo ? 1 : 0);
        }
        sql.append("ORDER BY r.nombre ASC, r.codigo ASC");
        sql.append(") WHERE ROWNUM <= ?");
        params.add(normalizarLimite(limite));

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<RolAdminDTO> roles = new ArrayList<>();
                while (rs.next()) {
                    roles.add(mapRol(rs));
                }
                return roles;
            }
        }
    }

    public RolAdminDTO obtenerPorId(Long idRol) throws SQLException {
        if (idRol == null) {
            return null;
        }
        String sql = selectRolesBase() + "WHERE r.id_rol = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRol(rs) : null;
            }
        }
    }

    public boolean existeCodigo(String codigo, Long excluirIdRol) throws SQLException {
        if (!hasText(codigo)) {
            return false;
        }
        StringBuilder sql = new StringBuilder("SELECT 1 FROM rol WHERE UPPER(codigo) = ? ");
        if (excluirIdRol != null) {
            sql.append("AND id_rol <> ? ");
        }
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, codigo.trim().toUpperCase(Locale.ROOT));
            if (excluirIdRol != null) {
                ps.setLong(2, excluirIdRol);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public RolAdminDTO insertar(RolAdminDTO rol, Long idUsuarioActual) throws SQLException {
        String sql = "INSERT INTO rol (codigo, nombre, descripcion, activo, creado_por, creado_en) "
                + "VALUES (?, ?, ?, ?, ?, SYSTIMESTAMP)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_ROL"})) {
            ps.setString(1, rol.getCodigo());
            ps.setString(2, rol.getNombre());
            ps.setString(3, emptyToNull(rol.getDescripcion()));
            ps.setInt(4, rol.isActivo() ? 1 : 0);
            setNullableLong(ps, 5, idUsuarioActual);
            ps.executeUpdate();
            return obtenerPorId(obtenerGeneratedKey(ps));
        }
    }

    public RolAdminDTO actualizar(RolAdminDTO rol, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE rol SET codigo = ?, nombre = ?, descripcion = ?, activo = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP WHERE id_rol = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rol.getCodigo());
            ps.setString(2, rol.getNombre());
            ps.setString(3, emptyToNull(rol.getDescripcion()));
            ps.setInt(4, rol.isActivo() ? 1 : 0);
            setNullableLong(ps, 5, idUsuarioActual);
            ps.setLong(6, rol.getIdRol());
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el rol seleccionado.");
            }
        }
        return obtenerPorId(rol.getIdRol());
    }

    public RolAdminDTO cambiarActivo(Long idRol, boolean activo, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE rol SET activo = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP WHERE id_rol = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, activo ? 1 : 0);
            setNullableLong(ps, 2, idUsuarioActual);
            ps.setLong(3, idRol);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo cambiar el estado del rol seleccionado.");
            }
        }
        return obtenerPorId(idRol);
    }

    public int contarUsuariosActivos(Long idRol) throws SQLException {
        if (idRol == null) {
            return 0;
        }
        String sql = "SELECT COUNT(1) AS total FROM usuario_rol ur "
                + "JOIN usuario u ON u.id_usuario = ur.id_usuario "
                + "WHERE ur.id_rol = ? AND ur.activo = 1 AND u.activo = 1 AND UPPER(u.estado) = 'ACTIVO'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    private String selectRolesBase() {
        return "SELECT r.id_rol, r.codigo, r.nombre, r.descripcion, r.activo, r.creado_en, r.modificado_en, "
                + "(SELECT COUNT(1) FROM usuario_rol ur JOIN usuario u ON u.id_usuario = ur.id_usuario "
                + " WHERE ur.id_rol = r.id_rol AND ur.activo = 1 AND u.activo = 1 AND UPPER(u.estado) = 'ACTIVO') AS usuarios_asociados, "
                + "(SELECT COUNT(1) FROM rol_permiso rp JOIN permiso p ON p.id_permiso = rp.id_permiso "
                + " WHERE rp.id_rol = r.id_rol AND rp.activo = 1 AND p.activo = 1) AS permisos_asociados "
                + "FROM rol r ";
    }

    private RolAdminDTO mapRol(ResultSet rs) throws SQLException {
        RolAdminDTO dto = new RolAdminDTO();
        dto.setIdRol(getLongOrNull(rs, "id_rol"));
        dto.setCodigo(rs.getString("codigo"));
        dto.setNombre(rs.getString("nombre"));
        dto.setDescripcion(rs.getString("descripcion"));
        dto.setActivo(rs.getInt("activo") == 1);
        dto.setUsuariosAsociados(rs.getInt("usuarios_asociados"));
        dto.setPermisosAsociados(rs.getInt("permisos_asociados"));
        dto.setCreadoEn(toLocalDateTime(rs.getTimestamp("creado_en")));
        dto.setModificadoEn(toLocalDateTime(rs.getTimestamp("modificado_en")));
        return dto;
    }

    private static int normalizarLimite(Integer limite) {
        if (limite == null || limite <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limite, MAX_LIMIT);
    }

    private static void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private static Long obtenerGeneratedKey(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
        throw new SQLException("No se obtuvo el identificador generado para el rol.");
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
