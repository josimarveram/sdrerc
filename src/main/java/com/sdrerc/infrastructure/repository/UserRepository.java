/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.Role;
import com.sdrerc.domain.model.PaginatedResult;
import com.sdrerc.domain.model.User;
import com.sdrerc.domain.model.UsuarioListadoItem;
import com.sdrerc.infrastructure.database.OracleConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author David
 */
public class UserRepository {
    /*
    public User login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, fullname, role FROM users WHERE username = ? AND password = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("fullname"),
                        rs.getString("role")
                );
            }
        }

        return null; // usuario no encontrado
    }
    
    */
    
    private static final String SQL_TIENE_ROL =
        "SELECT COUNT(1) " +
        "FROM APP_USER_ROLES ur " +
        "JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
        "JOIN APP_USERS u ON u.USER_ID = ur.USER_ID " +
        "WHERE ur.USER_ID = ? " +
        "AND r.ROLE_NAME = ? " +
        "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO') " +
        "AND UPPER(u.STATUS) IN ('ACTIVE', 'ACTIVO')";
    
    private static final String SQL_LISTAR_POR_ROL =
        "SELECT DISTINCT u.USER_ID, u.USERNAME, u.FULL_NAME, u.STATUS, u.ID_TECNICO " +
        "FROM APP_USERS u " +
        "JOIN APP_USER_ROLES ur ON ur.USER_ID = u.USER_ID " +
        "JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
        "WHERE r.ROLE_NAME = ? " +
        "AND UPPER(u.STATUS) IN ('ACTIVE', 'ACTIVO') " +
        "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO') " +
        "ORDER BY u.FULL_NAME";
    
    public User findByUsername(String username) {
        String sql = "SELECT USER_ID, USERNAME, PASSWORD_HASH, FULL_NAME, STATUS, ID_TECNICO "
                + "FROM APP_USERS WHERE UPPER(TRIM(USERNAME)) = UPPER(TRIM(?)) AND UPPER(STATUS) IN ('ACTIVE', 'ACTIVO')";
        try (Connection conn = OracleConnection.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeUsername(username));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = mapUser(rs);
                u.setPasswordHash(rs.getString("PASSWORD_HASH"));
                u.setRoles(listarRoleNamesPorUsuario(conn, u.getUserId()));
                return u;
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return null;
    }
    
    public List<User> findAll(String filter) {
        List<User> list = new ArrayList<>();
        String sql =
          "SELECT USER_ID, USERNAME, FULL_NAME, STATUS, ID_TECNICO "
        + "FROM APP_USERS "
        + "WHERE UPPER(USERNAME) LIKE ? "
        + "   OR UPPER(FULL_NAME) LIKE ? "
        + "ORDER BY USERNAME";
        
        try (Connection conn = OracleConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ) {
            
            String f = "%" + filter.toUpperCase() + "%"; 
            ps.setString(1, f);
            ps.setString(2, f);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
    
    public void save(User usuario) throws SQLException {
        String sql = "INSERT INTO APP_USERS (USERNAME, PASSWORD_HASH, FULL_NAME, STATUS) VALUES (?, ?, ?, ?)";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, normalizeUsername(usuario.getUsername()));
            ps.setString(2, usuario.getPasswordHash());
            ps.setString(3, usuario.getFullName());
            ps.setString(4, usuario.getStatus());

            ps.executeUpdate();
        }
    }
    
    public void update(User usuario) throws SQLException {
        String sql = "UPDATE APP_USERS  SET USERNAME =?, FULL_NAME =?, STATUS=? WHERE USER_ID =?";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, normalizeUsername(usuario.getUsername()));
            ps.setString(2, usuario.getFullName());
            ps.setString(3, usuario.getStatus());
            ps.setLong(4, usuario.getUserId());

            ps.executeUpdate();
        }
    }
    
    public List<User> findAll() throws SQLException {
        List<User> usuarios = new ArrayList<>();
        String sql = "SELECT USER_ID, USERNAME, FULL_NAME, STATUS, ID_TECNICO FROM APP_USERS ORDER BY USER_ID";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapUser(rs));
            }
        }
        return usuarios;
    }
    
    public List<User> buscar(String nombre, String estado) throws SQLException {

        List<User> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT USER_ID, USERNAME, FULL_NAME, STATUS, ID_TECNICO ");
        sql.append("FROM APP_USERS ");
        sql.append("WHERE UPPER(USERNAME) LIKE ? ");

        if (!"TODOS".equals(estado)) {
            sql.append(" AND STATUS = ? ");
        }

        sql.append(" ORDER BY USER_ID ");

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            ps.setString(1, "%" + nombre.toUpperCase() + "%");

            if (!"TODOS".equals(estado)) {
                ps.setString(2, estado);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapUser(rs));
            }
        }
        return lista;
    }

    public PaginatedResult<UsuarioListadoItem> buscarPaginado(
            String filtro, String estado, int page, int pageSize) throws SQLException {

        int safePage = Math.max(1, page);
        int safePageSize = pageSize <= 0 ? 25 : pageSize;
        String filtroNormalizado = filtro == null ? "" : filtro.trim().toUpperCase();

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        if (!filtroNormalizado.isEmpty()) {
            String like = "%" + filtroNormalizado + "%";
            where.append(" AND (");
            where.append("UPPER(u.USERNAME) LIKE ? ");
            where.append("OR UPPER(u.FULL_NAME) LIKE ? ");
            where.append("OR UPPER(TRIM(NVL(t.APELLIDO_PATERNO, '') || ' ' || ");
            where.append("NVL(t.APELLIDO_MATERNO, '') || ' ' || NVL(t.NOMBRES, ''))) LIKE ? ");
            where.append(") ");
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (estado != null && !"TODOS".equalsIgnoreCase(estado.trim())) {
            where.append(" AND u.STATUS = ? ");
            params.add(estado.trim());
        }

        int totalRecords = contarUsuariosPaginado(where.toString(), params);
        int totalPages = totalRecords == 0
                ? 1
                : (int) Math.ceil((double) totalRecords / safePageSize);
        if (safePage > totalPages) {
            safePage = totalPages;
        }

        String sql =
            "SELECT " +
            "u.USER_ID, " +
            "u.USERNAME, " +
            "CASE " +
            "  WHEN u.ID_TECNICO IS NOT NULL THEN " +
            "    TRIM(NVL(t.APELLIDO_PATERNO, '') || ' ' || " +
            "         NVL(t.APELLIDO_MATERNO, '') || ', ' || " +
            "         NVL(t.NOMBRES, '')) " +
            "  ELSE u.FULL_NAME " +
            "END AS NOMBRE_VISIBLE, " +
            "NVL(( " +
            "  SELECT LISTAGG(r.ROLE_NAME, ', ') WITHIN GROUP (ORDER BY r.ROLE_NAME) " +
            "  FROM APP_USER_ROLES ur " +
            "  JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
            "  WHERE ur.USER_ID = u.USER_ID " +
            "    AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO') " +
            "), '') AS ROLES_PERFIL, " +
            "u.STATUS, " +
            "u.ID_TECNICO, " +
            "CASE " +
            "  WHEN EXISTS ( " +
            "    SELECT 1 " +
            "    FROM APP_USER_ROLES ur " +
            "    JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
            "    WHERE ur.USER_ID = u.USER_ID " +
            "      AND UPPER(r.ROLE_NAME) = 'SUPERVISION' " +
            "      AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO') " +
            "  ) THEN 1 " +
            "  ELSE 0 " +
            "END AS ES_SUPERVISION, " +
            "CASE " +
            "  WHEN EXISTS ( " +
            "    SELECT 1 " +
            "    FROM APP_USER_ROLES ur " +
            "    JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
            "    WHERE ur.USER_ID = u.USER_ID " +
            "      AND UPPER(TRIM(r.ROLE_NAME)) IN ('ABOGADO', 'SUPERVISION') " +
            "      AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO') " +
            "  ) THEN 1 " +
            "  ELSE 0 " +
            "END AS ES_OPERATIVO_JURIDICO " +
            "FROM APP_USERS u " +
            "LEFT JOIN TECNICO t ON t.ID_TECNICO = u.ID_TECNICO " +
            where +
            "ORDER BY NOMBRE_VISIBLE, u.USERNAME " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<UsuarioListadoItem> data = new ArrayList<>();
        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            int index = 1;
            for (Object param : params) {
                ps.setObject(index++, param);
            }
            ps.setInt(index++, (safePage - 1) * safePageSize);
            ps.setInt(index, safePageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    data.add(mapUsuarioListadoItem(rs));
                }
            }
        }

        return new PaginatedResult<>(data, safePage, safePageSize, totalRecords, totalPages);
    }

    private int contarUsuariosPaginado(String where, List<Object> params) throws SQLException {
        String sql =
            "SELECT COUNT(1) " +
            "FROM APP_USERS u " +
            "LEFT JOIN TECNICO t ON t.ID_TECNICO = u.ID_TECNICO " +
            where;

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            int index = 1;
            for (Object param : params) {
                ps.setObject(index++, param);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
    
    public void cambiarEstado(Long id, String estado) throws SQLException {

        String sql = "UPDATE APP_USERS SET STATUS=? WHERE USER_ID=?";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public boolean existeTecnicoVinculadoAOtroUsuario(Long userId, Integer idTecnico) throws SQLException {
        String sql =
            "SELECT COUNT(1) " +
            "FROM APP_USERS " +
            "WHERE ID_TECNICO = ? " +
            "AND USER_ID <> ?";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idTecnico);
            ps.setLong(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void vincularTecnico(Long userId, Integer idTecnico) throws SQLException {
        String sql =
            "UPDATE APP_USERS " +
            "SET ID_TECNICO = ? " +
            "WHERE USER_ID = ?";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idTecnico);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    public Long obtenerIdTecnicoUsuario(Long userId) throws SQLException {
        String sql = "SELECT ID_TECNICO FROM APP_USERS WHERE USER_ID = ?";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return getNullableLong(rs, "ID_TECNICO");
                }
            }
        }
        return null;
    }
    
    public void actualizarPassword(Long userId, String hash) throws SQLException {

        String sql =
            "UPDATE APP_USERS SET PASSWORD_HASH = ? WHERE USER_ID = ?";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }
    
    
    
    public List<Role> listarRolesPorUsuario(Long userId)
            throws SQLException {

        List<Role> roles = new ArrayList<>();

        String sql =
            "SELECT r.ROLE_ID, r.ROLE_NAME " +
            "FROM APP_ROLES r " +
            "JOIN APP_USER_ROLES ur ON ur.ROLE_ID = r.ROLE_ID " +
            "WHERE ur.USER_ID = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Role r = new Role();
                    r.setRoleId(rs.getLong("ROLE_ID"));
                    r.setRoleName(rs.getString("ROLE_NAME"));
                    roles.add(r);
                }
            }
        }
        return roles;
    }
    
    public void asignarRoles(Long userId, List<Long> roles) throws SQLException {

        String del = "DELETE FROM APP_USER_ROLES WHERE USER_ID = ?";
        String ins = "INSERT INTO APP_USER_ROLES (USER_ID, ROLE_ID) VALUES (?, ?)";

        try (Connection cn = OracleConnection.getConnection()) {

            cn.setAutoCommit(false);

            try (PreparedStatement psDel = cn.prepareStatement(del)) {
                psDel.setLong(1, userId);
                psDel.executeUpdate();
            }

            try (PreparedStatement psIns = cn.prepareStatement(ins)) {
                for (Long roleId : roles) {
                    psIns.setLong(1, userId);
                    psIns.setLong(2, roleId);
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }

            cn.commit();
        }
    }

    public List<String> obtenerRoleNamesPorIds(List<Long> roleIds) throws SQLException {
        List<String> roleNames = new ArrayList<>();
        if (roleIds == null || roleIds.isEmpty()) {
            return roleNames;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < roleIds.size(); i++) {
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append("?");
        }

        String sql =
            "SELECT ROLE_NAME " +
            "FROM APP_ROLES " +
            "WHERE ROLE_ID IN (" + placeholders + ")";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            for (int i = 0; i < roleIds.size(); i++) {
                ps.setLong(i + 1, roleIds.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roleNames.add(rs.getString("ROLE_NAME"));
                }
            }
        }
        return roleNames;
    }
    
    
    public boolean tieneRol(Long userId, String roleName)
            throws SQLException {
              

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_TIENE_ROL)) {

            ps.setLong(1, userId);
            ps.setString(2, roleName);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    
    public List<User> listarPorRol(String roleName)
            throws SQLException {

        List<User> lista = new ArrayList<>();

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps =
                     cn.prepareStatement(SQL_LISTAR_POR_ROL)) {

            ps.setString(1, roleName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapUser(rs));
                }
            }
        }
        return lista;
    }
    
    public void deactivate(Long userId) {
        String sql = "UPDATE APP_USERS SET STATUS = 'INACTIVE' WHERE USER_ID = ?";
        try (Connection conn = OracleConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
        
    public void create(User u) {
        String sql = "INSERT INTO APP_USERS (USERNAME, PASSWORD_HASH, FULL_NAME, STATUS) VALUES (?, ?, ?, 'ACTIVE')";
        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeUsername(u.getUsername()));
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getFullName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean existsByUsername(String username) {

        String sql = "SELECT 1 FROM APP_USERS WHERE UPPER(TRIM(USERNAME)) = UPPER(TRIM(?))";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, normalizeUsername(username));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // si hay fila, existe
            }

        } catch (Exception e) {
            throw new RuntimeException(
                "Error verificando existencia de usuario", e
            );
        }
    }
    
    public void changeStatus(Long id, String status) {
        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                "UPDATE APP_USERS SET STATUS=? WHERE USER_ID=?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void assignRoles(Long userId, List<Long> roleIds) {
        try {Connection conn = OracleConnection.getConnection();
            conn.prepareStatement(
                "DELETE FROM APP_USER_ROLES WHERE USER_ID=?")
              .executeUpdate();

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO APP_USER_ROLES (USER_ID, ROLE_ID) VALUES (?, ?)");

            for (Long roleId : roleIds) {
                ps.setLong(1, userId);
                ps.setLong(2, roleId);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getLong("USER_ID"));
        u.setUsername(rs.getString("USERNAME"));
        u.setFullName(rs.getString("FULL_NAME"));
        u.setStatus(rs.getString("STATUS"));
        u.setIdTecnico(getNullableLong(rs, "ID_TECNICO"));
        return u;
    }

    private UsuarioListadoItem mapUsuarioListadoItem(ResultSet rs) throws SQLException {
        UsuarioListadoItem item = new UsuarioListadoItem();
        item.setUserId(rs.getLong("USER_ID"));
        item.setUsername(rs.getString("USERNAME"));
        item.setNombreVisible(rs.getString("NOMBRE_VISIBLE"));
        item.setRolesPerfil(rs.getString("ROLES_PERFIL"));
        item.setStatus(rs.getString("STATUS"));
        item.setIdTecnico(getNullableLong(rs, "ID_TECNICO"));
        item.setEsSupervision(rs.getInt("ES_SUPERVISION") == 1);
        item.setEsOperativoJuridico(rs.getInt("ES_OPERATIVO_JURIDICO") == 1);
        return item;
    }

    private Long getNullableLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private List<String> listarRoleNamesPorUsuario(Connection conn, Long userId) throws SQLException {
        List<String> roles = new ArrayList<>();
        String sql =
            "SELECT r.ROLE_NAME " +
            "FROM APP_ROLES r " +
            "JOIN APP_USER_ROLES ur ON ur.ROLE_ID = r.ROLE_ID " +
            "WHERE ur.USER_ID = ? " +
            "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO')";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("ROLE_NAME"));
                }
            }
        }
        return roles;
    }
}
