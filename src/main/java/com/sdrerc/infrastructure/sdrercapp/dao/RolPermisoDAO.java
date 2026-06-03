package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RolPermisoDAO {

    public void sincronizarPermisos(Long idRol, List<Long> idsPermisoSeleccionados, Long idUsuarioActual) throws SQLException {
        if (idRol == null) {
            throw new IllegalArgumentException("Seleccione un rol antes de guardar permisos.");
        }
        Set<Long> seleccionados = normalizarIds(idsPermisoSeleccionados);
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                validarRolExiste(conn, idRol);
                validarPermisosActivos(conn, seleccionados);

                Set<Long> actuales = obtenerPermisosActivosRol(conn, idRol);
                for (Long idPermisoActual : actuales) {
                    if (!seleccionados.contains(idPermisoActual)) {
                        desactivarPermiso(conn, idRol, idPermisoActual, idUsuarioActual);
                    }
                }
                for (Long idPermisoSeleccionado : seleccionados) {
                    if (!actuales.contains(idPermisoSeleccionado)) {
                        activarOCrearPermiso(conn, idRol, idPermisoSeleccionado, idUsuarioActual);
                    }
                }

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            }
        }
    }

    private void validarRolExiste(Connection conn, Long idRol) throws SQLException {
        String sql = "SELECT 1 FROM rol WHERE id_rol = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El rol seleccionado no existe.");
                }
            }
        }
    }

    private void validarPermisosActivos(Connection conn, Set<Long> idsPermiso) throws SQLException {
        for (Long idPermiso : idsPermiso) {
            String sql = "SELECT 1 FROM permiso WHERE id_permiso = ? AND activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idPermiso);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Uno de los permisos seleccionados no existe o no está activo.");
                    }
                }
            }
        }
    }

    private Set<Long> obtenerPermisosActivosRol(Connection conn, Long idRol) throws SQLException {
        String sql = "SELECT DISTINCT id_permiso FROM rol_permiso WHERE id_rol = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                Set<Long> ids = new LinkedHashSet<>();
                while (rs.next()) {
                    long value = rs.getLong("id_permiso");
                    if (!rs.wasNull()) {
                        ids.add(value);
                    }
                }
                return ids;
            }
        }
    }

    private void desactivarPermiso(Connection conn, Long idRol, Long idPermiso, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE rol_permiso SET activo = 0, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_rol = ? AND id_permiso = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idRol);
            ps.setLong(3, idPermiso);
            ps.executeUpdate();
        }
    }

    private void activarOCrearPermiso(Connection conn, Long idRol, Long idPermiso, Long idUsuarioActual) throws SQLException {
        if (tienePermisoActivo(conn, idRol, idPermiso)) {
            return;
        }
        if (reactivarPermiso(conn, idRol, idPermiso, idUsuarioActual)) {
            return;
        }
        insertarPermiso(conn, idRol, idPermiso, idUsuarioActual);
    }

    private boolean tienePermisoActivo(Connection conn, Long idRol, Long idPermiso) throws SQLException {
        String sql = "SELECT 1 FROM rol_permiso WHERE id_rol = ? AND id_permiso = ? AND activo = 1 AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idRol);
            ps.setLong(2, idPermiso);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean reactivarPermiso(Connection conn, Long idRol, Long idPermiso, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE rol_permiso SET activo = 1, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_rol = ? AND id_permiso = ? AND activo = 0 AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idRol);
            ps.setLong(3, idPermiso);
            return ps.executeUpdate() > 0;
        }
    }

    private void insertarPermiso(Connection conn, Long idRol, Long idPermiso, Long idUsuarioActual) throws SQLException {
        String sql = "INSERT INTO rol_permiso (id_rol, id_permiso, activo, creado_por, creado_en) "
                + "VALUES (?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idRol);
            ps.setLong(2, idPermiso);
            setNullableLong(ps, 3, idUsuarioActual);
            ps.executeUpdate();
        }
    }

    private static Set<Long> normalizarIds(List<Long> ids) {
        Set<Long> result = new LinkedHashSet<>();
        if (ids == null) {
            return result;
        }
        for (Long id : ids) {
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

    private static void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private static void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // No ocultar el error original.
        }
    }
}
