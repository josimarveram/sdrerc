package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.domain.PermisoAdminDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Port de {@code com.sdrerc.infrastructure.sdrercapp.dao.PermisoDAO#listarPermisosPorRol} +
 * {@code RolPermisoDAO#sincronizarPermisos} (V2), Administración &gt; Roles. La agrupación por
 * módulo (Accordion, mejora sobre la tabla plana de V2) vive en el frontend — acá se sigue
 * devolviendo la lista plana ordenada por módulo/nombre/código, igual que V2.
 */
@Repository
public class PermisoAdminDAO {

    private final DataSource dataSource;

    public PermisoAdminDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<PermisoAdminDTO> listarPermisosPorRol(Long idRol) throws SQLException {
        String sql = "SELECT p.id_permiso, p.codigo, p.nombre, p.modulo, p.activo, "
                + "CASE WHEN rp.id_permiso IS NULL THEN 0 ELSE 1 END AS asignado "
                + "FROM permiso p "
                + "LEFT JOIN (SELECT DISTINCT id_permiso FROM rol_permiso WHERE id_rol = ? AND activo = 1) rp "
                + "  ON rp.id_permiso = p.id_permiso "
                + "WHERE p.activo = 1 "
                + "ORDER BY p.modulo ASC, p.nombre ASC, p.codigo ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idRol == null) {
                ps.setNull(1, Types.NUMERIC);
            } else {
                ps.setLong(1, idRol);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<PermisoAdminDTO> permisos = new ArrayList<>();
                while (rs.next()) {
                    permisos.add(new PermisoAdminDTO(
                            getLongOrNull(rs, "id_permiso"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("modulo"),
                            rs.getInt("activo") == 1,
                            rs.getInt("asignado") == 1));
                }
                return permisos;
            }
        }
    }

    public void sincronizarPermisos(Long idRol, List<Long> idsPermisoSeleccionados, Long idUsuarioActual) throws SQLException {
        if (idRol == null) {
            throw new IllegalArgumentException("Seleccione un rol antes de guardar permisos.");
        }
        Set<Long> seleccionados = normalizarIds(idsPermisoSeleccionados);
        try (Connection conn = dataSource.getConnection()) {
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
                if (ex instanceof SQLException sqlEx) {
                    throw sqlEx;
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
        String sqlTiene = "SELECT 1 FROM rol_permiso WHERE id_rol = ? AND id_permiso = ? AND activo = 1 AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sqlTiene)) {
            ps.setLong(1, idRol);
            ps.setLong(2, idPermiso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }
        String sqlReactivar = "UPDATE rol_permiso SET activo = 1, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_rol = ? AND id_permiso = ? AND activo = 0 AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sqlReactivar)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idRol);
            ps.setLong(3, idPermiso);
            if (ps.executeUpdate() > 0) {
                return;
            }
        }
        String sqlInsertar = "INSERT INTO rol_permiso (id_rol, id_permiso, activo, creado_por, creado_en) "
                + "VALUES (?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsertar)) {
            ps.setLong(1, idRol);
            ps.setLong(2, idPermiso);
            setNullableLong(ps, 3, idUsuarioActual);
            ps.executeUpdate();
        }
    }

    private static Set<Long> normalizarIds(List<Long> ids) {
        Set<Long> result = new LinkedHashSet<>();
        if (ids != null) {
            for (Long id : ids) {
                if (id != null) {
                    result.add(id);
                }
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

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // No ocultar el error original.
        }
    }
}
