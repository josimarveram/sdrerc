/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.Role;
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

    public void cambiarEstado(Long id, String estado) throws SQLException {

        String sql = "UPDATE APP_ROLES SET STATUS = ? WHERE ROLE_ID = ?";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }
}
