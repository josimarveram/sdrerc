package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.domain.PlazoConfiguracionDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.stereotype.Repository;

/**
 * Subconjunto de com.sdrerc.infrastructure.sdrercapp.dao.PlazoConfiguracionDAO (V2): solo
 * {@code obtenerPlazoSolicitud}/{@code obtenerPlazoPorCodigo}, usados por CalendarioLaboralService
 * al calcular la fecha de vencimiento de una solicitud nueva. No incluye el fallback a una
 * columna legacy pre-PLAZO_CONFIGURACION (tabla ya sembrada con los plazos oficiales, ver
 * CLAUDE.md sección Plazos — ese fallback es código muerto contra la BD real compartida con V2).
 * Si la tabla/columna faltara, se cae al valor por defecto embebido en el propio
 * {@link PlazoConfiguracionDTO} (constructor sin argumentos, 30 días hábiles), igual que V2.
 * Mismo SQL para los métodos portados.
 */
@Repository
public class PlazoConfiguracionDAO {

    public PlazoConfiguracionDTO obtenerPlazoSolicitud(Connection conn) throws SQLException {
        return obtenerPlazoPorCodigo(conn, PlazoConfiguracionDTO.CODIGO_SOLICITUD_SDRERC);
    }

    public PlazoConfiguracionDTO obtenerPlazoPorCodigo(Connection conn, String codigo) throws SQLException {
        if (codigo == null || codigo.trim().isEmpty()) {
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

    private static String baseSelect() {
        return "SELECT pc.id_plazo_configuracion, pc.codigo, pc.nombre, pc.ambito, "
                + "pc.id_etapa, et.codigo AS etapa_codigo, et.nombre AS etapa_nombre, "
                + "pc.id_tipo_documento, td.codigo AS tipo_documento_codigo, td.nombre AS tipo_documento_nombre, "
                + "pc.dias_plazo, pc.porcentaje_verde_desde, pc.porcentaje_amarillo_desde, pc.porcentaje_rojo_desde, "
                + "pc.unidad_plazo, pc.fecha_vigencia_desde, pc.fecha_vigencia_hasta, "
                + "pc.activo, pc.observacion, pc.creado_en, pc.modificado_en "
                + "FROM plazo_configuracion pc "
                + "LEFT JOIN etapa_expediente et ON et.id_etapa = pc.id_etapa "
                + "LEFT JOIN tipo_documento td ON td.id_tipo_documento = pc.id_tipo_documento ";
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
                getIntegerOrNull(rs, "porcentaje_verde_desde"),
                getIntegerOrNull(rs, "porcentaje_amarillo_desde"),
                getIntegerOrNull(rs, "porcentaje_rojo_desde"),
                rs.getString("unidad_plazo"),
                toLocalDate(rs.getDate("fecha_vigencia_desde")),
                toLocalDate(rs.getDate("fecha_vigencia_hasta")),
                rs.getInt("activo") == 1,
                rs.getString("observacion"),
                toLocalDateTime(rs.getTimestamp("creado_en")),
                toLocalDateTime(rs.getTimestamp("modificado_en")));
    }

    private static boolean esColumnaNoExiste(SQLException ex) {
        return ex != null && (ex.getErrorCode() == 904
                || (ex.getMessage() != null && ex.getMessage().contains("ORA-00904")));
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Integer getIntegerOrNull(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDate toLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
