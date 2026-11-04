package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.application.CalendarioLaboralService;
import com.sdrerc.v3.domain.EjecucionExpedienteDTO;
import com.sdrerc.v3.domain.EjecucionResultadoDTO;
import com.sdrerc.v3.domain.EjecucionUltimoResultadoDTO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Port ACOTADO de com.sdrerc.infrastructure.sdrercapp.dao.EjecucionExpedienteDAO (V2, 919 líneas) —
 * solo el camino real de "Guardar Ejecución" MÁS las 3 acciones huérfanas de V2 que se decidió
 * exponer como reales en V3 (ver punto 3 del prompt de este incremento, confirmado por el usuario:
 * Observación ejecución / Error material / Devolver a Análisis).
 *
 * <p><b>Hallazgo crítico (investigación previa a este port, ver JPanelEjecucionV2.java)</b>: 7
 * métodos {@code crear...Panel()} (Resumen selección, Antecedentes, Documento emitido, Error
 * material, Carta notificación, Publicación prevista, Formulario ejecución completo) construyen
 * paneles enteros con labels que se siguen actualizando en memoria pero NUNCA se agregan a la UI
 * real — confirmado leyendo {@code crearPanelEjecucionOperativa()}: solo agrega
 * {@code crearResultadoEjecucion()} + {@code crearDocumentosPanel()} + {@code crearDestinoEjecucion()},
 * footer con únicamente {@code btnRegistrarEjecucion} ("Guardar Ejecución") + {@code btnCancelarEjecucion}.
 * 5 botones más ({@code btnMarcarEjecutado}, {@code btnObservar}, {@code btnDocumentoInconsistente},
 * {@code btnRevertirAnalisis}, {@code btnDerivarNotificacion}) invocan Service/DAO reales y
 * funcionales pero JAMÁS se agregan a ningún contenedor visible.</p>
 *
 * <p><b>Decisión de este incremento (confirmada por el usuario)</b>: a diferencia de Análisis/
 * Verificación (donde el código huérfano se documentó y NO se portó), acá SÍ se exponen 3 de esas
 * acciones como reales y alcanzables en V3 — la lógica ya existe completa en el DAO real de V2 y ya
 * tiene transiciones activas verificadas en Oracle:
 * <ul>
 *   <li>{@code registrarObservacion} → {@code OBSERVACION_EJECUCION}: EJECUCION/EN_EJECUCION →
 *   EJECUCION/REQUIERE_CORRECCION_EJECUCION.</li>
 *   <li>{@code registrarDocumentoInconsistente} → {@code REVERSION_ESTADO_DOCUMENTO_EJECUCION}:
 *   EJECUCION/EN_EJECUCION → EJECUCION/DOCUMENTO_INCONSISTENTE_EJECUCION.</li>
 *   <li>{@code revertirAnalisis} → {@code DEVOLUCION_A_ANALISIS}: EJECUCION/{DOCUMENTO_INCONSISTENTE_EJECUCION|
 *   REQUIERE_CORRECCION_EJECUCION} → ANALISIS/OBSERVADO.</li>
 * </ul>
 * <p><b>31/08/2026, decisión explícita del usuario</b>: los 2 estados destino de arriba dejaron de
 * ser las filas {@code REQUIERE_CORRECCION}/{@code DOCUMENTO_INCONSISTENTE} de Verificación
 * (reutilizadas por ID desde el port inicial — un atajo real del catálogo `flujo_transicion`,
 * documentado en el `CLAUDE.md` raíz como "cross-etapa ID reuse") — Ejecución ahora tiene sus
 * propias filas en `estado_expediente` (script 121), con código distinto (`_EJECUCION` como
 * sufijo) para que `CatalogoLookupDAO#obtenerEstadoId(codigo)` (que resuelve por código sin
 * filtrar por etapa) nunca vuelva a confundir ambas etapas.
 * {@code marcarEjecutado} (duplica exactamente lo que ya hace "Registrar resultado de ejecución") y
 * {@code derivarNotificacion} como botón SEPARADO (ya cubierto por "Enviar a Supervisión", ver
 * abajo) NO se portan — habrían sido accesos duplicados a la misma acción ya expuesta.</p>
 *
 * <p><b>"Guardar Ejecución" de V2 se separa en 2 acciones independientes</b> (pedido explícito del
 * usuario, resuelve de forma honesta la falta de atomicidad real de V2 — allá son 2 transacciones
 * SQL separadas con commits independientes bajo el mismo click, riesgo real si la 2ª falla tras el
 * commit de la 1ª):
 * <ul>
 *   <li><b>"Registrar resultado de ejecución"</b> ({@link #registrarResultado}) — ejecuta SOLO
 *   {@code INICIO_EJECUCION}: EJECUCION/EN_EJECUCION → EJECUCION/EJECUTADO. Valida que exista un
 *   documento de resolución con número (ver hallazgo de {@code expediente_resolucion} más abajo) Y
 *   (21/08/2026, pedido explícito del usuario) que exista una carta de notificación FINAL ya en
 *   despacho ({@code DocumentoAnalisisDAO#tieneDocumentoFinalEnDespacho}, MISMA validación que
 *   "Enviar a Supervisión" — antes solo se exigía ahí, dejando que un expediente quedara marcado
 *   Ejecutado sin tener todavía la carta lista, algo que V2 nunca permitía porque su "Guardar
 *   Ejecución" original validaba esto ANTES de hacer ambos saltos en un solo clic).</li>
 *   <li><b>"Enviar a Supervisión"</b> ({@link #enviarSupervision}) — ejecuta SOLO
 *   {@code DERIVACION_A_NOTIFICACION}: EJECUCION/EJECUTADO → NOTIFICACION/POR_ASIGNAR. Valida
 *   ANTES de tocar BD que exista una carta de notificación final en despacho
 *   ({@code DocumentoAnalisisDAO#tieneDocumentoFinalEnDespacho}, mismo criterio documentado en el
 *   CLAUDE.md raíz) — se conserva acá TAMBIÉN (no solo en "Registrar resultado"), porque la carta
 *   podría des-despacharse (ej. corrección) entre que se registra el resultado y se envía a
 *   Supervisión; doble chequeo, no redundante.
 *   Rotulada "Enviar a Supervisión" (no "Destino operativo") porque V2 ya hardcodea
 *   el combo de equipo a un único equipo real ({@code EQ_SUPERVISION}) — sin combo de equipo en V3,
 *   solo combo de usuario destino.</li>
 * </ul>
 * </p>
 *
 * <p><b>Hallazgo — {@code expediente_resolucion} es tabla muerta</b> (verificado con
 * {@code SELECT COUNT(*) FROM expediente_resolucion WHERE activo=1}: 0 filas, pese a 3 expedientes
 * reales en etapa EJECUCION/EN_EJECUCION). La validación real de V2
 * ({@code tieneDocumentoResolucionConNumero}, el fallback que SIEMPRE se ejercita en la práctica)
 * consulta {@code EXPEDIENTE_DOCUMENTO_ANALIZADO} (nivel 0, activo, tipo cuyo código contiene
 * "RESOLUCION", con {@code numero_documento}) — V3 usa ÚNICAMENTE ese camino, sin portar
 * {@code ResolucionDocumentoDAO}/{@code expediente_resolucion}.</p>
 */
@Repository
public class EjecucionExpedienteDAO {

    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String ETAPA_EJECUCION = "EJECUCION";
    private static final String ETAPA_ANALISIS = "ANALISIS";
    private static final String ETAPA_NOTIFICACION = "NOTIFICACION";
    private static final String ESTADO_EN_EJECUCION = "EN_EJECUCION";
    private static final String ESTADO_EJECUTADO = "EJECUTADO";
    // 31/08/2026: estados PROPIOS de Ejecución (script 121) — antes reutilizaban por ID las filas
    // DOCUMENTO_INCONSISTENTE/REQUIERE_CORRECCION de Verificación (misma fila de estado_expediente,
    // sin fila propia bajo la etapa EJECUCION). Decisión explícita del usuario: crear filas propias
    // para que `estado_expediente` deje de mezclar dos etapas bajo el mismo id_estado.
    private static final String ESTADO_DOCUMENTO_INCONSISTENTE = "DOCUMENTO_INCONSISTENTE_EJECUCION";
    private static final String ESTADO_REQUIERE_CORRECCION = "REQUIERE_CORRECCION_EJECUCION";
    private static final String ESTADO_ANALISIS_OBSERVADO = "OBSERVADO";
    private static final String ESTADO_POR_ASIGNAR = "POR_ASIGNAR";
    private static final String ACCION_INICIO_EJECUCION = "INICIO_EJECUCION";
    private static final String ACCION_OBSERVACION_EJECUCION = "OBSERVACION_EJECUCION";
    private static final String ACCION_DOCUMENTO_INCONSISTENTE = "REVERSION_ESTADO_DOCUMENTO_EJECUCION";
    private static final String ACCION_DEVOLUCION_ANALISIS = "DEVOLUCION_A_ANALISIS";
    private static final String ACCION_DERIVACION_NOTIFICACION = "DERIVACION_A_NOTIFICACION";
    private static final String TIPO_RELACION_DOCUMENTO_DUPLICADO = "DOCUMENTO_DUPLICADO_ASOCIADO";
    private static final String TIPO_RELACION_MISMA_ACTA_TITULAR = "MISMA_ACTA_TITULAR";

    private final DataSource dataSource;
    private final CalendarioLaboralService calendarioLaboralService;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final DocumentoAnalisisDAO documentoAnalisisDAO;

    public EjecucionExpedienteDAO(
            DataSource dataSource,
            CalendarioLaboralService calendarioLaboralService,
            CatalogoLookupDAO catalogoLookupDAO,
            DocumentoAnalisisDAO documentoAnalisisDAO) {
        this.dataSource = dataSource;
        this.calendarioLaboralService = calendarioLaboralService;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.documentoAnalisisDAO = documentoAnalisisDAO;
    }

    public List<EjecucionExpedienteDTO> buscarExpedientes(
            String textoLibre,
            String estadoCodigo,
            LocalDate fechaSolicitudDesde,
            LocalDate fechaSolicitudHasta,
            boolean esAdmin,
            Long idUsuarioActual,
            List<Long> idsEquipoActual,
            Integer limite) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("SELECT DISTINCT e.id_expediente, e.numero_expediente, esol.numero_expediente_sgd, ");
        sql.append("esol.asunto AS procedimiento, ");
        sql.append("(SELECT MAX(tda2.nombre) KEEP (DENSE_RANK LAST ORDER BY da2.fecha_documento NULLS FIRST, da2.id_documento_analizado) ");
        sql.append("  FROM expediente_documento_analizado da2 ");
        sql.append("  JOIN tipo_documento_adjunto tda2 ON tda2.id_tipo_documento_adjunto = da2.id_tipo_documento_adjunto ");
        sql.append("  WHERE da2.id_expediente = e.id_expediente AND da2.activo = 1 AND da2.nivel = 0 ");
        sql.append("  AND TRIM(da2.numero_documento) IS NOT NULL AND UPPER(NVL(tda2.codigo, '')) LIKE '%RESOLUCION%') AS tipo_documento_resolucion, ");
        sql.append("(SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM expediente_documento_analizado da3 ");
        sql.append("  JOIN tipo_documento_adjunto tda3 ON tda3.id_tipo_documento_adjunto = da3.id_tipo_documento_adjunto ");
        sql.append("  WHERE da3.id_expediente = e.id_expediente AND da3.activo = 1 AND da3.nivel = 0 ");
        sql.append("  AND TRIM(da3.numero_documento) IS NOT NULL AND UPPER(NVL(tda3.codigo, '')) LIKE '%RESOLUCION%') AS documento_resolucion_con_numero, ");
        sql.append("ta.nombre AS tipo_acta, ea.numero_acta, ").append(nombrePersona("p")).append(" AS titular, ");
        sql.append("esol.fecha_recepcion, e.fecha_vencimiento, ");
        sql.append("ur.nombre_completo AS responsable, e.id_usuario_responsable_actual, e.id_equipo_responsable_actual, ");
        sql.append("et.codigo AS etapa_codigo, est.codigo AS estado_codigo, ");
        sql.append("UPPER(NVL(").append(nombrePersona("p")).append(", 'ZZZ')) AS orden_titular, ");
        sql.append("(SELECT MAX(NVL(tre.nombre, tre.codigo)) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) ");
        sql.append("  FROM expediente_evaluacion ev LEFT JOIN tipo_resultado_evaluacion tre ON tre.id_tipo_resultado_evaluacion = ev.id_tipo_resultado_evaluacion ");
        sql.append("  WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS resultado_analisis, ");
        sql.append("(SELECT MAX(ua.nombre_completo) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) ");
        sql.append("  FROM expediente_evaluacion ev LEFT JOIN usuario ua ON ua.id_usuario = ev.creado_por ");
        sql.append("  WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS responsable_analisis, ");
        sql.append("(SELECT COUNT(*) FROM expediente_documento_analizado da WHERE da.id_expediente = e.id_expediente AND da.activo = 1) AS total_documentos, ");
        sql.append("NVL(esol.grupo_familiar, 0) AS grupo_familiar, ");
        sql.append("(SELECT COUNT(DISTINCT canon.id_asociado) FROM (");
        sql.append("SELECT CASE ");
        sql.append("WHEN NVL(TRIM(op.numero_expediente), '') <> '' AND NVL(TRIM(orrel.numero_expediente), '') = '' THEN op.id_expediente ");
        sql.append("WHEN NVL(TRIM(op.numero_expediente), '') = '' AND NVL(TRIM(orrel.numero_expediente), '') <> '' THEN orrel.id_expediente ");
        sql.append("ELSE CASE WHEN NVL(op.fecha_registro, DATE '1900-01-01') <= NVL(orrel.fecha_registro, DATE '1900-01-01') THEN op.id_expediente ELSE orrel.id_expediente END ");
        sql.append("END AS id_canonico, ");
        sql.append("CASE WHEN r_canon.id_expediente_principal = e.id_expediente THEN r_canon.id_expediente_relacionado ELSE r_canon.id_expediente_principal END AS id_asociado ");
        sql.append("FROM expediente_relacion r_canon ");
        sql.append("JOIN expediente op ON op.id_expediente = r_canon.id_expediente_principal AND op.activo = 1 ");
        sql.append("JOIN expediente orrel ON orrel.id_expediente = r_canon.id_expediente_relacionado AND orrel.activo = 1 ");
        sql.append("WHERE r_canon.activo = 1 AND UPPER(r_canon.tipo_relacion) IN (?, ?) ");
        sql.append("AND (r_canon.id_expediente_principal = e.id_expediente OR r_canon.id_expediente_relacionado = e.id_expediente)) canon ");
        params.add(TIPO_RELACION_DOCUMENTO_DUPLICADO);
        params.add(TIPO_RELACION_MISMA_ACTA_TITULAR);
        sql.append("WHERE canon.id_canonico = e.id_expediente) AS relaciones_confirmadas ");
        sql.append("FROM expediente e ");
        sql.append("JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual ");
        sql.append("JOIN estado_expediente est ON est.id_estado = e.id_estado_actual ");
        sql.append("LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual ");
        sql.append("LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 ");
        sql.append("LEFT JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1 ");
        sql.append("LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta ");
        sql.append("LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' ");
        sql.append("LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 ");
        sql.append("WHERE e.activo = 1 AND et.codigo = ? ");
        params.add(ETAPA_EJECUCION);

        appendFiltroPrincipalCanonico(sql, params);
        // Ejecución NO incluye abogados supervisados (confirmado en V2: sobrecarga de 6 parámetros,
        // sin el flag `incluirAbogadosSupervisados` que sí usa Verificación).
        sql.append(VisibilidadBandejaSql.construirCondicion(
                params, esAdmin, idUsuarioActual, idsEquipoActual,
                "e.id_usuario_responsable_actual", "e.id_equipo_responsable_actual", false));

        if (hasText(estadoCodigo) && !"TODOS".equalsIgnoreCase(estadoCodigo)) {
            sql.append("AND UPPER(est.codigo) = ? ");
            params.add(estadoCodigo.trim().toUpperCase(Locale.ROOT));
        }
        if (fechaSolicitudDesde != null) {
            sql.append("AND TRUNC(esol.fecha_recepcion) >= ? ");
            params.add(Date.valueOf(fechaSolicitudDesde));
        }
        if (fechaSolicitudHasta != null) {
            sql.append("AND TRUNC(esol.fecha_recepcion) <= ? ");
            params.add(Date.valueOf(fechaSolicitudHasta));
        }
        if (hasText(textoLibre)) {
            String pattern = "%" + textoLibre.trim().toUpperCase(Locale.ROOT) + "%";
            sql.append("AND (");
            sql.append("UPPER(NVL(e.numero_expediente, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(esol.numero_expediente_sgd, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(ea.numero_acta, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(").append(nombrePersona("p")).append(", '')) LIKE ? ");
            sql.append(") ");
            for (int i = 0; i < 4; i++) {
                params.add(pattern);
            }
        }
        sql.append("ORDER BY fecha_vencimiento ASC NULLS LAST, orden_titular ASC, id_expediente ASC");
        sql.append(")");
        if (limite != null && limite > 0) {
            sql.append(" WHERE ROWNUM <= ?");
            params.add(limite);
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<EjecucionExpedienteDTO> expedientes = new ArrayList<>();
                while (rs.next()) {
                    expedientes.add(map(conn, rs));
                }
                return expedientes;
            }
        }
    }

    /**
     * "Registrar resultado de ejecución" — ejecuta SOLO INICIO_EJECUCION (EN_EJECUCION → EJECUTADO).
     * Valida documento de resolución con número Y carta de notificación FINAL ya en despacho (esta
     * 2ª validación agregada 21/08/2026, pedido explícito del usuario — antes solo se exigía en
     * {@link #enviarSupervision}, permitiendo marcar Ejecutado sin tener la carta lista todavía).
     */
    public EjecucionResultadoDTO registrarResultado(Long idExpediente, String resultadoNombre, String comentario, Long idUsuarioActual) throws SQLException {
        if (!documentoAnalisisDAO.tieneDocumentoFinalEnDespacho(idExpediente)) {
            throw new IllegalStateException(
                    "Registre y despache la carta de notificación (FINAL) antes de registrar el resultado de ejecución.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEstadoActual estado = bloquearYObtenerEstado(conn, idExpediente);
                if (!ETAPA_EJECUCION.equalsIgnoreCase(estado.etapaCodigo) || !ESTADO_EN_EJECUCION.equalsIgnoreCase(estado.estadoCodigo)) {
                    throw new IllegalStateException(
                            "El expediente debe estar En ejecución para registrar el resultado (estado actual: "
                                    + estado.etapaCodigo + "/" + estado.estadoCodigo + ").");
                }
                if (!tieneDocumentoResolucionConNumero(conn, idExpediente)) {
                    throw new IllegalStateException("El expediente no tiene un documento de resolución con número registrado.");
                }
                Transicion transicion = requerirTransicion(conn, ACCION_INICIO_EJECUCION, ETAPA_EJECUCION, ESTADO_EN_EJECUCION, ETAPA_EJECUCION, ESTADO_EJECUTADO);
                validarRequisitosTransicion(conn, transicion, idExpediente);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_INICIO_EJECUCION), "movimiento " + ACCION_INICIO_EJECUCION);

                actualizarEtapaEstado(conn, idExpediente, transicion.idEtapaDestino, transicion.idEstadoDestino, null, null, idUsuarioActual);
                insertarHistorial(conn, idExpediente, idMovimiento, idUsuarioActual, null, null,
                        comentarioMovimiento(comentario, "La ejecución fue registrada correctamente."), resultadoNombre);

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new EjecucionResultadoDTO(idExpediente, null, ACCION_INICIO_EJECUCION, ETAPA_EJECUCION, ESTADO_EJECUTADO, "La ejecución fue registrada correctamente.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                throw ex;
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

    /**
     * Lee el último "Registrar resultado de ejecución" (INICIO_EJECUCION) ya aplicado al expediente,
     * sin importar si después ya avanzó a Supervisión — permite mostrar de forma persistente (no
     * efímera, sobrevive al remount de la subpestaña) qué resultado quedó registrado. {@code null}
     * si nunca se registró ninguno.
     */
    public EjecucionUltimoResultadoDTO obtenerUltimoResultadoRegistrado(Long idExpediente) throws SQLException {
        String sql = "SELECT eh.motivo, eh.fecha_movimiento "
                + "FROM expediente_historial eh "
                + "JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = eh.id_tipo_movimiento "
                + "WHERE eh.id_expediente = ? AND tm.codigo = ? AND eh.activo = 1 "
                + "ORDER BY eh.fecha_movimiento DESC, eh.id_expediente_historial DESC "
                + "FETCH FIRST 1 ROW ONLY";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, ACCION_INICIO_EJECUCION);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String resultadoNombre = rs.getString("motivo");
                Timestamp fecha = rs.getTimestamp("fecha_movimiento");
                return new EjecucionUltimoResultadoDTO(resultadoNombre, fecha == null ? null : fecha.toLocalDateTime());
            }
        }
    }

    /**
     * "Enviar a Supervisión" — ejecuta SOLO DERIVACION_A_NOTIFICACION (EJECUTADO → NOTIFICACION/
     * POR_ASIGNAR). Valida ANTES de tocar BD que exista una carta de notificación final en despacho
     * (mismo criterio que V2, ver Javadoc de la clase).
     */
    public EjecucionResultadoDTO enviarSupervision(Long idExpediente, String comentario, Long idEquipoSupervision, Long idUsuarioDestino, Long idUsuarioActual)
            throws SQLException {
        if (idUsuarioDestino == null) {
            throw new IllegalArgumentException("Seleccione el usuario destino en Supervisión.");
        }
        if (!documentoAnalisisDAO.tieneDocumentoFinalEnDespacho(idExpediente)) {
            throw new IllegalStateException(
                    "Registre y despache la carta de notificación antes de enviar a Supervisión."
                            + " La ejecución no se deriva hasta que pueda enviarse a Notificación.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEstadoActual estado = bloquearYObtenerEstado(conn, idExpediente);
                if (!ETAPA_EJECUCION.equalsIgnoreCase(estado.etapaCodigo) || !ESTADO_EJECUTADO.equalsIgnoreCase(estado.estadoCodigo)) {
                    throw new IllegalStateException(
                            "El expediente debe estar Ejecutado para enviarlo a Supervisión (estado actual: "
                                    + estado.etapaCodigo + "/" + estado.estadoCodigo + ").");
                }
                Transicion transicion = requerirTransicion(conn, ACCION_DERIVACION_NOTIFICACION, ETAPA_EJECUCION, ESTADO_EJECUTADO, ETAPA_NOTIFICACION, ESTADO_POR_ASIGNAR);
                validarRequisitosTransicion(conn, transicion, idExpediente);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_DERIVACION_NOTIFICACION), "movimiento " + ACCION_DERIVACION_NOTIFICACION);

                actualizarEtapaEstado(conn, idExpediente, transicion.idEtapaDestino, transicion.idEstadoDestino, idEquipoSupervision, idUsuarioDestino, idUsuarioActual);
                insertarHistorial(conn, idExpediente, idMovimiento, idUsuarioActual, idUsuarioDestino, idEquipoSupervision,
                        comentarioMovimiento(comentario, "El expediente fue derivado a Notificación, pendiente de asignar."));

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new EjecucionResultadoDTO(idExpediente, null, ACCION_DERIVACION_NOTIFICACION, ETAPA_NOTIFICACION, ESTADO_POR_ASIGNAR,
                        "El expediente fue enviado a Supervisión y derivado a Notificación, pendiente de asignar.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                throw ex;
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

    /** "Registrar observación" — OBSERVACION_EJECUCION: EN_EJECUCION → REQUIERE_CORRECCION. */
    public EjecucionResultadoDTO registrarObservacion(
            Long idExpediente, String tipoObservacionCodigo, String motivoCorreccionCodigo, String descripcion, Long idUsuarioActual) throws SQLException {
        return registrarObservacionEjecucion(
                idExpediente, ACCION_OBSERVACION_EJECUCION, ESTADO_REQUIERE_CORRECCION, tipoObservacionCodigo, motivoCorreccionCodigo, descripcion,
                idUsuarioActual, "La observación de ejecución fue registrada correctamente.");
    }

    /** "Registrar error material" — REVERSION_ESTADO_DOCUMENTO_EJECUCION: EN_EJECUCION → DOCUMENTO_INCONSISTENTE. */
    public EjecucionResultadoDTO registrarDocumentoInconsistente(
            Long idExpediente, String tipoObservacionCodigo, String motivoCorreccionCodigo, String descripcion, Long idUsuarioActual) throws SQLException {
        return registrarObservacionEjecucion(
                idExpediente, ACCION_DOCUMENTO_INCONSISTENTE, ESTADO_DOCUMENTO_INCONSISTENTE, tipoObservacionCodigo, motivoCorreccionCodigo, descripcion,
                idUsuarioActual, "El documento inconsistente fue registrado correctamente.");
    }

    private EjecucionResultadoDTO registrarObservacionEjecucion(
            Long idExpediente, String accionCodigo, String estadoDestinoCodigo, String tipoObservacionCodigo, String motivoCorreccionCodigo,
            String descripcion, Long idUsuarioActual, String mensaje) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEstadoActual estado = bloquearYObtenerEstado(conn, idExpediente);
                if (!ETAPA_EJECUCION.equalsIgnoreCase(estado.etapaCodigo) || !ESTADO_EN_EJECUCION.equalsIgnoreCase(estado.estadoCodigo)) {
                    throw new IllegalStateException("El expediente debe estar En ejecución para registrar la observación.");
                }
                Transicion transicion = requerirTransicion(conn, accionCodigo, ETAPA_EJECUCION, ESTADO_EN_EJECUCION, ETAPA_EJECUCION, estadoDestinoCodigo);
                validarRequisitosTransicion(conn, transicion, idExpediente);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, accionCodigo), "movimiento " + accionCodigo);

                insertarObservacionEjecucion(conn, idExpediente, tipoObservacionCodigo, motivoCorreccionCodigo, descripcion, idUsuarioActual);
                actualizarEtapaEstado(conn, idExpediente, transicion.idEtapaDestino, transicion.idEstadoDestino, null, null, idUsuarioActual);
                insertarHistorial(conn, idExpediente, idMovimiento, idUsuarioActual, null, null, comentarioMovimiento(descripcion, mensaje));

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new EjecucionResultadoDTO(idExpediente, null, accionCodigo, ETAPA_EJECUCION, estadoDestinoCodigo, mensaje);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                throw ex;
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

    /** "Devolver a Análisis" — DEVOLUCION_A_ANALISIS: {DOCUMENTO_INCONSISTENTE|REQUIERE_CORRECCION} → ANALISIS/OBSERVADO. */
    public EjecucionResultadoDTO revertirAnalisis(
            Long idExpediente, String tipoObservacionCodigo, String motivoCorreccionCodigo, String motivoReversion, String comentario, Long idUsuarioActual)
            throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEstadoActual estado = bloquearYObtenerEstado(conn, idExpediente);
                if (!ETAPA_EJECUCION.equalsIgnoreCase(estado.etapaCodigo)
                        || !(ESTADO_DOCUMENTO_INCONSISTENTE.equalsIgnoreCase(estado.estadoCodigo) || ESTADO_REQUIERE_CORRECCION.equalsIgnoreCase(estado.estadoCodigo))) {
                    throw new IllegalStateException("El expediente debe estar con observación o documento inconsistente para devolverlo a Análisis.");
                }
                Transicion transicion = requerirTransicion(conn, ACCION_DEVOLUCION_ANALISIS, ETAPA_EJECUCION, estado.estadoCodigo, ETAPA_ANALISIS, ESTADO_ANALISIS_OBSERVADO);
                validarRequisitosTransicion(conn, transicion, idExpediente);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_DEVOLUCION_ANALISIS), "movimiento " + ACCION_DEVOLUCION_ANALISIS);

                insertarObservacionEjecucion(conn, idExpediente, tipoObservacionCodigo, motivoCorreccionCodigo, motivoReversion, idUsuarioActual);
                actualizarEtapaEstado(conn, idExpediente, transicion.idEtapaDestino, transicion.idEstadoDestino, null, null, idUsuarioActual);
                insertarHistorial(conn, idExpediente, idMovimiento, idUsuarioActual, null, null,
                        comentarioMovimiento(comentario, "Devolución a Análisis desde Ejecución."));

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new EjecucionResultadoDTO(idExpediente, null, ACCION_DEVOLUCION_ANALISIS, ETAPA_ANALISIS, ESTADO_ANALISIS_OBSERVADO, "El expediente fue devuelto a Análisis.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                throw ex;
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

    private void insertarObservacionEjecucion(
            Connection conn, Long idExpediente, String tipoObservacionCodigo, String motivoCorreccionCodigo, String descripcion, Long idUsuarioActual)
            throws SQLException {
        if (!hasText(descripcion)) {
            return;
        }
        Long idTipoObservacion = null;
        if (hasText(tipoObservacionCodigo)) {
            idTipoObservacion = catalogoLookupDAO.obtenerTipoObservacionId(conn, tipoObservacionCodigo);
            if (idTipoObservacion == null) {
                throw new IllegalStateException("No se encontró el tipo de observación: " + tipoObservacionCodigo + ".");
            }
        }
        Long idMotivoCorreccion = null;
        if (hasText(motivoCorreccionCodigo)) {
            idMotivoCorreccion = catalogoLookupDAO.obtenerMotivoCorreccionId(conn, motivoCorreccionCodigo);
            if (idMotivoCorreccion == null) {
                throw new IllegalStateException("No se encontró el motivo de corrección: " + motivoCorreccionCodigo + ".");
            }
        }
        String sql = "INSERT INTO expediente_observacion ("
                + "id_expediente, id_tipo_observacion, id_motivo_correccion, origen_observacion, descripcion, "
                + "subsanada, fecha_observacion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, 'EJECUCION', ?, 0, SYSTIMESTAMP, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            setLongOrNull(ps, 2, idTipoObservacion);
            setLongOrNull(ps, 3, idMotivoCorreccion);
            ps.setString(4, limitar(descripcion, 1500));
            setLongOrNull(ps, 5, idUsuarioActual);
            ps.executeUpdate();
        }
    }

    /** Mismo criterio que `tieneDocumentoResolucionConNumero` de V2 — ver Javadoc de la clase. */
    private static boolean tieneDocumentoResolucionConNumero(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expediente_documento_analizado da "
                + "JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                + "WHERE da.id_expediente = ? AND da.activo = 1 AND da.nivel = 0 "
                + "AND TRIM(da.numero_documento) IS NOT NULL "
                + "AND UPPER(NVL(tda.codigo, '')) LIKE '%RESOLUCION%'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Mismo criterio que V2 (validarRequisitosTransicion): exige análisis registrado; exige ≥1 documento si la transición lo requiere. */
    private void validarRequisitosTransicion(Connection conn, Transicion transicion, Long idExpediente) throws SQLException {
        String sqlEvaluacion = "SELECT COUNT(*) FROM expediente_evaluacion WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sqlEvaluacion)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt(1) <= 0) {
                    throw new IllegalStateException("No existe análisis registrado para el expediente seleccionado.");
                }
            }
        }
        if (transicion.requiereDocumento && documentoAnalisisDAO.contarPorExpediente(conn, idExpediente) <= 0) {
            throw new IllegalStateException("No existen documentos analizados para sustentar la ejecución.");
        }
    }

    private void appendFiltroPrincipalCanonico(StringBuilder sql, List<Object> params) {
        sql.append("AND (");
        sql.append("NOT EXISTS (SELECT 1 FROM expediente_relacion r_excl ");
        sql.append("WHERE r_excl.activo = 1 AND UPPER(r_excl.tipo_relacion) IN (?, ?) ");
        sql.append("AND r_excl.id_expediente_relacionado = e.id_expediente) ");
        params.add(TIPO_RELACION_DOCUMENTO_DUPLICADO);
        params.add(TIPO_RELACION_MISMA_ACTA_TITULAR);
        sql.append("OR EXISTS (SELECT 1 FROM (");
        sql.append("SELECT DISTINCT CASE ");
        sql.append("WHEN NVL(TRIM(op.numero_expediente), '') <> '' AND NVL(TRIM(orrel.numero_expediente), '') = '' THEN op.id_expediente ");
        sql.append("WHEN NVL(TRIM(op.numero_expediente), '') = '' AND NVL(TRIM(orrel.numero_expediente), '') <> '' THEN orrel.id_expediente ");
        sql.append("ELSE CASE WHEN NVL(op.fecha_registro, DATE '1900-01-01') <= NVL(orrel.fecha_registro, DATE '1900-01-01') THEN op.id_expediente ELSE orrel.id_expediente END ");
        sql.append("END AS id_canonico ");
        sql.append("FROM expediente_relacion r_canon ");
        sql.append("JOIN expediente op ON op.id_expediente = r_canon.id_expediente_principal AND op.activo = 1 ");
        sql.append("JOIN expediente orrel ON orrel.id_expediente = r_canon.id_expediente_relacionado AND orrel.activo = 1 ");
        sql.append("WHERE r_canon.activo = 1 AND UPPER(r_canon.tipo_relacion) IN (?, ?) ");
        sql.append("AND (r_canon.id_expediente_principal = e.id_expediente OR r_canon.id_expediente_relacionado = e.id_expediente)) canon ");
        params.add(TIPO_RELACION_DOCUMENTO_DUPLICADO);
        params.add(TIPO_RELACION_MISMA_ACTA_TITULAR);
        sql.append("WHERE canon.id_canonico = e.id_expediente)) ");
    }

    private static String nombrePersona(String alias) {
        return "TRIM(NVL(" + alias + ".razon_social, TRIM(NVL(" + alias + ".nombres, '') || ' ' || NVL(" + alias + ".apellidos, ''))))";
    }

    private EjecucionExpedienteDTO map(Connection conn, ResultSet rs) throws SQLException {
        Long idExpediente = getLongOrNull(rs, "id_expediente");
        Date fechaVencimiento = rs.getDate("fecha_vencimiento");
        return new EjecucionExpedienteDTO(
                idExpediente,
                rs.getString("numero_expediente"),
                rs.getString("numero_expediente_sgd"),
                rs.getString("procedimiento"),
                rs.getString("tipo_documento_resolucion"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                rs.getString("responsable"),
                getLongOrNull(rs, "id_usuario_responsable_actual"),
                getLongOrNull(rs, "id_equipo_responsable_actual"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                toLocalDate(fechaVencimiento),
                calendarioLaboralService.calcularDiasHabilesRestantes(conn, idExpediente, fechaVencimiento),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getString("resultado_analisis"),
                rs.getString("responsable_analisis"),
                rs.getInt("total_documentos"),
                rs.getInt("relaciones_confirmadas"),
                rs.getInt("documento_resolucion_con_numero") == 1,
                rs.getInt("grupo_familiar") == 1);
    }

    private ExpedienteEstadoActual bloquearYObtenerEstado(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT e.id_etapa_actual, e.id_estado_actual, et.codigo AS etapa_codigo, est.codigo AS estado_codigo "
                + "FROM expediente e "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "WHERE e.id_expediente = ? AND e.activo = 1 FOR UPDATE OF e.id_etapa_actual";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("El expediente no existe o no está activo.");
                }
                return new ExpedienteEstadoActual(
                        getLongOrNull(rs, "id_etapa_actual"), getLongOrNull(rs, "id_estado_actual"),
                        rs.getString("etapa_codigo"), rs.getString("estado_codigo"));
            }
        }
    }

    private Transicion requerirTransicion(
            Connection conn, String accionCodigo, String etapaOrigenCodigo, String estadoOrigenCodigo,
            String etapaDestinoCodigo, String estadoDestinoCodigo) throws SQLException {
        Long idEtapaDestino = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, etapaDestinoCodigo), "etapa " + etapaDestinoCodigo);
        Long idEstadoDestino = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, estadoDestinoCodigo), "estado " + estadoDestinoCodigo);
        String sql = "SELECT ft.requiere_documento FROM flujo f "
                + "JOIN flujo_transicion ft ON ft.id_flujo = f.id_flujo "
                + "JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen "
                + "JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen "
                + "WHERE f.codigo = ? AND f.activo = 1 AND ft.activo = 1 "
                + "AND ft.codigo_accion = ? AND eo.codigo = ? AND so.codigo = ? "
                + "AND ft.id_etapa_destino = ? AND ft.id_estado_destino = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, CODIGO_FLUJO);
            ps.setString(2, accionCodigo);
            ps.setString(3, etapaOrigenCodigo);
            ps.setString(4, estadoOrigenCodigo);
            ps.setLong(5, idEtapaDestino);
            ps.setLong(6, idEstadoDestino);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("No existe transición activa " + etapaOrigenCodigo + "/" + estadoOrigenCodigo
                            + " -> " + etapaDestinoCodigo + "/" + estadoDestinoCodigo + " para " + accionCodigo + ".");
                }
                return new Transicion(idEtapaDestino, idEstadoDestino, rs.getInt("requiere_documento") == 1);
            }
        }
    }

    private void actualizarEtapaEstado(
            Connection conn, Long idExpediente, Long idEtapaDestino, Long idEstadoDestino,
            Long idEquipoDestino, Long idUsuarioDestino, Long idUsuarioModificador) throws SQLException {
        String sql = "UPDATE expediente SET id_etapa_actual = ?, id_estado_actual = ?, "
                + "id_usuario_responsable_actual = NVL(?, id_usuario_responsable_actual), "
                + "id_equipo_responsable_actual = NVL(?, id_equipo_responsable_actual), "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEtapaDestino);
            ps.setLong(2, idEstadoDestino);
            setLongOrNull(ps, 3, idUsuarioDestino);
            setLongOrNull(ps, 4, idEquipoDestino);
            setLongOrNull(ps, 5, idUsuarioModificador);
            ps.setLong(6, idExpediente);
            ps.executeUpdate();
        }
    }

    private void insertarHistorial(
            Connection conn, Long idExpediente, Long idMovimiento, Long idUsuarioActual,
            Long idUsuarioDestino, Long idEquipoDestino, String comentario) throws SQLException {
        insertarHistorial(conn, idExpediente, idMovimiento, idUsuarioActual, idUsuarioDestino, idEquipoDestino, comentario, null);
    }

    /**
     * Overload con {@code motivo} (01/09/2026, pedido explícito del usuario: estandarizar que el
     * resultado REGISTRADO de una acción quede visible en Trazabilidad, no solo el comentario libre
     * del usuario). {@code comentario} sigue siendo texto libre/mensaje genérico; {@code motivo} es
     * el valor estructurado real de la acción (ej. el nombre del resultado de ejecución elegido) —
     * columna ya existente en {@code EXPEDIENTE_HISTORIAL}, sin uso previo en este DAO.
     */
    private void insertarHistorial(
            Connection conn, Long idExpediente, Long idMovimiento, Long idUsuarioActual,
            Long idUsuarioDestino, Long idEquipoDestino, String comentario, String motivo) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_usuario_origen, id_usuario_destino, id_equipo_destino, "
                + "comentario, motivo, activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            setLongOrNull(ps, 3, idUsuarioActual);
            setLongOrNull(ps, 4, idUsuarioDestino);
            setLongOrNull(ps, 5, idEquipoDestino);
            ps.setString(6, limitar(comentario, 2000));
            ps.setString(7, limitar(motivo, 1000));
            setLongOrNull(ps, 8, idUsuarioActual);
            ps.executeUpdate();
        }
    }

    private static String comentarioMovimiento(String comentario, String defecto) {
        return hasText(comentario) ? comentario.trim() : defecto;
    }

    private static Long requerirId(Long id, String descripcion) {
        if (id == null) {
            throw new IllegalStateException("No se encontró el catálogo requerido: " + descripcion + ".");
        }
        return id;
    }

    private void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // El error original se reporta al usuario; el rollback fallido no debe ocultarlo.
        }
    }

    private static String limitar(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static final class ExpedienteEstadoActual {
        private final Long idEtapa;
        private final Long idEstado;
        private final String etapaCodigo;
        private final String estadoCodigo;

        private ExpedienteEstadoActual(Long idEtapa, Long idEstado, String etapaCodigo, String estadoCodigo) {
            this.idEtapa = idEtapa;
            this.idEstado = idEstado;
            this.etapaCodigo = etapaCodigo;
            this.estadoCodigo = estadoCodigo;
        }
    }

    private static final class Transicion {
        private final Long idEtapaDestino;
        private final Long idEstadoDestino;
        private final boolean requiereDocumento;

        private Transicion(Long idEtapaDestino, Long idEstadoDestino, boolean requiereDocumento) {
            this.idEtapaDestino = idEtapaDestino;
            this.idEstadoDestino = idEstadoDestino;
            this.requiereDocumento = requiereDocumento;
        }
    }
}
