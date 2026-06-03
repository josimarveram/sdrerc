package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.UsuarioDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioFiltroDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
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

public class UsuarioDAO {

    private static final int DEFAULT_LIMIT = 300;
    private static final int MAX_LIMIT = 1000;

    private final UsuarioRolDAO usuarioRolDAO;
    private final UsuarioEquipoDAO usuarioEquipoDAO;

    public UsuarioDAO() {
        this(new UsuarioRolDAO(), new UsuarioEquipoDAO());
    }

    public UsuarioDAO(UsuarioRolDAO usuarioRolDAO, UsuarioEquipoDAO usuarioEquipoDAO) {
        this.usuarioRolDAO = usuarioRolDAO;
        this.usuarioEquipoDAO = usuarioEquipoDAO;
    }

    public List<UsuarioDTO> buscar(UsuarioFiltroDTO filtro) throws SQLException {
        UsuarioFiltroDTO filtroSeguro = filtro == null ? new UsuarioFiltroDTO() : filtro;
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append(selectUsuariosBase());
        sql.append("WHERE 1 = 1 ");

        if (hasText(filtroSeguro.getTexto())) {
            sql.append("AND (UPPER(NVL(u.username, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(u.nombre_completo, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(u.correo, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(u.numero_documento, '')) LIKE ?) ");
            String pattern = "%" + filtroSeguro.getTexto().trim().toUpperCase(Locale.ROOT) + "%";
            for (int i = 0; i < 4; i++) {
                params.add(pattern);
            }
        }

        if (filtroSeguro.getActivo() != null) {
            sql.append("AND u.activo = ? ");
            params.add(filtroSeguro.getActivo() ? 1 : 0);
        }

        if (filtroSeguro.getIdRol() != null) {
            sql.append("AND EXISTS (SELECT 1 FROM usuario_rol urf ");
            sql.append("WHERE urf.id_usuario = u.id_usuario AND urf.id_rol = ? AND urf.activo = 1) ");
            params.add(filtroSeguro.getIdRol());
        }

        if (filtroSeguro.getIdEquipo() != null) {
            sql.append("AND EXISTS (SELECT 1 FROM equipo_usuario euf ");
            sql.append("WHERE euf.id_usuario = u.id_usuario AND euf.id_equipo = ? AND euf.activo = 1) ");
            params.add(filtroSeguro.getIdEquipo());
        }

        sql.append("ORDER BY u.nombre_completo ASC, u.username ASC");
        sql.append(") WHERE ROWNUM <= ?");
        params.add(normalizarLimite(filtroSeguro.getLimite()));

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<UsuarioDTO> usuarios = new ArrayList<>();
                while (rs.next()) {
                    usuarios.add(mapUsuario(rs));
                }
                return usuarios;
            }
        }
    }

    public UsuarioDTO obtenerPorId(Long idUsuario) throws SQLException {
        if (idUsuario == null) {
            return null;
        }
        String sql = selectUsuariosBase() + "WHERE u.id_usuario = ?";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUsuario(rs) : null;
            }
        }
    }

    public boolean existeUsername(String username, Long excluirIdUsuario) throws SQLException {
        if (!hasText(username)) {
            return false;
        }
        StringBuilder sql = new StringBuilder("SELECT 1 FROM usuario WHERE UPPER(username) = ? ");
        if (excluirIdUsuario != null) {
            sql.append("AND id_usuario <> ? ");
        }
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, username.trim().toUpperCase(Locale.ROOT));
            if (excluirIdUsuario != null) {
                ps.setLong(2, excluirIdUsuario);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public UsuarioDTO guardarUsuarioCompleto(
            UsuarioDTO usuario,
            List<Long> idsRoles,
            Long idEquipo,
            Long idUsuarioActual) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idUsuario = usuario.getIdUsuario();
                if (idUsuario == null) {
                    idUsuario = insertarUsuario(conn, usuario, idUsuarioActual);
                } else {
                    actualizarUsuario(conn, usuario, idUsuarioActual);
                }
                usuarioRolDAO.sincronizarRoles(conn, idUsuario, idsRoles, idUsuarioActual);
                usuarioEquipoDAO.sincronizarEquipo(conn, idUsuario, idEquipo, idUsuarioActual);
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return obtenerPorId(idUsuario);
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            }
        }
    }

    public UsuarioDTO cambiarActivo(Long idUsuario, boolean activo, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE usuario SET activo = ?, estado = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_usuario = ?";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, activo ? 1 : 0);
            ps.setString(2, activo ? "ACTIVO" : "INACTIVO");
            setNullableLong(ps, 3, idUsuarioActual);
            ps.setLong(4, idUsuario);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo cambiar el estado del usuario seleccionado.");
            }
        }
        return obtenerPorId(idUsuario);
    }

    public boolean usuarioTieneRolActivo(Long idUsuario, String codigoRol) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return usuarioRolDAO.usuarioTieneRolActivo(conn, idUsuario, codigoRol);
        }
    }

    public boolean seleccionIncluyeRolCodigo(List<Long> idsRoles, String codigoRol) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return usuarioRolDAO.seleccionIncluyeRolCodigo(conn, idsRoles, codigoRol);
        }
    }

    public int contarUsuariosActivosConRol(String codigoRol) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return usuarioRolDAO.contarUsuariosActivosConRol(conn, codigoRol);
        }
    }

    private Long insertarUsuario(Connection conn, UsuarioDTO usuario, Long idUsuarioActual) throws SQLException {
        String sql = "INSERT INTO usuario ("
                + "username, password_hash, nombre_completo, tipo_documento, numero_documento, correo, "
                + "estado, activo, creado_por, creado_en"
                + ") VALUES (?, NULL, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_USUARIO"})) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getNombreCompleto());
            ps.setString(3, emptyToNull(usuario.getTipoDocumento()));
            ps.setString(4, emptyToNull(usuario.getNumeroDocumento()));
            ps.setString(5, emptyToNull(usuario.getCorreo()));
            ps.setString(6, usuario.isActivo() ? "ACTIVO" : "INACTIVO");
            ps.setInt(7, usuario.isActivo() ? 1 : 0);
            setNullableLong(ps, 8, idUsuarioActual);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "usuario");
        }
    }

    private void actualizarUsuario(Connection conn, UsuarioDTO usuario, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE usuario SET username = ?, nombre_completo = ?, tipo_documento = ?, "
                + "numero_documento = ?, correo = ?, estado = ?, activo = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP WHERE id_usuario = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getNombreCompleto());
            ps.setString(3, emptyToNull(usuario.getTipoDocumento()));
            ps.setString(4, emptyToNull(usuario.getNumeroDocumento()));
            ps.setString(5, emptyToNull(usuario.getCorreo()));
            ps.setString(6, usuario.isActivo() ? "ACTIVO" : "INACTIVO");
            ps.setInt(7, usuario.isActivo() ? 1 : 0);
            setNullableLong(ps, 8, idUsuarioActual);
            ps.setLong(9, usuario.getIdUsuario());
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el usuario seleccionado.");
            }
        }
    }

    private String selectUsuariosBase() {
        return "SELECT u.id_usuario, u.username, u.nombre_completo, u.tipo_documento, u.numero_documento, "
                + "u.correo, u.estado, u.activo, u.creado_en, u.modificado_en, "
                + "(SELECT LISTAGG(r.nombre, ', ') WITHIN GROUP (ORDER BY r.nombre) "
                + " FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol "
                + " WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 AND r.activo = 1) AS roles_resumen, "
                + "(SELECT LISTAGG(TO_CHAR(r.id_rol), ',') WITHIN GROUP (ORDER BY r.nombre) "
                + " FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol "
                + " WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 AND r.activo = 1) AS roles_ids, "
                + "(SELECT MAX(eu.id_equipo) FROM equipo_usuario eu JOIN equipo eq ON eq.id_equipo = eu.id_equipo "
                + " WHERE eu.id_usuario = u.id_usuario AND eu.activo = 1 AND eq.activo = 1) AS id_equipo, "
                + "(SELECT MAX(eq.nombre) FROM equipo_usuario eu JOIN equipo eq ON eq.id_equipo = eu.id_equipo "
                + " WHERE eu.id_usuario = u.id_usuario AND eu.activo = 1 AND eq.activo = 1) AS equipo_nombre, "
                + "(SELECT MAX(a.nombre) FROM equipo_usuario eu JOIN equipo eq ON eq.id_equipo = eu.id_equipo "
                + " LEFT JOIN area a ON a.id_area = eq.id_area "
                + " WHERE eu.id_usuario = u.id_usuario AND eu.activo = 1 AND eq.activo = 1) AS area_nombre "
                + "FROM usuario u ";
    }

    private UsuarioDTO mapUsuario(ResultSet rs) throws SQLException {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(getLongOrNull(rs, "id_usuario"));
        dto.setUsername(rs.getString("username"));
        dto.setNombreCompleto(rs.getString("nombre_completo"));
        dto.setNombres(rs.getString("nombre_completo"));
        dto.setApellidos("");
        dto.setTipoDocumento(rs.getString("tipo_documento"));
        dto.setNumeroDocumento(rs.getString("numero_documento"));
        dto.setCorreo(rs.getString("correo"));
        dto.setEstado(rs.getString("estado"));
        dto.setActivo(rs.getInt("activo") == 1);
        dto.setIdEquipo(getLongOrNull(rs, "id_equipo"));
        dto.setEquipoNombre(rs.getString("equipo_nombre"));
        dto.setAreaNombre(rs.getString("area_nombre"));
        dto.setRolesResumen(rs.getString("roles_resumen"));
        dto.setIdsRoles(parseIds(rs.getString("roles_ids")));
        dto.setCreadoEn(toLocalDateTime(rs.getTimestamp("creado_en")));
        dto.setModificadoEn(toLocalDateTime(rs.getTimestamp("modificado_en")));
        return dto;
    }

    private static List<Long> parseIds(String value) {
        List<Long> ids = new ArrayList<>();
        if (!hasText(value)) {
            return ids;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            try {
                ids.add(Long.valueOf(part.trim()));
            } catch (NumberFormatException ignored) {
                // Ignorar datos no esperados sin romper la consulta.
            }
        }
        return ids;
    }

    private static int normalizarLimite(int limite) {
        if (limite <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limite, MAX_LIMIT);
    }

    private static void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private static void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private static Long obtenerGeneratedKey(PreparedStatement ps, String entidad) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
        throw new SQLException("No se obtuvo el identificador generado para " + entidad + ".");
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

    private static void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // No ocultar el error original.
        }
    }
}
