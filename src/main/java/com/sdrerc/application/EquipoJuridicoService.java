package com.sdrerc.application;

import com.sdrerc.domain.model.EquipoJuridicoRegistro;
import com.sdrerc.domain.model.User;
import com.sdrerc.infrastructure.database.OracleConnection;
import com.sdrerc.infrastructure.security.PasswordEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EquipoJuridicoService {

    private static final String ESTADO_ACTIVO = "ACTIVE";

    public void registrar(EquipoJuridicoRegistro registro) throws SQLException {
        validar(registro);

        try (Connection conn = OracleConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (existeUsername(conn, registro.getUsername())) {
                    throw new IllegalArgumentException("El username ya existe.");
                }

                Integer idTecnico = buscarTecnicoExistente(conn, registro);
                if (idTecnico == null) {
                    idTecnico = crearTecnico(conn, registro);
                } else if (tecnicoVinculadoAOtroUsuario(conn, idTecnico, null)) {
                    throw new IllegalStateException("El técnico ya está vinculado a otro usuario.");
                }

                Long userId = crearUsuario(conn, registro, idTecnico);

                if (registro.isAbogado()) {
                    asignarRol(conn, userId, "ABOGADO");
                }
                if (registro.isSupervision()) {
                    asignarRol(conn, userId, "SUPERVISION");
                }
                if (registro.isAbogado() && registro.getSupervisorId() != null) {
                    if (userId.equals(registro.getSupervisorId())) {
                        throw new IllegalArgumentException("No se puede asignar la persona como su propio supervisor.");
                    }
                    if (!tieneRol(conn, registro.getSupervisorId(), "SUPERVISION")) {
                        throw new IllegalArgumentException("El supervisor seleccionado no tiene rol SUPERVISION.");
                    }
                    if (abogadoTieneSupervisor(conn, userId)) {
                        throw new IllegalStateException("El abogado ya tiene un supervisor principal.");
                    }
                    asignarSupervisor(conn, registro.getSupervisorId(), userId);
                }

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                throw new IllegalStateException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<User> listarSupervisoresActivos() throws SQLException {
        List<User> supervisores = new ArrayList<>();
        String sql =
            "SELECT DISTINCT u.USER_ID, u.USERNAME, u.FULL_NAME, u.STATUS, u.ID_TECNICO " +
            "FROM APP_USERS u " +
            "JOIN APP_USER_ROLES ur ON ur.USER_ID = u.USER_ID " +
            "JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
            "WHERE r.ROLE_NAME = 'SUPERVISION' " +
            "AND UPPER(u.STATUS) IN ('ACTIVE', 'ACTIVO') " +
            "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO') " +
            "ORDER BY u.FULL_NAME";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getLong("USER_ID"));
                u.setUsername(rs.getString("USERNAME"));
                u.setFullName(rs.getString("FULL_NAME"));
                u.setStatus(rs.getString("STATUS"));
                long idTecnico = rs.getLong("ID_TECNICO");
                u.setIdTecnico(rs.wasNull() ? null : idTecnico);
                supervisores.add(u);
            }
        }
        return supervisores;
    }

    private void validar(EquipoJuridicoRegistro registro) {
        if (registro == null) {
            throw new IllegalArgumentException("Ingrese los datos del equipo jurídico.");
        }
        if (isBlank(registro.getApellidoPaterno())) {
            throw new IllegalArgumentException("Ingrese apellido paterno.");
        }
        if (isBlank(registro.getNombres())) {
            throw new IllegalArgumentException("Ingrese nombres.");
        }
        if (isBlank(registro.getUsername())) {
            throw new IllegalArgumentException("Ingrese username.");
        }
        if (isBlank(registro.getPasswordTemporal())) {
            throw new IllegalArgumentException("Ingrese contraseña temporal.");
        }
        if (!isBlank(registro.getNumeroDocumento()) && !registro.getNumeroDocumento().trim().matches("\\d+")) {
            throw new IllegalArgumentException("El número de documento debe ser numérico.");
        }
        if (!registro.isAbogado() && !registro.isSupervision()) {
            throw new IllegalArgumentException("Seleccione al menos un rol operativo.");
        }
    }

    private Integer buscarTecnicoExistente(Connection conn, EquipoJuridicoRegistro registro) throws SQLException {
        if (!isBlank(registro.getNumeroDocumento())) {
            Integer porDocumento = buscarTecnicoPorDocumento(conn, registro.getNumeroDocumento());
            if (porDocumento != null) {
                return porDocumento;
            }
        }
        return buscarTecnicoPorNombre(conn, registro);
    }

    private Integer buscarTecnicoPorDocumento(Connection conn, String numeroDocumento) throws SQLException {
        String sql = "SELECT ID_TECNICO FROM TECNICO WHERE NUMERO_DOCUMENTO = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroDocumento.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("ID_TECNICO") : null;
            }
        }
    }

    private Integer buscarTecnicoPorNombre(Connection conn, EquipoJuridicoRegistro registro) throws SQLException {
        String sql =
            "SELECT ID_TECNICO FROM TECNICO " +
            "WHERE UPPER(TRIM(APELLIDO_PATERNO)) = ? " +
            "AND UPPER(TRIM(NVL(APELLIDO_MATERNO, ''))) = ? " +
            "AND UPPER(TRIM(NOMBRES)) = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizar(registro.getApellidoPaterno()));
            ps.setString(2, normalizar(registro.getApellidoMaterno()));
            ps.setString(3, normalizar(registro.getNombres()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("ID_TECNICO") : null;
            }
        }
    }

    private Integer crearTecnico(Connection conn, EquipoJuridicoRegistro registro) throws SQLException {
        Integer idTecnico = obtenerSiguienteIdTecnico(conn);
        String sql =
            "INSERT INTO TECNICO " +
            "(ID_TECNICO, ID_TIPO_DOCUMENTO, NUMERO_DOCUMENTO, APELLIDO_PATERNO, APELLIDO_MATERNO, NOMBRES, ACTIVE, FECHA_REGISTRO) " +
            "VALUES (?, ?, ?, ?, ?, ?, 1, SYSDATE)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTecnico);
            if (registro.getIdTipoDocumento() == null) {
                ps.setNull(2, Types.NUMERIC);
            } else {
                ps.setInt(2, registro.getIdTipoDocumento());
            }
            if (isBlank(registro.getNumeroDocumento())) {
                ps.setNull(3, Types.NUMERIC);
            } else {
                ps.setString(3, registro.getNumeroDocumento().trim());
            }
            ps.setString(4, registro.getApellidoPaterno().trim());
            ps.setString(5, trimToNull(registro.getApellidoMaterno()));
            ps.setString(6, registro.getNombres().trim());
            ps.executeUpdate();
        }
        return idTecnico;
    }

    private Integer obtenerSiguienteIdTecnico(Connection conn) throws SQLException {
        String sql = "SELECT NVL(MAX(ID_TECNICO), 0) + 1 FROM TECNICO";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Long crearUsuario(Connection conn, EquipoJuridicoRegistro registro, Integer idTecnico) throws SQLException {
        String sql =
            "INSERT INTO APP_USERS (USERNAME, PASSWORD_HASH, FULL_NAME, STATUS, ID_TECNICO) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"USER_ID"})) {
            ps.setString(1, registro.getUsername().trim());
            ps.setString(2, PasswordEncoder.hash(registro.getPasswordTemporal()));
            ps.setString(3, construirFullName(registro));
            ps.setString(4, ESTADO_ACTIVO);
            ps.setInt(5, idTecnico);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        Long userId = buscarUserIdPorUsername(conn, registro.getUsername());
        if (userId != null) {
            return userId;
        }
        throw new SQLException("No se pudo obtener USER_ID del usuario creado.");
    }

    private void asignarRol(Connection conn, Long userId, String roleName) throws SQLException {
        String sql =
            "INSERT INTO APP_USER_ROLES (USER_ID, ROLE_ID) " +
            "SELECT ?, r.ROLE_ID " +
            "FROM APP_ROLES r " +
            "WHERE r.ROLE_NAME = ? " +
            "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO') " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM APP_USER_ROLES ur " +
            "    WHERE ur.USER_ID = ? AND ur.ROLE_ID = r.ROLE_ID " +
            ")";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, roleName);
            ps.setLong(3, userId);
            int rows = ps.executeUpdate();
            if (rows == 0 && !tieneRol(conn, userId, roleName)) {
                throw new SQLException("No existe el rol activo " + roleName + ".");
            }
        }
    }

    private void asignarSupervisor(Connection conn, Long supervisorId, Long abogadoId) throws SQLException {
        String sql =
            "INSERT INTO APP_USER_SUPERVISION (SUPERVISOR_ID, ABOGADO_ID) " +
            "VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, supervisorId);
            ps.setLong(2, abogadoId);
            ps.executeUpdate();
        }
    }

    private boolean existeUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT COUNT(1) FROM APP_USERS WHERE UPPER(USERNAME) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Long buscarUserIdPorUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT USER_ID FROM APP_USERS WHERE UPPER(USERNAME) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("USER_ID") : null;
            }
        }
    }

    private boolean tecnicoVinculadoAOtroUsuario(Connection conn, Integer idTecnico, Long userId) throws SQLException {
        String sql =
            "SELECT COUNT(1) FROM APP_USERS " +
            "WHERE ID_TECNICO = ? " +
            (userId == null ? "" : "AND USER_ID <> ?");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTecnico);
            if (userId != null) {
                ps.setLong(2, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean tieneRol(Connection conn, Long userId, String roleName) throws SQLException {
        String sql =
            "SELECT COUNT(1) " +
            "FROM APP_USER_ROLES ur " +
            "JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
            "JOIN APP_USERS u ON u.USER_ID = ur.USER_ID " +
            "WHERE ur.USER_ID = ? " +
            "AND r.ROLE_NAME = ? " +
            "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO') " +
            "AND UPPER(u.STATUS) IN ('ACTIVE', 'ACTIVO')";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean abogadoTieneSupervisor(Connection conn, Long abogadoId) throws SQLException {
        String sql = "SELECT COUNT(1) FROM APP_USER_SUPERVISION WHERE ABOGADO_ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, abogadoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private String construirFullName(EquipoJuridicoRegistro registro) {
        StringBuilder sb = new StringBuilder();
        sb.append(registro.getApellidoPaterno().trim());
        if (!isBlank(registro.getApellidoMaterno())) {
            sb.append(' ').append(registro.getApellidoMaterno().trim());
        }
        sb.append(' ').append(registro.getNombres().trim());
        return sb.toString();
    }

    private String normalizar(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
