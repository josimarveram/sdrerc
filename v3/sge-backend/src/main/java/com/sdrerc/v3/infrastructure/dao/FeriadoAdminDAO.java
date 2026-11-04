package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.domain.FeriadoAdminDTO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Port literal de {@code com.sdrerc.infrastructure.sdrercapp.dao.FeriadoNacionalDAO} (V2), CRUD
 * completo — Administración &gt; Feriados. Distinto del {@code FeriadoNacionalDAO} (V3) ya
 * existente, que solo expone {@code listarFechasActivas} de solo lectura para
 * {@code CalendarioLaboralService}.
 */
@Repository
public class FeriadoAdminDAO {

    private static final int DEFAULT_LIMIT = 500;
    private static final int MAX_LIMIT = 2000;

    private final DataSource dataSource;

    public FeriadoAdminDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<FeriadoAdminDTO> buscar(Integer anio, Boolean activo, int limite) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("SELECT id_feriado, fecha, nombre, tipo, activo, observacion, creado_en, modificado_en ");
        sql.append("FROM feriado_nacional WHERE 1 = 1 ");
        if (anio != null) {
            sql.append("AND fecha >= ? AND fecha < ? ");
            params.add(Date.valueOf(LocalDate.of(anio, 1, 1)));
            params.add(Date.valueOf(LocalDate.of(anio + 1, 1, 1)));
        }
        if (activo != null) {
            sql.append("AND activo = ? ");
            params.add(activo ? 1 : 0);
        }
        sql.append("ORDER BY fecha ASC, nombre ASC");
        sql.append(") WHERE ROWNUM <= ?");
        params.add(normalizarLimite(limite));

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<FeriadoAdminDTO> feriados = new ArrayList<>();
                while (rs.next()) {
                    feriados.add(map(rs));
                }
                return feriados;
            }
        }
    }

    public boolean existeFecha(LocalDate fecha, String tipo, Long excluirId) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return existeFecha(conn, fecha, tipo, excluirId);
        }
    }

    public boolean existeFecha(Connection conn, LocalDate fecha, String tipo, Long excluirId) throws SQLException {
        if (fecha == null) {
            return false;
        }
        String normalizedTipo = normalizarTipo(tipo);
        StringBuilder sql = new StringBuilder("SELECT 1 FROM feriado_nacional WHERE fecha = ? AND UPPER(tipo) = ? ");
        if (excluirId != null) {
            sql.append("AND id_feriado <> ? ");
        }
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setDate(1, Date.valueOf(fecha));
            ps.setString(2, normalizedTipo.toUpperCase(Locale.ROOT));
            if (excluirId != null) {
                ps.setLong(3, excluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public FeriadoAdminDTO obtenerPorId(Long idFeriado) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return obtenerPorId(conn, idFeriado);
        }
    }

    public FeriadoAdminDTO obtenerPorId(Connection conn, Long idFeriado) throws SQLException {
        if (idFeriado == null) {
            return null;
        }
        String sql = "SELECT id_feriado, fecha, nombre, tipo, activo, observacion, creado_en, modificado_en "
                + "FROM feriado_nacional WHERE id_feriado = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idFeriado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public FeriadoAdminDTO insertar(Connection conn, FeriadoAdminDTO feriado, Long idUsuario) throws SQLException {
        String sql = "INSERT INTO feriado_nacional (fecha, nombre, tipo, activo, observacion, creado_por, creado_en) "
                + "VALUES (?, ?, ?, ?, ?, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_FERIADO"})) {
            ps.setDate(1, Date.valueOf(feriado.getFecha()));
            ps.setString(2, feriado.getNombre());
            ps.setString(3, normalizarTipo(feriado.getTipo()));
            ps.setInt(4, feriado.isActivo() ? 1 : 0);
            ps.setString(5, emptyToNull(feriado.getObservacion()));
            setLongOrNull(ps, 6, idUsuario);
            ps.executeUpdate();
            Long id = obtenerGeneratedKey(ps);
            return obtenerPorId(conn, id);
        }
    }

    public FeriadoAdminDTO actualizar(Connection conn, FeriadoAdminDTO feriado, Long idUsuario) throws SQLException {
        String sql = "UPDATE feriado_nacional SET fecha = ?, nombre = ?, tipo = ?, activo = ?, observacion = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP WHERE id_feriado = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(feriado.getFecha()));
            ps.setString(2, feriado.getNombre());
            ps.setString(3, normalizarTipo(feriado.getTipo()));
            ps.setInt(4, feriado.isActivo() ? 1 : 0);
            ps.setString(5, emptyToNull(feriado.getObservacion()));
            setLongOrNull(ps, 6, idUsuario);
            ps.setLong(7, feriado.getIdFeriado());
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el feriado seleccionado.");
            }
        }
        return obtenerPorId(conn, feriado.getIdFeriado());
    }

    public FeriadoAdminDTO cambiarActivo(Connection conn, Long idFeriado, boolean activo, Long idUsuario) throws SQLException {
        String sql = "UPDATE feriado_nacional SET activo = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP WHERE id_feriado = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, activo ? 1 : 0);
            setLongOrNull(ps, 2, idUsuario);
            ps.setLong(3, idFeriado);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo cambiar el estado del feriado seleccionado.");
            }
        }
        return obtenerPorId(conn, idFeriado);
    }

    public Connection abrirConexion() throws SQLException {
        return dataSource.getConnection();
    }

    private FeriadoAdminDTO map(ResultSet rs) throws SQLException {
        FeriadoAdminDTO dto = new FeriadoAdminDTO();
        dto.setIdFeriado(getLongOrNull(rs, "id_feriado"));
        dto.setFecha(toLocalDate(rs.getDate("fecha")));
        dto.setNombre(rs.getString("nombre"));
        dto.setTipo(rs.getString("tipo"));
        dto.setActivo(rs.getInt("activo") == 1);
        dto.setObservacion(rs.getString("observacion"));
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

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private static String normalizarTipo(String tipo) {
        return tipo == null || tipo.trim().isEmpty() ? "NACIONAL" : tipo.trim().toUpperCase(Locale.ROOT);
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private static Long obtenerGeneratedKey(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
        throw new SQLException("No se obtuvo el identificador generado para el feriado.");
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
