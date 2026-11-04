package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.domain.UsuarioAutenticacionDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Subconjunto de metodos de autenticacion de com.sdrerc.infrastructure.sdrercapp.dao.UsuarioDAO
 * (V2), portados con el mismo SQL/logica. No incluye el resto de UsuarioDAO (CRUD administrativo
 * de usuarios), fuera de alcance de la Fase 0 (auth-only); se completa en la fase de
 * Administracion.
 *
 * <p>Unico cambio real respecto a V2: la conexion se obtiene de un {@link DataSource} inyectado
 * por Spring en vez de {@code SdrercAppConnection.getConnection()} estatico (mismo HikariCP por
 * debajo, ahora dimensionado para concurrencia web - ver plan de migracion, riesgo 2).</p>
 */
@Repository
public class UsuarioAuthDAO {

    private final DataSource dataSource;

    public UsuarioAuthDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Mismo criterio de coincidencia que V2: UPPER(username), activo=1, estado='ACTIVO'.
     */
    public UsuarioAutenticacionDTO buscarPorUsername(String username) throws SQLException {
        if (!hasText(username)) {
            return null;
        }
        String sql = "SELECT id_usuario, username, nombre_completo, password_hash, activo, estado, "
                + "debe_cambiar_password, totp_secret, totp_habilitado, intentos_fallidos, bloqueado_hasta, correo "
                + "FROM usuario "
                + "WHERE UPPER(username) = ? AND activo = 1 AND UPPER(estado) = 'ACTIVO'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUsuarioAutenticacion(rs) : null;
            }
        }
    }

    public UsuarioAutenticacionDTO obtenerAutenticacionPorId(Long idUsuario) throws SQLException {
        if (idUsuario == null) {
            return null;
        }
        String sql = "SELECT id_usuario, username, nombre_completo, password_hash, activo, estado, "
                + "debe_cambiar_password, totp_secret, totp_habilitado, intentos_fallidos, bloqueado_hasta, correo "
                + "FROM usuario WHERE id_usuario = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUsuarioAutenticacion(rs) : null;
            }
        }
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
                ps.setNull(1, java.sql.Types.VARCHAR);
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

    public void registrarIntentoFallido(Long idUsuario, int maxIntentos, int minutosBloqueo) throws SQLException {
        String sql = "UPDATE usuario SET intentos_fallidos = intentos_fallidos + 1, "
                + "bloqueado_hasta = CASE WHEN intentos_fallidos + 1 >= ? "
                + "THEN SYSTIMESTAMP + NUMTODSINTERVAL(?, 'MINUTE') ELSE bloqueado_hasta END "
                + "WHERE id_usuario = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maxIntentos);
            ps.setInt(2, minutosBloqueo);
            ps.setLong(3, idUsuario);
            ps.executeUpdate();
        }
    }

    public void resetearIntentosFallidos(Long idUsuario) throws SQLException {
        String sql = "UPDATE usuario SET intentos_fallidos = 0, bloqueado_hasta = NULL WHERE id_usuario = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ps.executeUpdate();
        }
    }

    public void registrarUltimoLogin(Long idUsuario) throws SQLException {
        String sql = "UPDATE usuario SET ultimo_login_en = SYSTIMESTAMP WHERE id_usuario = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ps.executeUpdate();
        }
    }

    public Set<String> listarCodigosRolPorUsuario(Long idUsuario) throws SQLException {
        Set<String> codigos = new HashSet<>();
        if (idUsuario == null) {
            return codigos;
        }
        String sql = "SELECT DISTINCT r.codigo "
                + "FROM usuario_rol ur "
                + "JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1 "
                + "WHERE ur.id_usuario = ? AND ur.activo = 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String codigo = rs.getString("codigo");
                    if (codigo != null) {
                        codigos.add(codigo.trim().toUpperCase(Locale.ROOT));
                    }
                }
            }
        }
        return codigos;
    }

    private UsuarioAutenticacionDTO mapUsuarioAutenticacion(ResultSet rs) throws SQLException {
        UsuarioAutenticacionDTO dto = new UsuarioAutenticacionDTO();
        dto.setIdUsuario(getLongOrNull(rs, "id_usuario"));
        dto.setUsername(rs.getString("username"));
        dto.setNombreCompleto(rs.getString("nombre_completo"));
        dto.setPasswordHash(rs.getString("password_hash"));
        dto.setActivo(rs.getInt("activo") == 1);
        dto.setEstado(rs.getString("estado"));
        dto.setDebeCambiarPassword(rs.getInt("debe_cambiar_password") == 1);
        dto.setTotpSecretCifrado(rs.getString("totp_secret"));
        dto.setTotpHabilitado(rs.getInt("totp_habilitado") == 1);
        dto.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        dto.setBloqueadoHasta(toLocalDateTime(rs.getTimestamp("bloqueado_hasta")));
        dto.setCorreo(rs.getString("correo"));
        return dto;
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
