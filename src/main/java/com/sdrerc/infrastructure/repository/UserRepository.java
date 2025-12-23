/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

import com.sdrerc.domain.model.User;
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
    
    public User findByUsername(String username) {
        String sql = "SELECT * FROM APP_USERS WHERE USERNAME=? AND STATUS='ACTIVE'";
        try (Connection conn = OracleConnection.getConnection();
           PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setUserId(rs.getLong("USER_ID"));
                u.setUsername(rs.getString("USERNAME"));
                u.setPasswordHash(rs.getString("PASSWORD_HASH"));
                u.setFullName(rs.getString("FULL_NAME"));
                return u;
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return null;
    }
    
    public List<User> findAll(String filter) {
        List<User> list = new ArrayList<>();
        String sql =
          "SELECT USER_ID, USERNAME, FULL_NAME, STATUS "
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
                User u = new User();
                u.setUserId(rs.getLong("USER_ID"));
                u.setUsername(rs.getString("USERNAME"));
                u.setFullName(rs.getString("FULL_NAME"));
                u.setStatus(rs.getString("STATUS"));
                list.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
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
    
    public void update(User u) {
        String sql = "UPDATE APP_USERS SET FULL_NAME = ?, PASSWORD_HASH = NVL(?, PASSWORD_HASH) WHERE USER_ID = ?";
        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getPasswordHash());
            ps.setLong(3, u.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void create(User u) {
        String sql = "INSERT INTO APP_USERS (USERNAME, PASSWORD_HASH, FULL_NAME, STATUS) VALUES (?, ?, ?, 'ACTIVE')";
        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getFullName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean existsByUsername(String username) {

        String sql = "SELECT 1 FROM APP_USERS WHERE USERNAME = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

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
}
