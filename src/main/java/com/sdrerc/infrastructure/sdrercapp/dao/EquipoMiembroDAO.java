package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.EquipoMiembroDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableEquipoDTO;
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

public class EquipoMiembroDAO {

    private static final String ROL_ABOGADO = "ABOGADO";
    private static final String ROL_SUPERVISION = "SUPERVISION";
    private static final String ROL_ADMIN = "ADMIN_SISTEMA";

    public List<EquipoMiembroDTO> listarMiembros(Long idEquipo) throws SQLException {
        if (idEquipo == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT eu.id_equipo_usuario, eu.id_equipo, eu.id_usuario, eu.es_responsable, "
                + "eu.activo AS relacion_activa, eu.creado_en, eu.modificado_en, "
                + "u.username, u.nombre_completo, u.activo AS usuario_activo, a.nombre AS area_nombre, "
                + "(SELECT LISTAGG(r.nombre, ', ') WITHIN GROUP (ORDER BY r.nombre) "
                + " FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol "
                + " WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 AND r.activo = 1) AS roles_resumen, "
                + "CASE WHEN EXISTS (SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol "
                + " WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 AND r.activo = 1 AND r.codigo = ?) THEN 1 ELSE 0 END AS es_abogado, "
                + "CASE WHEN EXISTS (SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol "
                + " WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 AND r.activo = 1 AND r.codigo IN (?, ?)) THEN 1 ELSE 0 END AS es_supervisor "
                + "FROM equipo_usuario eu "
                + "JOIN usuario u ON u.id_usuario = eu.id_usuario "
                + "JOIN equipo e ON e.id_equipo = eu.id_equipo "
                + "LEFT JOIN area a ON a.id_area = e.id_area "
                + "WHERE eu.id_equipo = ? AND eu.activo = 1 "
                + "ORDER BY eu.es_responsable DESC, u.nombre_completo ASC, u.username ASC";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ROL_ABOGADO);
            ps.setString(2, ROL_SUPERVISION);
            ps.setString(3, ROL_ADMIN);
            ps.setLong(4, idEquipo);
            try (ResultSet rs = ps.executeQuery()) {
                List<EquipoMiembroDTO> miembros = new ArrayList<>();
                while (rs.next()) {
                    miembros.add(mapMiembro(rs));
                }
                return miembros;
            }
        }
    }

    public List<UsuarioAsignableEquipoDTO> listarUsuariosAsignables(String texto, boolean soloSupervisores) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("SELECT u.id_usuario, u.username, u.nombre_completo, u.activo, ");
        sql.append("(SELECT LISTAGG(r.nombre, ', ') WITHIN GROUP (ORDER BY r.nombre) ");
        sql.append(" FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol ");
        sql.append(" WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 AND r.activo = 1) AS roles_resumen, ");
        sql.append("CASE WHEN EXISTS (SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol ");
        sql.append(" WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 AND r.activo = 1 AND r.codigo = ?) THEN 1 ELSE 0 END AS es_abogado, ");
        sql.append("CASE WHEN EXISTS (SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol ");
        sql.append(" WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 AND r.activo = 1 AND r.codigo IN (?, ?)) THEN 1 ELSE 0 END AS es_supervisor ");
        sql.append("FROM usuario u WHERE u.activo = 1 AND UPPER(u.estado) = 'ACTIVO' ");
        params.add(ROL_ABOGADO);
        params.add(ROL_SUPERVISION);
        params.add(ROL_ADMIN);

        if (hasText(texto)) {
            sql.append("AND (UPPER(NVL(u.username, '')) LIKE ? OR UPPER(NVL(u.nombre_completo, '')) LIKE ?) ");
            String pattern = "%" + texto.trim().toUpperCase(Locale.ROOT) + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (soloSupervisores) {
            sql.append("AND EXISTS (SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol ");
            sql.append("WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 AND r.activo = 1 AND r.codigo IN (?, ?)) ");
            params.add(ROL_SUPERVISION);
            params.add(ROL_ADMIN);
        }

        sql.append("ORDER BY u.nombre_completo ASC, u.username ASC");
        sql.append(") WHERE ROWNUM <= 1000");

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<UsuarioAsignableEquipoDTO> usuarios = new ArrayList<>();
                while (rs.next()) {
                    usuarios.add(mapUsuarioAsignable(rs));
                }
                return usuarios;
            }
        }
    }

    public void agregarMiembro(Long idEquipo, Long idUsuario, Long idUsuarioActual) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                asegurarMiembroActivo(conn, idEquipo, idUsuario, false, idUsuarioActual);
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
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

    public void quitarMiembro(Long idEquipo, Long idUsuario, Long idUsuarioActual) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                quitarMiembro(conn, idEquipo, idUsuario, idUsuarioActual);
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
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

    public void marcarResponsable(Long idEquipo, Long idUsuario, Long idUsuarioActual) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                marcarResponsable(conn, idEquipo, idUsuario, idUsuarioActual);
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
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

    public void sincronizarResponsable(Connection conn, Long idEquipo, Long idUsuarioResponsable, Long idUsuarioActual)
            throws SQLException {
        if (idUsuarioResponsable == null) {
            limpiarResponsables(conn, idEquipo, idUsuarioActual);
            return;
        }
        marcarResponsable(conn, idEquipo, idUsuarioResponsable, idUsuarioActual);
    }

    public void marcarResponsable(Connection conn, Long idEquipo, Long idUsuario, Long idUsuarioActual) throws SQLException {
        if (!usuarioActivo(conn, idUsuario)) {
            throw new SQLException("El usuario seleccionado no está activo.");
        }
        if (!usuarioSupervisorCompatible(conn, idUsuario)) {
            throw new SQLException("Seleccione un usuario con rol de supervisión para responsable del equipo.");
        }
        asegurarMiembroActivo(conn, idEquipo, idUsuario, false, idUsuarioActual);
        limpiarResponsables(conn, idEquipo, idUsuarioActual);
        String sql = "UPDATE equipo_usuario SET es_responsable = 1, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_equipo = ? AND id_usuario = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idEquipo);
            ps.setLong(3, idUsuario);
            int updated = ps.executeUpdate();
            if (updated < 1) {
                throw new SQLException("No se pudo marcar el responsable del equipo.");
            }
        }
    }

    public void quitarMiembro(Connection conn, Long idEquipo, Long idUsuario, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE equipo_usuario SET activo = 0, es_responsable = 0, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_equipo = ? AND id_usuario = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idEquipo);
            ps.setLong(3, idUsuario);
            int updated = ps.executeUpdate();
            if (updated < 1) {
                throw new SQLException("El usuario no es miembro activo del equipo.");
            }
        }
    }

    public int contarExpedientesActivosMiembro(Long idEquipo, Long idUsuario) throws SQLException {
        String sql = "SELECT COUNT(1) AS total "
                + "FROM expediente "
                + "WHERE activo = 1 "
                + "AND NVL(cerrado, 0) = 0 "
                + "AND NVL(archivado, 0) = 0 "
                + "AND id_equipo_responsable_actual = ? "
                + "AND (id_usuario_responsable_actual = ? OR id_usuario_abogado_inicial = ?)";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEquipo);
            ps.setLong(2, idUsuario);
            ps.setLong(3, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    public boolean usuarioSupervisorCompatible(Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return usuarioSupervisorCompatible(conn, idUsuario);
        }
    }

    private void asegurarMiembroActivo(
            Connection conn,
            Long idEquipo,
            Long idUsuario,
            boolean responsable,
            Long idUsuarioActual) throws SQLException {
        if (!usuarioActivo(conn, idUsuario)) {
            throw new SQLException("El usuario seleccionado no está activo.");
        }
        if (existeMiembroActivo(conn, idEquipo, idUsuario)) {
            return;
        }
        if (reactivarMiembro(conn, idEquipo, idUsuario, responsable, idUsuarioActual)) {
            return;
        }
        insertarMiembro(conn, idEquipo, idUsuario, responsable, idUsuarioActual);
    }

    private boolean existeMiembroActivo(Connection conn, Long idEquipo, Long idUsuario) throws SQLException {
        String sql = "SELECT 1 FROM equipo_usuario WHERE id_equipo = ? AND id_usuario = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEquipo);
            ps.setLong(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean reactivarMiembro(
            Connection conn,
            Long idEquipo,
            Long idUsuario,
            boolean responsable,
            Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE equipo_usuario SET activo = 1, es_responsable = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_equipo_usuario = (SELECT MIN(id_equipo_usuario) FROM equipo_usuario "
                + "WHERE id_equipo = ? AND id_usuario = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, responsable ? 1 : 0);
            setNullableLong(ps, 2, idUsuarioActual);
            ps.setLong(3, idEquipo);
            ps.setLong(4, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    private void insertarMiembro(
            Connection conn,
            Long idEquipo,
            Long idUsuario,
            boolean responsable,
            Long idUsuarioActual) throws SQLException {
        String sql = "INSERT INTO equipo_usuario (id_equipo, id_usuario, es_responsable, activo, creado_por, creado_en) "
                + "VALUES (?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEquipo);
            ps.setLong(2, idUsuario);
            ps.setInt(3, responsable ? 1 : 0);
            setNullableLong(ps, 4, idUsuarioActual);
            ps.executeUpdate();
        }
    }

    private void limpiarResponsables(Connection conn, Long idEquipo, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE equipo_usuario SET es_responsable = 0, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_equipo = ? AND activo = 1 AND es_responsable = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idEquipo);
            ps.executeUpdate();
        }
    }

    private boolean usuarioActivo(Connection conn, Long idUsuario) throws SQLException {
        if (idUsuario == null) {
            return false;
        }
        String sql = "SELECT 1 FROM usuario WHERE id_usuario = ? AND activo = 1 AND UPPER(estado) = 'ACTIVO'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean usuarioSupervisorCompatible(Connection conn, Long idUsuario) throws SQLException {
        String sql = "SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol "
                + "WHERE ur.id_usuario = ? AND ur.activo = 1 AND r.activo = 1 AND r.codigo IN (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ps.setString(2, ROL_SUPERVISION);
            ps.setString(3, ROL_ADMIN);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private EquipoMiembroDTO mapMiembro(ResultSet rs) throws SQLException {
        EquipoMiembroDTO dto = new EquipoMiembroDTO();
        dto.setIdEquipoUsuario(getLongOrNull(rs, "id_equipo_usuario"));
        dto.setIdEquipo(getLongOrNull(rs, "id_equipo"));
        dto.setIdUsuario(getLongOrNull(rs, "id_usuario"));
        dto.setUsername(rs.getString("username"));
        dto.setNombreCompleto(rs.getString("nombre_completo"));
        dto.setRolesResumen(rs.getString("roles_resumen"));
        dto.setAreaNombre(rs.getString("area_nombre"));
        dto.setUsuarioActivo(rs.getInt("usuario_activo") == 1);
        dto.setRelacionActiva(rs.getInt("relacion_activa") == 1);
        dto.setResponsable(rs.getInt("es_responsable") == 1);
        dto.setAbogado(rs.getInt("es_abogado") == 1);
        dto.setSupervisor(rs.getInt("es_supervisor") == 1);
        dto.setCreadoEn(toLocalDateTime(rs.getTimestamp("creado_en")));
        dto.setModificadoEn(toLocalDateTime(rs.getTimestamp("modificado_en")));
        return dto;
    }

    private UsuarioAsignableEquipoDTO mapUsuarioAsignable(ResultSet rs) throws SQLException {
        UsuarioAsignableEquipoDTO dto = new UsuarioAsignableEquipoDTO();
        dto.setIdUsuario(getLongOrNull(rs, "id_usuario"));
        dto.setUsername(rs.getString("username"));
        dto.setNombreCompleto(rs.getString("nombre_completo"));
        dto.setRolesResumen(rs.getString("roles_resumen"));
        dto.setActivo(rs.getInt("activo") == 1);
        dto.setAbogado(rs.getInt("es_abogado") == 1);
        dto.setSupervisor(rs.getInt("es_supervisor") == 1);
        return dto;
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

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // Mantener el error original.
        }
    }
}
