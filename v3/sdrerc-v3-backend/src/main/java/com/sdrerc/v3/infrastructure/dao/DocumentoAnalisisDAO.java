package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.application.CalendarioLaboralService;
import com.sdrerc.v3.domain.AsignacionCartaRespuestaDTO;
import com.sdrerc.v3.domain.AsignacionHistorialDTO;
import com.sdrerc.v3.domain.DocumentoAnalizadoDTO;
import com.sdrerc.v3.domain.NotificacionAsignacionDocumentoDTO;
import com.sdrerc.v3.domain.NotificacionIntentoDTO;
import com.sdrerc.v3.domain.PlazoConfiguracionDTO;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Subconjunto de com.sdrerc.infrastructure.sdrercapp.dao.DocumentoAnalisisDAO (V2, 2838 líneas —
 * el DAO grande de documentos analizados que respalda Análisis/Verificación/Ejecución/
 * Notificación). Port acotado, crece incremento a incremento (mismo criterio ya documentado): hoy
 * incluye {@link #listarCartasRespuestaPendientes} (bandeja "Cartas de respuesta" de Asignación),
 * el CRUD jerárquico de documentos analizados (Análisis/Verificación/Ejecución), y desde
 * 21/08/2026 el camino REAL de la Bandeja Asignación de Notificación ({@link
 * #listarDocumentosAsignacionNotificacion}/{@link #asignarNotificacionMultiple}/{@link
 * #registrarFirmaDocumentoNotificacion}/{@link #registrarSupervisionEmisionAprobada}/{@link
 * #derivarDocumentoNotificacionAEjecucion}/{@link #listarHistorialAsignacionesNotificacion}).
 *
 * <p><b>Bandeja Asignación de Notificación (21/08/2026, primera fase del módulo, pedido explícito
 * del usuario)</b> — investigación previa contra el código real de V2
 * ({@code JPanelNotificacionV2.java}, 7493 líneas) confirmó: la grilla real que ve el usuario es
 * a nivel de DOCUMENTO ({@code listarDocumentosAsignacionNotificacion}, sin filtro de visibilidad
 * por fila — es una bandeja de coordinación, igual criterio que Registro/Asignación), NO a nivel
 * de expediente (existe una 2ª tabla completa en V2, {@code NotificacionTableModel}, que se calcula
 * pero NUNCA se agrega a ningún contenedor visible — código huérfano descartado por completo, no
 * portado). Simplificaciones deliberadas respecto a V2, documentadas explícitamente:
 * <ul>
 *   <li>{@link #asignarNotificacionMultiple} NO propaga el cambio de etapa/estado a expedientes
 *   asociados/duplicados confirmados ({@code ExpedienteEstadoPropagacionDAO.propagarEstadoAAsociados}
 *   en V2) — mismo criterio ya aceptado en {@code AsignacionExpedienteDAO} (Asignación de Registro),
 *   que tampoco lo porta.</li>
 *   <li>"Destino operativo" del mini-panel "② Asignación" en V2 permite derivar a
 *   {@code EQ_ANALISIS}/{@code EQ_EJECUCION} además de asignar a {@code EQ_VALIDACION}/
 *   {@code EQ_NOTIFICACION}. Esta fase solo porta {@link #asignarNotificacionMultiple} (los 2
 *   equipos de asignación real) y {@link #derivarDocumentoNotificacionAEjecucion} (Eq. Ejecución,
 *   sin reasignar responsable — mismo criterio que {@code VerificacionExpedienteDAO}: Ejecución
 *   exige el mismo abogado ya ligado). La derivación a Eq. Análisis (reutiliza
 *   {@code AsignacionExpedienteDAO.reasignarDesdeCartaRespuesta} en V2) queda DIFERIDA: ese método
 *   no está portado a V3 todavía (documentado como excluido en el Javadoc de
 *   {@code AsignacionExpedienteDAO}) — portarlo ahora habría expandido el alcance de este
 *   incremento más allá de lo pedido explícitamente.</li>
 * </ul>
 */
@Repository
public class DocumentoAnalisisDAO {

    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String CODIGO_MOVIMIENTO_ASIGNACION_NOTIFICACION = "ASIGNACION_NOTIFICACION";
    private static final String CODIGO_MOVIMIENTO_REASIGNACION_NOTIFICACION = "REASIGNACION_NOTIFICACION";
    private static final String CODIGO_MOVIMIENTO_SUPERVISION_EMISION_NOTIFICACION = "SUPERVISION_EMISION_NOTIFICACION";
    private static final String CODIGO_MOVIMIENTO_DEVOLUCION_EJECUCION = "DEVOLUCION_A_EJECUCION";

    /** Port literal de {@code CONDICION_ASIGNACION_NOTIFICACION} (V2, DocumentoAnalisisDAO). */
    private static final String CONDICION_ASIGNACION_NOTIFICACION =
            "((UPPER(NVL(eest.codigo, '')) = 'POR_ASIGNAR' "
            + "AND ((UPPER(NVL(tda.clasificacion, '')) = 'INTERMEDIO' AND UPPER(NVL(ed.codigo, '')) = 'EMITIDO') "
            + "OR (UPPER(NVL(tda.clasificacion, '')) = 'FINAL' AND UPPER(NVL(ed.codigo, '')) IN ('EN_DESPACHO', 'VALIDADO', 'EMITIDO')))) "
            + "OR (UPPER(NVL(eest.codigo, '')) = 'POR_VALIDAR' AND UPPER(NVL(ed.codigo, '')) = 'OBSERVADO' "
            + "AND UPPER(NVL(tda.clasificacion, '')) = 'FINAL'))";

    /**
     * Port literal de {@code CONDICION_VALIDACION_NOTIFICACION} (V2, DocumentoAnalisisDAO) —
     * confirmado contra el código real: solo cartas finales, expediente {@code POR_VALIDAR},
     * documento {@code EN_DESPACHO} (nunca llega OBSERVADO/VALIDADO/EMITIDO a esta bandeja).
     */
    private static final String CONDICION_VALIDACION_NOTIFICACION =
            "(UPPER(NVL(eest.codigo, '')) = 'POR_VALIDAR' AND UPPER(NVL(tda.clasificacion, '')) = 'FINAL' "
            + "AND UPPER(NVL(ed.codigo, '')) = 'EN_DESPACHO')";

    /**
     * Port literal de {@code CONDICION_BANDEJA_NOTIFICACION} (V2, DocumentoAnalisisDAO) — Bandeja
     * Notificación (3ra pestaña): expediente {@code EN_NOTIFICACION} ("Por notificar"), documento
     * ya {@code EMITIDO}, cualquier clasificación (INTERMEDIO o FINAL, ambas llegan acá tras pasar
     * por la 2ª asignación de la Bandeja Asignación hacia `EQ_NOTIFICACION`).
     */
    private static final String CONDICION_BANDEJA_NOTIFICACION =
            "(UPPER(NVL(eest.codigo, '')) = 'EN_NOTIFICACION' "
            + "AND UPPER(NVL(tda.clasificacion, '')) IN ('INTERMEDIO', 'FINAL') AND UPPER(NVL(ed.codigo, '')) = 'EMITIDO')";

    private final DataSource dataSource;
    private final CalendarioLaboralService calendarioLaboralService;
    private final PlazoConfiguracionDAO plazoConfiguracionDAO;
    private final CatalogoLookupDAO catalogoLookupDAO;

    public DocumentoAnalisisDAO(
            DataSource dataSource,
            CalendarioLaboralService calendarioLaboralService,
            PlazoConfiguracionDAO plazoConfiguracionDAO,
            CatalogoLookupDAO catalogoLookupDAO) {
        this.dataSource = dataSource;
        this.calendarioLaboralService = calendarioLaboralService;
        this.plazoConfiguracionDAO = plazoConfiguracionDAO;
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    /**
     * "Documentos Analizados" de la subpestaña "Análisis" (20/08/2026) — jerarquía de máximo 2
     * niveles ({@code id_documento_padre} nulo = principal). Port acotado de
     * {@code DocumentoAnalisisDAO#listarPorExpediente} (V2, 2838 líneas) — simplificado respecto al
     * original: se asume que TODAS las columnas de {@code EXPEDIENTE_DOCUMENTO_ANALIZADO} existen
     * (V2 tiene ~7 chequeos {@code soportaXxx(conn)} de compatibilidad retro con versiones viejas
     * del esquema; la BD compartida por V3 ya tiene todas esas columnas, mismo criterio de
     * simplificación ya establecido en el resto de V3, ver "Compatibilidad retro de V2 se omite" en
     * v3/CLAUDE.md).
     */
    public List<DocumentoAnalizadoDTO> listarPorExpediente(Connection conn, Long idExpediente) throws SQLException {
        List<DocumentoAnalizadoDTO> documentos = new ArrayList<>();
        if (idExpediente == null) {
            return documentos;
        }
        String sql = "SELECT da.id_documento_analizado, da.id_expediente, "
                + "da.id_documento_padre, NVL(da.nivel, 0) AS nivel, NVL(da.orden, 0) AS orden, "
                + "da.estado_respuesta, da.creado_en, da.modificado_en, "
                + "td.codigo AS tipo_documento_codigo, td.nombre AS tipo_documento_nombre, td.clasificacion, "
                + "ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre, "
                + "da.fecha_documento, da.numero_documento, da.detalle_observacion, da.descripcion, "
                + "NVL(da.notificado, 0) AS notificado, da.fecha_acuse, "
                + "NVL(da.requiere_respuesta, 0) AS requiere_respuesta, "
                + "da.confirmacion_respuesta, da.fecha_respuesta, da.numero_hoja_envio_respuesta, "
                + "NVL(e.requiere_publicacion, 0) AS requiere_publicacion, "
                + "(SELECT fecha_publicacion FROM ("
                + " SELECT p.fecha_publicacion FROM expediente_publicacion p "
                + " WHERE p.id_expediente = da.id_expediente AND p.activo = 1 "
                + " ORDER BY p.creado_en DESC, p.id_expediente_publicacion DESC"
                + ") WHERE ROWNUM = 1) AS fecha_publicacion, "
                + "da.existe_oposicion "
                + "FROM expediente_documento_analizado da "
                + "JOIN expediente e ON e.id_expediente = da.id_expediente "
                + "LEFT JOIN tipo_documento_adjunto td ON td.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                + "WHERE da.id_expediente = ? AND da.activo = 1 "
                + "ORDER BY NVL(da.id_documento_padre, da.id_documento_analizado), NVL(da.nivel, 0), "
                + "NVL(da.orden, da.id_documento_analizado), da.id_documento_analizado";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    documentos.add(new DocumentoAnalizadoDTO(
                            getLongOrNull(rs, "id_documento_analizado"),
                            getLongOrNull(rs, "id_expediente"),
                            null,
                            rs.getString("tipo_documento_codigo"),
                            rs.getString("tipo_documento_nombre"),
                            rs.getString("estado_documento_codigo"),
                            rs.getString("estado_documento_nombre"),
                            toLocalDate(rs.getDate("fecha_documento")),
                            rs.getString("numero_documento"),
                            rs.getString("descripcion"),
                            rs.getInt("notificado") == 1,
                            toLocalDate(rs.getDate("fecha_acuse")),
                            rs.getInt("requiere_respuesta") == 1,
                            rs.getString("confirmacion_respuesta"),
                            toLocalDate(rs.getDate("fecha_respuesta")),
                            rs.getString("numero_hoja_envio_respuesta"),
                            rs.getInt("requiere_publicacion") == 1,
                            toLocalDate(rs.getDate("fecha_publicacion")),
                            rs.getString("detalle_observacion"),
                            getLongOrNull(rs, "id_documento_padre"),
                            rs.getInt("nivel"),
                            rs.getInt("orden"),
                            rs.getString("estado_respuesta"),
                            true,
                            toLocalDateTime(rs.getTimestamp("creado_en")),
                            toLocalDateTime(rs.getTimestamp("modificado_en")),
                            getBooleanOrNull(rs, "existe_oposicion"),
                            rs.getString("clasificacion")));
                }
            }
        }
        return documentos;
    }

    /** Wrapper sin transacción externa — usado por el Service al listar la grilla (solo lectura). */
    public List<DocumentoAnalizadoDTO> listarPorExpediente(Long idExpediente) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return listarPorExpediente(conn, idExpediente);
        }
    }

    /**
     * Guarda 1 documento con su propia transacción — el ícono "Guardar" por fila de la grilla
     * maestro-detalle (ver {@code CLAUDE.md} raíz: "Guardar documentos no exige resultado/fundamento
     * final" ni mueve el expediente a Verificación). Devuelve el id REAL persistido (para que el
     * frontend pueda reemplazar el id temporal negativo de una fila recién creada y, si era un
     * documento padre recién guardado, habilitar de inmediato "+Relacionado" sobre él).
     */
    public Long guardarDocumentoAnalizado(Long idExpediente, DocumentoAnalizadoDTO documento, Long idUsuario) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Map<Long, Long> idsTemporales = guardarDocumentosJerarquicos(conn, idExpediente, Collections.singletonList(documento), idUsuario);
                Long idOriginal = documento.getIdDocumentoAnalizado();
                Long idReal = (idOriginal == null || idOriginal < 0L) ? idsTemporales.get(idOriginal) : idOriginal;
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return idReal;
            } catch (Exception ex) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                    // El error original se reporta al usuario; el rollback fallido no debe ocultarlo.
                }
                conn.setAutoCommit(previousAutoCommit);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            }
        }
    }

    /** Baja lógica de 1 documento con su propia transacción — ícono "Eliminar" por fila. */
    public void darBajaDocumentoAnalizado(Long idExpediente, Long idDocumentoAnalizado, Long idUsuario) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            darBajaDocumentoAnalizado(conn, idExpediente, idDocumentoAnalizado, idUsuario);
        }
    }

    public int contarPorExpediente(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expediente_documento_analizado WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Port literal de {@code DocumentoAnalisisService#tieneDocumentoFinalEnDespacho} (V2) — usado
     * por Ejecución (20/08/2026) antes de "Enviar a Supervisión": verifica que exista un documento
     * nivel 0 activo cuya clasificación de tipo sea FINAL (carta final/resolución) y cuyo estado sea
     * EN_DESPACHO. Mismo criterio documentado en el CLAUDE.md raíz ("Guardar Ejecución valida... la
     * carta de notificación final en despacho... ANTES de tocar la base de datos").
     */
    public boolean tieneDocumentoFinalEnDespacho(Long idExpediente) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expediente_documento_analizado da "
                + "JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                + "JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                + "WHERE da.id_expediente = ? AND da.activo = 1 AND da.nivel = 0 "
                + "AND UPPER(NVL(tda.clasificacion, '')) = 'FINAL' AND UPPER(ed.codigo) = 'EN_DESPACHO'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Guarda (inserta o actualiza) 1 o más documentos analizados dentro de la MISMA transacción del
     * llamador ({@code AnalisisExpedienteDAO}, que ya bloqueó el expediente). Port de
     * {@code DocumentoAnalisisDAO#guardarDocumentosJerarquicos} (V2): valida jerarquía (máx. nivel
     * 1, un hijo debe tener padre ya guardado — {@code idsTemporales} resuelve ids negativos
     * temporales de filas nuevas agregadas en la misma llamada, mismo patrón que el panel real de
     * V2 al agregar "+Documento"/"+Relacionado" antes de guardar). Devuelve el mapa id
     * temporal→id real de las filas insertadas (vacío si todas ya tenían id real).
     */
    public Map<Long, Long> guardarDocumentosJerarquicos(
            Connection conn, Long idExpediente, List<DocumentoAnalizadoDTO> documentos, Long idUsuario) throws SQLException {
        Map<Long, Long> idsTemporales = new LinkedHashMap<>();
        if (documentos == null) {
            return idsTemporales;
        }
        for (DocumentoAnalizadoDTO documento : documentos) {
            if (documento == null) {
                continue;
            }
            validarJerarquia(documento, idsTemporales);
            Long idPadre = resolverIdPadreJerarquico(documento, idsTemporales);
            Long idDocumento = documento.getIdDocumentoAnalizado();
            if (idDocumento == null || idDocumento < 0L) {
                Long nuevoId = insertarDocumentoAnalizadoJerarquico(conn, idExpediente, documento, idPadre, idUsuario);
                if (idDocumento != null) {
                    idsTemporales.put(idDocumento, nuevoId);
                }
            } else {
                actualizarDocumentoAnalizadoJerarquico(conn, idExpediente, documento, idPadre, idUsuario);
            }
        }
        return idsTemporales;
    }

    private void validarJerarquia(DocumentoAnalizadoDTO documento, Map<Long, Long> idsTemporales) throws SQLException {
        if (documento.getNivel() < 0 || documento.getNivel() > 1) {
            throw new SQLException("Solo se permiten documentos principales e hijos directos.");
        }
        if (documento.getNivel() == 0 && documento.getIdDocumentoPadre() != null) {
            throw new SQLException("Un documento principal no debe tener documento padre.");
        }
        if (documento.getNivel() == 1) {
            Long idPadre = documento.getIdDocumentoPadre();
            if (idPadre == null) {
                throw new SQLException("Todo documento relacionado/respuesta debe estar asociado a un documento principal.");
            }
            if (idPadre < 0L && !idsTemporales.containsKey(idPadre)) {
                throw new SQLException("El documento principal debe guardarse antes de registrar el documento relacionado.");
            }
        }
    }

    private static Long resolverIdPadreJerarquico(DocumentoAnalizadoDTO documento, Map<Long, Long> idsTemporales) {
        Long idPadre = documento.getIdDocumentoPadre();
        if (idPadre == null) {
            return null;
        }
        return idPadre < 0L ? idsTemporales.get(idPadre) : idPadre;
    }

    private Long insertarDocumentoAnalizadoJerarquico(
            Connection conn, Long idExpediente, DocumentoAnalizadoDTO documento, Long idPadre, Long idUsuario) throws SQLException {
        Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, documento.getTipoDocumentoCodigo());
        if (idTipoDocumento == null) {
            throw new SQLException("No se encontró el tipo de documento analizado: " + documento.getTipoDocumentoCodigo() + ".");
        }
        // 25/08/2026, pedido explícito del usuario ("la carta de respuesta no debería tener estado
        // documento"): un documento hijo (nivel=1, ej. carta de respuesta) nunca tiene un combo
        // "Estado" en ninguna grilla del sistema (Análisis ni Cartas de Respuesta) — `ID_ESTADO_DOCUMENTO`
        // ya es nullable en el esquema, así que solo se exige/resuelve para el documento padre (nivel=0).
        Long idEstadoDocumento = documento.getNivel() == 0
                ? catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo())
                : null;
        if (documento.getNivel() == 0 && idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        RespuestaPersistencia respuesta = respuestaPersistencia(documento);
        String sql = "INSERT INTO expediente_documento_analizado ("
                + "id_expediente, id_documento_padre, nivel, orden, id_tipo_documento_adjunto, id_estado_documento, "
                + "fecha_documento, numero_documento, detalle_observacion, descripcion, "
                + "notificado, fecha_acuse, requiere_respuesta, confirmacion_respuesta, "
                + "fecha_respuesta, numero_hoja_envio_respuesta, estado_respuesta, "
                + "activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_DOCUMENTO_ANALIZADO"})) {
            int index = 1;
            ps.setLong(index++, idExpediente);
            setLongOrNull(ps, index++, idPadre);
            ps.setInt(index++, documento.getNivel());
            ps.setInt(index++, documento.getOrden());
            ps.setLong(index++, idTipoDocumento);
            setLongOrNull(ps, index++, idEstadoDocumento);
            setDateOrNull(ps, index++, documento.getFechaDocumento());
            ps.setString(index++, limitar(documento.getNumeroDocumento(), 120));
            setStringOrNull(ps, index++, detalleObservacionPersistencia(documento));
            ps.setString(index++, limitar(documento.getDescripcion(), 1000));
            ps.setInt(index++, documento.isNotificado() ? 1 : 0);
            setDateOrNull(ps, index++, documento.getFechaAcuse());
            ps.setInt(index++, documento.isRequiereRespuesta() ? 1 : 0);
            setStringOrNull(ps, index++, respuesta.confirmacion);
            setDateOrNull(ps, index++, respuesta.fechaRespuesta);
            setStringOrNull(ps, index++, respuesta.hojaRespuesta);
            setStringOrNull(ps, index++, estadoRespuestaPersistencia(documento));
            setLongOrNull(ps, index, idUsuario);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el identificador generado del documento analizado.");
    }

    private void actualizarDocumentoAnalizadoJerarquico(
            Connection conn, Long idExpediente, DocumentoAnalizadoDTO documento, Long idPadre, Long idUsuario) throws SQLException {
        Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, documento.getTipoDocumentoCodigo());
        if (idTipoDocumento == null) {
            throw new SQLException("No se encontró el tipo de documento analizado: " + documento.getTipoDocumentoCodigo() + ".");
        }
        // Ver Javadoc de insertarDocumentoAnalizadoJerarquico: un documento hijo (nivel=1) nunca
        // exige/expone Estado — se persiste NULL en vez de intentar resolverlo.
        Long idEstadoDocumento = documento.getNivel() == 0
                ? catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo())
                : null;
        if (documento.getNivel() == 0 && idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        RespuestaPersistencia respuesta = respuestaPersistencia(documento);
        String sql = "UPDATE expediente_documento_analizado SET "
                + "id_documento_padre = ?, nivel = ?, orden = ?, id_tipo_documento_adjunto = ?, id_estado_documento = ?, "
                + "fecha_documento = ?, numero_documento = ?, detalle_observacion = ?, descripcion = ?, "
                + "notificado = ?, fecha_acuse = ?, requiere_respuesta = ?, confirmacion_respuesta = ?, "
                + "fecha_respuesta = ?, numero_hoja_envio_respuesta = ?, estado_respuesta = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            setLongOrNull(ps, index++, idPadre);
            ps.setInt(index++, documento.getNivel());
            ps.setInt(index++, documento.getOrden());
            ps.setLong(index++, idTipoDocumento);
            setLongOrNull(ps, index++, idEstadoDocumento);
            setDateOrNull(ps, index++, documento.getFechaDocumento());
            ps.setString(index++, limitar(documento.getNumeroDocumento(), 120));
            setStringOrNull(ps, index++, detalleObservacionPersistencia(documento));
            ps.setString(index++, limitar(documento.getDescripcion(), 1000));
            ps.setInt(index++, documento.isNotificado() ? 1 : 0);
            setDateOrNull(ps, index++, documento.getFechaAcuse());
            ps.setInt(index++, documento.isRequiereRespuesta() ? 1 : 0);
            setStringOrNull(ps, index++, respuesta.confirmacion);
            setDateOrNull(ps, index++, respuesta.fechaRespuesta);
            setStringOrNull(ps, index++, respuesta.hojaRespuesta);
            setStringOrNull(ps, index++, estadoRespuestaPersistencia(documento));
            setLongOrNull(ps, index++, idUsuario);
            ps.setLong(index++, documento.getIdDocumentoAnalizado());
            ps.setLong(index, idExpediente);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar el documento analizado (puede que ya no esté activo).");
            }
        }
    }

    /** Baja lógica de 1 o más documentos (padre + sus hijos activos, cuando corresponde) — port de {@code darBajaDocumentoAnalizado}. */
    public void darBajaDocumentoAnalizado(Connection conn, Long idExpediente, Long idDocumentoAnalizado, Long idUsuario) throws SQLException {
        if (idDocumentoAnalizado == null || idDocumentoAnalizado < 0L) {
            return;
        }
        String sql = "UPDATE expediente_documento_analizado SET activo = 0, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setLongOrNull(ps, 1, idUsuario);
            ps.setLong(2, idDocumentoAnalizado);
            ps.setLong(3, idExpediente);
            ps.executeUpdate();
        }
    }

    private static final class RespuestaPersistencia {
        private final String confirmacion;
        private final LocalDate fechaRespuesta;
        private final String hojaRespuesta;

        private RespuestaPersistencia(String confirmacion, LocalDate fechaRespuesta, String hojaRespuesta) {
            this.confirmacion = confirmacion;
            this.fechaRespuesta = fechaRespuesta;
            this.hojaRespuesta = hojaRespuesta;
        }
    }

    private static RespuestaPersistencia respuestaPersistencia(DocumentoAnalizadoDTO documento) {
        // 25/08/2026, bug real corregido (pedido explícito del usuario: "se resetean todos los
        // campos supuestamente grabados"): `requiereRespuesta` es un flag exclusivo del documento
        // PADRE ("¿Requiere respuesta?") — en la carta de respuesta (hijo, nivel=1) siempre llega en
        // `false` (ver `documentoVacio` en CartaRespuestaContent.tsx/AnalisisContent.tsx, que solo lo
        // marca `true` para nivel=0). Con la condición original, un hijo NUNCA podía persistir su
        // propia Confirmación de respuesta/Fecha Respuesta/Hoja de Envío — se guardaban siempre como
        // NULL sin ningún error visible. Para nivel=1 estos 3 campos se persisten siempre;
        // para nivel=0 se conserva el criterio original (solo si el padre requiere respuesta).
        boolean aplicaRespuesta = documento.getNivel() == 1 || documento.isRequiereRespuesta();
        String confirmacion = aplicaRespuesta ? normalizarConfirmacionRespuesta(documento.getConfirmacionRespuesta()) : null;
        LocalDate fechaRespuesta = aplicaRespuesta ? documento.getFechaRespuesta() : null;
        String hojaRespuesta = aplicaRespuesta ? limitar(emptyToNull(documento.getNumeroHojaEnvioRespuesta()), 120) : null;
        return new RespuestaPersistencia(confirmacion, fechaRespuesta, hojaRespuesta);
    }

    private static String normalizarConfirmacionRespuesta(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT).replace('Í', 'I');
        if ("SI".equals(normalized) || "NO".equals(normalized) || "PENDIENTE".equals(normalized)) {
            return normalized;
        }
        return "PENDIENTE";
    }

    private static String estadoRespuestaPersistencia(DocumentoAnalizadoDTO documento) {
        String estado = documento.getEstadoRespuesta();
        if (estado != null && !estado.trim().isEmpty()) {
            return limitar(estado.trim().toUpperCase(Locale.ROOT), 40);
        }
        return documento.isRequiereRespuesta() ? "PENDIENTE" : null;
    }

    private static String detalleObservacionPersistencia(DocumentoAnalizadoDTO documento) {
        return "OBSERVADO".equalsIgnoreCase(documento.getEstadoDocumentoCodigo())
                ? limitar(emptyToNull(documento.getDetalleObservacion()), 1000)
                : null;
    }

    private static String limitar(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
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

    private static void setStringOrNull(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    private static void setDateOrNull(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(value));
        }
    }

    private static Boolean getBooleanOrNull(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value == 1;
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    // Estado Final del documento en Notificación (4 estados, ver v3/CLAUDE.md y AGENTS.md):
    // POR_NOTIFICAR (sin intentos), ATENDIDO (algún intento EXITOSA/ubicado), POR_PUBLICAR
    // (intento 1 y 2 ambos FALLIDA/no ubicado) y PENDIENTE (resto). Se deriva de
    // expediente_notificacion + estado_notificacion, sin columna nueva.
    private static final String ESTADO_FINAL_NOTIFICACION_SQL =
            "(SELECT CASE "
            + "WHEN COUNT(*) = 0 THEN 'POR_NOTIFICAR' "
            + "WHEN MAX(CASE WHEN en3.codigo = 'EXITOSA' THEN 1 ELSE 0 END) = 1 THEN 'ATENDIDO' "
            + "WHEN MAX(CASE WHEN n3.numero_intento = 1 AND en3.codigo = 'FALLIDA' THEN 1 ELSE 0 END) = 1 "
            + "AND MAX(CASE WHEN n3.numero_intento = 2 AND en3.codigo = 'FALLIDA' THEN 1 ELSE 0 END) = 1 "
            + "THEN 'POR_PUBLICAR' "
            + "ELSE 'PENDIENTE' END "
            + "FROM expediente_notificacion n3 "
            + "JOIN estado_notificacion en3 ON en3.id_estado_notificacion = n3.id_estado_notificacion "
            + "WHERE n3.id_documento_analizado = da.id_documento_analizado AND n3.activo = 1) "
            + "AS estado_final_notificacion_codigo";

    /**
     * Port literal de {@code AGOTO_INTENTOS_DIRECTOS_SQL} (V2) — bandera 0/1 independiente de
     * {@code estado_final_notificacion_codigo}: "ambos intentos directos (1 y 2) ya fallaron", sin
     * importar si después se registra o no un 3er intento de PUBLICACION. A diferencia del estado
     * final (que pasa a ATENDIDO en cuanto la publicación se marca EXITOSA), esta bandera permanece
     * en 1 para siempre una vez que el documento entra al circuito de publicación — usada para que
     * la Bandeja Notificación (3ra pestaña) excluya el documento de forma DEFINITIVA (no "rebota"
     * de vuelta aunque luego se publique); la Bandeja Publicación (4ta pestaña, no construida
     * todavía) la usaría para incluir exclusivamente esos documentos.
     */
    private static final String AGOTO_INTENTOS_DIRECTOS_SQL =
            "(SELECT CASE WHEN "
            + "MAX(CASE WHEN n4.numero_intento = 1 AND en4.codigo = 'FALLIDA' THEN 1 ELSE 0 END) = 1 "
            + "AND MAX(CASE WHEN n4.numero_intento = 2 AND en4.codigo = 'FALLIDA' THEN 1 ELSE 0 END) = 1 "
            + "THEN 1 ELSE 0 END "
            + "FROM expediente_notificacion n4 "
            + "JOIN estado_notificacion en4 ON en4.id_estado_notificacion = n4.id_estado_notificacion "
            + "WHERE n4.id_documento_analizado = da.id_documento_analizado AND n4.activo = 1) "
            + "AS agoto_intentos_directos";

    /**
     * Port literal de {@code FECHA_PUBLICACION_NOTIF_SQL} (V2) — fecha del intento de Publicación
     * (tipo_notificacion=PUBLICACION) ya EXITOSA, expuesta a nivel de documento (fila padre) para
     * que la Bandeja Publicación pueda mostrarla sin necesitar expandir el árbol. Mismo
     * patrón/subconsulta que "Fecha Publ. Notif." de la Bandeja Cartas de Respuesta
     * ({@link #listarCartasRespuestaPendientes}).
     */
    private static final String FECHA_PUBLICACION_NOTIF_SQL =
            "(SELECT n5.fecha_envio FROM expediente_notificacion n5 "
            + "JOIN tipo_notificacion tn5 ON tn5.id_tipo_notificacion = n5.id_tipo_notificacion "
            + "JOIN estado_notificacion en5 ON en5.id_estado_notificacion = n5.id_estado_notificacion "
            + "WHERE n5.id_documento_analizado = da.id_documento_analizado AND n5.activo = 1 "
            + "AND UPPER(tn5.codigo) = 'PUBLICACION' AND UPPER(en5.codigo) = 'EXITOSA' AND ROWNUM = 1) "
            + "AS fecha_publicacion_notif";

    /**
     * Bandeja "Cartas de respuesta" de Asignación: documentos analizados INTERMEDIO que requieren
     * respuesta y ya fueron notificados, cuyo estado final de notificación es ATENDIDO (el
     * ciudadano ya fue ubicado en algún intento directo, o el documento ya quedó Publicado), y que
     * el expediente todavía NO volvió a etapa ANALISIS (en cuanto se deriva de vuelta, deja de
     * listarse aquí — el seguimiento pasa a Análisis).
     */
    public List<AsignacionCartaRespuestaDTO> listarCartasRespuestaPendientes() throws SQLException {
        List<AsignacionCartaRespuestaDTO> items = new ArrayList<>();
        String sqlInterno = "SELECT da.id_documento_analizado, da.id_expediente, e.numero_expediente, "
                + "esol.numero_expediente_sgd, esol.fecha_recepcion AS fecha_solicitud, "
                + nombrePersona("p") + " AS titular, "
                + "tda.codigo AS tipo_documento_codigo, tda.nombre AS tipo_documento_nombre, "
                + "ed.nombre AS estado_documento_nombre, "
                + "et.codigo AS etapa_codigo, "
                + "da.fecha_documento, da.numero_documento, "
                + "da.descripcion, NVL(da.requiere_respuesta, 0) AS requiere_respuesta, "
                + "NVL(da.notificado, 0) AS notificado, da.fecha_acuse, da.confirmacion_respuesta, "
                + "da.fecha_respuesta, da.numero_hoja_envio_respuesta, "
                + "NVL(e.requiere_publicacion, 0) AS requiere_publicacion, "
                + "(SELECT fecha_publicacion FROM ("
                + " SELECT p2.fecha_publicacion FROM expediente_publicacion p2 "
                + " WHERE p2.id_expediente = da.id_expediente AND p2.activo = 1 "
                + " ORDER BY p2.creado_en DESC, p2.id_expediente_publicacion DESC"
                + ") WHERE ROWNUM = 1) AS fecha_publicacion_edicto, "
                + "(SELECT n2.fecha_envio FROM expediente_notificacion n2 "
                + " JOIN tipo_notificacion tn2 ON tn2.id_tipo_notificacion = n2.id_tipo_notificacion "
                + " JOIN estado_notificacion en2 ON en2.id_estado_notificacion = n2.id_estado_notificacion "
                + " WHERE n2.id_documento_analizado = da.id_documento_analizado AND n2.activo = 1 "
                + " AND UPPER(tn2.codigo) = 'PUBLICACION' AND UPPER(en2.codigo) = 'EXITOSA' AND ROWNUM = 1"
                + ") AS fecha_publicacion_notif, "
                + ESTADO_FINAL_NOTIFICACION_SQL + ", "
                // Mejora V3 (sin equivalente en V2, ver Javadoc de AsignacionCartaRespuestaDTO#isTieneRespuestaRegistrada):
                // si ya existe una carta de respuesta (nivel=1) activa vinculada a este documento.
                + "(SELECT COUNT(1) FROM expediente_documento_analizado hijo "
                + " WHERE hijo.activo = 1 AND hijo.nivel = 1 AND hijo.id_documento_padre = da.id_documento_analizado) AS total_respuestas "
                + "FROM expediente_documento_analizado da "
                + "JOIN expediente e ON e.id_expediente = da.id_expediente AND e.activo = 1 "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                + "LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente "
                + " AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                + "LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                + "LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                + "WHERE da.activo = 1 "
                + "AND NVL(da.requiere_respuesta, 0) = 1 "
                + "AND NVL(da.notificado, 0) = 1 "
                + "AND UPPER(NVL(tda.clasificacion, '')) = 'INTERMEDIO' ";
        String sql = "SELECT * FROM (" + sqlInterno + ") "
                + "WHERE estado_final_notificacion_codigo = 'ATENDIDO' "
                + "AND UPPER(etapa_codigo) <> 'ANALISIS' "
                + "ORDER BY fecha_documento DESC NULLS LAST, id_documento_analizado DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Map<String, Integer> diasPlazoCache = new HashMap<>();
            while (rs.next()) {
                String tipoDocumentoCodigo = rs.getString("tipo_documento_codigo");
                String etapaCodigo = rs.getString("etapa_codigo");
                LocalDate fechaAcuse = toLocalDate(rs.getDate("fecha_acuse"));
                LocalDate fechaPublicacionEdicto = toLocalDate(rs.getDate("fecha_publicacion_edicto"));
                LocalDate fechaPublicacionNotif = toLocalDate(rs.getDate("fecha_publicacion_notif"));

                VencimientoCarta vencimiento = resolverVencimientoCarta(
                        conn, tipoDocumentoCodigo, fechaAcuse, fechaPublicacionNotif, fechaPublicacionEdicto, diasPlazoCache);

                items.add(new AsignacionCartaRespuestaDTO(
                        getLongOrNull(rs, "id_documento_analizado"),
                        getLongOrNull(rs, "id_expediente"),
                        rs.getString("numero_expediente"),
                        rs.getString("numero_expediente_sgd"),
                        rs.getString("titular"),
                        rs.getString("tipo_documento_nombre"),
                        rs.getString("estado_documento_nombre"),
                        toLocalDate(rs.getDate("fecha_documento")),
                        rs.getString("numero_documento"),
                        rs.getString("descripcion"),
                        rs.getInt("requiere_respuesta") == 1,
                        rs.getInt("notificado") == 1,
                        fechaAcuse,
                        rs.getString("confirmacion_respuesta"),
                        toLocalDate(rs.getDate("fecha_respuesta")),
                        rs.getString("numero_hoja_envio_respuesta"),
                        rs.getInt("requiere_publicacion") == 1,
                        fechaPublicacionEdicto,
                        fechaPublicacionNotif,
                        tipoDocumentoCodigo,
                        etapaCodigo,
                        vencimiento == null ? null : vencimiento.dias,
                        vencimiento == null ? null : vencimiento.diasPlazo,
                        vencimiento == null ? null : vencimiento.fecha,
                        vencimiento == null ? null : vencimiento.tipoAlerta,
                        rs.getInt("total_respuestas") > 0,
                        toLocalDate(rs.getDate("fecha_solicitud"))));
            }
        }
        return items;
    }

    private static final class VencimientoCarta {
        private final Long dias;
        private final Integer diasPlazo;
        private final LocalDate fecha;
        private final String tipoAlerta;

        private VencimientoCarta(Long dias, Integer diasPlazo, LocalDate fecha, String tipoAlerta) {
            this.dias = dias;
            this.diasPlazo = diasPlazo;
            this.fecha = fecha;
            this.tipoAlerta = tipoAlerta;
        }
    }

    /**
     * Alerta de vencimiento en cascada de una carta intermedia: mientras el documento no tenga
     * Fecha Publ. Edicto registrada, la alerta activa es el vencimiento de RESPUESTA (desde Fecha
     * Acuse o, si no hay acuse directo, desde Fecha Publ. Notif.). En cuanto se registra Fecha
     * Publ. Edicto, la alerta cambia a PUBLICACION (desde esa fecha, plazo = código +
     * "_PUBLICACION"). Sin plazo configurado para el código que corresponda, no hay alerta (null).
     */
    private VencimientoCarta resolverVencimientoCarta(
            Connection conn,
            String tipoDocumentoCodigo,
            LocalDate fechaAcuse,
            LocalDate fechaPublicacionNotif,
            LocalDate fechaPublicacionEdicto,
            Map<String, Integer> diasPlazoCache) throws SQLException {
        if (fechaPublicacionEdicto != null) {
            Integer diasPlazoPublicacion = resolverDiasPlazoCarta(conn, tipoDocumentoCodigo + "_PUBLICACION", diasPlazoCache);
            if (diasPlazoPublicacion != null) {
                LocalDate fechaVencimiento = calendarioLaboralService.calcularFechaVencimientoHabil(
                        conn, fechaPublicacionEdicto, diasPlazoPublicacion);
                long dias = calendarioLaboralService.calcularDiasHabilesRestantes(conn, LocalDate.now(), fechaVencimiento);
                return new VencimientoCarta(dias, diasPlazoPublicacion, fechaVencimiento, "PUBLICACION");
            }
        }
        LocalDate fechaInicioRespuesta = fechaAcuse != null ? fechaAcuse : fechaPublicacionNotif;
        if (fechaInicioRespuesta != null) {
            Integer diasPlazoRespuesta = resolverDiasPlazoCarta(conn, tipoDocumentoCodigo, diasPlazoCache);
            if (diasPlazoRespuesta != null) {
                LocalDate fechaVencimiento = calendarioLaboralService.calcularFechaVencimientoHabil(
                        conn, fechaInicioRespuesta, diasPlazoRespuesta);
                long dias = calendarioLaboralService.calcularDiasHabilesRestantes(conn, LocalDate.now(), fechaVencimiento);
                return new VencimientoCarta(dias, diasPlazoRespuesta, fechaVencimiento, "RESPUESTA");
            }
        }
        return null;
    }

    /**
     * Días configurados en PLAZO_CONFIGURACION para el código de plazo de una carta intermedia,
     * cacheado por código dentro de la misma consulta.
     */
    private Integer resolverDiasPlazoCarta(Connection conn, String codigoPlazo, Map<String, Integer> cache) throws SQLException {
        if (codigoPlazo == null) {
            return null;
        }
        if (cache.containsKey(codigoPlazo)) {
            return cache.get(codigoPlazo);
        }
        PlazoConfiguracionDTO plazo = plazoConfiguracionDAO.obtenerPlazoPorCodigo(conn, codigoPlazo);
        Integer dias = plazo == null || plazo.getDiasPlazo() == null || plazo.getDiasPlazo() <= 0
                ? null
                : plazo.getDiasPlazo();
        cache.put(codigoPlazo, dias);
        return dias;
    }

    // ------------------------------------------------------------------------------------------
    // Notificación — Bandeja Asignación (21/08/2026). Ver Javadoc de la clase para el alcance.
    // ------------------------------------------------------------------------------------------

    /**
     * Cola completa de la Bandeja Asignación de Notificación — SIN filtro de visibilidad por fila
     * (bandeja de coordinación, mismo criterio que Registro/Asignación: el supervisor de
     * notificación debe ver y repartir toda la cola, no solo lo ya asignado a sí mismo). Port
     * literal de {@code CONDICION_ASIGNACION_NOTIFICACION} (V2).
     */
    public List<NotificacionAsignacionDocumentoDTO> listarDocumentosAsignacionNotificacion() throws SQLException {
        List<NotificacionAsignacionDocumentoDTO> items = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT da.id_documento_analizado, da.id_expediente, e.numero_expediente, esol.numero_expediente_sgd, "
                    + "tda.clasificacion, tda.nombre AS tipo_documento_nombre, "
                    + "da.numero_documento, da.fecha_documento, "
                    + nombrePersona("p") + " AS titular, "
                    + "ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre, "
                    + "(SELECT COUNT(*) FROM expediente_relacion r WHERE r.activo = 1 "
                    + "AND (r.id_expediente_principal = e.id_expediente OR r.id_expediente_relacionado = e.id_expediente)) AS relaciones_confirmadas, "
                    + "CASE WHEN da.id_usuario_notificacion IS NOT NULL THEN 1 ELSE 0 END AS asignado, "
                    + "da.numero_hoja_envio_notificacion, un.nombre_completo AS usuario_notificacion_actual, "
                    + "eest.codigo AS estado_expediente_codigo, eest.nombre AS estado_expediente_nombre, "
                    + "e.fecha_vencimiento, esol.fecha_recepcion, ur.nombre_completo AS responsable, "
                    + "(SELECT MAX(NVL(tre.nombre, tre.codigo)) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) "
                    + " FROM expediente_evaluacion ev LEFT JOIN tipo_resultado_evaluacion tre ON tre.id_tipo_resultado_evaluacion = ev.id_tipo_resultado_evaluacion "
                    + " WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS resultado_analisis "
                    + "FROM expediente_documento_analizado da "
                    + "JOIN expediente e ON e.id_expediente = da.id_expediente AND e.activo = 1 "
                    + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                    + "LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente "
                    + " AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                    + "LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                    + "LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                    + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                    + "LEFT JOIN usuario un ON un.id_usuario = da.id_usuario_notificacion "
                    + "LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual "
                    + "LEFT JOIN estado_expediente eest ON eest.id_estado = e.id_estado_actual "
                    + "WHERE da.activo = 1 AND " + CONDICION_ASIGNACION_NOTIFICACION + " "
                    + "ORDER BY da.fecha_documento DESC NULLS LAST, da.id_documento_analizado DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapNotificacionAsignacion(conn, rs));
                }
            }
        }
        return items;
    }

    /**
     * Cola de la Bandeja Validación de Notificación (21/08/2026) — a diferencia de la Bandeja
     * Asignación, SÍ filtra por visibilidad de fila (confirmado contra V2 real:
     * {@code listarDocumentosValidacion(...)} pasa {@code true} para activar el filtro, a diferencia
     * de la Bandeja Asignación que pasa {@code false}) — el validador solo ve lo asignado a sí mismo
     * o a su equipo (`EQ_VALIDACION`), vía {@code id_usuario_notificacion`/`id_equipo_notificacion}
     * (mismas columnas que ya usa {@code asignarNotificacionMultiple} para fijar el responsable).
     *
     * <p>30/08/2026, fix real (encontrado al replicar el diseño de columnas de Bandeja de
     * supervisión aquí): este método usa el mismo mapper compartido {@link #mapNotificacionAsignacion}
     * que {@link #listarDocumentosAsignacionNotificacion()} — al extender ese mapper en la Parte 2
     * para leer {@code fecha_recepcion}/{@code responsable}/{@code resultado_analisis}, este SQL se
     * quedó sin esas 3 columnas, lo que hacía fallar esta bandeja completa con
     * {@code SQLException: columna inválida} en cuanto se ejercitaba. Se agregan acá los mismos JOIN/
     * subconsulta ya usados ahí (copiados verbatim, mismo patrón Oracle {@code KEEP (DENSE_RANK LAST
     * ORDER BY ...)}), corrigiendo el bug y poblando de paso las 3 columnas nuevas para el mismo
     * rediseño de "Fecha solicitud"/"Abogado"/"Resultado" pedido para esta bandeja.
     */
    public List<NotificacionAsignacionDocumentoDTO> listarDocumentosValidacion(
            boolean esAdmin, Long idUsuarioActual, List<Long> idsEquipoActual) throws SQLException {
        List<NotificacionAsignacionDocumentoDTO> items = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            StringBuilder sql = new StringBuilder(
                    "SELECT da.id_documento_analizado, da.id_expediente, e.numero_expediente, esol.numero_expediente_sgd, "
                    + "tda.clasificacion, tda.nombre AS tipo_documento_nombre, "
                    + "da.numero_documento, da.fecha_documento, "
                    + nombrePersona("p") + " AS titular, "
                    + "ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre, "
                    + "(SELECT COUNT(*) FROM expediente_relacion r WHERE r.activo = 1 "
                    + "AND (r.id_expediente_principal = e.id_expediente OR r.id_expediente_relacionado = e.id_expediente)) AS relaciones_confirmadas, "
                    + "CASE WHEN da.id_usuario_notificacion IS NOT NULL THEN 1 ELSE 0 END AS asignado, "
                    + "da.numero_hoja_envio_notificacion, un.nombre_completo AS usuario_notificacion_actual, "
                    + "eest.codigo AS estado_expediente_codigo, eest.nombre AS estado_expediente_nombre, "
                    + "e.fecha_vencimiento, esol.fecha_recepcion, ur.nombre_completo AS responsable, "
                    + "(SELECT MAX(NVL(tre.nombre, tre.codigo)) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) "
                    + " FROM expediente_evaluacion ev LEFT JOIN tipo_resultado_evaluacion tre ON tre.id_tipo_resultado_evaluacion = ev.id_tipo_resultado_evaluacion "
                    + " WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS resultado_analisis "
                    + "FROM expediente_documento_analizado da "
                    + "JOIN expediente e ON e.id_expediente = da.id_expediente AND e.activo = 1 "
                    + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                    + "LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente "
                    + " AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                    + "LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                    + "LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                    + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                    + "LEFT JOIN usuario un ON un.id_usuario = da.id_usuario_notificacion "
                    + "LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual "
                    + "LEFT JOIN estado_expediente eest ON eest.id_estado = e.id_estado_actual "
                    + "WHERE da.activo = 1 AND " + CONDICION_VALIDACION_NOTIFICACION + " ");
            sql.append(VisibilidadBandejaSql.construirCondicion(
                    params, esAdmin, idUsuarioActual, idsEquipoActual, "da.id_usuario_notificacion", "da.id_equipo_notificacion"));
            sql.append("ORDER BY da.fecha_documento DESC NULLS LAST, da.id_documento_analizado DESC");
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(mapNotificacionAsignacion(conn, rs));
                    }
                }
            }
        }
        return items;
    }

    /**
     * Cola de la Bandeja Notificación (3ra pestaña, 22/08/2026) — excluye DEFINITIVAMENTE los
     * documentos que ya agotaron sus 2 intentos directos (ver {@link #AGOTO_INTENTOS_DIRECTOS_SQL}):
     * esos pasan a mostrarse exclusivamente en la Bandeja Publicación (4ta pestaña, no construida
     * todavía en V3), incluso después de que la publicación ya se registre/marque Publicado — no
     * "rebotan" de vuelta aquí. Mismo filtro de visibilidad por fila que Bandeja Validación
     * ({@code da.id_usuario_notificacion}/{@code da.id_equipo_notificacion}).
     */
    public List<NotificacionAsignacionDocumentoDTO> listarDocumentosNotificacion(
            boolean esAdmin, Long idUsuarioActual, List<Long> idsEquipoActual) throws SQLException {
        List<NotificacionAsignacionDocumentoDTO> items = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            StringBuilder sqlInterno = new StringBuilder(
                    "SELECT da.id_documento_analizado, da.id_expediente, e.numero_expediente, esol.numero_expediente_sgd, "
                    + "tda.clasificacion, tda.nombre AS tipo_documento_nombre, "
                    + "da.numero_documento, da.fecha_documento, "
                    + nombrePersona("p") + " AS titular, "
                    + "ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre, "
                    + "(SELECT COUNT(*) FROM expediente_relacion r WHERE r.activo = 1 "
                    + "AND (r.id_expediente_principal = e.id_expediente OR r.id_expediente_relacionado = e.id_expediente)) AS relaciones_confirmadas, "
                    + "CASE WHEN da.id_usuario_notificacion IS NOT NULL THEN 1 ELSE 0 END AS asignado, "
                    + "da.numero_hoja_envio_notificacion, un.nombre_completo AS usuario_notificacion_actual, "
                    + "eest.codigo AS estado_expediente_codigo, eest.nombre AS estado_expediente_nombre, "
                    + "e.fecha_vencimiento, "
                    + "(SELECT COUNT(*) FROM expediente_notificacion en2 "
                    + " WHERE en2.id_documento_analizado = da.id_documento_analizado AND en2.activo = 1) AS total_intentos, "
                    + ESTADO_FINAL_NOTIFICACION_SQL + ", " + AGOTO_INTENTOS_DIRECTOS_SQL + " "
                    + "FROM expediente_documento_analizado da "
                    + "JOIN expediente e ON e.id_expediente = da.id_expediente AND e.activo = 1 "
                    + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                    + "LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente "
                    + " AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                    + "LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                    + "LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                    + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                    + "LEFT JOIN usuario un ON un.id_usuario = da.id_usuario_notificacion "
                    + "LEFT JOIN estado_expediente eest ON eest.id_estado = e.id_estado_actual "
                    + "WHERE da.activo = 1 AND " + CONDICION_BANDEJA_NOTIFICACION + " ");
            sqlInterno.append(VisibilidadBandejaSql.construirCondicion(
                    params, esAdmin, idUsuarioActual, idsEquipoActual, "da.id_usuario_notificacion", "da.id_equipo_notificacion"));
            String sql = "SELECT * FROM (" + sqlInterno + ") notif "
                    + "WHERE notif.agoto_intentos_directos = 0 "
                    + "ORDER BY notif.fecha_documento DESC NULLS LAST, notif.id_documento_analizado DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(mapNotificacionBandeja(conn, rs));
                    }
                }
            }
        }
        return items;
    }

    private NotificacionAsignacionDocumentoDTO mapNotificacionBandeja(Connection conn, ResultSet rs) throws SQLException {
        Date fechaVencimiento = rs.getDate("fecha_vencimiento");
        Long idExpediente = getLongOrNull(rs, "id_expediente");
        String estadoFinalCodigo = rs.getString("estado_final_notificacion_codigo");
        return new NotificacionAsignacionDocumentoDTO(
                getLongOrNull(rs, "id_documento_analizado"),
                idExpediente,
                rs.getString("numero_expediente"),
                rs.getString("numero_expediente_sgd"),
                rs.getString("clasificacion"),
                rs.getString("tipo_documento_nombre"),
                rs.getString("numero_documento"),
                toLocalDate(rs.getDate("fecha_documento")),
                rs.getString("titular"),
                rs.getString("estado_documento_codigo"),
                rs.getString("estado_documento_nombre"),
                rs.getInt("relaciones_confirmadas"),
                rs.getInt("asignado") == 1,
                rs.getString("numero_hoja_envio_notificacion"),
                rs.getString("usuario_notificacion_actual"),
                rs.getString("estado_expediente_codigo"),
                rs.getString("estado_expediente_nombre"),
                toLocalDate(fechaVencimiento),
                calendarioLaboralService.calcularDiasHabilesRestantes(conn, idExpediente, fechaVencimiento),
                rs.getInt("total_intentos"),
                estadoFinalCodigo,
                nombreEstadoFinalNotificacion(estadoFinalCodigo),
                null, null, null, null);
    }

    /** Port literal de {@code nombreEstadoFinalNotificacion} (V2) — nombre visible de los 4 estados finales. */
    private static String nombreEstadoFinalNotificacion(String codigo) {
        if (codigo == null) {
            return "Por notificar";
        }
        switch (codigo.trim().toUpperCase()) {
            case "ATENDIDO":
                return "Atendido";
            case "POR_PUBLICAR":
                return "Por publicar";
            case "PENDIENTE":
                return "Pendiente";
            case "POR_NOTIFICAR":
            default:
                return "Por notificar";
        }
    }

    /**
     * Cola de la Bandeja Publicación (4ta pestaña, 24/08/2026) — mismo universo que
     * {@link #listarDocumentosNotificacion} ({@link #CONDICION_BANDEJA_NOTIFICACION} + misma
     * visibilidad por fila), acotado a los documentos que YA agotaron sus 2 intentos directos
     * ({@code agoto_intentos_directos = 1}, justo lo opuesto de la Bandeja Notificación). El
     * documento permanece aquí aunque ya se haya marcado "Publicado" — el filtro depende de
     * {@code AGOTO_INTENTOS_DIRECTOS_SQL}, no de {@code estado_final_notificacion_codigo} (que sí
     * pasa a {@code ATENDIDO} una vez publicado, ver {@link #mapPublicacionBandeja}), para que el
     * documento nunca "rebote" de vuelta a la Bandeja Notificación.
     */
    public List<NotificacionAsignacionDocumentoDTO> listarDocumentosBandejaPublicacion(
            boolean esAdmin, Long idUsuarioActual, List<Long> idsEquipoActual) throws SQLException {
        List<NotificacionAsignacionDocumentoDTO> items = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            StringBuilder sqlInterno = new StringBuilder(
                    "SELECT da.id_documento_analizado, da.id_expediente, e.numero_expediente, esol.numero_expediente_sgd, "
                    + "tda.clasificacion, tda.nombre AS tipo_documento_nombre, "
                    + "da.numero_documento, da.fecha_documento, "
                    + nombrePersona("p") + " AS titular, "
                    + "ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre, "
                    + "(SELECT COUNT(*) FROM expediente_relacion r WHERE r.activo = 1 "
                    + "AND (r.id_expediente_principal = e.id_expediente OR r.id_expediente_relacionado = e.id_expediente)) AS relaciones_confirmadas, "
                    + "CASE WHEN da.id_usuario_notificacion IS NOT NULL THEN 1 ELSE 0 END AS asignado, "
                    + "da.numero_hoja_envio_notificacion, un.nombre_completo AS usuario_notificacion_actual, "
                    + "eest.codigo AS estado_expediente_codigo, eest.nombre AS estado_expediente_nombre, "
                    + "e.fecha_vencimiento, "
                    + "(SELECT COUNT(*) FROM expediente_notificacion en2 "
                    + " WHERE en2.id_documento_analizado = da.id_documento_analizado AND en2.activo = 1) AS total_intentos, "
                    + ESTADO_FINAL_NOTIFICACION_SQL + ", " + AGOTO_INTENTOS_DIRECTOS_SQL + ", " + FECHA_PUBLICACION_NOTIF_SQL + " "
                    + "FROM expediente_documento_analizado da "
                    + "JOIN expediente e ON e.id_expediente = da.id_expediente AND e.activo = 1 "
                    + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                    + "LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente "
                    + " AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                    + "LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                    + "LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                    + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                    + "LEFT JOIN usuario un ON un.id_usuario = da.id_usuario_notificacion "
                    + "LEFT JOIN estado_expediente eest ON eest.id_estado = e.id_estado_actual "
                    + "WHERE da.activo = 1 AND " + CONDICION_BANDEJA_NOTIFICACION + " ");
            sqlInterno.append(VisibilidadBandejaSql.construirCondicion(
                    params, esAdmin, idUsuarioActual, idsEquipoActual, "da.id_usuario_notificacion", "da.id_equipo_notificacion"));
            String sql = "SELECT * FROM (" + sqlInterno + ") pub "
                    + "WHERE pub.agoto_intentos_directos = 1 "
                    + "ORDER BY pub.fecha_documento DESC NULLS LAST, pub.id_documento_analizado DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(mapPublicacionBandeja(conn, rs));
                    }
                }
            }
        }
        return items;
    }

    /**
     * Port de {@code mapNotificacionBandeja}, con 2 diferencias puntuales de la Bandeja Publicación
     * (24/08/2026): (1) sí lee {@code fecha_publicacion_notif} (columna nueva de esta consulta); (2)
     * traduce {@code ATENDIDO} a "Publicado" — en este universo (documentos que ya agotaron sus 2
     * intentos directos) ese código SOLO puede provenir de un intento de Publicación exitoso, nunca
     * de un intento 1/2 (imposible aquí), port de {@code textoEstadoFinalPublicacion} (V2).
     */
    private NotificacionAsignacionDocumentoDTO mapPublicacionBandeja(Connection conn, ResultSet rs) throws SQLException {
        Date fechaVencimiento = rs.getDate("fecha_vencimiento");
        Long idExpediente = getLongOrNull(rs, "id_expediente");
        String estadoFinalCodigo = rs.getString("estado_final_notificacion_codigo");
        String estadoFinalNombre = "ATENDIDO".equalsIgnoreCase(estadoFinalCodigo)
                ? "Publicado"
                : nombreEstadoFinalNotificacion(estadoFinalCodigo);
        return new NotificacionAsignacionDocumentoDTO(
                getLongOrNull(rs, "id_documento_analizado"),
                idExpediente,
                rs.getString("numero_expediente"),
                rs.getString("numero_expediente_sgd"),
                rs.getString("clasificacion"),
                rs.getString("tipo_documento_nombre"),
                rs.getString("numero_documento"),
                toLocalDate(rs.getDate("fecha_documento")),
                rs.getString("titular"),
                rs.getString("estado_documento_codigo"),
                rs.getString("estado_documento_nombre"),
                rs.getInt("relaciones_confirmadas"),
                rs.getInt("asignado") == 1,
                rs.getString("numero_hoja_envio_notificacion"),
                rs.getString("usuario_notificacion_actual"),
                rs.getString("estado_expediente_codigo"),
                rs.getString("estado_expediente_nombre"),
                toLocalDate(fechaVencimiento),
                calendarioLaboralService.calcularDiasHabilesRestantes(conn, idExpediente, fechaVencimiento),
                rs.getInt("total_intentos"),
                estadoFinalCodigo,
                estadoFinalNombre,
                toLocalDate(rs.getDate("fecha_publicacion_notif")),
                null, null, null);
    }

    /**
     * Intentos directos (1 y 2) de un documento — Bandeja Notificación (3ra pestaña). Port literal
     * de {@code listarIntentosNotificacion} (V2); a diferencia de V2 no filtra
     * {@code tipo_notificacion=PUBLICACION} en SQL (esa clasificación no existe hoy en el universo
     * de {@link #listarDocumentosNotificacion} porque el 3er intento de Publicación pertenece a la
     * Bandeja Publicación, todavía no construida) — se deja el filtro explícito de todos modos por
     * si algún documento llega a tener un intento de ese tipo mezclado, mismo criterio defensivo que
     * V2 ({@code filtrarIntentosDirectos}).
     */
    public List<NotificacionIntentoDTO> listarIntentosNotificacion(Long idDocumentoAnalizado) throws SQLException {
        List<NotificacionIntentoDTO> items = new ArrayList<>();
        if (idDocumentoAnalizado == null) {
            return items;
        }
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT n.id_expediente_notificacion, n.id_expediente, n.id_documento_analizado, "
                    + "n.numero_intento, tn.codigo AS tipo_notificacion_codigo, tn.nombre AS tipo_notificacion, "
                    + "n.fecha_envio, en.codigo AS estado_notificacion_codigo, en.nombre AS estado_notificacion, "
                    + "n.codigo_notificacion, "
                    + "(SELECT MAX(c.fecha_recepcion) FROM expediente_cargo_acuse c "
                    + " WHERE c.id_expediente_notificacion = n.id_expediente_notificacion AND c.activo = 1) AS fecha_recepcion "
                    + "FROM expediente_notificacion n "
                    + "JOIN tipo_notificacion tn ON tn.id_tipo_notificacion = n.id_tipo_notificacion "
                    + "JOIN estado_notificacion en ON en.id_estado_notificacion = n.id_estado_notificacion "
                    + "WHERE n.id_documento_analizado = ? AND n.activo = 1 AND UPPER(tn.codigo) <> 'PUBLICACION' "
                    + "ORDER BY n.numero_intento, n.id_expediente_notificacion";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idDocumentoAnalizado);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        LocalDateTime fechaRecepcion = toLocalDateTime(rs.getTimestamp("fecha_recepcion"));
                        String estadoCodigo = rs.getString("estado_notificacion_codigo");
                        boolean ubicado = "EXITOSA".equalsIgnoreCase(estadoCodigo) || fechaRecepcion != null;
                        items.add(new NotificacionIntentoDTO(
                                getLongOrNull(rs, "id_expediente_notificacion"),
                                getLongOrNull(rs, "id_expediente"),
                                getLongOrNull(rs, "id_documento_analizado"),
                                rs.getInt("numero_intento"),
                                rs.getString("tipo_notificacion_codigo"),
                                rs.getString("tipo_notificacion"),
                                toLocalDateTime(rs.getTimestamp("fecha_envio")),
                                estadoCodigo,
                                rs.getString("estado_notificacion"),
                                rs.getString("codigo_notificacion"),
                                fechaRecepcion,
                                ubicado,
                                null));
                    }
                }
            }
        }
        return items;
    }

    /**
     * Los 3 registros posibles de un documento (intento 1, intento 2 y la fila de Publicación) —
     * Bandeja Publicación (4ta pestaña, 24/08/2026). A diferencia de {@link #listarIntentosNotificacion}
     * (que EXCLUYE {@code tipo_notificacion=PUBLICACION}, exclusivo de la Bandeja Notificación), acá
     * se INCLUYE — es la única bandeja donde ese 3er registro debe verse y editarse.
     */
    public List<NotificacionIntentoDTO> listarIntentosPublicacion(Long idDocumentoAnalizado) throws SQLException {
        List<NotificacionIntentoDTO> items = new ArrayList<>();
        if (idDocumentoAnalizado == null) {
            return items;
        }
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT n.id_expediente_notificacion, n.id_expediente, n.id_documento_analizado, "
                    + "n.numero_intento, tn.codigo AS tipo_notificacion_codigo, tn.nombre AS tipo_notificacion, "
                    + "n.fecha_envio, en.codigo AS estado_notificacion_codigo, en.nombre AS estado_notificacion, "
                    + "n.codigo_notificacion, "
                    + "(SELECT MAX(c.fecha_recepcion) FROM expediente_cargo_acuse c "
                    + " WHERE c.id_expediente_notificacion = n.id_expediente_notificacion AND c.activo = 1) AS fecha_recepcion "
                    + "FROM expediente_notificacion n "
                    + "JOIN tipo_notificacion tn ON tn.id_tipo_notificacion = n.id_tipo_notificacion "
                    + "JOIN estado_notificacion en ON en.id_estado_notificacion = n.id_estado_notificacion "
                    + "WHERE n.id_documento_analizado = ? AND n.activo = 1 "
                    + "ORDER BY n.numero_intento, n.id_expediente_notificacion";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idDocumentoAnalizado);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        LocalDateTime fechaRecepcion = toLocalDateTime(rs.getTimestamp("fecha_recepcion"));
                        String estadoCodigo = rs.getString("estado_notificacion_codigo");
                        boolean ubicado = "EXITOSA".equalsIgnoreCase(estadoCodigo) || fechaRecepcion != null;
                        items.add(new NotificacionIntentoDTO(
                                getLongOrNull(rs, "id_expediente_notificacion"),
                                getLongOrNull(rs, "id_expediente"),
                                getLongOrNull(rs, "id_documento_analizado"),
                                rs.getInt("numero_intento"),
                                rs.getString("tipo_notificacion_codigo"),
                                rs.getString("tipo_notificacion"),
                                toLocalDateTime(rs.getTimestamp("fecha_envio")),
                                estadoCodigo,
                                rs.getString("estado_notificacion"),
                                rs.getString("codigo_notificacion"),
                                fechaRecepcion,
                                ubicado,
                                null));
                    }
                }
            }
        }
        return items;
    }

    /**
     * "+ Agregar intento", rama borrador — port literal de {@code registrarIntentoNotificacion}
     * (V2, overload de 6 args): el intento SIEMPRE nace {@code PENDIENTE} (comportamiento histórico;
     * confirmar recepción es una acción posterior separada). {@code numeroIntento} se resuelve en
     * servidor ({@link #obtenerSiguienteIntentoNotificacion}), no lo decide el cliente — evita una
     * condición de carrera si 2 usuarios agregan un intento al mismo documento casi simultáneamente.
     */
    public void registrarIntentoNotificacion(
            Long idExpediente,
            Long idDocumentoAnalizado,
            String tipoNotificacionCodigo,
            String codigoNotificacion,
            LocalDate fechaEnvio,
            Long idUsuarioActual) throws SQLException {
        if (idExpediente == null || idDocumentoAnalizado == null) {
            throw new IllegalArgumentException("Seleccione expediente y documento para registrar el intento.");
        }
        String tipoCodigo = hasTextEstado(tipoNotificacionCodigo) ? tipoNotificacionCodigo.trim().toUpperCase() : "VIRTUAL";
        try (Connection conn = dataSource.getConnection()) {
            Long idTipoNotificacion = catalogoLookupDAO.obtenerTipoNotificacionId(conn, tipoCodigo);
            if (idTipoNotificacion == null) {
                throw new IllegalStateException("No se encontró el tipo de notificación " + tipoCodigo + ".");
            }
            Long idEstadoInicial = catalogoLookupDAO.obtenerEstadoNotificacionId(conn, "PENDIENTE");
            if (idEstadoInicial == null) {
                throw new IllegalStateException("No se encontró el estado de notificación PENDIENTE.");
            }
            int numeroIntento = obtenerSiguienteIntentoNotificacion(conn, idDocumentoAnalizado);
            if (numeroIntento > 3) {
                throw new IllegalStateException("Solo se permiten hasta 3 intentos de notificación por documento.");
            }
            String sql = "INSERT INTO expediente_notificacion ("
                    + "id_expediente, id_documento_analizado, id_tipo_notificacion, id_estado_notificacion, "
                    + "numero_intento, fecha_envio, resultado, requiere_publicacion, codigo_notificacion, "
                    + "observacion, activo, creado_por, creado_en"
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?, 1, ?, SYSTIMESTAMP)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idExpediente);
                ps.setLong(2, idDocumentoAnalizado);
                ps.setLong(3, idTipoNotificacion);
                ps.setLong(4, idEstadoInicial);
                ps.setInt(5, numeroIntento);
                if (fechaEnvio == null) {
                    ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                } else {
                    ps.setTimestamp(6, Timestamp.valueOf(fechaEnvio.atStartOfDay()));
                }
                ps.setString(7, "PENDIENTE");
                setStringOrNull(ps, 8, limitar(codigoNotificacion, 60));
                setStringOrNull(ps, 9, "Intento registrado desde bandeja de notificación.");
                setLongOrNull(ps, 10, idUsuarioActual);
                ps.executeUpdate();
            }
        }
    }

    private int obtenerSiguienteIntentoNotificacion(Connection conn, Long idDocumentoAnalizado) throws SQLException {
        String sql = "SELECT NVL(MAX(numero_intento), 0) + 1 AS siguiente "
                + "FROM expediente_notificacion WHERE id_documento_analizado = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idDocumentoAnalizado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("siguiente") : 1;
            }
        }
    }

    /**
     * Guardar una fila de intento YA persistida sin marcarla "Ubicado" — port literal de
     * {@code actualizarIntentoNotificacion} (V2). El resultado final (Estado) se recibe ya resuelto
     * por el cliente ({@code PENDIENTE}/{@code ENVIADA}/{@code FALLIDA}), replicando la misma
     * derivación que hacía V2 en la UI a partir de las columnas "Estado"/"Estado Notificación" — ver
     * Javadoc del frontend (`NotificacionNotificacionBandejaPage.tsx`).
     */
    public void actualizarIntentoNotificacion(
            Long idExpedienteNotificacion,
            String tipoNotificacionCodigo,
            String estadoNotificacionCodigo,
            String codigoNotificacion,
            LocalDate fechaEnvio,
            Long idUsuarioActual) throws SQLException {
        if (idExpedienteNotificacion == null) {
            throw new IllegalArgumentException("Seleccione el intento de notificación a actualizar.");
        }
        String tipoCodigo = hasTextEstado(tipoNotificacionCodigo) ? tipoNotificacionCodigo.trim().toUpperCase() : "VIRTUAL";
        String estadoCodigo = hasTextEstado(estadoNotificacionCodigo) ? estadoNotificacionCodigo.trim().toUpperCase() : "PENDIENTE";
        try (Connection conn = dataSource.getConnection()) {
            Long idTipoNotificacion = catalogoLookupDAO.obtenerTipoNotificacionId(conn, tipoCodigo);
            if (idTipoNotificacion == null) {
                throw new IllegalStateException("No se encontró el tipo de notificación " + tipoCodigo + ".");
            }
            Long idEstadoNotificacion = catalogoLookupDAO.obtenerEstadoNotificacionId(conn, estadoCodigo);
            if (idEstadoNotificacion == null) {
                throw new IllegalStateException("No se encontró el estado de notificación " + estadoCodigo + ".");
            }
            String sql = "UPDATE expediente_notificacion SET "
                    + "id_tipo_notificacion = ?, id_estado_notificacion = ?, resultado = ?, "
                    + "codigo_notificacion = ?, fecha_envio = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                    + "WHERE id_expediente_notificacion = ? AND activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idTipoNotificacion);
                ps.setLong(2, idEstadoNotificacion);
                ps.setString(3, estadoCodigo);
                setStringOrNull(ps, 4, limitar(codigoNotificacion, 60));
                if (fechaEnvio == null) {
                    ps.setNull(5, Types.TIMESTAMP);
                } else {
                    ps.setTimestamp(5, Timestamp.valueOf(fechaEnvio.atStartOfDay()));
                }
                setLongOrNull(ps, 6, idUsuarioActual);
                ps.setLong(7, idExpedienteNotificacion);
                if (ps.executeUpdate() == 0) {
                    throw new SQLException("No se encontró el intento de notificación indicado.");
                }
            }
        }
    }

    /**
     * Confirma la recepción (Fecha Recepción = Fecha Acuse) de un intento — port literal de
     * {@code confirmarRecepcionIntentoNotificacion} (V2): marca el intento EXITOSA/ubicado, registra
     * el cargo en {@code expediente_cargo_acuse} con el código digitado (Código Notificación si la
     * modalidad es virtual, Usuario Notificación si es presencial — ambos comparten el mismo campo
     * de texto, {@code recibido_por}) y refleja la misma fecha como "fecha acuse" del documento
     * analizado ({@code expediente_documento_analizado.fecha_acuse}/{@code notificado}, ya usadas
     * por Análisis/Cartas de Respuesta).
     */
    public void confirmarRecepcionIntentoNotificacion(
            Long idExpediente,
            Long idExpedienteNotificacion,
            Long idDocumentoAnalizado,
            String codigoORecibidoPor,
            LocalDate fechaEnvio,
            LocalDate fechaRecepcion,
            Long idUsuarioActual) throws SQLException {
        if (idExpediente == null || idExpedienteNotificacion == null) {
            throw new IllegalArgumentException("Seleccione el intento de notificación a confirmar.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idEstadoNotificacion = catalogoLookupDAO.obtenerEstadoNotificacionId(conn, "EXITOSA");
                if (idEstadoNotificacion == null) {
                    throw new IllegalStateException("No se encontró el estado de notificación EXITOSA.");
                }
                String sqlUpdate = "UPDATE expediente_notificacion SET "
                        + "id_estado_notificacion = ?, resultado = 'EXITOSA', codigo_notificacion = ?, "
                        + (fechaEnvio != null ? "fecha_envio = ?, " : "")
                        + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                        + "WHERE id_expediente_notificacion = ? AND activo = 1";
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    int index = 1;
                    ps.setLong(index++, idEstadoNotificacion);
                    setStringOrNull(ps, index++, limitar(codigoORecibidoPor, 60));
                    if (fechaEnvio != null) {
                        ps.setTimestamp(index++, Timestamp.valueOf(fechaEnvio.atStartOfDay()));
                    }
                    setLongOrNull(ps, index++, idUsuarioActual);
                    ps.setLong(index, idExpedienteNotificacion);
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("No se encontró el intento de notificación indicado.");
                    }
                }
                Long idEstadoCargo = catalogoLookupDAO.obtenerEstadoCargoAcuseId(conn, "CARGO_RECIBIDO");
                if (idEstadoCargo == null) {
                    throw new IllegalStateException("No se encontró el estado de cargo CARGO_RECIBIDO.");
                }
                LocalDate fechaAcuse = fechaRecepcion == null ? LocalDate.now() : fechaRecepcion;
                String sqlInsert = "INSERT INTO expediente_cargo_acuse ("
                        + "id_expediente, id_expediente_notificacion, id_estado_cargo_acuse, fecha_recepcion, "
                        + "recibido_por, activo, creado_por, creado_en"
                        + ") VALUES (?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
                try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                    ps.setLong(1, idExpediente);
                    ps.setLong(2, idExpedienteNotificacion);
                    ps.setLong(3, idEstadoCargo);
                    ps.setTimestamp(4, Timestamp.valueOf(fechaAcuse.atStartOfDay()));
                    setStringOrNull(ps, 5, limitar(codigoORecibidoPor, 250));
                    setLongOrNull(ps, 6, idUsuarioActual);
                    ps.executeUpdate();
                }
                if (idDocumentoAnalizado != null) {
                    String sqlAcuse = "UPDATE expediente_documento_analizado SET "
                            + "fecha_acuse = ?, notificado = 1, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                            + "WHERE id_documento_analizado = ? AND activo = 1";
                    try (PreparedStatement ps = conn.prepareStatement(sqlAcuse)) {
                        ps.setDate(1, Date.valueOf(fechaAcuse));
                        setLongOrNull(ps, 2, idUsuarioActual);
                        ps.setLong(3, idDocumentoAnalizado);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
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

    /** Baja lógica de un intento ya registrado — port literal de {@code darBajaIntentoNotificacion} (V2). */
    public void darBajaIntentoNotificacion(Long idExpedienteNotificacion, Long idUsuarioActual) throws SQLException {
        if (idExpedienteNotificacion == null) {
            return;
        }
        try (Connection conn = dataSource.getConnection()) {
            String sql = "UPDATE expediente_notificacion SET activo = 0, "
                    + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                    + "WHERE id_expediente_notificacion = ? AND activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                setLongOrNull(ps, 1, idUsuarioActual);
                ps.setLong(2, idExpedienteNotificacion);
                ps.executeUpdate();
            }
        }
    }

    /**
     * "Registrar Validación" (Bandeja Validación de Notificación, 21/08/2026) — port literal de
     * {@code registrarResultadoValidacion} (V2, DocumentoAnalisisDAO). Confirmado contra V2 real:
     * NO usa el motor de {@code flujo_transicion} en absoluto (verificado con `SELECT` de solo
     * lectura contra Oracle: ninguna fila de esa tabla respalda el caso Aprobado) — son 2 `UPDATE`
     * directos dentro de la misma transacción.
     *
     * <ul>
     *   <li><b>Aprobado</b> (resultadoCodigo distinto de {@code OBSERVADO}): documento → estado
     *   {@code VALIDADO} (su responsable de notificación — equipo/usuario/hoja de envío — se deja
     *   INTACTO); expediente → revierte a {@code POR_ASIGNAR}, para reaparecer en la Bandeja
     *   Asignación del Supervisor ya con la Parte "Emitir" ({@link
     *   NotificacionAsignacionDocumentoDTO#isListoParaFirma()}). Sin propagación a expedientes
     *   asociados ({@code ExpedienteEstadoPropagacionDAO} en V2, no portado — mismo criterio ya
     *   aceptado en {@link #asignarNotificacionMultiple}).</li>
     *   <li><b>Observado</b>: documento → estado {@code OBSERVADO} + limpia su responsable de
     *   notificación (equipo/usuario/hoja de envío = NULL) + comentario en {@code
     *   detalle_observacion}; expediente NO cambia de estado (sigue {@code POR_VALIDAR}) pero se
     *   limpia su responsable actual — con esto el documento ya reaparece en la Bandeja Asignación
     *   del Supervisor (Parte "Asignación"), vía la condición ya portada en {@link
     *   #CONDICION_ASIGNACION_NOTIFICACION} (`POR_VALIDAR` + `OBSERVADO` + `FINAL`).</li>
     * </ul>
     *
     * Sin registro de historial (V2 tampoco lo hace acá — confirmado con grep sobre el método real,
     * cero {@code INSERT INTO expediente_historial}).
     */
    public void registrarResultadoValidacion(
            Long idDocumentoAnalizado, String resultadoCodigo, String comentario, Long idUsuarioActual) throws SQLException {
        if (idDocumentoAnalizado == null || !hasTextEstado(resultadoCodigo)) {
            throw new IllegalArgumentException("Seleccione el resultado de la validación.");
        }
        boolean observado = "OBSERVADO".equalsIgnoreCase(resultadoCodigo.trim());
        String estadoDestino = observado ? "OBSERVADO" : "VALIDADO";
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, estadoDestino);
                if (idEstadoDocumento == null) {
                    throw new IllegalStateException("No se encontró el estado de documento " + estadoDestino + ".");
                }
                String sqlDocumento = "UPDATE expediente_documento_analizado SET id_estado_documento = ?, "
                        + (observado
                                ? "id_equipo_notificacion = NULL, id_usuario_notificacion = NULL, numero_hoja_envio_notificacion = NULL, "
                                        + "detalle_observacion = ?, "
                                : "")
                        + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                        + "WHERE id_documento_analizado = ? AND activo = 1";
                try (PreparedStatement ps = conn.prepareStatement(sqlDocumento)) {
                    int idx = 1;
                    ps.setLong(idx++, idEstadoDocumento);
                    if (observado) {
                        setStringOrNull(ps, idx++, comentario);
                    }
                    setLongOrNull(ps, idx++, idUsuarioActual);
                    ps.setLong(idx, idDocumentoAnalizado);
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalStateException("No se encontró el documento a validar.");
                    }
                }

                Long idExpediente = obtenerIdExpedientePorDocumento(conn, idDocumentoAnalizado);
                if (idExpediente == null) {
                    throw new IllegalStateException("No se encontró el expediente del documento.");
                }
                if (observado) {
                    String sqlExpediente = "UPDATE expediente SET id_equipo_responsable_actual = NULL, "
                            + "id_usuario_responsable_actual = NULL, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                            + "WHERE id_expediente = ? AND activo = 1";
                    try (PreparedStatement ps = conn.prepareStatement(sqlExpediente)) {
                        setLongOrNull(ps, 1, idUsuarioActual);
                        ps.setLong(2, idExpediente);
                        ps.executeUpdate();
                    }
                } else {
                    Long idEstadoPorAsignar = catalogoLookupDAO.obtenerEstadoId(conn, "POR_ASIGNAR");
                    if (idEstadoPorAsignar == null) {
                        throw new IllegalStateException("No se encontró el estado de expediente POR_ASIGNAR.");
                    }
                    String sqlExpediente = "UPDATE expediente SET id_estado_actual = ?, "
                            + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                            + "WHERE id_expediente = ? AND activo = 1";
                    try (PreparedStatement ps = conn.prepareStatement(sqlExpediente)) {
                        ps.setLong(1, idEstadoPorAsignar);
                        setLongOrNull(ps, 2, idUsuarioActual);
                        ps.setLong(3, idExpediente);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
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
     * Todos los documentos analizados principales (nivel 0, activos) de un expediente — alimenta
     * el mini-panel "① Emisión" ("Documentos a firmar") de la Bandeja Asignación. A diferencia de
     * {@link #listarPorExpediente}, expone {@code clasificacion} (necesaria para decidir qué filas
     * son editables: INTERMEDIO siempre, FINAL solo si VALIDADO/EMITIDO — regla del frontend).
     */
    public List<NotificacionAsignacionDocumentoDTO> listarDocumentosNotificacionPorExpediente(Long idExpediente) throws SQLException {
        List<NotificacionAsignacionDocumentoDTO> items = new ArrayList<>();
        if (idExpediente == null) {
            return items;
        }
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT da.id_documento_analizado, da.id_expediente, e.numero_expediente, esol.numero_expediente_sgd, "
                    + "tda.clasificacion, tda.nombre AS tipo_documento_nombre, "
                    + "da.numero_documento, da.fecha_documento, "
                    + nombrePersona("p") + " AS titular, "
                    + "ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre, "
                    + "e.fecha_vencimiento "
                    + "FROM expediente_documento_analizado da "
                    + "JOIN expediente e ON e.id_expediente = da.id_expediente AND e.activo = 1 "
                    + "LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 "
                    + "LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente "
                    + " AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' "
                    + "LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 "
                    + "LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                    + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                    + "WHERE da.activo = 1 AND da.nivel = 0 AND da.id_expediente = ? "
                    + "ORDER BY da.fecha_documento DESC NULLS LAST, da.id_documento_analizado DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idExpediente);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(new NotificacionAsignacionDocumentoDTO(
                                getLongOrNull(rs, "id_documento_analizado"),
                                getLongOrNull(rs, "id_expediente"),
                                rs.getString("numero_expediente"),
                                rs.getString("numero_expediente_sgd"),
                                rs.getString("clasificacion"),
                                rs.getString("tipo_documento_nombre"),
                                rs.getString("numero_documento"),
                                toLocalDate(rs.getDate("fecha_documento")),
                                rs.getString("titular"),
                                rs.getString("estado_documento_codigo"),
                                rs.getString("estado_documento_nombre"),
                                0, false, "", "", "", "",
                                toLocalDate(rs.getDate("fecha_vencimiento")),
                                null, 0, null, null, null,
                                null, null, null));
                    }
                }
            }
        }
        return items;
    }

    private NotificacionAsignacionDocumentoDTO mapNotificacionAsignacion(Connection conn, ResultSet rs) throws SQLException {
        Date fechaVencimiento = rs.getDate("fecha_vencimiento");
        Long idExpediente = getLongOrNull(rs, "id_expediente");
        return new NotificacionAsignacionDocumentoDTO(
                getLongOrNull(rs, "id_documento_analizado"),
                idExpediente,
                rs.getString("numero_expediente"),
                rs.getString("numero_expediente_sgd"),
                rs.getString("clasificacion"),
                rs.getString("tipo_documento_nombre"),
                rs.getString("numero_documento"),
                toLocalDate(rs.getDate("fecha_documento")),
                rs.getString("titular"),
                rs.getString("estado_documento_codigo"),
                rs.getString("estado_documento_nombre"),
                rs.getInt("relaciones_confirmadas"),
                rs.getInt("asignado") == 1,
                rs.getString("numero_hoja_envio_notificacion"),
                rs.getString("usuario_notificacion_actual"),
                rs.getString("estado_expediente_codigo"),
                rs.getString("estado_expediente_nombre"),
                toLocalDate(fechaVencimiento),
                calendarioLaboralService.calcularDiasHabilesRestantes(conn, idExpediente, fechaVencimiento),
                0, null, null, null,
                toLocalDate(rs.getDate("fecha_recepcion")),
                rs.getString("responsable"),
                rs.getString("resultado_analisis"));
    }

    /**
     * Asigna/reasigna un lote de documentos a un equipo/usuario (validador o notificador). Port
     * acotado de {@code asignarNotificacionMultiple} (V2) — sin propagación a asociados, ver
     * Javadoc de la clase. Actualiza también {@code EXPEDIENTE.id_estado_actual} cuando el equipo
     * destino es {@code EQ_VALIDACION} (→ POR_VALIDAR) o {@code EQ_NOTIFICACION} (→ EN_NOTIFICACION,
     * "Por notificar"), y {@code id_usuario_responsable_actual}/{@code id_equipo_responsable_actual}
     * siempre (para que "Abogado actual" en otras bandejas refleje a quien trabaja el expediente
     * ahora).
     */
    public void asignarNotificacionMultiple(
            List<Long> idsDocumentoAnalizado,
            Long idEquipoDestino,
            Long idUsuarioDestino,
            Map<Long, String> hojasEnvioPorDocumento,
            Long idUsuarioActual,
            boolean actorEsAdminSistema,
            boolean reasignacion) throws SQLException {
        if (idsDocumentoAnalizado == null || idsDocumentoAnalizado.isEmpty() || idEquipoDestino == null || idUsuarioDestino == null) {
            throw new IllegalArgumentException("Seleccione documentos, equipo destino y usuario destino para generar la asignación.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn,
                        reasignacion ? CODIGO_MOVIMIENTO_REASIGNACION_NOTIFICACION : CODIGO_MOVIMIENTO_ASIGNACION_NOTIFICACION);
                Long idEstadoExpedienteDestino = resolverEstadoExpedienteDestinoNotificacion(conn, idEquipoDestino);
                String sql = "UPDATE expediente_documento_analizado SET "
                        + "id_equipo_notificacion = ?, id_usuario_notificacion = ?, numero_hoja_envio_notificacion = ?, "
                        + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                        + "WHERE id_documento_analizado = ? AND activo = 1";
                String sqlExpediente = "SELECT id_expediente FROM expediente_documento_analizado WHERE id_documento_analizado = ?";
                String sqlResponsable = "UPDATE expediente SET "
                        + "id_equipo_responsable_actual = ?, id_usuario_responsable_actual = ?, "
                        + (idEstadoExpedienteDestino != null ? "id_estado_actual = ?, " : "")
                        + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                        + "WHERE id_expediente = ? AND activo = 1";
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     PreparedStatement psExpediente = conn.prepareStatement(sqlExpediente);
                     PreparedStatement psResponsable = conn.prepareStatement(sqlResponsable)) {
                    for (Long idDocumento : idsDocumentoAnalizado) {
                        String numeroHojaEnvio = hojasEnvioPorDocumento == null ? null : hojasEnvioPorDocumento.get(idDocumento);
                        int index = 1;
                        ps.setLong(index++, idEquipoDestino);
                        ps.setLong(index++, idUsuarioDestino);
                        setStringOrNull(ps, index++, numeroHojaEnvio);
                        setLongOrNull(ps, index++, idUsuarioActual);
                        ps.setLong(index, idDocumento);
                        ps.executeUpdate();

                        Long idExpediente = null;
                        psExpediente.setLong(1, idDocumento);
                        try (ResultSet rs = psExpediente.executeQuery()) {
                            if (rs.next()) {
                                idExpediente = getLongOrNull(rs, "id_expediente");
                            }
                        }
                        if (idExpediente != null) {
                            int idxResp = 1;
                            psResponsable.setLong(idxResp++, idEquipoDestino);
                            psResponsable.setLong(idxResp++, idUsuarioDestino);
                            if (idEstadoExpedienteDestino != null) {
                                psResponsable.setLong(idxResp++, idEstadoExpedienteDestino);
                            }
                            setLongOrNull(psResponsable, idxResp++, idUsuarioActual);
                            psResponsable.setLong(idxResp, idExpediente);
                            psResponsable.executeUpdate();
                            if (idMovimiento != null) {
                                insertarHistorialNotificacion(conn, idExpediente, idDocumento, idMovimiento,
                                        idUsuarioDestino, idEquipoDestino, numeroHojaEnvio, idUsuarioActual, actorEsAdminSistema);
                            }
                        }
                    }
                }
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
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

    private void insertarHistorialNotificacion(
            Connection conn, Long idExpediente, Long idDocumentoAnalizado, Long idMovimiento,
            Long idUsuarioDestino, Long idEquipoDestino, String numeroHojaEnvio,
            Long idUsuarioActual, boolean actorEsAdminSistema) throws SQLException {
        Long idAutorHistorial = resolverAutorHistorial(idUsuarioActual, actorEsAdminSistema, idUsuarioDestino);
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, "
                + "id_usuario_destino, id_equipo_destino, "
                + "tabla_relacionada, id_registro_relacionado, comentario, activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, 'EXPEDIENTE_DOCUMENTO_ANALIZADO', ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            setLongOrNull(ps, 3, idUsuarioDestino);
            setLongOrNull(ps, 4, idEquipoDestino);
            ps.setLong(5, idDocumentoAnalizado);
            setStringOrNull(ps, 6, numeroHojaEnvio);
            setLongOrNull(ps, 7, idAutorHistorial);
            ps.executeUpdate();
        }
    }

    /** Si el actor es ADMIN_SISTEMA, el historial se sustituye por el destino de la acción. */
    private static Long resolverAutorHistorial(Long idUsuarioActor, boolean actorEsAdminSistema, Long idUsuarioDestino) {
        if (idUsuarioDestino == null || !actorEsAdminSistema) {
            return idUsuarioActor;
        }
        return idUsuarioDestino;
    }

    /**
     * Resuelve a qué estado de EXPEDIENTE (etapa NOTIFICACION) debe pasar según el equipo al que se
     * asigna un documento: EQ_VALIDACION → Por validar, EQ_NOTIFICACION → Por notificar (código
     * EN_NOTIFICACION, reutilizado). Cualquier otro equipo retorna {@code null} (no se toca el
     * estado del expediente).
     */
    private Long resolverEstadoExpedienteDestinoNotificacion(Connection conn, Long idEquipoDestino) throws SQLException {
        if (idEquipoDestino == null) {
            return null;
        }
        String sql = "SELECT codigo FROM equipo WHERE id_equipo = ?";
        String codigoEquipo = null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEquipoDestino);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    codigoEquipo = rs.getString("codigo");
                }
            }
        }
        if (codigoEquipo == null) {
            return null;
        }
        String estadoDestino;
        if ("EQ_VALIDACION".equalsIgnoreCase(codigoEquipo)) {
            estadoDestino = "POR_VALIDAR";
        } else if ("EQ_NOTIFICACION".equalsIgnoreCase(codigoEquipo)) {
            estadoDestino = "EN_NOTIFICACION";
        } else {
            return null;
        }
        return catalogoLookupDAO.obtenerEstadoId(conn, estadoDestino);
    }

    /** Historial de asignación/reasignación de un documento — usado por el mini-panel "② Asignación". */
    public List<AsignacionHistorialDTO> listarHistorialAsignacionesNotificacion(Long idDocumentoAnalizado) throws SQLException {
        List<AsignacionHistorialDTO> items = new ArrayList<>();
        if (idDocumentoAnalizado == null) {
            return items;
        }
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT eh.id_expediente_historial, un.nombre_completo AS usuario_destino, "
                    + "eq.nombre AS equipo_destino, eh.comentario AS numero_hoja_envio, eh.fecha_movimiento, "
                    + "uc.nombre_completo AS asignado_por, tm.codigo AS codigo_movimiento "
                    + "FROM expediente_historial eh "
                    + "LEFT JOIN usuario un ON un.id_usuario = eh.id_usuario_destino "
                    + "LEFT JOIN equipo eq ON eq.id_equipo = eh.id_equipo_destino "
                    + "LEFT JOIN usuario uc ON uc.id_usuario = eh.creado_por "
                    + "LEFT JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = eh.id_tipo_movimiento "
                    + "WHERE eh.activo = 1 AND eh.tabla_relacionada = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                    + "AND eh.id_registro_relacionado = ? "
                    + "AND tm.codigo IN (?, ?) "
                    + "ORDER BY eh.fecha_movimiento DESC, eh.id_expediente_historial DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idDocumentoAnalizado);
                ps.setString(2, CODIGO_MOVIMIENTO_ASIGNACION_NOTIFICACION);
                ps.setString(3, CODIGO_MOVIMIENTO_REASIGNACION_NOTIFICACION);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean primero = true;
                    while (rs.next()) {
                        items.add(new AsignacionHistorialDTO(
                                getLongOrNull(rs, "id_expediente_historial"),
                                rs.getString("usuario_destino"),
                                rs.getString("equipo_destino"),
                                rs.getString("numero_hoja_envio"),
                                toLocalDateTime(rs.getTimestamp("fecha_movimiento")),
                                primero,
                                CODIGO_MOVIMIENTO_REASIGNACION_NOTIFICACION.equalsIgnoreCase(rs.getString("codigo_movimiento")),
                                null,
                                rs.getString("asignado_por")));
                        primero = false;
                    }
                }
            }
        }
        return items;
    }

    /** Guarda número/fecha/estado de un documento en la grilla "Documentos a firmar" (sin cambio de etapa). */
    public void registrarFirmaDocumentoNotificacion(
            Long idDocumentoAnalizado, String numeroDocumento, LocalDate fechaEmision,
            String estadoDocumentoCodigo, Long idUsuarioActual) throws SQLException {
        if (idDocumentoAnalizado == null) {
            throw new IllegalArgumentException("Seleccione un documento para registrar la firma.");
        }
        String codigoResuelto = hasTextEstado(estadoDocumentoCodigo) ? estadoDocumentoCodigo.trim() : "EMITIDO";
        try (Connection conn = dataSource.getConnection()) {
            Long idEstado = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, codigoResuelto);
            if (idEstado == null) {
                throw new IllegalStateException("No se encontró el estado de documento " + codigoResuelto + ".");
            }
            String sql = "UPDATE expediente_documento_analizado SET "
                    + "id_estado_documento = ?, numero_documento = ?, fecha_documento = ?, "
                    + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                    + "WHERE id_documento_analizado = ? AND activo = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idEstado);
                setStringOrNull(ps, 2, numeroDocumento);
                setDateOrNull(ps, 3, fechaEmision);
                setLongOrNull(ps, 4, idUsuarioActual);
                ps.setLong(5, idDocumentoAnalizado);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("No se pudo registrar la firma del documento.");
                }
            }
        }
    }

    private static boolean hasTextEstado(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * "Registrar Supervisión" (resultado Aprobado) — solo auditoría en EXPEDIENTE_HISTORIAL, no
     * mueve etapa/estado (eso ocurre después, al generar la 2ª asignación hacia Eq. Notificación).
     */
    public void registrarSupervisionEmisionAprobada(Long idExpediente, Long idDocumentoAnalizado, Long idUsuarioActual) throws SQLException {
        if (idExpediente == null || idDocumentoAnalizado == null) {
            throw new IllegalArgumentException("Seleccione un documento para registrar la supervisión.");
        }
        try (Connection conn = dataSource.getConnection()) {
            Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_SUPERVISION_EMISION_NOTIFICACION);
            if (idMovimiento == null) {
                throw new IllegalStateException("No se encontró el movimiento " + CODIGO_MOVIMIENTO_SUPERVISION_EMISION_NOTIFICACION
                        + ". Ejecute el script de catálogo correspondiente.");
            }
            String sql = "INSERT INTO expediente_historial ("
                    + "id_expediente, id_tipo_movimiento, fecha_movimiento, "
                    + "tabla_relacionada, id_registro_relacionado, comentario, activo, creado_por, creado_en"
                    + ") VALUES (?, ?, SYSTIMESTAMP, 'EXPEDIENTE_DOCUMENTO_ANALIZADO', ?, ?, 1, ?, SYSTIMESTAMP)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idExpediente);
                ps.setLong(2, idMovimiento);
                ps.setLong(3, idDocumentoAnalizado);
                ps.setString(4, "Emisión aprobada por supervisión.");
                setLongOrNull(ps, 5, idUsuarioActual);
                ps.executeUpdate();
            }
        }
    }

    /** true si ya existe un registro de "Registrar Supervisión" (Aprobado) para este documento. */
    public boolean existeSupervisionEmisionAprobada(Long idDocumentoAnalizado) throws SQLException {
        if (idDocumentoAnalizado == null) {
            return false;
        }
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT 1 FROM expediente_historial eh "
                    + "JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = eh.id_tipo_movimiento "
                    + "WHERE eh.activo = 1 AND eh.tabla_relacionada = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                    + "AND eh.id_registro_relacionado = ? "
                    + "AND UPPER(tm.codigo) = 'SUPERVISION_EMISION_NOTIFICACION' AND ROWNUM = 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, idDocumentoAnalizado);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        }
    }

    /**
     * Deriva el expediente de un documento (etapa NOTIFICACION) de vuelta a EJECUCION/EN_EJECUCION.
     * No reasigna responsable (Ejecución exige el mismo abogado ya ligado vía EXPEDIENTE_ASIGNACION,
     * no el usuario elegido en "Usuario destino" — ese solo se registra en el historial).
     */
    public void derivarDocumentoNotificacionAEjecucion(
            Long idDocumentoAnalizado, Long idEquipoDestino, Long idUsuarioDestino,
            String comentario, Long idUsuarioActual, boolean actorEsAdminSistema) throws SQLException {
        if (idDocumentoAnalizado == null) {
            throw new IllegalArgumentException("Seleccione un documento para derivar a Ejecución.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idExpediente = obtenerIdExpedientePorDocumento(conn, idDocumentoAnalizado);
                if (idExpediente == null) {
                    throw new IllegalStateException("El documento seleccionado no existe o no está activo.");
                }
                String sqlExpediente = "SELECT e.id_etapa_actual, e.id_estado_actual, "
                        + "et.codigo AS etapa_codigo, es.codigo AS estado_codigo "
                        + "FROM expediente e "
                        + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                        + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                        + "WHERE e.id_expediente = ? AND e.activo = 1 FOR UPDATE";
                Long idEtapaOrigen;
                Long idEstadoOrigen;
                String etapaCodigo;
                String estadoCodigo;
                try (PreparedStatement ps = conn.prepareStatement(sqlExpediente)) {
                    ps.setLong(1, idExpediente);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("El expediente del documento seleccionado no existe o no está activo.");
                        }
                        idEtapaOrigen = getLongOrNull(rs, "id_etapa_actual");
                        idEstadoOrigen = getLongOrNull(rs, "id_estado_actual");
                        etapaCodigo = rs.getString("etapa_codigo");
                        estadoCodigo = rs.getString("estado_codigo");
                    }
                }
                long[] destino = resolverTransicionPorCodigo(
                        conn, CODIGO_MOVIMIENTO_DEVOLUCION_EJECUCION, etapaCodigo, estadoCodigo, "EJECUCION", "EN_EJECUCION");
                Long idEtapaDestino = destino[0];
                Long idEstadoDestino = destino[1];

                String sqlUpdate = "UPDATE expediente SET id_etapa_actual = ?, id_estado_actual = ?, "
                        + "fecha_ultimo_movimiento = SYSTIMESTAMP, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                        + "WHERE id_expediente = ? AND activo = 1";
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setLong(1, idEtapaDestino);
                    ps.setLong(2, idEstadoDestino);
                    setLongOrNull(ps, 3, idUsuarioActual);
                    ps.setLong(4, idExpediente);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("No se pudo actualizar el expediente seleccionado.");
                    }
                }

                Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_DEVOLUCION_EJECUCION);
                if (idMovimiento != null) {
                    Long idAutorHistorial = resolverAutorHistorial(idUsuarioActual, actorEsAdminSistema, idUsuarioDestino);
                    String sqlHistorial = "INSERT INTO expediente_historial ("
                            + "id_expediente, id_tipo_movimiento, fecha_movimiento, "
                            + "id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino, "
                            + "id_usuario_origen, id_usuario_destino, id_equipo_destino, "
                            + "comentario, activo, creado_por, creado_en"
                            + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlHistorial)) {
                        ps.setLong(1, idExpediente);
                        ps.setLong(2, idMovimiento);
                        setLongOrNull(ps, 3, idEtapaOrigen);
                        setLongOrNull(ps, 4, idEstadoOrigen);
                        ps.setLong(5, idEtapaDestino);
                        ps.setLong(6, idEstadoDestino);
                        setLongOrNull(ps, 7, idAutorHistorial);
                        setLongOrNull(ps, 8, idUsuarioDestino);
                        setLongOrNull(ps, 9, idEquipoDestino);
                        setStringOrNull(ps, 10, comentario);
                        setLongOrNull(ps, 11, idAutorHistorial);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
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

    private Long obtenerIdExpedientePorDocumento(Connection conn, Long idDocumentoAnalizado) throws SQLException {
        String sql = "SELECT id_expediente FROM expediente_documento_analizado WHERE id_documento_analizado = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idDocumentoAnalizado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? getLongOrNull(rs, "id_expediente") : null;
            }
        }
    }

    /** Resuelve dinámicamente la transición activa (etapa/estado destino) para una acción y un origen real. */
    private long[] resolverTransicionPorCodigo(
            Connection conn, String accionCodigo, String etapaOrigenCodigo, String estadoOrigenCodigo,
            String etapaDestinoCodigo, String estadoDestinoCodigo) throws SQLException {
        String sql = "SELECT ft.id_etapa_destino, ft.id_estado_destino "
                + "FROM flujo f "
                + "JOIN flujo_transicion ft ON ft.id_flujo = f.id_flujo "
                + "JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen "
                + "JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen "
                + "JOIN etapa_expediente ed ON ed.id_etapa = ft.id_etapa_destino "
                + "JOIN estado_expediente sd ON sd.id_estado = ft.id_estado_destino "
                + "WHERE f.codigo = ? AND f.activo = 1 AND ft.activo = 1 "
                + "AND ft.codigo_accion = ? "
                + "AND eo.codigo = ? AND so.codigo = ? "
                + "AND ed.codigo = ? AND sd.codigo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, CODIGO_FLUJO);
            ps.setString(2, accionCodigo);
            ps.setString(3, etapaOrigenCodigo);
            ps.setString(4, estadoOrigenCodigo);
            ps.setString(5, etapaDestinoCodigo);
            ps.setString(6, estadoDestinoCodigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("No existe transición activa "
                            + etapaOrigenCodigo + "/" + estadoOrigenCodigo + " -> "
                            + etapaDestinoCodigo + "/" + estadoDestinoCodigo
                            + " para " + accionCodigo + " en " + CODIGO_FLUJO + ".");
                }
                return new long[]{rs.getLong("id_etapa_destino"), rs.getLong("id_estado_destino")};
            }
        }
    }

    private static String nombrePersona(String alias) {
        return "TRIM(NVL(" + alias + ".razon_social, TRIM(NVL(" + alias + ".nombres, '') || ' ' || NVL(" + alias + ".apellidos, ''))))";
    }

    private static void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // El error original se reporta al usuario; el rollback fallido no debe ocultarlo.
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
