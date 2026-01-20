/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.infrastructure.repository;

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
public class SupervisionRepository {
    private static final String SQL_INSERT =
        "INSERT INTO APP_USER_SUPERVISION (SUPERVISOR_ID, ABOGADO_ID) " +
        "VALUES (?, ?)";

    private static final String SQL_DELETE =
        "DELETE FROM APP_USER_SUPERVISION " +
        "WHERE SUPERVISOR_ID = ? AND ABOGADO_ID = ?";

    private static final String SQL_EXISTS =
        "SELECT 1 FROM APP_USER_SUPERVISION " +
        "WHERE SUPERVISOR_ID = ? AND ABOGADO_ID = ?";

    private static final String SQL_FIND_ABOGADOS =
        "SELECT ABOGADO_ID FROM APP_USER_SUPERVISION " +
        "WHERE SUPERVISOR_ID = ?";
    
    public void insert(Long supervisorId, Long abogadoId)
            throws SQLException {

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_INSERT)) {

            ps.setLong(1, supervisorId);
            ps.setLong(2, abogadoId);
            ps.executeUpdate();
        }
    }

    
    public void delete(Long supervisorId, Long abogadoId)
            throws SQLException {

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_DELETE)) {

            ps.setLong(1, supervisorId);
            ps.setLong(2, abogadoId);
            ps.executeUpdate();
        }
    }

    
    public boolean exists(Long supervisorId, Long abogadoId)
            throws SQLException {

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_EXISTS)) {

            ps.setLong(1, supervisorId);
            ps.setLong(2, abogadoId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    
    public List<Long> findAbogadosBySupervisor(Long supervisorId)
            throws SQLException {

        List<Long> lista = new ArrayList<>();

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps =
                     cn.prepareStatement(SQL_FIND_ABOGADOS)) {

            ps.setLong(1, supervisorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getLong("ABOGADO_ID"));
                }
            }
        }
        return lista;
    }
}
