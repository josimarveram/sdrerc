package com.sdrerc.infrastructure.sdrercapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class UsuarioEquipoDAO {

    public void sincronizarEquipo(Connection conn, Long idUsuario, Long idEquipo, Long idUsuarioActual) throws SQLException {
        if (idEquipo == null) {
            desactivarEquiposActivos(conn, idUsuario, idUsuarioActual);
            return;
        }
        validarEquipoActivo(conn, idEquipo);
        desactivarOtrosEquipos(conn, idUsuario, idEquipo, idUsuarioActual);
        if (tieneEquipoActivo(conn, idUsuario, idEquipo)) {
            return;
        }
        if (reactivarEquipo(conn, idUsuario, idEquipo, idUsuarioActual)) {
            return;
        }
        insertarEquipo(conn, idUsuario, idEquipo, idUsuarioActual);
    }

    private void validarEquipoActivo(Connection conn, Long idEquipo) throws SQLException {
        String sql = "SELECT 1 FROM equipo WHERE id_equipo = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEquipo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El equipo seleccionado no existe o no está activo.");
                }
            }
        }
    }

    private void desactivarEquiposActivos(Connection conn, Long idUsuario, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE equipo_usuario SET activo = 0, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_usuario = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idUsuario);
            ps.executeUpdate();
        }
    }

    private void desactivarOtrosEquipos(Connection conn, Long idUsuario, Long idEquipo, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE equipo_usuario SET activo = 0, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_usuario = ? AND id_equipo <> ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idUsuario);
            ps.setLong(3, idEquipo);
            ps.executeUpdate();
        }
    }

    private boolean tieneEquipoActivo(Connection conn, Long idUsuario, Long idEquipo) throws SQLException {
        String sql = "SELECT 1 FROM equipo_usuario WHERE id_usuario = ? AND id_equipo = ? AND activo = 1 AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ps.setLong(2, idEquipo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean reactivarEquipo(Connection conn, Long idUsuario, Long idEquipo, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE equipo_usuario SET activo = 1, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_usuario = ? AND id_equipo = ? AND activo = 0 AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idUsuario);
            ps.setLong(3, idEquipo);
            return ps.executeUpdate() > 0;
        }
    }

    private void insertarEquipo(Connection conn, Long idUsuario, Long idEquipo, Long idUsuarioActual) throws SQLException {
        String sql = "INSERT INTO equipo_usuario (id_equipo, id_usuario, es_responsable, activo, creado_por, creado_en) "
                + "VALUES (?, ?, 0, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEquipo);
            ps.setLong(2, idUsuario);
            setNullableLong(ps, 3, idUsuarioActual);
            ps.executeUpdate();
        }
    }

    private static void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }
}
