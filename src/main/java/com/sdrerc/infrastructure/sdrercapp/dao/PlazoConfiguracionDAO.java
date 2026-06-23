package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.PlazoConfiguracionDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
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

public class PlazoConfiguracionDAO {

    private static final int DEFAULT_LIMIT = 300;
    private static final int MAX_LIMIT = 1000;

    public PlazoConfiguracionDTO obtenerPlazoSolicitud(Connection conn) throws SQLException {
        PlazoConfiguracionDTO plazo = obtenerPlazoPorCodigo(conn, PlazoConfiguracionDTO.CODIGO_SOLICITUD_SDRERC);
        if (plazo != null) {
            return plazo;
        }

        Integer dias = obtenerDiasPlazoSolicitudLegacy(conn);
        if (dias == null || dias.intValue() <= 0) {
            return null;
        }
        PlazoConfiguracionDTO fallback = new PlazoConfiguracionDTO();
        fallback.setDiasPlazo(dias);
        fallback.setUnidadPlazo(PlazoConfiguracionDTO.UNIDAD_HABILES);
        fallback.setActivo(true);
        return fallback;
    }

    public PlazoConfiguracionDTO obtenerPlazoPorCodigo(Connection conn, String codigo) throws SQLException {
        if (!hasText(codigo)) {
            return null;
        }
        try {
            String sql = "SELECT * FROM ("
                    + baseSelect()
                    + " WHERE pc.activo = 1 "
                    + " AND UPPER(NVL(pc.codigo, pc.ambito)) = ? "
                    + " AND (pc.fecha_vigencia_desde IS NULL OR TRUNC(pc.fecha_vigencia_desde) <= TRUNC(SYSDATE)) "
                    + " AND (pc.fecha_vigencia_hasta IS NULL OR TRUNC(pc.fecha_vigencia_hasta) >= TRUNC(SYSDATE)) "
                    + " ORDER BY CASE WHEN pc.fecha_vigencia_desde IS NULL THEN 1 ELSE 0 END, "
                    + " pc.fecha_vigencia_desde DESC NULLS LAST, pc.id_plazo_configuracion DESC"
                    + ") WHERE ROWNUM = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, codigo.trim().toUpperCase(Locale.ROOT));
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? map(rs) : null;
                }
            }
        } catch (SQLException ex) {
            if (esColumnaNoExiste(ex)) {
                return null;
            }
            throw ex;
        }
    }

    public Integer obtenerDiasPlazoSolicitud(Connection conn) throws SQLException {
        PlazoConfiguracionDTO plazo = obtenerPlazoSolicitud(conn);
        return plazo == null ? null : plazo.getDiasPlazo();
    }

    public List<PlazoConfiguracionDTO> buscar(String textoLibre, Boolean activo, int limite) throws SQLException {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append(baseSelect());
        sql.append(" WHERE 1 = 1 ");
        if (activo != null) {
            sql.append("AND pc.activo = ? ");
            params.add(activo.booleanValue() ? 1 : 0);
        }
        if (hasText(textoLibre)) {
            String pattern = "%" + textoLibre.trim().toUpperCase(Locale.ROOT) + "%";
            sql.append("AND (UPPER(NVL(pc.codigo, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(pc.nombre, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(pc.ambito, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(et.codigo, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(td.codigo, '')) LIKE ?) ");
            for (int i = 0; i < 5; i++) {
                params.add(pattern);
            }
        }
        sql.append("ORDER BY pc.activo DESC, pc.codigo ASC NULLS LAST, pc.id_plazo_configuracion DESC");
        sql.append(") WHERE ROWNUM <= ?");
        params.add(normalizarLimite(limite));

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<PlazoConfiguracionDTO> plazos = new ArrayList<PlazoConfiguracionDTO>();
                while (rs.next()) {
                    plazos.add(map(rs));
                }
                return plazos;
            }
        }
    }

    public boolean existeCodigoActivo(Connection conn, String codigo, Long excluirId) throws SQLException {
        if (!hasText(codigo)) {
            return false;
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT 1 FROM plazo_configuracion WHERE activo = 1 AND UPPER(codigo) = ? ");
        if (excluirId != null) {
            sql.append("AND id_plazo_configuracion <> ? ");
        }
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, codigo.trim().toUpperCase(Locale.ROOT));
            if (excluirId != null) {
                ps.setLong(2, excluirId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public PlazoConfiguracionDTO obtenerPorId(Connection conn, Long idPlazoConfiguracion) throws SQLException {
        if (idPlazoConfiguracion == null) {
            return null;
        }
        String sql = baseSelect() + " WHERE pc.id_plazo_configuracion = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idPlazoConfiguracion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public PlazoConfiguracionDTO insertar(Connection conn, PlazoConfiguracionDTO plazo, Long idUsuario) throws SQLException {
        String sql = "INSERT INTO plazo_configuracion ("
                + "codigo, nombre, ambito, id_etapa, id_tipo_documento, dias_plazo, unidad_plazo, "
                + "fecha_vigencia_desde, fecha_vigencia_hasta, activo, observacion, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_PLAZO_CONFIGURACION"})) {
            bindEditable(ps, plazo);
            setLongOrNull(ps, 12, idUsuario);
            ps.executeUpdate();
            Long id = obtenerGeneratedKey(ps, "plazo_configuracion");
            return obtenerPorId(conn, id);
        }
    }

    public PlazoConfiguracionDTO actualizar(Connection conn, PlazoConfiguracionDTO plazo, Long idUsuario) throws SQLException {
        String sql = "UPDATE plazo_configuracion SET "
                + "codigo = ?, nombre = ?, ambito = ?, id_etapa = ?, id_tipo_documento = ?, dias_plazo = ?, unidad_plazo = ?, "
                + "fecha_vigencia_desde = ?, fecha_vigencia_hasta = ?, activo = ?, observacion = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_plazo_configuracion = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindEditable(ps, plazo);
            setLongOrNull(ps, 12, idUsuario);
            ps.setLong(13, plazo.getIdPlazoConfiguracion());
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar la configuración de plazo seleccionada.");
            }
        }
        return obtenerPorId(conn, plazo.getIdPlazoConfiguracion());
    }

    public PlazoConfiguracionDTO cambiarActivo(Connection conn, Long idPlazoConfiguracion, boolean activo, Long idUsuario) throws SQLException {
        String sql = "UPDATE plazo_configuracion SET activo = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_plazo_configuracion = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, activo ? 1 : 0);
            setLongOrNull(ps, 2, idUsuario);
            ps.setLong(3, idPlazoConfiguracion);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo cambiar el estado de la configuración seleccionada.");
            }
        }
        return obtenerPorId(conn, idPlazoConfiguracion);
    }

    private Integer obtenerDiasPlazoSolicitudLegacy(Connection conn) throws SQLException {
        String sql = "SELECT dias_plazo FROM ("
                + "SELECT pc.dias_plazo "
                + "FROM plazo_configuracion pc "
                + "LEFT JOIN etapa_expediente et ON et.id_etapa = pc.id_etapa "
                + "WHERE pc.activo = 1 "
                + "AND (pc.id_etapa IS NULL OR UPPER(et.codigo) = 'REGISTRO') "
                + "ORDER BY CASE WHEN UPPER(et.codigo) = 'REGISTRO' THEN 0 ELSE 1 END, "
                + "CASE WHEN pc.id_tipo_documento IS NULL THEN 0 ELSE 1 END, "
                + "pc.id_plazo_configuracion"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            int dias = rs.getInt("dias_plazo");
            return rs.wasNull() ? null : Integer.valueOf(dias);
        }
    }

    private static String baseSelect() {
        return "SELECT pc.id_plazo_configuracion, pc.codigo, pc.nombre, pc.ambito, "
                + "pc.id_etapa, et.codigo AS etapa_codigo, et.nombre AS etapa_nombre, "
                + "pc.id_tipo_documento, td.codigo AS tipo_documento_codigo, td.nombre AS tipo_documento_nombre, "
                + "pc.dias_plazo, pc.unidad_plazo, pc.fecha_vigencia_desde, pc.fecha_vigencia_hasta, "
                + "pc.activo, pc.observacion, pc.creado_en, pc.modificado_en "
                + "FROM plazo_configuracion pc "
                + "LEFT JOIN etapa_expediente et ON et.id_etapa = pc.id_etapa "
                + "LEFT JOIN tipo_documento td ON td.id_tipo_documento = pc.id_tipo_documento ";
    }

    private void bindEditable(PreparedStatement ps, PlazoConfiguracionDTO plazo) throws SQLException {
        ps.setString(1, emptyToNull(plazo.getCodigo()));
        ps.setString(2, emptyToNull(plazo.getNombre()));
        ps.setString(3, emptyToNull(plazo.getAmbito()));
        setLongOrNull(ps, 4, plazo.getIdEtapa());
        setLongOrNull(ps, 5, plazo.getIdTipoDocumento());
        ps.setInt(6, plazo.getDiasPlazo() == null ? 0 : plazo.getDiasPlazo().intValue());
        ps.setString(7, emptyToNull(plazo.getUnidadPlazo()));
        setDateOrNull(ps, 8, plazo.getFechaVigenciaDesde());
        setDateOrNull(ps, 9, plazo.getFechaVigenciaHasta());
        ps.setInt(10, plazo.isActivo() ? 1 : 0);
        ps.setString(11, emptyToNull(plazo.getObservacion()));
    }

    private PlazoConfiguracionDTO map(ResultSet rs) throws SQLException {
        return new PlazoConfiguracionDTO(
                getLongOrNull(rs, "id_plazo_configuracion"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("ambito"),
                getLongOrNull(rs, "id_etapa"),
                rs.getString("etapa_codigo"),
                rs.getString("etapa_nombre"),
                getLongOrNull(rs, "id_tipo_documento"),
                rs.getString("tipo_documento_codigo"),
                rs.getString("tipo_documento_nombre"),
                getIntegerOrNull(rs, "dias_plazo"),
                rs.getString("unidad_plazo"),
                toLocalDate(rs.getDate("fecha_vigencia_desde")),
                toLocalDate(rs.getDate("fecha_vigencia_hasta")),
                rs.getInt("activo") == 1,
                rs.getString("observacion"),
                toLocalDateTime(rs.getTimestamp("creado_en")),
                toLocalDateTime(rs.getTimestamp("modificado_en")));
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

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
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

    private static void setDateOrNull(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(value));
        }
    }

    private static Long obtenerGeneratedKey(PreparedStatement ps, String entidad) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                long value = rs.getLong(1);
                return rs.wasNull() ? null : Long.valueOf(value);
            }
        }
        throw new SQLException("No se obtuvo el identificador generado para " + entidad + ".");
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : Long.valueOf(value);
    }

    private static Integer getIntegerOrNull(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : Integer.valueOf(value);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static boolean esColumnaNoExiste(SQLException ex) {
        return ex != null && (ex.getErrorCode() == 904
                || (ex.getMessage() != null && ex.getMessage().contains("ORA-00904")));
    }
}
