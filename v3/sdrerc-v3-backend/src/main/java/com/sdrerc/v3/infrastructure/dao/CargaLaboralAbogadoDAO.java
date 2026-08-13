package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.application.CalendarioLaboralService;
import com.sdrerc.v3.domain.CargaLaboralAbogadoDTO;
import com.sdrerc.v3.domain.CargaLaboralDocumentoDTO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Subconjunto de com.sdrerc.infrastructure.sdrercapp.dao.UsuarioAsignacionDAO (V2). Originalmente
 * (Fase 1) solo tenía {@code listarCargaLaboralAbogados} (para el gráfico "Carga por abogado" del
 * Dashboard); en este incremento (Fase 3, sub-pestaña "Carga Abogados" de Asignación) se agregan
 * {@link #listarDocumentosPorAbogado} (panel de detalle al doble clic) y
 * {@link #listarIdsUsuarioConVencimientoEnRango} (filtro de fecha del buscador). Mismo SQL que V2,
 * solo cambia el origen de la conexión. El resto de {@code UsuarioAsignacionDAO} (resolución de
 * usuario actual por username, ids de equipo del usuario) sigue sin portarse — no hace falta,
 * {@code CurrentUser} ya trae esos datos directo del JWT.
 */
@Repository
public class CargaLaboralAbogadoDAO {

    private final DataSource dataSource;
    private final CalendarioLaboralService calendarioLaboralService;

    public CargaLaboralAbogadoDAO(DataSource dataSource, CalendarioLaboralService calendarioLaboralService) {
        this.dataSource = dataSource;
        this.calendarioLaboralService = calendarioLaboralService;
    }

    public List<CargaLaboralAbogadoDTO> listarCargaLaboralAbogados(Long idEquipo) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.id_usuario, u.nombre_completo AS abogado, ");
        sql.append("(SELECT MAX(sup2.nombre_completo) FROM usuario_supervision us2 ");
        sql.append("   JOIN usuario sup2 ON sup2.id_usuario = us2.id_supervisor AND sup2.activo = 1 ");
        sql.append("  WHERE us2.id_abogado = u.id_usuario AND us2.activo = 1) AS supervisor, ");
        sql.append(subconsultaAnalisisPorRecibir());
        sql.append(subconsultaAnalisisEnProceso());
        sql.append(subconsultaAnalisisObservado());
        sql.append(subconsultaAnalisisCartaIntermedia());
        sql.append(subconsultaConteoPorEtapa("VERIFICACION", "en_verificacion"));
        sql.append(subconsultaConteoPorEtapa("EJECUCION", "en_ejecucion"));
        sql.append("(SELECT COUNT(*) FROM expediente e ");
        sql.append(" JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 ");
        sql.append(" WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 ");
        sql.append(" AND ea.id_usuario_asignado = u.id_usuario ");
        sql.append(" AND e.fecha_vencimiento IS NOT NULL ");
        sql.append(" AND TRUNC(e.fecha_vencimiento) BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 5) AS por_vencer, ");
        sql.append("(SELECT COUNT(*) FROM expediente e ");
        sql.append(" JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 ");
        sql.append(" WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 ");
        sql.append(" AND ea.id_usuario_asignado = u.id_usuario ");
        sql.append(" AND e.fecha_vencimiento IS NOT NULL AND TRUNC(e.fecha_vencimiento) < TRUNC(SYSDATE)) AS vencidos ");
        sql.append("FROM usuario u ");
        sql.append("WHERE u.activo = 1 ");
        sql.append("AND UPPER(u.estado) = 'ACTIVO' ");
        sql.append("AND EXISTS (SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1 ");
        sql.append("  WHERE ur.id_usuario = u.id_usuario AND ur.activo = 1 ");
        sql.append("    AND UPPER(r.codigo) IN ('ABOGADO', 'ANALISTA')) ");
        if (idEquipo != null) {
            sql.append("AND EXISTS (SELECT 1 FROM equipo_usuario eu3 ");
            sql.append("  WHERE eu3.id_usuario = u.id_usuario AND eu3.activo = 1 AND eu3.id_equipo = ?) ");
            params.add(idEquipo);
        }
        sql.append("ORDER BY (analisis_por_recibir + analisis_en_proceso + analisis_observado + analisis_carta_intermedia ");
        sql.append("+ en_verificacion + en_ejecucion) ASC, vencidos ASC, u.nombre_completo ASC");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<CargaLaboralAbogadoDTO> cargas = new ArrayList<>();
                while (rs.next()) {
                    cargas.add(new CargaLaboralAbogadoDTO(
                            getLongOrNull(rs, "id_usuario"),
                            rs.getString("abogado"),
                            rs.getString("supervisor"),
                            rs.getInt("analisis_por_recibir"),
                            rs.getInt("analisis_en_proceso"),
                            rs.getInt("analisis_observado"),
                            rs.getInt("analisis_carta_intermedia"),
                            rs.getInt("en_verificacion"),
                            rs.getInt("en_ejecucion"),
                            rs.getInt("por_vencer"),
                            rs.getInt("vencidos")));
                }
                return cargas;
            }
        }
    }

    private static String subconsultaConteoPorEtapa(String codigoEtapa, String alias) {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario AND et.codigo = '" + codigoEtapa + "') AS " + alias + ", ";
    }

    private static final String CONDICION_CARTA_INTERMEDIA_RESPONDIDA =
            "EXISTS (SELECT 1 FROM expediente_documento_analizado da "
                    + "JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                    + "WHERE da.id_expediente = e.id_expediente AND da.activo = 1 "
                    + "AND UPPER(NVL(tda.clasificacion, '')) = 'INTERMEDIO' "
                    + "AND NVL(da.requiere_respuesta, 0) = 1 AND NVL(da.notificado, 0) = 1 "
                    + "AND da.confirmacion_respuesta IS NOT NULL)";

    private static String subconsultaAnalisisPorRecibir() {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario "
                + "AND et.codigo = 'ASIGNACION' AND es.codigo = 'ASIGNADO') AS analisis_por_recibir, ";
    }

    private static String subconsultaAnalisisObservado() {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario "
                + "AND et.codigo = 'ANALISIS' AND es.codigo = 'OBSERVADO') AS analisis_observado, ";
    }

    private static String subconsultaAnalisisCartaIntermedia() {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario "
                + "AND et.codigo = 'ANALISIS' AND es.codigo <> 'OBSERVADO' "
                + "AND " + CONDICION_CARTA_INTERMEDIA_RESPONDIDA + ") AS analisis_carta_intermedia, ";
    }

    private static String subconsultaAnalisisEnProceso() {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario "
                + "AND et.codigo = 'ANALISIS' AND es.codigo <> 'OBSERVADO' "
                + "AND NOT " + CONDICION_CARTA_INTERMEDIA_RESPONDIDA + ") AS analisis_en_proceso, ";
    }

    /**
     * Expedientes de la carga de Análisis de un abogado (mismo alcance que las 4 subcolumnas de
     * {@link #listarCargaLaboralAbogados}: "Por recibir" en ASIGNACION/ASIGNADO, o cualquier
     * expediente en etapa ANALISIS — sin Verificación/Ejecución), para el panel "Detalle de carga"
     * al doble clic sobre una fila.
     */
    public List<CargaLaboralDocumentoDTO> listarDocumentosPorAbogado(Long idUsuario) throws SQLException {
        List<CargaLaboralDocumentoDTO> documentos = new ArrayList<>();
        if (idUsuario == null) {
            return documentos;
        }
        String sql = "SELECT e.id_expediente, e.numero_expediente, et.nombre AS etapa, "
                + "es.nombre AS estado, e.fecha_vencimiento "
                + "FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = ? "
                + "AND ((et.codigo = 'ASIGNACION' AND es.codigo = 'ASIGNADO') "
                + "     OR et.codigo = 'ANALISIS') "
                + "ORDER BY e.fecha_vencimiento ASC NULLS LAST, e.numero_expediente ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date fechaVencimiento = rs.getDate("fecha_vencimiento");
                    Long idExpedienteFila = getLongOrNull(rs, "id_expediente");
                    documentos.add(new CargaLaboralDocumentoDTO(
                            idExpedienteFila,
                            rs.getString("numero_expediente"),
                            rs.getString("etapa"),
                            rs.getString("estado"),
                            fechaVencimiento == null ? null : fechaVencimiento.toLocalDate(),
                            calendarioLaboralService.calcularDiasHabilesRestantes(conn, idExpedienteFila, fechaVencimiento)));
                }
            }
        }
        return documentos;
    }

    /**
     * IDs de abogado con al menos un expediente de carga de Análisis (mismo alcance que
     * {@link #listarDocumentosPorAbogado}) cuya fecha de vencimiento cae en [desde, hasta].
     * Cualquiera de los dos límites puede ser nulo (rango abierto de ese lado). Usado por el
     * filtro de fecha del buscador de la bandeja Carga Abogados.
     */
    public Set<Long> listarIdsUsuarioConVencimientoEnRango(LocalDate desde, LocalDate hasta) throws SQLException {
        Set<Long> ids = new HashSet<>();
        if (desde == null && hasta == null) {
            return ids;
        }
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sql.append("SELECT DISTINCT ea.id_usuario_asignado AS id_usuario FROM expediente e ");
        sql.append("JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 ");
        sql.append("JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual ");
        sql.append("JOIN estado_expediente es ON es.id_estado = e.id_estado_actual ");
        sql.append("WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 ");
        sql.append("AND ((et.codigo = 'ASIGNACION' AND es.codigo = 'ASIGNADO') ");
        sql.append("     OR et.codigo = 'ANALISIS') ");
        sql.append("AND e.fecha_vencimiento IS NOT NULL ");
        if (desde != null) {
            sql.append("AND TRUNC(e.fecha_vencimiento) >= ? ");
            params.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append("AND TRUNC(e.fecha_vencimiento) <= ? ");
            params.add(Date.valueOf(hasta));
        }
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long idUsuario = getLongOrNull(rs, "id_usuario");
                    if (idUsuario != null) {
                        ids.add(idUsuario);
                    }
                }
            }
        }
        return ids;
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
