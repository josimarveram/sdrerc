package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.EquipoJuridicoDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoJuridicoFiltroDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EquipoJuridicoDAO {

    private static final int DEFAULT_LIMIT = 300;
    private static final int MAX_LIMIT = 1000;
    private static final String ROL_ABOGADO = "ABOGADO";

    private final EquipoMiembroDAO equipoMiembroDAO;

    public EquipoJuridicoDAO() {
        this(new EquipoMiembroDAO());
    }

    public EquipoJuridicoDAO(EquipoMiembroDAO equipoMiembroDAO) {
        this.equipoMiembroDAO = equipoMiembroDAO;
    }

    public List<EquipoJuridicoDTO> buscar(EquipoJuridicoFiltroDTO filtro) throws SQLException {
        EquipoJuridicoFiltroDTO filtroSeguro = filtro == null ? new EquipoJuridicoFiltroDTO() : filtro;
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append(selectEquiposBase());
        sql.append("WHERE 1 = 1 ");

        if (hasText(filtroSeguro.getTexto())) {
            sql.append("AND (UPPER(NVL(e.codigo, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(e.nombre, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(e.descripcion, '')) LIKE ?) ");
            String pattern = "%" + filtroSeguro.getTexto().trim().toUpperCase(Locale.ROOT) + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (filtroSeguro.getActivo() != null) {
            sql.append("AND e.activo = ? ");
            params.add(filtroSeguro.getActivo() ? 1 : 0);
        }

        if (filtroSeguro.getIdArea() != null) {
            sql.append("AND e.id_area = ? ");
            params.add(filtroSeguro.getIdArea());
        }

        if (filtroSeguro.getIdSupervisor() != null) {
            sql.append("AND EXISTS (SELECT 1 FROM equipo_usuario euf ");
            sql.append("WHERE euf.id_equipo = e.id_equipo AND euf.id_usuario = ? ");
            sql.append("AND euf.activo = 1 AND euf.es_responsable = 1) ");
            params.add(filtroSeguro.getIdSupervisor());
        }

        sql.append("ORDER BY e.nombre ASC, e.codigo ASC");
        sql.append(") WHERE ROWNUM <= ?");
        params.add(normalizarLimite(filtroSeguro.getLimite()));

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<EquipoJuridicoDTO> equipos = new ArrayList<>();
                while (rs.next()) {
                    equipos.add(mapEquipo(rs));
                }
                return equipos;
            }
        }
    }

    public EquipoJuridicoDTO obtenerPorId(Long idEquipo) throws SQLException {
        if (idEquipo == null) {
            return null;
        }
        String sql = selectEquiposBase() + "WHERE e.id_equipo = ?";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEquipo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapEquipo(rs) : null;
            }
        }
    }

    public boolean existeCodigo(String codigo, Long excluirIdEquipo) throws SQLException {
        if (!hasText(codigo)) {
            return false;
        }
        StringBuilder sql = new StringBuilder("SELECT 1 FROM equipo WHERE UPPER(codigo) = ? ");
        if (excluirIdEquipo != null) {
            sql.append("AND id_equipo <> ? ");
        }
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, codigo.trim().toUpperCase(Locale.ROOT));
            if (excluirIdEquipo != null) {
                ps.setLong(2, excluirIdEquipo);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public EquipoJuridicoDTO guardarConResponsable(
            EquipoJuridicoDTO equipo,
            Long idResponsable,
            Long idUsuarioActual) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idEquipo = equipo.getIdEquipo();
                if (idEquipo == null) {
                    idEquipo = insertarEquipo(conn, equipo, idUsuarioActual);
                } else {
                    actualizarEquipo(conn, equipo, idUsuarioActual);
                }
                equipoMiembroDAO.sincronizarResponsable(conn, idEquipo, idResponsable, idUsuarioActual);
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return obtenerPorId(idEquipo);
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

    public EquipoJuridicoDTO cambiarActivo(Long idEquipo, boolean activo, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE equipo SET activo = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_equipo = ?";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, activo ? 1 : 0);
            setNullableLong(ps, 2, idUsuarioActual);
            ps.setLong(3, idEquipo);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo cambiar el estado del equipo seleccionado.");
            }
        }
        return obtenerPorId(idEquipo);
    }

    public int contarExpedientesActivosEquipo(Long idEquipo) throws SQLException {
        String sql = "SELECT COUNT(1) AS total "
                + "FROM expediente "
                + "WHERE activo = 1 "
                + "AND NVL(cerrado, 0) = 0 "
                + "AND NVL(archivado, 0) = 0 "
                + "AND id_equipo_responsable_actual = ?";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEquipo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total") : 0;
            }
        }
    }

    private Long insertarEquipo(Connection conn, EquipoJuridicoDTO equipo, Long idUsuarioActual) throws SQLException {
        String sql = "INSERT INTO equipo (id_area, codigo, nombre, descripcion, activo, creado_por, creado_en) "
                + "VALUES (?, ?, ?, ?, ?, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EQUIPO"})) {
            setNullableLong(ps, 1, equipo.getIdArea());
            ps.setString(2, equipo.getCodigo());
            ps.setString(3, equipo.getNombre());
            ps.setString(4, emptyToNull(equipo.getDescripcion()));
            ps.setInt(5, equipo.isActivo() ? 1 : 0);
            setNullableLong(ps, 6, idUsuarioActual);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "equipo");
        }
    }

    private void actualizarEquipo(Connection conn, EquipoJuridicoDTO equipo, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE equipo SET id_area = ?, codigo = ?, nombre = ?, descripcion = ?, activo = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP WHERE id_equipo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableLong(ps, 1, equipo.getIdArea());
            ps.setString(2, equipo.getCodigo());
            ps.setString(3, equipo.getNombre());
            ps.setString(4, emptyToNull(equipo.getDescripcion()));
            ps.setInt(5, equipo.isActivo() ? 1 : 0);
            setNullableLong(ps, 6, idUsuarioActual);
            ps.setLong(7, equipo.getIdEquipo());
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el equipo seleccionado.");
            }
        }
    }

    private String selectEquiposBase() {
        return "SELECT e.id_equipo, e.id_area, e.codigo, e.nombre, e.descripcion, e.activo, "
                + "e.creado_en, e.modificado_en, a.nombre AS area_nombre, "
                + "(SELECT MAX(u.id_usuario) FROM equipo_usuario eu JOIN usuario u ON u.id_usuario = eu.id_usuario "
                + " WHERE eu.id_equipo = e.id_equipo AND eu.activo = 1 AND eu.es_responsable = 1 AND u.activo = 1) AS id_responsable, "
                + "(SELECT MAX(u.nombre_completo) FROM equipo_usuario eu JOIN usuario u ON u.id_usuario = eu.id_usuario "
                + " WHERE eu.id_equipo = e.id_equipo AND eu.activo = 1 AND eu.es_responsable = 1 AND u.activo = 1) AS responsable_nombre, "
                + "(SELECT COUNT(1) FROM equipo_usuario eu JOIN usuario u ON u.id_usuario = eu.id_usuario "
                + " WHERE eu.id_equipo = e.id_equipo AND eu.activo = 1 AND u.activo = 1 AND UPPER(u.estado) = 'ACTIVO') AS miembros_activos, "
                + "(SELECT COUNT(1) FROM equipo_usuario eu JOIN usuario u ON u.id_usuario = eu.id_usuario "
                + " WHERE eu.id_equipo = e.id_equipo AND eu.activo = 1 AND u.activo = 1 AND UPPER(u.estado) = 'ACTIVO' "
                + " AND EXISTS (SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol "
                + " WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 AND r.activo = 1 AND r.codigo = '" + ROL_ABOGADO + "')) AS abogados_activos "
                + "FROM equipo e LEFT JOIN area a ON a.id_area = e.id_area ";
    }

    private EquipoJuridicoDTO mapEquipo(ResultSet rs) throws SQLException {
        EquipoJuridicoDTO dto = new EquipoJuridicoDTO();
        dto.setIdEquipo(getLongOrNull(rs, "id_equipo"));
        dto.setIdArea(getLongOrNull(rs, "id_area"));
        dto.setCodigo(rs.getString("codigo"));
        dto.setNombre(rs.getString("nombre"));
        dto.setDescripcion(rs.getString("descripcion"));
        dto.setActivo(rs.getInt("activo") == 1);
        dto.setAreaNombre(rs.getString("area_nombre"));
        dto.setIdResponsable(getLongOrNull(rs, "id_responsable"));
        dto.setResponsableNombre(rs.getString("responsable_nombre"));
        dto.setMiembrosActivos(rs.getInt("miembros_activos"));
        dto.setAbogadosActivos(rs.getInt("abogados_activos"));
        dto.setCreadoEn(toLocalDateTime(rs.getTimestamp("creado_en")));
        dto.setModificadoEn(toLocalDateTime(rs.getTimestamp("modificado_en")));
        return dto;
    }

    private static int normalizarLimite(int limite) {
        if (limite <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limite, MAX_LIMIT);
    }

    private static void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private static void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private static Long obtenerGeneratedKey(PreparedStatement ps, String entidad) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
        throw new SQLException("No se obtuvo el identificador generado para " + entidad + ".");
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // Mantener el error original.
        }
    }
}
