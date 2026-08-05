package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.AbogadoSupervisadoDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableEquipoDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UsuarioSupervisionDAO {

    public List<UsuarioAsignableEquipoDTO> listarSupervisoresConAbogados() throws SQLException {
        String sql = "SELECT DISTINCT sup.id_usuario, sup.username, sup.nombre_completo, "
                + "(SELECT LISTAGG(r.nombre, ', ') WITHIN GROUP (ORDER BY r.nombre) "
                + " FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol "
                + " WHERE ur.id_usuario = sup.id_usuario AND ur.activo = 1 AND r.activo = 1) AS roles_resumen, "
                + "sup.activo AS usuario_activo "
                + "FROM usuario_supervision us "
                + "JOIN usuario sup ON sup.id_usuario = us.id_supervisor "
                + "WHERE us.activo = 1 "
                + "ORDER BY sup.nombre_completo ASC, sup.username ASC";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<UsuarioAsignableEquipoDTO> supervisores = new ArrayList<>();
            while (rs.next()) {
                UsuarioAsignableEquipoDTO dto = new UsuarioAsignableEquipoDTO();
                dto.setIdUsuario(getLongOrNull(rs, "id_usuario"));
                dto.setUsername(rs.getString("username"));
                dto.setNombreCompleto(rs.getString("nombre_completo"));
                dto.setRolesResumen(rs.getString("roles_resumen"));
                dto.setSupervisor(true);
                dto.setActivo(rs.getInt("usuario_activo") == 1);
                supervisores.add(dto);
            }
            return supervisores;
        }
    }

    public List<AbogadoSupervisadoDTO> listarAbogadosPorSupervisor(Long idSupervisor) throws SQLException {
        if (idSupervisor == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT ab.id_usuario, ab.username, ab.nombre_completo, ab.activo AS usuario_activo, "
                + "us.activo AS relacion_activa, us.creado_en AS asignado_en, "
                + "(SELECT LISTAGG(r.nombre, ', ') WITHIN GROUP (ORDER BY r.nombre) "
                + " FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol "
                + " WHERE ur.id_usuario = ab.id_usuario AND ur.activo = 1 AND r.activo = 1) AS roles_resumen, "
                + "(SELECT LISTAGG(eq.nombre, ', ') WITHIN GROUP (ORDER BY eq.nombre) "
                + " FROM equipo_usuario eu JOIN equipo eq ON eq.id_equipo = eu.id_equipo "
                + " WHERE eu.id_usuario = ab.id_usuario AND eu.activo = 1 AND eq.activo = 1) AS equipos_resumen "
                + "FROM usuario_supervision us "
                + "JOIN usuario ab ON ab.id_usuario = us.id_abogado "
                + "WHERE us.id_supervisor = ? "
                + "ORDER BY us.activo DESC, ab.nombre_completo ASC, ab.username ASC";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idSupervisor);
            try (ResultSet rs = ps.executeQuery()) {
                List<AbogadoSupervisadoDTO> abogados = new ArrayList<>();
                while (rs.next()) {
                    AbogadoSupervisadoDTO dto = new AbogadoSupervisadoDTO();
                    dto.setIdUsuario(getLongOrNull(rs, "id_usuario"));
                    dto.setUsername(rs.getString("username"));
                    dto.setNombreCompleto(rs.getString("nombre_completo"));
                    dto.setRolesResumen(rs.getString("roles_resumen"));
                    dto.setEquiposResumen(rs.getString("equipos_resumen"));
                    dto.setUsuarioActivo(rs.getInt("usuario_activo") == 1);
                    dto.setRelacionActiva(rs.getInt("relacion_activa") == 1);
                    dto.setAsignadoEn(toLocalDateTime(rs.getTimestamp("asignado_en")));
                    abogados.add(dto);
                }
                return abogados;
            }
        }
    }

    public void quitarAbogado(Long idSupervisor, Long idAbogado, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE usuario_supervision SET activo = 0, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_supervisor = ? AND id_abogado = ? AND activo = 1";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, idUsuarioActual);
            ps.setLong(2, idSupervisor);
            ps.setLong(3, idAbogado);
            int updated = ps.executeUpdate();
            if (updated < 1) {
                throw new SQLException("El abogado no está asignado actualmente a ese supervisor.");
            }
        }
    }

    public void agregarAbogado(Long idSupervisor, Long idAbogado, Long idUsuarioActual) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            String sqlExiste = "SELECT 1 FROM usuario_supervision WHERE id_supervisor = ? AND id_abogado = ? AND activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlExiste)) {
                ps.setLong(1, idSupervisor);
                ps.setLong(2, idAbogado);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return;
                    }
                }
            }
            String sqlReactivar = "UPDATE usuario_supervision SET activo = 1, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                    + "WHERE id_usuario_supervision = (SELECT MIN(id_usuario_supervision) FROM usuario_supervision "
                    + "WHERE id_supervisor = ? AND id_abogado = ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlReactivar)) {
                setNullableLong(ps, 1, idUsuarioActual);
                ps.setLong(2, idSupervisor);
                ps.setLong(3, idAbogado);
                if (ps.executeUpdate() > 0) {
                    return;
                }
            }
            String sqlInsertar = "INSERT INTO usuario_supervision (id_supervisor, id_abogado, activo, creado_por, creado_en) "
                    + "VALUES (?, ?, 1, ?, SYSTIMESTAMP)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertar)) {
                ps.setLong(1, idSupervisor);
                ps.setLong(2, idAbogado);
                setNullableLong(ps, 3, idUsuarioActual);
                ps.executeUpdate();
            }
        }
    }

    private static void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
