package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.DashboardConteoDTO;
import com.sdrerc.domain.dto.sdrercapp.DashboardResumenDTO;
import com.sdrerc.domain.dto.sdrercapp.DashboardTendenciaMensualDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agregados de solo lectura para el modulo Dashboard (reportes gerenciales, exclusivo
 * ADMIN_SISTEMA). Reutiliza las mismas tablas/columnas ya usadas por las bandejas
 * operativas (EXPEDIENTE, ETAPA_EXPEDIENTE, ESTADO_EXPEDIENTE, EXPEDIENTE_EVALUACION,
 * EXPEDIENTE_HISTORIAL, EXPEDIENTE_NOTIFICACION); no crea tablas ni columnas nuevas.
 * No aplica VisibilidadBandejaSql: al ser exclusivo de ADMIN_SISTEMA siempre agrega
 * sobre el universo completo de expedientes.
 */
public class DashboardDAO {

    /**
     * KPIs globales. "Vencidos"/"Por vencer" usan el mismo criterio de dias calendario
     * (no dias habiles) ya usado por UsuarioAsignacionDAO.listarCargaLaboralAbogados,
     * para no mostrar un numero distinto al que ya ve un supervisor en Carga Abogados.
     */
    public DashboardResumenDTO obtenerResumen(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM expediente e WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0) AS activos, "
                + "(SELECT COUNT(*) FROM expediente e WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + " AND e.fecha_vencimiento IS NOT NULL AND TRUNC(e.fecha_vencimiento) < TRUNC(SYSDATE)) AS vencidos, "
                + "(SELECT COUNT(*) FROM expediente e WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + " AND e.fecha_vencimiento IS NOT NULL "
                + " AND TRUNC(e.fecha_vencimiento) BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 5) AS por_vencer, "
                + "(SELECT COUNT(*) FROM expediente e WHERE e.activo = 1 "
                + " AND e.fecha_registro IS NOT NULL AND TRUNC(e.fecha_registro) BETWEEN ? AND ?) AS ingresados_periodo, "
                + "(SELECT COUNT(*) FROM expediente_historial h JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = h.id_tipo_movimiento "
                + " WHERE h.activo = 1 AND tm.codigo = 'CIERRE' AND TRUNC(h.fecha_movimiento) BETWEEN ? AND ?) AS cerrados_periodo "
                + "FROM dual";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            ps.setDate(3, Date.valueOf(desde));
            ps.setDate(4, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DashboardResumenDTO(
                            rs.getInt("activos"),
                            rs.getInt("vencidos"),
                            rs.getInt("por_vencer"),
                            rs.getInt("ingresados_periodo"),
                            rs.getInt("cerrados_periodo"));
                }
            }
        }
        return new DashboardResumenDTO(0, 0, 0, 0, 0);
    }

    /** Foto actual (no depende del rango de fechas): expedientes activos agrupados por etapa. */
    public List<DashboardConteoDTO> listarExpedientesPorEtapa() throws SQLException {
        String sql = "SELECT et.nombre AS etiqueta, COUNT(*) AS total "
                + "FROM expediente e "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "GROUP BY et.nombre "
                + "ORDER BY COUNT(*) DESC";
        return listarConteos(sql, null, null);
    }

    /**
     * Resultado de analisis registrado dentro del periodo (cuenta el evento de evaluacion,
     * no el expediente distinto): mide cuantos analisis se resolvieron con cada resultado en
     * el rango, util para una lectura gerencial de "produccion" mes a mes, no solo "estado
     * actual".
     */
    public List<DashboardConteoDTO> listarResultadosAnalisis(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT NVL(tre.nombre, 'Sin resultado') AS etiqueta, COUNT(*) AS total "
                + "FROM expediente_evaluacion ev "
                + "LEFT JOIN tipo_resultado_evaluacion tre ON tre.id_tipo_resultado_evaluacion = ev.id_tipo_resultado_evaluacion "
                + "WHERE ev.activo = 1 AND ev.fecha_evaluacion IS NOT NULL "
                + "AND TRUNC(ev.fecha_evaluacion) BETWEEN ? AND ? "
                + "GROUP BY NVL(tre.nombre, 'Sin resultado') "
                + "ORDER BY COUNT(*) DESC";
        return listarConteos(sql, desde, hasta);
    }

    /**
     * Estado final de notificacion (mismas 4 categorias y misma logica que
     * DocumentoAnalisisDAO.ESTADO_FINAL_NOTIFICACION_SQL, aqui agregada en vez de por
     * documento) sobre el universo de documentos emitidos INTERMEDIO/FINAL, foto actual.
     */
    public List<DashboardConteoDTO> listarEstadoFinalNotificacion() throws SQLException {
        String sql = "SELECT CASE "
                + "WHEN total_intentos = 0 THEN 'Por notificar' "
                + "WHEN max_exitosa = 1 THEN 'Atendido' "
                + "WHEN fallida_1 = 1 AND fallida_2 = 1 THEN 'Por publicar' "
                + "ELSE 'Pendiente' END AS etiqueta, "
                + "COUNT(*) AS total "
                + "FROM ("
                + "  SELECT da.id_documento_analizado, "
                + "    COUNT(n.id_expediente_notificacion) AS total_intentos, "
                + "    MAX(CASE WHEN en.codigo = 'EXITOSA' THEN 1 ELSE 0 END) AS max_exitosa, "
                + "    MAX(CASE WHEN n.numero_intento = 1 AND en.codigo = 'FALLIDA' THEN 1 ELSE 0 END) AS fallida_1, "
                + "    MAX(CASE WHEN n.numero_intento = 2 AND en.codigo = 'FALLIDA' THEN 1 ELSE 0 END) AS fallida_2 "
                + "  FROM expediente_documento_analizado da "
                + "  JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                + "  JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                + "  LEFT JOIN expediente_notificacion n ON n.id_documento_analizado = da.id_documento_analizado AND n.activo = 1 "
                + "  LEFT JOIN estado_notificacion en ON en.id_estado_notificacion = n.id_estado_notificacion "
                + "  WHERE da.activo = 1 AND UPPER(NVL(tda.clasificacion, '')) IN ('INTERMEDIO', 'FINAL') "
                + "  AND UPPER(NVL(ed.codigo, '')) = 'EMITIDO' "
                + "  GROUP BY da.id_documento_analizado"
                + ") t "
                + "GROUP BY CASE "
                + "WHEN total_intentos = 0 THEN 'Por notificar' "
                + "WHEN max_exitosa = 1 THEN 'Atendido' "
                + "WHEN fallida_1 = 1 AND fallida_2 = 1 THEN 'Por publicar' "
                + "ELSE 'Pendiente' END";
        return listarConteos(sql, null, null);
    }

    private List<DashboardConteoDTO> listarConteos(String sql, LocalDate desde, LocalDate hasta) throws SQLException {
        List<DashboardConteoDTO> items = new ArrayList<DashboardConteoDTO>();
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (desde != null) {
                ps.setDate(1, Date.valueOf(desde));
                ps.setDate(2, Date.valueOf(hasta));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new DashboardConteoDTO(rs.getString("etiqueta"), rs.getInt("total")));
                }
            }
        }
        return items;
    }

    /**
     * Tendencia mensual ingresados vs. cerrados. Ejecuta 2 consultas GROUP BY TRUNC(fecha,'MM')
     * y las combina en memoria por mes, rellenando con 0 los meses del rango sin movimiento
     * (para que el grafico de linea no tenga huecos).
     */
    public List<DashboardTendenciaMensualDTO> listarTendenciaMensual(LocalDate desde, LocalDate hasta) throws SQLException {
        Map<YearMonth, int[]> porMes = new LinkedHashMap<YearMonth, int[]>();
        for (YearMonth mes = YearMonth.from(desde); !mes.isAfter(YearMonth.from(hasta)); mes = mes.plusMonths(1)) {
            porMes.put(mes, new int[]{0, 0});
        }
        String sqlIngresados = "SELECT TRUNC(e.fecha_registro, 'MM') AS mes, COUNT(*) AS total "
                + "FROM expediente e "
                + "WHERE e.activo = 1 AND e.fecha_registro IS NOT NULL "
                + "AND TRUNC(e.fecha_registro) BETWEEN ? AND ? "
                + "GROUP BY TRUNC(e.fecha_registro, 'MM')";
        String sqlCerrados = "SELECT TRUNC(h.fecha_movimiento, 'MM') AS mes, COUNT(*) AS total "
                + "FROM expediente_historial h "
                + "JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = h.id_tipo_movimiento "
                + "WHERE h.activo = 1 AND tm.codigo = 'CIERRE' "
                + "AND TRUNC(h.fecha_movimiento) BETWEEN ? AND ? "
                + "GROUP BY TRUNC(h.fecha_movimiento, 'MM')";
        try (Connection conn = SdrercAppConnection.getConnection()) {
            acumularPorMes(conn, sqlIngresados, desde, hasta, porMes, 0);
            acumularPorMes(conn, sqlCerrados, desde, hasta, porMes, 1);
        }
        List<DashboardTendenciaMensualDTO> resultado = new ArrayList<DashboardTendenciaMensualDTO>();
        for (Map.Entry<YearMonth, int[]> entry : porMes.entrySet()) {
            resultado.add(new DashboardTendenciaMensualDTO(
                    entry.getKey().atDay(1), entry.getValue()[0], entry.getValue()[1]));
        }
        return resultado;
    }

    private void acumularPorMes(
            Connection conn, String sql, LocalDate desde, LocalDate hasta, Map<YearMonth, int[]> porMes, int indice)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(desde));
            ps.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate mes = toLocalDate(rs.getDate("mes"));
                    if (mes == null) {
                        continue;
                    }
                    YearMonth ym = YearMonth.from(mes);
                    int[] valores = porMes.get(ym);
                    if (valores != null) {
                        valores[indice] = rs.getInt("total");
                    }
                }
            }
        }
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
