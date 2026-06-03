package com.sdrerc.infrastructure.sdrercapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UsuarioRolDAO {

    public void sincronizarRoles(Connection conn, Long idUsuario, List<Long> idsRoles, Long idUsuarioActual) throws SQLException {
        Set<Long> seleccionados = normalizarIds(idsRoles);
        validarRolesActivos(conn, seleccionados);
        Set<Long> actuales = obtenerRolesActivosUsuario(conn, idUsuario);

        for (Long idRolActual : actuales) {
            if (!seleccionados.contains(idRolActual)) {
                desactivarRol(conn, idUsuario, idRolActual, idUsuarioActual);
            }
        }
        for (Long idRolSeleccionado : seleccionados) {
            if (!actuales.contains(idRolSeleccionado)) {
                activarOCrearRol(conn, idUsuario, idRolSeleccionado, idUsuarioActual);
            }
        }
    }

    public boolean usuarioTieneRolActivo(Connection conn, Long idUsuario, String codigoRol) throws SQLException {
        String sql = "SELECT 1 FROM usuario_rol ur "
                + "JOIN rol r ON r.id_rol = ur.id_rol "
                + "WHERE ur.id_usuario = ? "
                + "AND ur.activo = 1 "
                + "AND r.activo = 1 "
                + "AND UPPER(r.codigo) = ? "
                + "AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ps.setString(2, codigoRol == null ? "" : codigoRol.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean seleccionIncluyeRolCodigo(Connection conn, List<Long> idsRoles, String codigoRol) throws SQLException {
        Set<Long> seleccionados = normalizarIds(idsRoles);
        if (seleccionados.isEmpty()) {
            return false;
        }
        StringBuilder sql = new StringBuilder("SELECT 1 FROM rol WHERE activo = 1 AND UPPER(codigo) = ? AND id_rol IN (");
        int count = 0;
        for (Long ignored : seleccionados) {
            if (count++ > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(") AND ROWNUM = 1");
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, codigoRol == null ? "" : codigoRol.trim().toUpperCase());
            int index = 2;
            for (Long idRol : seleccionados) {
                ps.setLong(index++, idRol);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int contarUsuariosActivosConRol(Connection conn, String codigoRol) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT u.id_usuario) AS total "
                + "FROM usuario u "
                + "JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario AND ur.activo = 1 "
                + "JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1 "
                + "WHERE u.activo = 1 "
                + "AND UPPER(u.estado) = 'ACTIVO' "
                + "AND UPPER(r.codigo) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoRol == null ? "" : codigoRol.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    private void validarRolesActivos(Connection conn, Set<Long> idsRoles) throws SQLException {
        for (Long idRol : idsRoles) {
            String sql = "SELECT 1 FROM rol WHERE id_rol = ? AND activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idRol);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Uno de los roles seleccionados no existe o no está activo.");
                    }
                }
            }
        }
    }

    private Set<Long> obtenerRolesActivosUsuario(Connection conn, Long idUsuario) throws SQLException {
        Set<Long> ids = new LinkedHashSet<>();
        String sql = "SELECT DISTINCT id_rol FROM usuario_rol WHERE id_usuario = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long value = rs.getLong("id_rol");
                    if (!rs.wasNull()) {
                        ids.add(value);
                    }
                }
            }
        }
        return ids;
    }

    private void desactivarRol(Connection conn, Long idUsuario, Long idRol, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE usuario_rol SET activo = 0, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_usuario = ? AND id_rol = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idUsuario);
            ps.setLong(3, idRol);
            ps.executeUpdate();
        }
    }

    private void activarOCrearRol(Connection conn, Long idUsuario, Long idRol, Long idUsuarioActual) throws SQLException {
        if (tieneRolActivo(conn, idUsuario, idRol)) {
            return;
        }
        if (reactivarRol(conn, idUsuario, idRol, idUsuarioActual)) {
            return;
        }
        insertarRol(conn, idUsuario, idRol, idUsuarioActual);
    }

    private boolean tieneRolActivo(Connection conn, Long idUsuario, Long idRol) throws SQLException {
        String sql = "SELECT 1 FROM usuario_rol WHERE id_usuario = ? AND id_rol = ? AND activo = 1 AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ps.setLong(2, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean reactivarRol(Connection conn, Long idUsuario, Long idRol, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE usuario_rol SET activo = 1, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_usuario = ? AND id_rol = ? AND activo = 0 AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idUsuario);
            ps.setLong(3, idRol);
            return ps.executeUpdate() > 0;
        }
    }

    private void insertarRol(Connection conn, Long idUsuario, Long idRol, Long idUsuarioActual) throws SQLException {
        String sql = "INSERT INTO usuario_rol (id_usuario, id_rol, activo, creado_por, creado_en) "
                + "VALUES (?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ps.setLong(2, idRol);
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
}
