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
        sql.append(subconsultaAnalisisDerivado());
        sql.append(subconsultaConteoPorEtapa("VERIFICACION", "en_verificacion"));
        sql.append(subconsultaConteoPorEtapa("EJECUCION", "en_ejecucion"));
        sql.append("(SELECT COUNT(*) FROM expediente e ");
        sql.append(" JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 ");
        sql.append(" WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 ");
        sql.append(" AND ea.id_usuario_asignado = u.id_usuario ");
        sql.append(" AND e.fecha_vencimiento IS NOT NULL ");
        sql.append(" AND TRUNC(e.fecha_vencimiento) BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 5 ");
        sql.append(CONDICION_CARGA_REAL);
        sql.append(") AS por_vencer, ");
        sql.append("(SELECT COUNT(*) FROM expediente e ");
        sql.append(" JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 ");
        sql.append(" WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 ");
        sql.append(" AND ea.id_usuario_asignado = u.id_usuario ");
        sql.append(" AND e.fecha_vencimiento IS NOT NULL AND TRUNC(e.fecha_vencimiento) < TRUNC(SYSDATE) ");
        sql.append(CONDICION_CARGA_REAL);
        sql.append(") AS vencidos ");
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
        sql.append("ORDER BY (analisis_por_recibir + analisis_en_proceso + analisis_observado + analisis_derivado ");
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
                            rs.getInt("analisis_derivado"),
                            rs.getInt("en_verificacion"),
                            rs.getInt("en_ejecucion"),
                            rs.getInt("por_vencer"),
                            rs.getInt("vencidos")));
                }
                return cargas;
            }
        }
    }

    /**
     * 31/08/2026, pedido explícito del usuario: "duplicados confirmados" (el lado ya asociado de un
     * `EXPEDIENTE_RELACION`, mismo criterio de exclusión que `vw_expediente_asignacion_detalle`,
     * script 99) no debe contarse como carga propia de un abogado — es el mismo caso/documento que
     * su expediente principal, no un expediente adicional que trabajar. Se combina con una segunda
     * condición, también pedida explícitamente: solo cuenta como carga real un expediente que
     * efectivamente pasó por "Generar asignación" con su propia hoja de envío registrada (mismo
     * criterio de "hoja de envío no ingresada" que `AsignacionExpedienteDAO#esHojaEnvioVacia` —
     * vacío o solo un guion "-"). Verificado con `SELECT` de solo lectura contra datos reales antes
     * de aplicar: de 1736 expedientes con asignación activa, 3 eran el lado duplicado (ninguno con
     * hoja de envío propia) y 50 más carecían de hoja de envío sin ser duplicados — el filtro
     * remueve ambos casos por igual, sin depender de cuál de los 2 motivos aplique.
     *
     * <p><b>Ojo con el operador SQL</b>: `TRIM(x) NOT IN ('', '-')` NUNCA es verdadero en Oracle,
     * para ningún valor de `x` — el literal `''` es `NULL` en Oracle, y cualquier comparación
     * `IN`/`NOT IN` contra una lista que contiene `NULL` evalúa a `UNKNOWN` (nunca `TRUE`) por lógica
     * de 3 valores. Confirmado con una prueba real antes de escribir este código: esa forma zombiaba
     * el filtro completo a 0 conteos para TODOS los abogados. La forma correcta es `IS NOT NULL AND
     * TRIM(x) <> '-'` (sin lista `IN`) — un string vacío tras `TRIM` ya es `NULL` en Oracle, así que
     * `IS NOT NULL` ya cubre ese caso sin necesitar comparar contra `''` explícitamente.</p>
     */
    private static final String CONDICION_CARGA_REAL =
            "AND NOT EXISTS (SELECT 1 FROM expediente_relacion r "
                    + "WHERE r.activo = 1 AND r.id_expediente_relacionado = e.id_expediente "
                    + "AND UPPER(r.tipo_relacion) IN ('DOCUMENTO_DUPLICADO_ASOCIADO', 'MISMA_ACTA_TITULAR')) "
                    + "AND ea.numero_hoja_envio IS NOT NULL AND TRIM(ea.numero_hoja_envio) <> '-' ";

    private static String subconsultaConteoPorEtapa(String codigoEtapa, String alias) {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario AND et.codigo = '" + codigoEtapa + "' "
                + CONDICION_CARGA_REAL + ") AS " + alias + ", ";
    }

    private static String subconsultaAnalisisPorRecibir() {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario "
                + "AND et.codigo = 'ASIGNACION' AND es.codigo = 'ASIGNADO' "
                + CONDICION_CARGA_REAL + ") AS analisis_por_recibir, ";
    }

    private static String subconsultaAnalisisObservado() {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario "
                + "AND et.codigo = 'ANALISIS' AND es.codigo = 'OBSERVADO' "
                + CONDICION_CARGA_REAL + ") AS analisis_observado, ";
    }

    /**
     * 31/08/2026, pedido explícito del usuario: la columna "Carta intermedia" pasa a llamarse
     * "Derivado" y a contar directamente el estado REAL `ANALISIS/DERIVADO` (script 96, ya sembrado
     * y con datos reales — verificado con `SELECT` de solo lectura antes de aplicar el cambio), en
     * vez de la heurística anterior (`da.clasificacion='INTERMEDIO' AND requiere_respuesta=1 AND
     * notificado=1 AND confirmacion_respuesta IS NOT NULL`, que aproximaba "carta intermedia ya
     * respondida" sin depender de un estado propio). Mismo criterio que la Bandeja Análisis ya usa
     * para su propio KPI "Derivado" desde el 08/08/2026 (ver `CLAUDE.md` raíz, sección Análisis:
     * "el KPI... ahora se llama 'Derivado' y cuenta/filtra directamente por estadoCodigo=DERIVADO") —
     * esta bandeja se queda alineada con esa misma definición real en vez de su propia heurística.
     */
    private static String subconsultaAnalisisDerivado() {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario "
                + "AND et.codigo = 'ANALISIS' AND es.codigo = 'DERIVADO' "
                + CONDICION_CARGA_REAL + ") AS analisis_derivado, ";
    }

    private static String subconsultaAnalisisEnProceso() {
        return "(SELECT COUNT(*) FROM expediente e "
                + "JOIN expediente_asignacion ea ON ea.id_expediente = e.id_expediente AND ea.activa = 1 AND ea.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 "
                + "AND ea.id_usuario_asignado = u.id_usuario "
                + "AND et.codigo = 'ANALISIS' AND es.codigo NOT IN ('OBSERVADO', 'DERIVADO') "
                + CONDICION_CARGA_REAL + ") AS analisis_en_proceso, ";
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
                + CONDICION_CARGA_REAL
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
     * {@link #listarDocumentosPorAbogado}) cuya Fecha Solicitud (`EXPEDIENTE_SOLICITUD.fecha_recepcion`,
     * mismo campo que ya usa "Fecha Solicitud" en Registro/Asignación) cae en [desde, hasta].
     * Cualquiera de los dos límites puede ser nulo (rango abierto de ese lado). Usado por el
     * filtro de fecha del buscador de la bandeja Carga Abogados.
     *
     * <p>25/08/2026, pedido explícito del usuario: reemplaza el filtro anterior sobre
     * {@code EXPEDIENTE.fecha_vencimiento} — ese campo es por naturaleza futuro (vence semanas/meses
     * adelante), incompatible con el default "01 del mes actual → hoy" que sí tiene sentido para un
     * campo de Fecha Solicitud (orientado al pasado, igual que en el resto de bandejas).</p>
     */
    public Set<Long> listarIdsUsuarioConSolicitudEnRango(LocalDate desde, LocalDate hasta) throws SQLException {
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
        sql.append("JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 ");
        sql.append("WHERE e.activo = 1 AND NVL(e.cerrado, 0) = 0 AND NVL(e.archivado, 0) = 0 ");
        sql.append("AND ((et.codigo = 'ASIGNACION' AND es.codigo = 'ASIGNADO') ");
        sql.append("     OR et.codigo = 'ANALISIS') ");
        sql.append(CONDICION_CARGA_REAL);
        sql.append("AND esol.fecha_recepcion IS NOT NULL ");
        if (desde != null) {
            sql.append("AND TRUNC(esol.fecha_recepcion) >= ? ");
            params.add(Date.valueOf(desde));
        }
        if (hasta != null) {
            sql.append("AND TRUNC(esol.fecha_recepcion) <= ? ");
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
