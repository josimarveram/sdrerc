package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.domain.ExpedienteTimelineDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Línea de tiempo completa de un expediente (subpestaña "Trazabilidad" de la nueva Bandeja de
 * Expedientes) — consulta {@code EXPEDIENTE_HISTORIAL} con los MISMOS joins que la vista ya
 * existente {@code vw_expediente_timeline} (V2, {@code 10_vistas_bandejas_consola.sql}, ya
 * ejecutada), pero sin depender de esa vista: se rearma el SQL directo para poder exponer 2
 * columnas que la vista no trae ({@code TABLA_RELACIONADA}/{@code ID_REGISTRO_RELACIONADO}) más el
 * nombre de {@code EQUIPO_DESTINO} — ver Javadoc de {@link ExpedienteTimelineDTO} para el porqué de
 * cada mejora. Confirmado contra Oracle real (solo lectura) antes de escribir esta clase: la vista
 * y las 21 columnas de {@code EXPEDIENTE_HISTORIAL} ya existen, con 419 filas reales activas.
 *
 * <p>Sin filtro de visibilidad por asignación (V2 tampoco lo tiene en su consola de expediente):
 * el objetivo explícito de este módulo es que cualquier usuario con acceso a la Bandeja de
 * Expedientes vea la trazabilidad completa, sin importar si el expediente le fue asignado a él.</p>
 */
@Repository
public class ExpedienteTrazabilidadDAO {

    private final DataSource dataSource;

    public ExpedienteTrazabilidadDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ExpedienteTimelineDTO> listarPorExpediente(Long idExpediente) throws SQLException {
        List<ExpedienteTimelineDTO> timeline = new ArrayList<>();
        if (idExpediente == null) {
            return timeline;
        }
        String sql = "SELECT h.id_expediente_historial, h.id_expediente, h.fecha_movimiento, "
                + "tm.codigo AS movimiento_codigo, tm.nombre AS movimiento_nombre, "
                + "eo.codigo AS etapa_origen_codigo, eo.nombre AS etapa_origen_nombre, "
                + "ed.codigo AS etapa_destino_codigo, ed.nombre AS etapa_destino_nombre, "
                + "so.codigo AS estado_origen_codigo, so.nombre AS estado_origen_nombre, "
                + "sd.codigo AS estado_destino_codigo, sd.nombre AS estado_destino_nombre, "
                + "uo.nombre_completo AS usuario_origen, ud.nombre_completo AS usuario_destino, "
                + "eqd.nombre AS equipo_destino, "
                + "h.comentario, h.motivo, h.tabla_relacionada, h.id_registro_relacionado "
                + "FROM expediente_historial h "
                + "JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = h.id_tipo_movimiento "
                + "LEFT JOIN etapa_expediente eo ON eo.id_etapa = h.id_etapa_origen "
                + "LEFT JOIN etapa_expediente ed ON ed.id_etapa = h.id_etapa_destino "
                + "LEFT JOIN estado_expediente so ON so.id_estado = h.id_estado_origen "
                + "LEFT JOIN estado_expediente sd ON sd.id_estado = h.id_estado_destino "
                + "LEFT JOIN usuario uo ON uo.id_usuario = h.id_usuario_origen "
                + "LEFT JOIN usuario ud ON ud.id_usuario = h.id_usuario_destino "
                + "LEFT JOIN equipo eqd ON eqd.id_equipo = h.id_equipo_destino "
                + "WHERE h.activo = 1 AND h.id_expediente = ? "
                + "ORDER BY h.fecha_movimiento DESC NULLS LAST, h.id_expediente_historial DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    timeline.add(map(rs));
                }
            }
        }
        return timeline;
    }

    private ExpedienteTimelineDTO map(ResultSet rs) throws SQLException {
        return new ExpedienteTimelineDTO(
                getLongOrNull(rs, "id_expediente_historial"),
                getLongOrNull(rs, "id_expediente"),
                toLocalDateTime(rs.getTimestamp("fecha_movimiento")),
                rs.getString("movimiento_codigo"),
                rs.getString("movimiento_nombre"),
                rs.getString("etapa_origen_codigo"),
                rs.getString("etapa_origen_nombre"),
                rs.getString("etapa_destino_codigo"),
                rs.getString("etapa_destino_nombre"),
                rs.getString("estado_origen_codigo"),
                rs.getString("estado_origen_nombre"),
                rs.getString("estado_destino_codigo"),
                rs.getString("estado_destino_nombre"),
                rs.getString("usuario_origen"),
                rs.getString("usuario_destino"),
                rs.getString("equipo_destino"),
                rs.getString("comentario"),
                rs.getString("motivo"),
                rs.getString("tabla_relacionada"),
                getLongOrNull(rs, "id_registro_relacionado"));
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
