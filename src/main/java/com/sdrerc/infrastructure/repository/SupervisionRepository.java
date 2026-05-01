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

    private static final String SQL_FIND_ABOGADOS_DISPONIBLES =
        "SELECT DISTINCT u.USER_ID, u.USERNAME, u.FULL_NAME, u.STATUS, u.ID_TECNICO " +
        "FROM APP_USERS u " +
        "JOIN APP_USER_ROLES ur ON ur.USER_ID = u.USER_ID " +
        "JOIN APP_ROLES r ON r.ROLE_ID = ur.ROLE_ID " +
        "WHERE r.ROLE_NAME = 'ABOGADO' " +
        "AND UPPER(u.STATUS) IN ('ACTIVE', 'ACTIVO') " +
        "AND UPPER(r.STATUS) IN ('ACTIVE', 'ACTIVO') " +
        "AND NOT EXISTS ( " +
        "    SELECT 1 FROM APP_USER_SUPERVISION s " +
        "    WHERE s.ABOGADO_ID = u.USER_ID " +
        "    AND s.SUPERVISOR_ID <> ? " +
        ") " +
        "ORDER BY u.FULL_NAME";
    
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

    public List<User> findAbogadosDisponiblesParaSupervisor(Long supervisorId)
            throws SQLException {

        List<User> lista = new ArrayList<>();

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL_FIND_ABOGADOS_DISPONIBLES)) {

            ps.setLong(1, supervisorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getLong("USER_ID"));
                    u.setUsername(rs.getString("USERNAME"));
                    u.setFullName(rs.getString("FULL_NAME"));
                    u.setStatus(rs.getString("STATUS"));
                    long idTecnico = rs.getLong("ID_TECNICO");
                    u.setIdTecnico(rs.wasNull() ? null : idTecnico);
                    lista.add(u);
                }
            }
        }
        return lista;
    }

    public boolean abogadoAsignadoAOtroSupervisor(Long supervisorId, Long abogadoId)
            throws SQLException {

        String sql =
            "SELECT COUNT(1) FROM APP_USER_SUPERVISION " +
            "WHERE ABOGADO_ID = ? " +
            "AND SUPERVISOR_ID <> ?";

        try (Connection cn = OracleConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, abogadoId);
            ps.setLong(2, supervisorId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void reemplazarAbogados(Long supervisorId, List<Long> abogados)
            throws SQLException {

        try (Connection cn = OracleConnection.getConnection()) {
            cn.setAutoCommit(false);
            try {
                List<Long> actuales = findAbogadosBySupervisor(cn, supervisorId);

                try (PreparedStatement psInsert = cn.prepareStatement(SQL_INSERT)) {
                    for (Long abogadoId : abogados) {
                        if (!actuales.contains(abogadoId)) {
                            psInsert.setLong(1, supervisorId);
                            psInsert.setLong(2, abogadoId);
                            psInsert.addBatch();
                        }
                    }
                    psInsert.executeBatch();
                }

                try (PreparedStatement psDelete = cn.prepareStatement(SQL_DELETE)) {
                    for (Long abogadoId : actuales) {
                        if (!abogados.contains(abogadoId)) {
                            psDelete.setLong(1, supervisorId);
                            psDelete.setLong(2, abogadoId);
                            psDelete.addBatch();
                        }
                    }
                    psDelete.executeBatch();
                }

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private List<Long> findAbogadosBySupervisor(Connection cn, Long supervisorId)
            throws SQLException {

        List<Long> lista = new ArrayList<>();

        try (PreparedStatement ps = cn.prepareStatement(SQL_FIND_ABOGADOS)) {
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
