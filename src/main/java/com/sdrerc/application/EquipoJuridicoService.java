package com.sdrerc.application;

import com.sdrerc.domain.model.EquipoJuridicoImportRowResult;
import com.sdrerc.domain.model.EquipoJuridicoRegistro;
import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.User;
import com.sdrerc.infrastructure.database.OracleConnection;
import com.sdrerc.infrastructure.security.PasswordEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EquipoJuridicoService {

    private static final String ESTADO_ACTIVO = "ACTIVE";
    private static final String CATALOGO_TIPO_PERSONAL = "TIPO PERSONAL";

    public void registrar(EquipoJuridicoRegistro registro) throws SQLException {
        validar(registro);
        registro.setUsername(normalizarUsername(registro.getUsername()));

        try (Connection conn = OracleConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (existeUsername(conn, registro.getUsername())) {
                    throw new IllegalArgumentException("El nombre de usuario ya existe.");
                }

                Integer idTecnico = buscarTecnicoExistente(conn, registro);
                if (idTecnico == null) {
                    idTecnico = crearTecnico(conn, registro);
                } else if (tecnicoVinculadoAOtroUsuario(conn, idTecnico, null)) {
                    throw new IllegalStateException("El técnico ya está vinculado a otro usuario.");
                } else {
                    actualizarTipoPersonalTecnicoSiNecesario(conn, idTecnico, registro.getIdTipoPersonal());
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
                if (esErrorUniqueUsername(ex)) {
                    throw new IllegalStateException("El nombre de usuario ya existe.", ex);
                }
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

    public List<CatalogoItem> listarTiposPersonalOficiales() throws SQLException {
        String sql =
            "SELECT ci.ID_CATALOGO_ITEM, ci.ID_CATALOGO, ci.DESCRIPCION, ci.ACTIVE " +
            "FROM CATALOGO_ITEM ci " +
            "JOIN CATALOGO c ON c.ID_CATALOGO = ci.ID_CATALOGO " +
            "WHERE UPPER(TRIM(c.DESCRIPCION)) = ? " +
            "AND UPPER(TRIM(ci.DESCRIPCION)) IN ('CAS ELECTORAL', 'PERSONAL OR', 'PERSONAL PLANTA') " +
            "AND NVL(ci.ACTIVE, 1) = 1 " +
            "ORDER BY CASE UPPER(TRIM(ci.DESCRIPCION)) " +
            "WHEN 'CAS ELECTORAL' THEN 1 " +
            "WHEN 'PERSONAL OR' THEN 2 " +
            "WHEN 'PERSONAL PLANTA' THEN 3 " +
            "ELSE 99 END";

        List<CatalogoItem> tipos = new ArrayList<>();
        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, CATALOGO_TIPO_PERSONAL);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tipos.add(new CatalogoItem(
                            rs.getInt("ID_CATALOGO_ITEM"),
                            rs.getInt("ID_CATALOGO"),
                            rs.getString("DESCRIPCION"),
                            rs.getInt("ACTIVE")
                    ));
                }
            }
        }
        return tipos;
    }

    public Integer obtenerIdTipoPersonal(Connection conn, String descripcion) throws SQLException {
        if (isBlank(descripcion)) {
            return null;
        }

        String sql =
            "SELECT ci.ID_CATALOGO_ITEM " +
            "FROM CATALOGO_ITEM ci " +
            "JOIN CATALOGO c ON c.ID_CATALOGO = ci.ID_CATALOGO " +
            "WHERE UPPER(TRIM(c.DESCRIPCION)) = ? " +
            "AND UPPER(TRIM(ci.DESCRIPCION)) = UPPER(TRIM(?)) " +
            "AND UPPER(TRIM(ci.DESCRIPCION)) IN ('CAS ELECTORAL', 'PERSONAL OR', 'PERSONAL PLANTA') " +
            "AND NVL(ci.ACTIVE, 1) = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, CATALOGO_TIPO_PERSONAL);
            ps.setString(2, descripcion);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID_CATALOGO_ITEM");
                }
            }
        }
        throw new IllegalArgumentException("El tipo de personal no existe en el catálogo TIPO PERSONAL: " + descripcion);
    }

    public EquipoJuridicoImportRowResult importarFila(
            Connection conn,
            String item,
            EquipoJuridicoRegistro abogado,
            EquipoJuridicoRegistro supervisor) throws SQLException {

        validar(abogado);
        abogado.setUsername(normalizarUsername(abogado.getUsername()));

        EquipoJuridicoImportRowResult result = new EquipoJuridicoImportRowResult();
        result.setItem(item);
        result.setAbogado(construirFullName(abogado));
        result.setUsernameAbogado(abogado.getUsername());

        PersonaOperativa abogadoOperativo = crearOReutilizarPersonaOperativa(
                conn,
                abogado,
                "ABOGADO",
                result.getAcciones(),
                "ABOGADO"
        );

        if (supervisor != null) {
            validar(supervisor);
            supervisor.setUsername(normalizarUsername(supervisor.getUsername()));
            result.setSupervisor(construirFullName(supervisor));
            result.setUsernameSupervisor(supervisor.getUsername());

            PersonaOperativa supervisorOperativo = crearOReutilizarPersonaOperativa(
                    conn,
                    supervisor,
                    "SUPERVISION",
                    result.getAcciones(),
                    "SUPERVISOR"
            );

            if (abogadoOperativo.userId.equals(supervisorOperativo.userId)) {
                throw new IllegalArgumentException("No se puede asignar la persona como su propio supervisor.");
            }

            asignarSupervisorSiNoExiste(conn, supervisorOperativo.userId, abogadoOperativo.userId, result.getAcciones());
        } else {
            result.setMensaje("Importado sin supervisor.");
        }

        result.setEstado("IMPORTADO");
        if (isBlank(result.getMensaje())) {
            result.setMensaje("Importación realizada correctamente.");
        }
        return result;
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
            "(ID_TECNICO, ID_TIPO_DOCUMENTO, NUMERO_DOCUMENTO, APELLIDO_PATERNO, APELLIDO_MATERNO, NOMBRES, ID_TIPO_PERSONAL, ACTIVE, FECHA_REGISTRO) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 1, SYSDATE)";

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
            if (registro.getIdTipoPersonal() == null) {
                ps.setNull(7, Types.NUMERIC);
            } else {
                ps.setInt(7, registro.getIdTipoPersonal());
            }
            ps.executeUpdate();
        }
        return idTecnico;
    }

    private void actualizarTipoPersonalTecnicoSiNecesario(Connection conn, Integer idTecnico, Integer idTipoPersonal) throws SQLException {
        if (idTipoPersonal == null) {
            return;
        }

        String sql = "UPDATE TECNICO SET ID_TIPO_PERSONAL = ? WHERE ID_TECNICO = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTipoPersonal);
            ps.setInt(2, idTecnico);
            ps.executeUpdate();
        }
    }

    private Integer obtenerSiguienteIdTecnico(Connection conn) throws SQLException {
        String sql = "SELECT SEQ_TECNICO.NEXTVAL FROM DUAL";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
            String message = ex.getMessage() == null ? "" : ex.getMessage().toUpperCase();
            if (message.contains("SEQ_TECNICO") || ex.getErrorCode() == 2289) {
                throw new SQLException("No existe o no es accesible la secuencia SEQ_TECNICO para generar ID_TECNICO.", ex);
            }
            throw ex;
        }
    }

    private Long crearUsuario(Connection conn, EquipoJuridicoRegistro registro, Integer idTecnico) throws SQLException {
        String username = normalizarUsername(registro.getUsername());
        String sql =
            "INSERT INTO APP_USERS (USERNAME, PASSWORD_HASH, FULL_NAME, STATUS, ID_TECNICO) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"USER_ID"})) {
            ps.setString(1, username);
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
        } catch (SQLException ex) {
            if (esErrorUniqueUsername(ex)) {
                throw new IllegalStateException("El nombre de usuario ya existe.", ex);
            }
            throw ex;
        }
        Long userId = buscarUserIdPorUsername(conn, username);
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

    private PersonaOperativa crearOReutilizarPersonaOperativa(
            Connection conn,
            EquipoJuridicoRegistro registro,
            String roleName,
            List<String> acciones,
            String prefijoAccion) throws SQLException {

        Integer idTecnico = buscarTecnicoExistente(conn, registro);
        boolean tecnicoCreado = false;
        if (idTecnico == null) {
            idTecnico = crearTecnico(conn, registro);
            tecnicoCreado = true;
            acciones.add("TECNICO_" + prefijoAccion + "_CREADO");
        } else {
            acciones.add("TECNICO_" + prefijoAccion + "_REUTILIZADO");
            actualizarTipoPersonalTecnicoSiNecesario(conn, idTecnico, registro.getIdTipoPersonal());
        }

        Long userId = buscarUserIdPorIdTecnico(conn, idTecnico);
        boolean usuarioCreado = false;
        if (userId == null) {
            if (existeUsername(conn, registro.getUsername())) {
                throw new IllegalStateException("El nombre de usuario ya existe.");
            }
            userId = crearUsuario(conn, registro, idTecnico);
            usuarioCreado = true;
            acciones.add("USUARIO_" + prefijoAccion + "_CREADO");
        } else {
            acciones.add("USUARIO_" + prefijoAccion + "_REUTILIZADO");
            vincularTecnicoSiNecesario(conn, userId, idTecnico);
        }

        boolean teniaRol = tieneRol(conn, userId, roleName);
        asignarRol(conn, userId, roleName);
        if (!teniaRol) {
            acciones.add("ROL_" + roleName + "_ASIGNADO");
        }

        return new PersonaOperativa(idTecnico, userId, tecnicoCreado, usuarioCreado);
    }

    private void vincularTecnicoSiNecesario(Connection conn, Long userId, Integer idTecnico) throws SQLException {
        Integer actual = obtenerIdTecnicoUsuario(conn, userId);
        if (actual == null) {
            if (tecnicoVinculadoAOtroUsuario(conn, idTecnico, userId)) {
                throw new IllegalStateException("El técnico ya está vinculado a otro usuario.");
            }
            String sql = "UPDATE APP_USERS SET ID_TECNICO = ? WHERE USER_ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idTecnico);
                ps.setLong(2, userId);
                ps.executeUpdate();
            }
        } else if (!actual.equals(idTecnico)) {
            throw new IllegalStateException("El usuario ya está vinculado a otro técnico.");
        }
    }

    private Integer obtenerIdTecnicoUsuario(Connection conn, Long userId) throws SQLException {
        String sql = "SELECT ID_TECNICO FROM APP_USERS WHERE USER_ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idTecnico = rs.getInt("ID_TECNICO");
                    return rs.wasNull() ? null : idTecnico;
                }
            }
        }
        return null;
    }

    private Long buscarUserIdPorIdTecnico(Connection conn, Integer idTecnico) throws SQLException {
        String sql = "SELECT USER_ID FROM APP_USERS WHERE ID_TECNICO = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTecnico);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("USER_ID") : null;
            }
        }
    }

    private void asignarSupervisorSiNoExiste(Connection conn, Long supervisorId, Long abogadoId, List<String> acciones) throws SQLException {
        Long supervisorActual = obtenerSupervisorActual(conn, abogadoId);
        if (supervisorActual != null) {
            if (supervisorActual.equals(supervisorId)) {
                acciones.add("RELACION_SUPERVISION_EXISTENTE");
                return;
            }
            throw new IllegalStateException("El abogado ya tiene otro supervisor asignado.");
        }

        asignarSupervisor(conn, supervisorId, abogadoId);
        acciones.add("RELACION_SUPERVISION_CREADA");
    }

    private Long obtenerSupervisorActual(Connection conn, Long abogadoId) throws SQLException {
        String sql = "SELECT SUPERVISOR_ID FROM APP_USER_SUPERVISION WHERE ABOGADO_ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, abogadoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("SUPERVISOR_ID") : null;
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
        String sql = "SELECT COUNT(1) FROM APP_USERS WHERE UPPER(TRIM(USERNAME)) = UPPER(TRIM(?))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizarUsername(username));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Long buscarUserIdPorUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT USER_ID FROM APP_USERS WHERE UPPER(TRIM(USERNAME)) = UPPER(TRIM(?))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizarUsername(username));
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

    private String normalizarUsername(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean esErrorUniqueUsername(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof SQLException) {
                SQLException sqlEx = (SQLException) current;
                String message = sqlEx.getMessage() == null ? "" : sqlEx.getMessage().toUpperCase();
                if (sqlEx.getErrorCode() == 1 && (message.contains("UK_APP_USERS_USERNAME") || message.contains("UNIQUE"))) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class PersonaOperativa {
        private final Integer idTecnico;
        private final Long userId;
        private final boolean tecnicoCreado;
        private final boolean usuarioCreado;

        private PersonaOperativa(Integer idTecnico, Long userId, boolean tecnicoCreado, boolean usuarioCreado) {
            this.idTecnico = idTecnico;
            this.userId = userId;
            this.tecnicoCreado = tecnicoCreado;
            this.usuarioCreado = usuarioCreado;
        }
    }
}
