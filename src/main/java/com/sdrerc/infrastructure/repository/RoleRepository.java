/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.Role;
import com.sdrerc.domain.model.PaginatedResult;
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
public class RoleRepository {
    public void save(Role role) throws SQLException {
        String sql = "INSERT INTO APP_ROLES (ROLE_NAME, DESCRIPTION, STATUS) VALUES (?, ?, ?)";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, role.getRoleName());
            ps.setString(2, role.getDescription());
            ps.setString(3, role.getStatus());

            ps.executeUpdate();
        }
    }
    
    public void update(Role role) throws SQLException {
        String sql = "UPDATE APP_ROLES SET ROLE_NAME=?, DESCRIPTION=?, STATUS=? WHERE ROLE_ID=?";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, role.getRoleName());
            ps.setString(2, role.getDescription());
            ps.setString(3, role.getStatus());
            ps.setLong(4, role.getRoleId());

            ps.executeUpdate();
        }
    }
    
    public List<Role> findAll() throws SQLException {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT ROLE_ID, ROLE_NAME, DESCRIPTION, STATUS FROM APP_ROLES ORDER BY ROLE_ID";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                roles.add(new Role(
                        rs.getLong("ROLE_ID"),
                        rs.getString("ROLE_NAME"),
                        rs.getString("DESCRIPTION"),
                        rs.getString("STATUS")
                ));
            }
        }
        return roles;
    }
    
    public List<Role> buscar(String nombre, String estado) throws SQLException {

        List<Role> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ROLE_ID, ROLE_NAME, DESCRIPTION, STATUS ");
        sql.append("FROM APP_ROLES ");
        sql.append("WHERE UPPER(ROLE_NAME) LIKE ? ");

        if (!"TODOS".equals(estado)) {
            sql.append(" AND STATUS = ? ");
        }

        sql.append(" ORDER BY ROLE_ID ");

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            ps.setString(1, "%" + nombre.toUpperCase() + "%");

            if (!"TODOS".equals(estado)) {
                ps.setString(2, estado);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Role(
                    rs.getLong("ROLE_ID"),
                    rs.getString("ROLE_NAME"),
                    rs.getString("DESCRIPTION"),
                    rs.getString("STATUS")
                ));
            }
        }
        return lista;
    }

    public PaginatedResult<Role> buscarPaginado(
            String filtro, String estado, int page, int pageSize) throws SQLException {

        int safePage = Math.max(1, page);
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        String filtroNormalizado = filtro == null ? "" : filtro.trim().toUpperCase();

        StringBuilder where = new StringBuilder(" WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        if (!filtroNormalizado.isEmpty()) {
            String like = "%" + filtroNormalizado + "%";
            where.append(" AND (UPPER(ROLE_NAME) LIKE ? OR UPPER(DESCRIPTION) LIKE ?) ");
            params.add(like);
            params.add(like);
        }

        if (estado != null && !"TODOS".equalsIgnoreCase(estado.trim())) {
            where.append(" AND STATUS = ? ");
            params.add(estado.trim());
        }

        int totalRecords = contarRolesPaginado(where.toString(), params);
        int totalPages = totalRecords == 0
                ? 1
                : (int) Math.ceil((double) totalRecords / safePageSize);
        if (safePage > totalPages) {
            safePage = totalPages;
        }

        String sql =
            "SELECT ROLE_ID, ROLE_NAME, DESCRIPTION, STATUS " +
            "FROM APP_ROLES " +
            where +
            "ORDER BY ROLE_ID " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<Role> data = new ArrayList<>();
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
                    data.add(mapRole(rs));
                }
            }
        }

        return new PaginatedResult<>(data, safePage, safePageSize, totalRecords, totalPages);
    }

    private int contarRolesPaginado(String where, List<Object> params) throws SQLException {
        String sql = "SELECT COUNT(1) FROM APP_ROLES " + where;

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

        String sql = "UPDATE APP_ROLES SET STATUS = ? WHERE ROLE_ID = ?";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    private Role mapRole(ResultSet rs) throws SQLException {
        return new Role(
                rs.getLong("ROLE_ID"),
                rs.getString("ROLE_NAME"),
                rs.getString("DESCRIPTION"),
                rs.getString("STATUS")
        );
    }
}
