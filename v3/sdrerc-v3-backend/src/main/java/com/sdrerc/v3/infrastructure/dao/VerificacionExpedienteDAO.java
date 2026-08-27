package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.application.CalendarioLaboralService;
import com.sdrerc.v3.domain.ObservacionVerificacionDTO;
import com.sdrerc.v3.domain.VerificacionExpedienteDTO;
import com.sdrerc.v3.domain.VerificacionRegistroDTO;
import com.sdrerc.v3.domain.VerificacionResultadoDTO;
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
 * Port ACOTADO de com.sdrerc.infrastructure.sdrercapp.dao.VerificacionExpedienteDAO (V2, ~1050
 * líneas) — solo el camino REAL y vigente de "Registrar Verificación", verificado leyendo
 * {@code crearPanelVerificacionOperativa()}/{@code registrarVerificacion()} en
 * {@code JPanelVerificacionV2.java} línea por línea, NO asumido de los nombres de método.
 *
 * <p><b>Hallazgo crítico (20/08/2026, investigación previa a este port)</b>: V2 declara MUCHOS
 * métodos/botones que jamás se conectan a un panel visible — {@code aprobarVerificacion} (sin
 * "Directa"), {@code enviarFirma}, {@code registrarObservacion} vía el dispatcher genérico
 * {@code registrarVerificacion(VerificacionRegistroDTO,...)}, y todo lo que depende de
 * {@code FirmaEmisionExpedienteService} (registrar firma/emisión/número de resolución/enviar a
 * ejecución o notificación desde el flujo de "Firma y Emisión" standalone) — código huérfano de un
 * diseño anterior a la consolidación "Firma/Emisión integrada en Verificación" (ver CLAUDE.md raíz).
 * NINGUNO de esos caminos se porta acá.</p>
 *
 * <p><b>Rediseño 21/08/2026, pedido explícito del usuario</b>: en V2 y en el primer port de V3,
 * "Registrar Verificación" era un ÚNICO botón que, además de guardar el resultado, YA MOVÍA el
 * expediente fuera de Verificación en el mismo clic (a Análisis si Observado, o directo a Ejecución
 * si Aprobado + resultado resolutivo) — sin pasar nunca por la sección "Destino operativo". El
 * usuario pidió separar esto en 2 acciones independientes, mismo patrón que ya existe en Análisis
 * ("Registrar resultado final" guarda sin mover de etapa; "Enviar a Verificación" es un botón
 * aparte que recién mueve el expediente):</p>
 * <ul>
 *   <li><b>"Registrar" ({@link #registrarObservado}/{@link #registrarAprobado})</b>: SOLO guarda el
 *   resultado (Aprobado/Observado) + comentario/observación, sin salir de la etapa VERIFICACION.
 *   Reutiliza 2 transiciones que YA existían en el catálogo real de V2 (nunca usadas hasta ahora en
 *   V3 porque el primer port saltaba directo al envío final): {@code REGISTRO_OBSERVACION_VERIFICACION}
 *   (EN_VERIFICACION → VERIFICACION/REQUIERE_CORRECCION) y {@code APROBACION_VERIFICACION}
 *   (EN_VERIFICACION → VERIFICACION/VERIFICADO) — esta 2ª fila es la MISMA que V2 usaba como primer
 *   salto de la rama "Eq. Supervisión" (ver {@link #aprobarConDestinoANotificacion}), ahora
 *   reutilizada también para Ejecución/Análisis vía destino operativo.</li>
 *   <li><b>"Enviar a ..." ({@link #aprobarVerificacionConDestino})</b>: recién mueve el expediente,
 *   partiendo del estado YA registrado (REQUIERE_CORRECCION si Observado, VERIFICADO si Aprobado)
 *   hacia el equipo elegido en "Destino operativo". <b>Rediseñado 21/08/2026 (2ª pasada, mismo día,
 *   pedido explícito del usuario): el criterio de ramificación ya NO es "resultado de Análisis
 *   resolutivo"</b> (ese criterio bloqueaba incorrectamente el combo, excluyendo Ejecución cuando en
 *   realidad el destino real depende de la CLASIFICACIÓN DEL DOCUMENTO — {@code
 *   TIPO_DOCUMENTO_ADJUNTO.CLASIFICACION}, ver `CLAUDE.md` raíz: FINAL=resoluciones/cartas finales,
 *   INTERMEDIO=cartas intermedias/oficios —, no del resultado de Análisis). El combo "Equipo destino"
 *   del frontend ya NO se deshabilita en ningún caso; solo se filtran las opciones válidas según el
 *   resultado YA registrado:
 *     <ul>
 *       <li>Observado (REQUIERE_CORRECCION) → solo puede elegirse {@code EQ_ANALISIS}:
 *       {@code DEVOLUCION_A_ANALISIS}: VERIFICACION/REQUIERE_CORRECCION → ANALISIS/OBSERVADO
 *       (fila ya existente en catálogo), SÍ reasigna responsable.</li>
 *       <li>Aprobado (VERIFICADO) + {@code EQ_EJECUCION} (documento FINAL/resolución) →
 *       {@code APROBACION_VERIFICACION}: VERIFICACION/VERIFICADO → EJECUCION/EN_EJECUCION (fila
 *       NUEVA, script 109), NO reasigna (Ejecución exige el mismo abogado de Análisis).</li>
 *       <li>Aprobado (VERIFICADO) + {@code EQ_SUPERVISION} (documento INTERMEDIO/oficio o carta
 *       intermedia) → 2 saltos automáticos en la MISMA transacción, sin cambios respecto al diseño
 *       anterior: {@code DERIVACION_A_NOTIFICACION}: VERIFICACION/VERIFICADO → NOTIFICACION/POR_ASIGNAR.</li>
 *       <li>Aprobado (VERIFICADO) + {@code EQ_ANALISIS} → RECHAZADO (un expediente ya Aprobado nunca
 *       vuelve a Análisis; ese destino es exclusivo de la rama Observado). La fila de catálogo
 *       {@code DEVOLUCION_A_ANALISIS VERIFICADO→ANALISIS/OBSERVADO} que la 1ª pasada de este
 *       rediseño había agregado al script 109 se retiró del script por quedar sin uso real.</li>
 *     </ul>
 *   Elegir entre Ejecución/Supervisión para un expediente Aprobado queda a criterio del usuario
 *   (según la clasificación real del documento que está verificando) — no hay detección automática
 *   ni bloqueo server-side de esa elección, solo la guía visual del frontend.</li>
 * </ul>
 * <p>Ver {@code db/sdrerc_app/scripts/109_transiciones_verificacion_registrar_enviar.sql} — agrega
 * la única fila de {@code flujo_transicion} que faltaba (VERIFICADO → EJECUCION/EN_EJECUCION); todo
 * lo demás ya existía en el catálogo real de V2. Script creado, NO ejecutado.</p>
 *
 * <p><b>Etapa FIRMA_EMISION descartada por completo</b>: verificado con {@code SELECT COUNT(*)
 * FROM expediente WHERE activo=1 AND etapa=FIRMA_EMISION} (20/08/2026) — 0 filas reales en la BD
 * compartida. Ningún camino vivo produce ese estado; se omite del alcance de la bandeja
 * ({@code etapaCodigo='VERIFICACION'} únicamente) y de {@link VerificacionExpedienteDTO}.</p>
 *
 * <p><b>Grilla "Documentos verificados"</b>: NO tiene tabla/DTO propia — lee y actualiza la MISMA
 * tabla {@code EXPEDIENTE_DOCUMENTO_ANALIZADO} que ya gestiona {@link DocumentoAnalisisDAO} (V2
 * confirma esto inyectando literalmente {@code DocumentoAnalisisDAO} dentro de
 * {@code VerificacionExpedienteDAO}). Se reutiliza el mismo DAO ya construido para Análisis; ver
 * {@code VerificacionController} para los endpoints delgados que delegan ahí.</p>
 *
 * <p><b>Campo "Comentario"</b>: en V2, el {@code JTextArea} que alimenta el comentario de las 3
 * ramas ({@code txtComentario}) NUNCA se agrega a un panel visible — en producción el comentario
 * real SIEMPRE llega vacío. V3 agrega un campo "Comentario (opcional)" real y funcional (mejora de
 * UX, libertad de diseño per Principio rector), el backend ya lo acepta en las 3 rutas.</p>
 */
@Repository
public class VerificacionExpedienteDAO {

    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String ETAPA_VERIFICACION = "VERIFICACION";
    private static final String ETAPA_ANALISIS = "ANALISIS";
    private static final String ETAPA_EJECUCION = "EJECUCION";
    private static final String ETAPA_NOTIFICACION = "NOTIFICACION";
    private static final String ESTADO_EN_VERIFICACION = "EN_VERIFICACION";
    private static final String ESTADO_VERIFICADO = "VERIFICADO";
    private static final String ESTADO_REQUIERE_CORRECCION = "REQUIERE_CORRECCION";
    private static final String ESTADO_ANALISIS_OBSERVADO = "OBSERVADO";
    private static final String ESTADO_EN_EJECUCION = "EN_EJECUCION";
    private static final String ESTADO_POR_ASIGNAR = "POR_ASIGNAR";
    private static final String ACCION_APROBACION = "APROBACION_VERIFICACION";
    private static final String ACCION_REGISTRO_OBSERVACION = "REGISTRO_OBSERVACION_VERIFICACION";
    private static final String ACCION_DEVOLUCION_ANALISIS = "DEVOLUCION_A_ANALISIS";
    private static final String ACCION_DERIVACION_NOTIFICACION = "DERIVACION_A_NOTIFICACION";
    private static final String EQUIPO_ANALISIS = "EQ_ANALISIS";
    private static final String EQUIPO_EJECUCION = "EQ_EJECUCION";
    private static final String EQUIPO_SUPERVISION = "EQ_SUPERVISION";
    private static final String TIPO_RELACION_DOCUMENTO_DUPLICADO = "DOCUMENTO_DUPLICADO_ASOCIADO";
    private static final String TIPO_RELACION_MISMA_ACTA_TITULAR = "MISMA_ACTA_TITULAR";

    private final DataSource dataSource;
    private final CalendarioLaboralService calendarioLaboralService;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final DocumentoAnalisisDAO documentoAnalisisDAO;

    public VerificacionExpedienteDAO(
            DataSource dataSource,
            CalendarioLaboralService calendarioLaboralService,
            CatalogoLookupDAO catalogoLookupDAO,
            DocumentoAnalisisDAO documentoAnalisisDAO) {
        this.dataSource = dataSource;
        this.calendarioLaboralService = calendarioLaboralService;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.documentoAnalisisDAO = documentoAnalisisDAO;
    }

    public List<VerificacionExpedienteDTO> buscarExpedientes(
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
        sql.append("  LEFT JOIN estado_documento ed2 ON ed2.id_estado_documento = da2.id_estado_documento ");
        sql.append("  LEFT JOIN tipo_documento_adjunto tda2 ON tda2.id_tipo_documento_adjunto = da2.id_tipo_documento_adjunto ");
        sql.append("  WHERE da2.id_expediente = e.id_expediente AND da2.activo = 1 ");
        sql.append("  AND UPPER(NVL(ed2.codigo, '')) = 'EN_DESPACHO') AS tipo_documento_pendiente, ");
        sql.append("ta.nombre AS tipo_acta, ea.numero_acta, ").append(nombrePersona("p")).append(" AS titular, ");
        sql.append("esol.fecha_recepcion, e.fecha_vencimiento, e.fecha_registro, ");
        sql.append("ur.nombre_completo AS responsable, e.id_usuario_responsable_actual, e.id_equipo_responsable_actual, ");
        sql.append("et.codigo AS etapa_codigo, est.codigo AS estado_codigo, ");
        sql.append("UPPER(NVL(").append(nombrePersona("p")).append(", 'ZZZ')) AS orden_titular, ");
        sql.append("NVL(esol.grupo_familiar, 0) AS grupo_familiar, ");
        sql.append("(SELECT COUNT(*) FROM expediente_documento_analizado da WHERE da.id_expediente = e.id_expediente AND da.activo = 1) AS documentos_analizados, ");
        sql.append("(SELECT MAX(NVL(tre.nombre, tre.codigo)) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) ");
        sql.append("  FROM expediente_evaluacion ev LEFT JOIN tipo_resultado_evaluacion tre ON tre.id_tipo_resultado_evaluacion = ev.id_tipo_resultado_evaluacion ");
        sql.append("  WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS ultimo_resultado, ");
        sql.append("(SELECT MAX(ua.nombre_completo) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) ");
        sql.append("  FROM expediente_evaluacion ev LEFT JOIN usuario ua ON ua.id_usuario = ev.creado_por ");
        sql.append("  WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS responsable_analisis, ");
        sql.append("(SELECT MAX(eqa.nombre) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) ");
        sql.append("  FROM expediente_evaluacion ev ");
        sql.append("  LEFT JOIN equipo_usuario eua ON eua.id_usuario = ev.creado_por AND eua.activo = 1 ");
        sql.append("  LEFT JOIN equipo eqa ON eqa.id_equipo = eua.id_equipo AND eqa.activo = 1 ");
        sql.append("  WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS equipo_analisis, ");
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
        params.add(ETAPA_VERIFICACION);

        appendFiltroPrincipalCanonico(sql, params);
        // Verificación SÍ filtra por visibilidad de asignación, incluyendo abogados supervisados
        // (el responsable actual sigue siendo el abogado de Análisis; el supervisor de Verificación
        // debe ver igual esos expedientes vía USUARIO_SUPERVISION) — ver CLAUDE.md raíz.
        sql.append(VisibilidadBandejaSql.construirCondicion(
                params, esAdmin, idUsuarioActual, idsEquipoActual,
                "e.id_usuario_responsable_actual", "e.id_equipo_responsable_actual", true));

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
                List<VerificacionExpedienteDTO> expedientes = new ArrayList<>();
                while (rs.next()) {
                    expedientes.add(map(conn, rs));
                }
                return expedientes;
            }
        }
    }

    /**
     * "Registrar Verificación" — rama Observado. SOLO GUARDA (comentario + descripción de la
     * observación) y mueve el estado DENTRO de la misma etapa VERIFICACION (EN_VERIFICACION →
     * REQUIERE_CORRECCION); no envía el expediente a Análisis todavía — eso ocurre en un segundo
     * paso independiente ({@link #aprobarVerificacionConDestino}), ver Javadoc de la clase.
     */
    public VerificacionResultadoDTO registrarObservado(Long idExpediente, String comentario, ObservacionVerificacionDTO observacion, Long idUsuarioActual) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEstadoActual estado = bloquearYObtenerEstado(conn, idExpediente);
                if (!ETAPA_VERIFICACION.equalsIgnoreCase(estado.etapaCodigo) || !ESTADO_EN_VERIFICACION.equalsIgnoreCase(estado.estadoCodigo)) {
                    throw new IllegalStateException(
                            "El expediente debe estar En verificación para registrar la observación (estado actual: "
                                    + estado.etapaCodigo + "/" + estado.estadoCodigo + ").");
                }
                Transicion transicion = requerirTransicion(conn, ACCION_REGISTRO_OBSERVACION, ETAPA_VERIFICACION, ESTADO_EN_VERIFICACION, ETAPA_VERIFICACION, ESTADO_REQUIERE_CORRECCION);
                validarRequisitosTransicion(conn, transicion, idExpediente);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_REGISTRO_OBSERVACION), "movimiento " + ACCION_REGISTRO_OBSERVACION);

                insertarObservacionVerificacion(conn, idExpediente, observacion, idUsuarioActual);
                actualizarEtapaEstado(conn, idExpediente, transicion.idEtapaDestino, transicion.idEstadoDestino, null, null, idUsuarioActual);
                insertarHistorial(conn, idExpediente, idMovimiento, idUsuarioActual, null, null,
                        comentarioMovimiento(comentario, "El expediente quedó observado. Use \"Destino operativo\" para enviarlo a Análisis."));

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new VerificacionResultadoDTO(idExpediente, null, ACCION_REGISTRO_OBSERVACION, ETAPA_VERIFICACION, ESTADO_REQUIERE_CORRECCION,
                        "La observación fue registrada. Use \"Destino operativo\" para enviar el expediente a Análisis.");
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
     * "Registrar Verificación" — rama Aprobado. SOLO GUARDA, mueve el estado DENTRO de la misma
     * etapa VERIFICACION (EN_VERIFICACION → VERIFICADO); ya NO envía directo a Ejecución (eso
     * quedaba oculto detrás de un solo clic incluso cuando el resultado de Análisis era resolutivo).
     * El envío real, a cualquier equipo (incluida Ejecución), ocurre siempre después vía
     * {@link #aprobarVerificacionConDestino}. Antes se llamaba {@code aprobarVerificacionDirecta}.
     */
    public VerificacionResultadoDTO registrarAprobado(Long idExpediente, String comentario, Long idUsuarioActual) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEstadoActual estado = bloquearYObtenerEstado(conn, idExpediente);
                if (!ETAPA_VERIFICACION.equalsIgnoreCase(estado.etapaCodigo) || !ESTADO_EN_VERIFICACION.equalsIgnoreCase(estado.estadoCodigo)) {
                    throw new IllegalStateException(
                            "El expediente debe estar En verificación para aprobarlo (estado actual: "
                                    + estado.etapaCodigo + "/" + estado.estadoCodigo + ").");
                }
                Transicion transicion = requerirTransicion(conn, ACCION_APROBACION, ETAPA_VERIFICACION, ESTADO_EN_VERIFICACION, ETAPA_VERIFICACION, ESTADO_VERIFICADO);
                validarRequisitosTransicion(conn, transicion, idExpediente);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_APROBACION), "movimiento " + ACCION_APROBACION);

                actualizarEtapaEstado(conn, idExpediente, transicion.idEtapaDestino, transicion.idEstadoDestino, null, null, idUsuarioActual);
                insertarHistorial(conn, idExpediente, idMovimiento, idUsuarioActual, null, null,
                        comentarioMovimiento(comentario, "La verificación fue aprobada. Use \"Destino operativo\" para enviarla al equipo correspondiente."));

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new VerificacionResultadoDTO(idExpediente, null, ACCION_APROBACION, ETAPA_VERIFICACION, ESTADO_VERIFICADO,
                        "La verificación fue aprobada. Use \"Destino operativo\" para enviar el expediente al equipo correspondiente.");
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
     * "Enviar a ..." — mueve el expediente desde el estado YA registrado (REQUIERE_CORRECCION si
     * Observado, VERIFICADO si Aprobado) hacia el equipo elegido en "Destino operativo". Antes
     * partía siempre de EN_VERIFICACION (cuando "Registrar" y "Enviar" eran el mismo clic); ver
     * Javadoc de la clase para el detalle completo de las 4 combinaciones posibles.
     */
    public VerificacionResultadoDTO aprobarVerificacionConDestino(
            Long idExpediente, String comentario, Long idEquipoDestino, Long idUsuarioDestino, Long idUsuarioActual) throws SQLException {
        if (idEquipoDestino == null || idUsuarioDestino == null) {
            throw new IllegalArgumentException("Seleccione equipo destino y usuario destino para enviar el expediente.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEstadoActual estado = bloquearYObtenerEstado(conn, idExpediente);
                boolean esObservado = ESTADO_REQUIERE_CORRECCION.equalsIgnoreCase(estado.estadoCodigo);
                boolean esAprobado = ESTADO_VERIFICADO.equalsIgnoreCase(estado.estadoCodigo);
                if (!ETAPA_VERIFICACION.equalsIgnoreCase(estado.etapaCodigo) || !(esObservado || esAprobado)) {
                    throw new IllegalStateException(
                            "Registre primero el resultado de la verificación (Aprobado u Observado) antes de enviarlo a un equipo destino (estado actual: "
                                    + estado.etapaCodigo + "/" + estado.estadoCodigo + ").");
                }
                String codigoEquipoDestino = obtenerCodigoEquipo(conn, idEquipoDestino);

                VerificacionResultadoDTO resultado;
                if (esObservado) {
                    if (!EQUIPO_ANALISIS.equalsIgnoreCase(codigoEquipoDestino)) {
                        throw new IllegalStateException("Un expediente Observado solo puede enviarse al equipo de Análisis.");
                    }
                    resultado = aprobarConDestinoAAnalisis(conn, idExpediente, comentario, ESTADO_REQUIERE_CORRECCION, idEquipoDestino, idUsuarioDestino, idUsuarioActual);
                } else if (EQUIPO_EJECUCION.equalsIgnoreCase(codigoEquipoDestino)) {
                    // Documento FINAL (resolución) -> Ejecución.
                    resultado = aprobarConDestinoAEjecucion(conn, idExpediente, comentario, ESTADO_VERIFICADO, idEquipoDestino, idUsuarioDestino, idUsuarioActual);
                } else if (EQUIPO_SUPERVISION.equalsIgnoreCase(codigoEquipoDestino)) {
                    // Documento INTERMEDIO (oficio/carta intermedia) -> Supervisión (2 saltos hasta Notificación/Por asignar).
                    resultado = aprobarConDestinoANotificacion(conn, idExpediente, comentario, idEquipoDestino, idUsuarioDestino, idUsuarioActual);
                } else {
                    throw new IllegalStateException("Un expediente Aprobado solo puede enviarse al equipo de Ejecución o de Supervisión.");
                }
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return resultado;
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

    private VerificacionResultadoDTO aprobarConDestinoAAnalisis(
            Connection conn, Long idExpediente, String comentario, String estadoOrigenCodigo,
            Long idEquipoDestino, Long idUsuarioDestino, Long idUsuarioActual) throws SQLException {
        Transicion transicion = requerirTransicion(conn, ACCION_DEVOLUCION_ANALISIS, ETAPA_VERIFICACION, estadoOrigenCodigo, ETAPA_ANALISIS, ESTADO_ANALISIS_OBSERVADO);
        validarRequisitosTransicion(conn, transicion, idExpediente);
        Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_DEVOLUCION_ANALISIS), "movimiento " + ACCION_DEVOLUCION_ANALISIS);
        actualizarEtapaEstado(conn, idExpediente, transicion.idEtapaDestino, transicion.idEstadoDestino, idEquipoDestino, idUsuarioDestino, idUsuarioActual);
        insertarHistorial(conn, idExpediente, idMovimiento, idUsuarioActual, idUsuarioDestino, idEquipoDestino, comentarioMovimiento(comentario, "Derivado al equipo de Análisis."));
        return new VerificacionResultadoDTO(idExpediente, null, ACCION_DEVOLUCION_ANALISIS, ETAPA_ANALISIS, ESTADO_ANALISIS_OBSERVADO, "El expediente fue enviado a Análisis para corrección.");
    }

    /** Eq. Ejecución: no reasigna responsable — Ejecución exige que la atienda el mismo abogado de Análisis. */
    private VerificacionResultadoDTO aprobarConDestinoAEjecucion(
            Connection conn, Long idExpediente, String comentario, String estadoOrigenCodigo,
            Long idEquipoDestino, Long idUsuarioDestino, Long idUsuarioActual) throws SQLException {
        Transicion transicion = requerirTransicion(conn, ACCION_APROBACION, ETAPA_VERIFICACION, estadoOrigenCodigo, ETAPA_EJECUCION, ESTADO_EN_EJECUCION);
        validarRequisitosTransicion(conn, transicion, idExpediente);
        Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_APROBACION), "movimiento " + ACCION_APROBACION);
        actualizarEtapaEstado(conn, idExpediente, transicion.idEtapaDestino, transicion.idEstadoDestino, null, null, idUsuarioActual);
        insertarHistorial(conn, idExpediente, idMovimiento, idUsuarioActual, idUsuarioDestino, idEquipoDestino, comentarioMovimiento(comentario, "El expediente fue enviado a Ejecución."));
        return new VerificacionResultadoDTO(idExpediente, null, ACCION_APROBACION, ETAPA_EJECUCION, ESTADO_EN_EJECUCION, "El expediente fue enviado a Ejecución.");
    }

    /** Eq. Supervisión (o cualquier otro no listado arriba): 2 saltos automáticos en la misma transacción hacia Notificación. */
    /**
     * Eq. Supervisión (o cualquier otro no listado arriba): a diferencia del diseño anterior (que
     * hacía 2 saltos en esta misma llamada — EN_VERIFICACION→VERIFICADO y luego VERIFICADO→
     * NOTIFICACION/POR_ASIGNAR — porque "Registrar" y "Enviar" eran el mismo clic), el primer salto
     * ya lo hizo {@link #registrarAprobado} antes; acá solo queda el segundo, con la reasignación de
     * responsable (idEquipoDestino/idUsuarioDestino) que antes ocurría en el salto ya removido.
     */
    private VerificacionResultadoDTO aprobarConDestinoANotificacion(
            Connection conn, Long idExpediente, String comentario, Long idEquipoDestino, Long idUsuarioDestino, Long idUsuarioActual) throws SQLException {
        Transicion transicion = requerirTransicion(conn, ACCION_DERIVACION_NOTIFICACION, ETAPA_VERIFICACION, ESTADO_VERIFICADO, ETAPA_NOTIFICACION, ESTADO_POR_ASIGNAR);
        validarRequisitosTransicion(conn, transicion, idExpediente);
        Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_DERIVACION_NOTIFICACION), "movimiento " + ACCION_DERIVACION_NOTIFICACION);
        actualizarEtapaEstado(conn, idExpediente, transicion.idEtapaDestino, transicion.idEstadoDestino, idEquipoDestino, idUsuarioDestino, idUsuarioActual);
        insertarHistorial(conn, idExpediente, idMovimiento, idUsuarioActual, idUsuarioDestino, idEquipoDestino, comentarioMovimiento(comentario, "El expediente fue enviado a Notificación, pendiente de asignar."));
        return new VerificacionResultadoDTO(idExpediente, null, ACCION_DERIVACION_NOTIFICACION, ETAPA_NOTIFICACION, ESTADO_POR_ASIGNAR, "El expediente fue enviado a Notificación, pendiente de asignar.");
    }

    private void insertarObservacionVerificacion(Connection conn, Long idExpediente, ObservacionVerificacionDTO observacion, Long idUsuario) throws SQLException {
        if (observacion == null || !observacion.hasDescripcion()) {
            return;
        }
        String sql = "INSERT INTO expediente_observacion ("
                + "id_expediente, origen_observacion, descripcion, subsanada, fecha_observacion, activo, creado_por, creado_en"
                + ") VALUES (?, 'VERIFICACION', ?, 0, SYSTIMESTAMP, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, limitar(observacion.getDescripcion(), 1500));
            setLongOrNull(ps, 3, idUsuario);
            ps.executeUpdate();
        }
    }

    private String obtenerCodigoEquipo(Connection conn, Long idEquipo) throws SQLException {
        if (idEquipo == null) {
            return null;
        }
        String sql = "SELECT codigo FROM equipo WHERE id_equipo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEquipo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("codigo") : null;
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
            throw new IllegalStateException("No existen documentos analizados para sustentar la verificación.");
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

    private VerificacionExpedienteDTO map(Connection conn, ResultSet rs) throws SQLException {
        Long idExpediente = getLongOrNull(rs, "id_expediente");
        Date fechaVencimiento = rs.getDate("fecha_vencimiento");
        return new VerificacionExpedienteDTO(
                idExpediente,
                rs.getString("numero_expediente"),
                rs.getString("numero_expediente_sgd"),
                rs.getString("procedimiento"),
                rs.getString("tipo_documento_pendiente"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                rs.getString("responsable"),
                getLongOrNull(rs, "id_usuario_responsable_actual"),
                getLongOrNull(rs, "id_equipo_responsable_actual"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                toLocalDate(fechaVencimiento),
                calendarioLaboralService.calcularDiasHabilesRestantes(conn, idExpediente, fechaVencimiento),
                toLocalDateTime(rs.getTimestamp("fecha_registro")),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getInt("grupo_familiar") == 1,
                rs.getInt("relaciones_confirmadas"),
                rs.getInt("documentos_analizados"),
                rs.getString("ultimo_resultado"),
                rs.getString("responsable_analisis"),
                rs.getString("equipo_analisis"));
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
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_usuario_origen, id_usuario_destino, id_equipo_destino, "
                + "comentario, activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            setLongOrNull(ps, 3, idUsuarioActual);
            setLongOrNull(ps, 4, idUsuarioDestino);
            setLongOrNull(ps, 5, idEquipoDestino);
            ps.setString(6, limitar(comentario, 2000));
            setLongOrNull(ps, 7, idUsuarioActual);
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

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
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
