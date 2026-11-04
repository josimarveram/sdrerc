package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.domain.UsuarioAdminDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Port de {@code com.sdrerc.infrastructure.sdrercapp.dao.UsuarioDAO} +
 * {@code UsuarioRolDAO}/{@code UsuarioEquipoDAO} (V2), combinados en un único DAO (Administración
 * &gt; Usuarios V3). Único cambio real de fondo respecto a V2: {@link #sincronizarEquipos} permite
 * multi-equipo (diff activar/desactivar, mismo patrón ya usado para roles) en vez del
 * "desactivar todos los demás" de {@code UsuarioEquipoDAO.sincronizarEquipo} — ver Javadoc de
 * {@link UsuarioAdminDTO}.
 */
@Repository
public class UsuarioAdminDAO {

    private static final int DEFAULT_LIMIT = 300;
    private static final int MAX_LIMIT = 1000;

    private final DataSource dataSource;

    public UsuarioAdminDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<UsuarioAdminDTO> buscar(String texto, Boolean activo, Long idRol, Long idEquipo, Integer limite)
            throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM (");
        sql.append(selectUsuariosBase());
        sql.append("WHERE 1 = 1 ");

        if (hasText(texto)) {
            sql.append("AND (UPPER(NVL(u.username, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(u.nombre_completo, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(u.correo, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(u.numero_documento, '')) LIKE ?) ");
            String pattern = "%" + texto.trim().toUpperCase(Locale.ROOT) + "%";
            for (int i = 0; i < 4; i++) {
                params.add(pattern);
            }
        }
        if (activo != null) {
            sql.append("AND u.activo = ? ");
            params.add(activo ? 1 : 0);
        }
        if (idRol != null) {
            sql.append("AND EXISTS (SELECT 1 FROM usuario_rol urf WHERE urf.id_usuario = u.id_usuario ");
            sql.append("AND urf.id_rol = ? AND urf.activo = 1) ");
            params.add(idRol);
        }
        if (idEquipo != null) {
            sql.append("AND EXISTS (SELECT 1 FROM equipo_usuario euf WHERE euf.id_usuario = u.id_usuario ");
            sql.append("AND euf.id_equipo = ? AND euf.activo = 1) ");
            params.add(idEquipo);
        }

        sql.append("ORDER BY u.nombre_completo ASC, u.username ASC");
        sql.append(") WHERE ROWNUM <= ?");
        params.add(normalizarLimite(limite));

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<UsuarioAdminDTO> usuarios = new ArrayList<>();
                while (rs.next()) {
                    usuarios.add(mapUsuario(rs));
                }
                return usuarios;
            }
        }
    }

    public UsuarioAdminDTO obtenerPorId(Long idUsuario) throws SQLException {
        if (idUsuario == null) {
            return null;
        }
        String sql = selectUsuariosBase() + "WHERE u.id_usuario = ?";
        try (Connection conn = dataSource.getConnection();
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
        try (Connection conn = dataSource.getConnection();
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

    public UsuarioAdminDTO guardarUsuarioCompleto(
            UsuarioAdminDTO usuario, List<Long> idsRoles, List<Long> idsEquipos, Long idUsuarioActual)
            throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idUsuario = usuario.getIdUsuario();
                if (idUsuario == null) {
                    idUsuario = insertarUsuario(conn, usuario, idUsuarioActual);
                } else {
                    actualizarUsuario(conn, usuario, idUsuarioActual);
                }
                sincronizarRoles(conn, idUsuario, idsRoles, idUsuarioActual);
                sincronizarEquipos(conn, idUsuario, idsEquipos, idUsuarioActual);
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return obtenerPorId(idUsuario);
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                if (ex instanceof SQLException sqlEx) {
                    throw sqlEx;
                }
                throw new SQLException(ex.getMessage(), ex);
            }
        }
    }

    public UsuarioAdminDTO cambiarActivo(Long idUsuario, boolean activo, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE usuario SET activo = ?, estado = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_usuario = ?";
        try (Connection conn = dataSource.getConnection();
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

    public void actualizarPasswordHash(Long idUsuario, String passwordHash, boolean debeCambiarPassword,
            Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE usuario SET password_hash = ?, debe_cambiar_password = ?, "
                + "password_actualizado_en = SYSTIMESTAMP, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_usuario = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, debeCambiarPassword ? 1 : 0);
            setNullableLong(ps, 3, idUsuarioActual);
            ps.setLong(4, idUsuario);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar la contraseña del usuario seleccionado.");
            }
        }
    }

    public void actualizarTotp(Long idUsuario, String totpSecretCifradoONull, boolean habilitado) throws SQLException {
        String sql = "UPDATE usuario SET totp_secret = ?, totp_habilitado = ?, "
                + "totp_confirmado_en = CASE WHEN ? = 1 THEN SYSTIMESTAMP ELSE totp_confirmado_en END "
                + "WHERE id_usuario = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (totpSecretCifradoONull == null) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, totpSecretCifradoONull);
            }
            ps.setInt(2, habilitado ? 1 : 0);
            ps.setInt(3, habilitado ? 1 : 0);
            ps.setLong(4, idUsuario);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar la verificación en dos pasos del usuario seleccionado.");
            }
        }
    }

    public boolean usuarioTieneRolActivo(Long idUsuario, String codigoRol) throws SQLException {
        String sql = "SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol "
                + "WHERE ur.id_usuario = ? AND ur.activo = 1 AND r.activo = 1 AND UPPER(r.codigo) = ? AND ROWNUM = 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ps.setString(2, codigoRol == null ? "" : codigoRol.trim().toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean seleccionIncluyeRolCodigo(List<Long> idsRoles, String codigoRol) throws SQLException {
        Set<Long> seleccionados = normalizarIds(idsRoles);
        if (seleccionados.isEmpty()) {
            return false;
        }
        StringBuilder sql = new StringBuilder("SELECT 1 FROM rol WHERE activo = 1 AND UPPER(codigo) = ? AND id_rol IN (");
        int count = 0;
        for (Long ignored : seleccionados) {
            sql.append(count++ > 0 ? ", ?" : "?");
        }
        sql.append(") AND ROWNUM = 1");
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, codigoRol == null ? "" : codigoRol.trim().toUpperCase(Locale.ROOT));
            int index = 2;
            for (Long idRol : seleccionados) {
                ps.setLong(index++, idRol);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int contarUsuariosActivosConRol(String codigoRol) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT u.id_usuario) AS total FROM usuario u "
                + "JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario AND ur.activo = 1 "
                + "JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1 "
                + "WHERE u.activo = 1 AND UPPER(u.estado) = 'ACTIVO' AND UPPER(r.codigo) = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoRol == null ? "" : codigoRol.trim().toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    private Long insertarUsuario(Connection conn, UsuarioAdminDTO usuario, Long idUsuarioActual) throws SQLException {
        String sql = "INSERT INTO usuario ("
                + "username, password_hash, nombre_completo, tipo_documento, numero_documento, correo, telefono, "
                + "estado, activo, creado_por, creado_en"
                + ") VALUES (?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_USUARIO"})) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getNombreCompleto());
            ps.setString(3, emptyToNull(usuario.getTipoDocumento()));
            ps.setString(4, emptyToNull(usuario.getNumeroDocumento()));
            ps.setString(5, emptyToNull(usuario.getCorreo()));
            ps.setString(6, emptyToNull(usuario.getTelefono()));
            ps.setString(7, usuario.isActivo() ? "ACTIVO" : "INACTIVO");
            ps.setInt(8, usuario.isActivo() ? 1 : 0);
            setNullableLong(ps, 9, idUsuarioActual);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps);
        }
    }

    private void actualizarUsuario(Connection conn, UsuarioAdminDTO usuario, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE usuario SET username = ?, nombre_completo = ?, tipo_documento = ?, "
                + "numero_documento = ?, correo = ?, telefono = ?, estado = ?, activo = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP WHERE id_usuario = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getNombreCompleto());
            ps.setString(3, emptyToNull(usuario.getTipoDocumento()));
            ps.setString(4, emptyToNull(usuario.getNumeroDocumento()));
            ps.setString(5, emptyToNull(usuario.getCorreo()));
            ps.setString(6, emptyToNull(usuario.getTelefono()));
            ps.setString(7, usuario.isActivo() ? "ACTIVO" : "INACTIVO");
            ps.setInt(8, usuario.isActivo() ? 1 : 0);
            setNullableLong(ps, 9, idUsuarioActual);
            ps.setLong(10, usuario.getIdUsuario());
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el usuario seleccionado.");
            }
        }
    }

    private void sincronizarRoles(Connection conn, Long idUsuario, List<Long> idsRoles, Long idUsuarioActual)
            throws SQLException {
        Set<Long> seleccionados = normalizarIds(idsRoles);
        for (Long idRol : seleccionados) {
            String sqlValidar = "SELECT 1 FROM rol WHERE id_rol = ? AND activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlValidar)) {
                ps.setLong(1, idRol);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Uno de los roles seleccionados no existe o no está activo.");
                    }
                }
            }
        }
        Set<Long> actuales = obtenerIdsActivos(conn, "usuario_rol", "id_rol", idUsuario);
        for (Long idActual : actuales) {
            if (!seleccionados.contains(idActual)) {
                desactivarRelacion(conn, "usuario_rol", "id_rol", idUsuario, idActual, idUsuarioActual);
            }
        }
        for (Long idNuevo : seleccionados) {
            if (!actuales.contains(idNuevo)) {
                activarOCrearRelacion(conn, "usuario_rol", "id_rol", idUsuario, idNuevo, idUsuarioActual, false);
            }
        }
    }

    private void sincronizarEquipos(Connection conn, Long idUsuario, List<Long> idsEquipos, Long idUsuarioActual)
            throws SQLException {
        Set<Long> seleccionados = normalizarIds(idsEquipos);
        for (Long idEquipo : seleccionados) {
            String sqlValidar = "SELECT 1 FROM equipo WHERE id_equipo = ? AND activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlValidar)) {
                ps.setLong(1, idEquipo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Uno de los equipos seleccionados no existe o no está activo.");
                    }
                }
            }
        }
        Set<Long> actuales = obtenerIdsActivos(conn, "equipo_usuario", "id_equipo", idUsuario);
        for (Long idActual : actuales) {
            if (!seleccionados.contains(idActual)) {
                desactivarRelacion(conn, "equipo_usuario", "id_equipo", idUsuario, idActual, idUsuarioActual);
            }
        }
        for (Long idNuevo : seleccionados) {
            if (!actuales.contains(idNuevo)) {
                activarOCrearRelacion(conn, "equipo_usuario", "id_equipo", idUsuario, idNuevo, idUsuarioActual, true);
            }
        }
    }

    private Set<Long> obtenerIdsActivos(Connection conn, String tabla, String columnaId, Long idUsuario)
            throws SQLException {
        Set<Long> ids = new LinkedHashSet<>();
        String sql = "SELECT DISTINCT " + columnaId + " FROM " + tabla + " WHERE id_usuario = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long value = rs.getLong(1);
                    if (!rs.wasNull()) {
                        ids.add(value);
                    }
                }
            }
        }
        return ids;
    }

    private void desactivarRelacion(Connection conn, String tabla, String columnaId, Long idUsuario, Long idValor,
            Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE " + tabla + " SET activo = 0, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_usuario = ? AND " + columnaId + " = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idUsuario);
            ps.setLong(3, idValor);
            ps.executeUpdate();
        }
    }

    private void activarOCrearRelacion(Connection conn, String tabla, String columnaId, Long idUsuario, Long idValor,
            Long idUsuarioActual, boolean conEsResponsable) throws SQLException {
        String sqlTiene = "SELECT 1 FROM " + tabla + " WHERE id_usuario = ? AND " + columnaId + " = ? AND activo = 1 "
                + "AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sqlTiene)) {
            ps.setLong(1, idUsuario);
            ps.setLong(2, idValor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }
        String sqlReactivar = "UPDATE " + tabla + " SET activo = 1, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_usuario = ? AND " + columnaId + " = ? AND activo = 0 AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sqlReactivar)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idUsuario);
            ps.setLong(3, idValor);
            if (ps.executeUpdate() > 0) {
                return;
            }
        }
        String sqlInsertar = conEsResponsable
                ? "INSERT INTO " + tabla + " (" + columnaId + ", id_usuario, es_responsable, activo, creado_por, creado_en) "
                        + "VALUES (?, ?, 0, 1, ?, SYSTIMESTAMP)"
                : "INSERT INTO " + tabla + " (id_usuario, " + columnaId + ", activo, creado_por, creado_en) "
                        + "VALUES (?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsertar)) {
            if (conEsResponsable) {
                ps.setLong(1, idValor);
                ps.setLong(2, idUsuario);
            } else {
                ps.setLong(1, idUsuario);
                ps.setLong(2, idValor);
            }
            setNullableLong(ps, 3, idUsuarioActual);
            ps.executeUpdate();
        }
    }

    private String selectUsuariosBase() {
        return "SELECT u.id_usuario, u.username, u.nombre_completo, u.tipo_documento, u.numero_documento, "
                + "u.correo, u.telefono, u.estado, u.activo, u.creado_en, u.modificado_en, "
                + "(SELECT LISTAGG(r.nombre, ', ') WITHIN GROUP (ORDER BY r.nombre) "
                + " FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol "
                + " WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 AND r.activo = 1) AS roles_resumen, "
                + "(SELECT LISTAGG(TO_CHAR(r.id_rol), ',') WITHIN GROUP (ORDER BY r.nombre) "
                + " FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol "
                + " WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 AND r.activo = 1) AS roles_ids, "
                + "(SELECT LISTAGG(eq.nombre, ', ') WITHIN GROUP (ORDER BY eq.nombre) "
                + " FROM equipo_usuario eu JOIN equipo eq ON eq.id_equipo = eu.id_equipo "
                + " WHERE eu.id_usuario = u.id_usuario AND eu.activo = 1 AND eq.activo = 1) AS equipos_resumen, "
                + "(SELECT LISTAGG(TO_CHAR(eq.id_equipo), ',') WITHIN GROUP (ORDER BY eq.nombre) "
                + " FROM equipo_usuario eu JOIN equipo eq ON eq.id_equipo = eu.id_equipo "
                + " WHERE eu.id_usuario = u.id_usuario AND eu.activo = 1 AND eq.activo = 1) AS equipos_ids "
                + "FROM usuario u ";
    }

    private UsuarioAdminDTO mapUsuario(ResultSet rs) throws SQLException {
        UsuarioAdminDTO dto = new UsuarioAdminDTO();
        dto.setIdUsuario(getLongOrNull(rs, "id_usuario"));
        dto.setUsername(rs.getString("username"));
        dto.setNombreCompleto(rs.getString("nombre_completo"));
        dto.setTipoDocumento(rs.getString("tipo_documento"));
        dto.setNumeroDocumento(rs.getString("numero_documento"));
        dto.setCorreo(rs.getString("correo"));
        dto.setTelefono(rs.getString("telefono"));
        dto.setEstado(rs.getString("estado"));
        dto.setActivo(rs.getInt("activo") == 1);
        dto.setRolesResumen(rs.getString("roles_resumen"));
        dto.setIdsRoles(parseIds(rs.getString("roles_ids")));
        dto.setEquiposResumen(rs.getString("equipos_resumen"));
        dto.setIdsEquipos(parseIds(rs.getString("equipos_ids")));
        dto.setCreadoEn(toLocalDateTime(rs.getTimestamp("creado_en")));
        dto.setModificadoEn(toLocalDateTime(rs.getTimestamp("modificado_en")));
        return dto;
    }

    private static List<Long> parseIds(String value) {
        List<Long> ids = new ArrayList<>();
        if (!hasText(value)) {
            return ids;
        }
        for (String part : value.split(",")) {
            try {
                ids.add(Long.valueOf(part.trim()));
            } catch (NumberFormatException ignored) {
                // Ignorar datos no esperados sin romper la consulta.
            }
        }
        return ids;
    }

    private static Set<Long> normalizarIds(List<Long> ids) {
        Set<Long> result = new LinkedHashSet<>();
        if (ids != null) {
            for (Long id : ids) {
                if (id != null) {
                    result.add(id);
                }
            }
        }
        return result;
    }

    private static int normalizarLimite(Integer limite) {
        if (limite == null || limite <= 0) {
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

    private static Long obtenerGeneratedKey(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
        throw new SQLException("No se obtuvo el identificador generado para el usuario.");
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
