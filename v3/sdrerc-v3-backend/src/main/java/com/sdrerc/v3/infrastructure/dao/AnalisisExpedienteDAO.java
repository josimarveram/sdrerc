package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.application.CalendarioLaboralService;
import com.sdrerc.v3.domain.AnalisisDetalleDTO;
import com.sdrerc.v3.domain.AnalisisExpedienteDTO;
import com.sdrerc.v3.domain.AnalisisRegistroDTO;
import com.sdrerc.v3.domain.AnalisisResultadoDTO;
import com.sdrerc.v3.domain.ObservacionAnalisisDTO;
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
 * Port acotado de com.sdrerc.infrastructure.sdrercapp.dao.AnalisisExpedienteDAO (V2, 1841 líneas)
 * — solo el camino de la Bandeja Análisis + "Recibir expediente" (20/08/2026, primer incremento
 * del módulo, alcance acordado explícitamente con el usuario). NO incluye todavía (quedan como
 * incrementos siguientes, cada uno se documentará en {@code v3/CLAUDE.md} al construirse):
 * <ul>
 *   <li>{@code registrarAnalisis}/{@code validarRegistroAnalisis} ("Registrar resultado final",
 *       transición {@code REGISTRO_RESULTADO_ANALISIS}, 4 orígenes × 5 destinos, ver
 *       {@code flujo_transicion} real verificado contra la BD).</li>
 *   <li>{@code enviarVerificacion} (transición {@code ENVIO_VERIFICACION} desde ATENDIDO,
 *       {@code REENVIO_VERIFICACION} desde SUBSANADO).</li>
 *   <li>{@code guardarDocumentosAnalisis}/{@code darBajaDocumentosAnalisis} y toda la grilla
 *       jerárquica de "Documentos Analizados" (tabla {@code EXPEDIENTE_DOCUMENTO_ANALIZADO}) — vive
 *       en {@code DocumentoAnalisisDAO}, no en este archivo (mismo split que V2).</li>
 *   <li>{@code recibirDocumentoAsociado} (botón "Recibir" en la mini-grilla de documentos
 *       asociados).</li>

 *   <li>Generación de documentos Word (plantillas) — {@code AnalisisPlantillaDocumentoService}.</li>
 *   <li>Propagación de la recepción hacia expedientes asociados/duplicados confirmados (V2 lo hace
 *       dentro de {@code recibirExpediente} vía {@code ExpedienteEstadoPropagacionDAO}, sin
 *       equivalente todavía en V3) — {@link #recibirExpediente} de este port solo mueve el
 *       expediente principal.</li>
 * </ul>
 *
 * <p>La bandeja consulta {@code vw_expediente_analisis_detalle} (script 108, NO ejecutado
 * todavía) en vez de repetir el join completo en Java — mismo patrón que
 * {@code vw_expediente_asignacion_detalle} (script 99). Alcance de la vista verificado línea por
 * línea contra el SQL real de {@code AnalisisExpedienteDAO#buscarExpedientes} en V2: expedientes en
 * etapa ASIGNACION/estado ASIGNADO (pendientes de "Recibir") UNION expedientes ya en etapa
 * ANALISIS (cualquier estado). El filtro de visibilidad por asignación
 * ({@link VisibilidadBandejaSql}, primer consumidor real en V3) se aplica acá sobre la vista, no
 * dentro de ella — Análisis SÍ filtra por fila (a diferencia de Registro/Asignación), ver
 * {@code CLAUDE.md} raíz, sección "Visibilidad por asignación dentro de una bandeja".</p>
 *
 * <p>La transición "Recibir expediente" (ASIGNACION/ASIGNADO → ANALISIS/RECIBIDO_POR_ABOGADO,
 * acción {@code RECEPCION_ASIGNACION}) fue verificada con un {@code SELECT} de solo lectura contra
 * {@code flujo_transicion} en la BD real (20/08/2026) — no asumida desde el código de V2, que no
 * expone el código de acción como constante fácil de grepear en el DAO real.</p>
 */
@Repository
public class AnalisisExpedienteDAO {

    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    // Mismo código para flujo_transicion.codigo_accion Y tipo_movimiento.codigo (confirmado contra
    // el DAO real de V2, com.sdrerc.infrastructure.sdrercapp.dao.AnalisisExpedienteDAO#ACCION_RECEPCION
    // — no son 2 catálogos con nombres distintos, es el mismo código reutilizado en ambos).
    private static final String CODIGO_ACCION_RECEPCION = "RECEPCION_ASIGNACION";

    private static final String ETAPA_ASIGNACION = "ASIGNACION";
    private static final String ESTADO_ASIGNADO = "ASIGNADO";
    private static final String ETAPA_ANALISIS = "ANALISIS";
    private static final String ESTADO_RECIBIDO_POR_ABOGADO = "RECIBIDO_POR_ABOGADO";
    private static final String ESTADO_OBSERVADO = "OBSERVADO";
    private static final String ESTADO_DERIVADO = "DERIVADO";
    private static final String ESTADO_SUBSANADO = "SUBSANADO";
    private static final String ESTADO_ATENDIDO = "ATENDIDO";
    private static final String ESTADO_NO_CORRESPONDE = "NO_CORRESPONDE";
    private static final String ESTADO_OBSERVACION_ADMINISTRATIVA = "OBSERVACION_ADMINISTRATIVA";
    private static final String ETAPA_VERIFICACION = "VERIFICACION";
    private static final String ESTADO_EN_VERIFICACION = "EN_VERIFICACION";
    private static final String CODIGO_ACCION_REGISTRO_ANALISIS = "REGISTRO_RESULTADO_ANALISIS";
    private static final String CODIGO_ACCION_ENVIO_VERIFICACION = "ENVIO_VERIFICACION";
    private static final String CODIGO_ACCION_REENVIO_VERIFICACION = "REENVIO_VERIFICACION";
    // Catálogo TIPO_DOCUMENTO_ADJUNTO, mismo par de códigos que V2 usa para guardar "N° documento
    // proveído"/"Hoja de envío" como fila de EXPEDIENTE_DOCUMENTO (no EXPEDIENTE_DOCUMENTO_ANALIZADO)
    // cuando el resultado es NO_CORRESPONDE.
    private static final String TIPO_DOCUMENTO_PROVEIDO = "PROVEIDO";
    private static final String TIPO_DOCUMENTO_HOJA_ENVIO = "HOJA_ENVIO";

    private final DataSource dataSource;
    private final CalendarioLaboralService calendarioLaboralService;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final DocumentoAnalisisDAO documentoAnalisisDAO;
    private final ExpedienteRelacionadoDAO expedienteRelacionadoDAO;

    public AnalisisExpedienteDAO(
            DataSource dataSource,
            CalendarioLaboralService calendarioLaboralService,
            CatalogoLookupDAO catalogoLookupDAO,
            DocumentoAnalisisDAO documentoAnalisisDAO,
            ExpedienteRelacionadoDAO expedienteRelacionadoDAO) {
        this.dataSource = dataSource;
        this.calendarioLaboralService = calendarioLaboralService;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.documentoAnalisisDAO = documentoAnalisisDAO;
        this.expedienteRelacionadoDAO = expedienteRelacionadoDAO;
    }

    public List<AnalisisExpedienteDTO> buscarExpedientes(
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
        sql.append("SELECT * FROM (SELECT v.* FROM vw_expediente_analisis_detalle v WHERE 1 = 1 ");

        sql.append(VisibilidadBandejaSql.construirCondicion(
                params, esAdmin, idUsuarioActual, idsEquipoActual, "id_responsable", "id_equipo_responsable"));

        if (hasText(estadoCodigo) && !"TODOS".equalsIgnoreCase(estadoCodigo)) {
            sql.append("AND UPPER(estado_codigo) = ? ");
            params.add(estadoCodigo.trim().toUpperCase(Locale.ROOT));
        }
        if (fechaSolicitudDesde != null) {
            sql.append("AND TRUNC(fecha_recepcion) >= ? ");
            params.add(Date.valueOf(fechaSolicitudDesde));
        }
        if (fechaSolicitudHasta != null) {
            sql.append("AND TRUNC(fecha_recepcion) <= ? ");
            params.add(Date.valueOf(fechaSolicitudHasta));
        }
        if (hasText(textoLibre)) {
            String pattern = "%" + textoLibre.trim().toUpperCase(Locale.ROOT) + "%";
            sql.append("AND (");
            sql.append("UPPER(NVL(numero_expediente, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(numero_expediente_sgd, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(numero_acta, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(titular, '')) LIKE ? ");
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
                List<AnalisisExpedienteDTO> expedientes = new ArrayList<>();
                while (rs.next()) {
                    expedientes.add(map(conn, rs));
                }
                return expedientes;
            }
        }
    }

    /**
     * ASIGNACION/ASIGNADO → ANALISIS/RECIBIDO_POR_ABOGADO, acción {@code RECEPCION_ASIGNACION}
     * (verificada contra {@code flujo_transicion} real). Port de
     * {@code AnalisisExpedienteDAO#recibirExpediente} (V2) — misma validación de estado origen +
     * lock pesimista antes de mutar, mismo criterio de "autor de historial sustituido si actúa
     * ADMIN_SISTEMA" que el resto de DAOs de V3 (ver {@code CLAUDE.md} raíz).
     */
    public AnalisisResultadoDTO recibirExpediente(Long idExpediente, String comentario, Long idUsuarioActual) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione el expediente a recibir.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEstadoActual estado = bloquearYObtenerEstado(conn, idExpediente);
                if (!ETAPA_ASIGNACION.equalsIgnoreCase(estado.etapaCodigo) || !ESTADO_ASIGNADO.equalsIgnoreCase(estado.estadoCodigo)) {
                    throw new IllegalStateException(
                            "El expediente ya no está en Asignación/Asignado (estado actual: "
                                    + estado.etapaCodigo + "/" + estado.estadoCodigo + ").");
                }
                Long idEtapaDestino = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, ETAPA_ANALISIS), "etapa ANALISIS");
                Long idEstadoDestino = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, ESTADO_RECIBIDO_POR_ABOGADO), "estado RECIBIDO_POR_ABOGADO");
                validarTransicion(conn, estado.idEtapa, estado.idEstado, idEtapaDestino, idEstadoDestino);

                actualizarEtapaEstado(conn, idExpediente, idEtapaDestino, idEstadoDestino, idUsuarioActual);
                Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_ACCION_RECEPCION);
                if (idMovimiento != null) {
                    insertarHistorial(conn, idExpediente, idMovimiento, idUsuarioActual, comentario);
                }

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new AnalisisResultadoDTO(
                        idExpediente, null, CODIGO_ACCION_RECEPCION, ETAPA_ANALISIS, ESTADO_RECIBIDO_POR_ABOGADO,
                        "Expediente recibido para análisis.");
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
     * "Recibir" en la mini-grilla de solicitudes asociadas (duplicados confirmados) del expediente
     * enfocado — port acotado de {@code AnalisisExpedienteDAO#recibirDocumentoAsociado} (V2). A
     * diferencia de V2, no se replica la propagación automática hacia el resto de asociados (ver
     * Javadoc de la clase); es la MISMA transición {@code RECEPCION_ASIGNACION} que
     * {@link #recibirExpediente}, solo que primero valida que {@code idExpedienteAsociado} sea
     * realmente un asociado confirmado de {@code idExpedientePrincipal} (evita que el botón de un
     * panel desactualizado reciba un expediente que ya no corresponde a ese grupo).
     */
    public AnalisisResultadoDTO recibirDocumentoAsociado(
            Long idExpedientePrincipal, Long idExpedienteAsociado, String comentario, Long idUsuarioActual) throws SQLException {
        if (idExpedientePrincipal == null || idExpedienteAsociado == null) {
            throw new IllegalArgumentException("Seleccione el expediente principal y el asociado a recibir.");
        }
        boolean confirmado = expedienteRelacionadoDAO.listarAsociadosConfirmados(idExpedientePrincipal).stream()
                .anyMatch(a -> idExpedienteAsociado.equals(a.getIdExpediente()));
        if (!confirmado) {
            throw new IllegalStateException("El expediente seleccionado ya no está asociado al expediente principal.");
        }
        return recibirExpediente(idExpedienteAsociado, comentario, idUsuarioActual);
    }

    /**
     * "Registrar resultado final" — port acotado de {@code AnalisisExpedienteDAO#registrarAnalisis}
     * (V2). Simplificaciones deliberadas respecto a V2 (sin equivalente de "análisis múltiple" en
     * V3, ver {@code AnalisisDetalleDTO}/{@code AnalisisRegistroDTO}): no se toca
     * {@code EXPEDIENTE_EVALUACION.ID_EXPEDIENTE_ANALISIS} (queda siempre NULL), y este método NO
     * guarda documentos analizados (eso vive en {@code DocumentoAnalisisDAO}, guardado por fila
     * desde la grilla, ver {@code CLAUDE.md} raíz: "Guardar documentos no exige resultado final").
     *
     * <p>{@link #resolverEstadoDestinoResultado(String)} replica EXACTAMENTE la lógica real (y algo
     * inconsistente) de V2: solo 3 resultados mapean su propio código como estado destino
     * (NO_CORRESPONDE, OBSERVACION_ADMINISTRATIVA, OBSERVADO); cualquier otro resultado
     * (PROCEDENTE/IMPROCEDENTE/EDICTO/EN_ABANDONO/etc.) cae por defecto en ATENDIDO. Se porta tal
     * cual, sin "corregir" el caso EN_ABANDONO (que en V2 nunca resulta en el estado EN_ABANDONO del
     * expediente pese a existir como código de catálogo) — ver v3/CLAUDE.md.</p>
     */
    public AnalisisResultadoDTO registrarAnalisis(AnalisisRegistroDTO registro, Long idUsuarioActual) throws SQLException {
        if (registro == null || registro.getIdExpediente() == null || !hasText(registro.getResultadoCodigo())) {
            throw new IllegalArgumentException("Seleccione el resultado del análisis antes de registrar.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEstadoActual estado = bloquearYObtenerEstado(conn, registro.getIdExpediente());
                if (!ETAPA_ANALISIS.equalsIgnoreCase(estado.etapaCodigo) || !esEstadoRegistrable(estado.estadoCodigo)) {
                    throw new IllegalStateException(
                            "El expediente debe estar Recibido, Observado, Derivado o Subsanado en Análisis (estado actual: "
                                    + estado.etapaCodigo + "/" + estado.estadoCodigo + ").");
                }
                String estadoDestinoCodigo = resolverEstadoDestinoResultado(registro.getResultadoCodigo());
                Transicion transicion = requerirTransicion(
                        conn, CODIGO_ACCION_REGISTRO_ANALISIS, ETAPA_ANALISIS, estado.estadoCodigo, ETAPA_ANALISIS, estadoDestinoCodigo);
                Long idMovimiento = requerirId(
                        catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_ACCION_REGISTRO_ANALISIS),
                        "movimiento " + CODIGO_ACCION_REGISTRO_ANALISIS);

                Long idResultado = requiereResultadoCatalogo(estadoDestinoCodigo)
                        ? requerirId(
                                catalogoLookupDAO.obtenerTipoResultadoEvaluacionId(conn, registro.getResultadoCodigo()),
                                "resultado " + registro.getResultadoCodigo())
                        : null;
                Long idMotivoNoCorresponde = hasText(registro.getMotivoNoCorrespondeCodigo())
                        ? catalogoLookupDAO.obtenerMotivoNoCorrespondeId(conn, registro.getMotivoNoCorrespondeCodigo())
                        : null;

                guardarEvaluacion(conn, registro, idResultado, idMotivoNoCorresponde, idUsuarioActual);
                registrarDocumentoNoCorrespondeSiInformado(conn, registro, idUsuarioActual);
                insertarObservacionAnalisis(conn, registro.getIdExpediente(), registro.getObservacion(), idUsuarioActual);
                actualizarEtapaEstado(conn, registro.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuarioActual);
                insertarHistorial(conn, registro.getIdExpediente(), idMovimiento, idUsuarioActual, comentarioAnalisis(registro));

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new AnalisisResultadoDTO(
                        registro.getIdExpediente(), null, CODIGO_ACCION_REGISTRO_ANALISIS, ETAPA_ANALISIS, estadoDestinoCodigo,
                        "El resultado del análisis fue registrado correctamente.");
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
     * "Enviar a Verificación" — habilitado solo si ATENDIDO o SUBSANADO (pedido explícito del
     * usuario para este incremento, más estricto que {@code AnalisisExpedienteDTO#isEnviableVerificacion()}
     * que también da luz verde a RECIBIDO_POR_ABOGADO/OBSERVADO/DERIVADO como hint de UI). Exige
     * además al menos 1 documento analizado activo, igual que V2.
     */
    public AnalisisResultadoDTO enviarVerificacion(Long idExpediente, String comentario, Long idUsuarioActual) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione el expediente a enviar a verificación.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEstadoActual estado = bloquearYObtenerEstado(conn, idExpediente);
                if (!ETAPA_ANALISIS.equalsIgnoreCase(estado.etapaCodigo) || !esEstadoEnviableVerificacion(estado.estadoCodigo)) {
                    throw new IllegalStateException(
                            "El expediente debe estar Atendido o Subsanado en Análisis para enviarlo a verificación (estado actual: "
                                    + estado.etapaCodigo + "/" + estado.estadoCodigo + ").");
                }
                if (documentoAnalisisDAO.contarPorExpediente(conn, idExpediente) <= 0) {
                    throw new IllegalStateException("Registre al menos un documento analizado antes de enviar a verificación.");
                }
                String accionCodigo = ESTADO_SUBSANADO.equalsIgnoreCase(estado.estadoCodigo)
                        ? CODIGO_ACCION_REENVIO_VERIFICACION
                        : CODIGO_ACCION_ENVIO_VERIFICACION;
                Transicion transicion = requerirTransicion(
                        conn, accionCodigo, ETAPA_ANALISIS, estado.estadoCodigo, ETAPA_VERIFICACION, ESTADO_EN_VERIFICACION);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, accionCodigo), "movimiento " + accionCodigo);

                actualizarEtapaEstado(conn, idExpediente, transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuarioActual);
                insertarHistorial(
                        conn, idExpediente, idMovimiento, idUsuarioActual,
                        hasText(comentario) ? comentario.trim() : "Expediente enviado a verificación.");

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new AnalisisResultadoDTO(
                        idExpediente, null, accionCodigo, ETAPA_VERIFICACION, ESTADO_EN_VERIFICACION,
                        "El expediente fue enviado a verificación.");
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

    /** Resultado del análisis ya registrado (repobla el formulario al reabrir el expediente). */
    public AnalisisDetalleDTO obtenerAnalisisRegistrado(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return analisisVacio();
        }
        try (Connection conn = dataSource.getConnection()) {
            EvaluacionRegistrada evaluacion = obtenerEvaluacionRegistrada(conn, idExpediente);
            ObservacionAnalisisDTO observacion = obtenerUltimaObservacionAnalisis(conn, idExpediente);
            DocumentoNoCorrespondeInfo documentoNoCorresponde = obtenerUltimoDocumentoNoCorresponde(conn, idExpediente);
            if (evaluacion == null) {
                return new AnalisisDetalleDTO(
                        false, "", "", null, null, false, false, false, "", "", "",
                        documentoNoCorresponde.codigo, documentoNoCorresponde.nombre, documentoNoCorresponde.numero,
                        null, observacion);
            }
            return new AnalisisDetalleDTO(
                    true,
                    evaluacion.resultadoCodigo,
                    evaluacion.resultadoNombre,
                    evaluacion.corresponde,
                    evaluacion.incorporado,
                    evaluacion.requiereReconstitucion,
                    evaluacion.tieneLegitimidad,
                    evaluacion.cumpleMediosProbatorios,
                    evaluacion.fundamento,
                    evaluacion.motivoNoCorrespondeCodigo,
                    evaluacion.motivoNoCorrespondeNombre,
                    documentoNoCorresponde.codigo,
                    documentoNoCorresponde.nombre,
                    documentoNoCorresponde.numero,
                    evaluacion.fechaEvaluacion,
                    observacion);
        }
    }

    private static boolean esEstadoRegistrable(String estadoCodigo) {
        return ESTADO_RECIBIDO_POR_ABOGADO.equalsIgnoreCase(estadoCodigo)
                || ESTADO_OBSERVADO.equalsIgnoreCase(estadoCodigo)
                || ESTADO_DERIVADO.equalsIgnoreCase(estadoCodigo)
                || ESTADO_SUBSANADO.equalsIgnoreCase(estadoCodigo);
    }

    private static boolean esEstadoEnviableVerificacion(String estadoCodigo) {
        return ESTADO_ATENDIDO.equalsIgnoreCase(estadoCodigo) || ESTADO_SUBSANADO.equalsIgnoreCase(estadoCodigo);
    }

    private static String resolverEstadoDestinoResultado(String resultadoCodigo) {
        if (ESTADO_NO_CORRESPONDE.equalsIgnoreCase(resultadoCodigo)) {
            return ESTADO_NO_CORRESPONDE;
        }
        if (ESTADO_OBSERVACION_ADMINISTRATIVA.equalsIgnoreCase(resultadoCodigo)) {
            return ESTADO_OBSERVACION_ADMINISTRATIVA;
        }
        if (ESTADO_OBSERVADO.equalsIgnoreCase(resultadoCodigo)) {
            return ESTADO_OBSERVADO;
        }
        return ESTADO_ATENDIDO;
    }

    private static boolean requiereResultadoCatalogo(String estadoDestinoCodigo) {
        return !ESTADO_NO_CORRESPONDE.equalsIgnoreCase(estadoDestinoCodigo) && !ESTADO_OBSERVADO.equalsIgnoreCase(estadoDestinoCodigo);
    }

    private Transicion requerirTransicion(
            Connection conn,
            String accionCodigo,
            String etapaOrigenCodigo,
            String estadoOrigenCodigo,
            String etapaDestinoCodigo,
            String estadoDestinoCodigo) throws SQLException {
        Long idEtapaDestino = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, etapaDestinoCodigo), "etapa " + etapaDestinoCodigo);
        Long idEstadoDestino = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, estadoDestinoCodigo), "estado " + estadoDestinoCodigo);
        String sql = "SELECT 1 FROM flujo f "
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
            }
        }
        return new Transicion(idEtapaDestino, idEstadoDestino);
    }

    private Long guardarEvaluacion(
            Connection conn, AnalisisRegistroDTO registro, Long idResultado, Long idMotivoNoCorresponde, Long idUsuario) throws SQLException {
        Long idEvaluacionActiva = obtenerEvaluacionActivaId(conn, registro.getIdExpediente());
        if (idEvaluacionActiva == null) {
            return insertarEvaluacion(conn, registro, idResultado, idMotivoNoCorresponde, idUsuario);
        }
        actualizarEvaluacion(conn, idEvaluacionActiva, registro, idResultado, idMotivoNoCorresponde, idUsuario);
        return idEvaluacionActiva;
    }

    private Long obtenerEvaluacionActivaId(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT id_expediente_evaluacion FROM (SELECT id_expediente_evaluacion FROM expediente_evaluacion "
                + "WHERE id_expediente = ? AND activo = 1 ORDER BY creado_en DESC) WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? getLongOrNull(rs, "id_expediente_evaluacion") : null;
            }
        }
    }

    private Long insertarEvaluacion(
            Connection conn, AnalisisRegistroDTO registro, Long idResultado, Long idMotivoNoCorresponde, Long idUsuario) throws SQLException {
        String sql = "INSERT INTO expediente_evaluacion ("
                + "id_expediente, id_tipo_resultado_evaluacion, corresponde, id_motivo_no_corresponde, "
                + "incorporado, requiere_reconstitucion, tiene_legitimidad, cumple_medios_probatorios, "
                + "fundamento, fecha_evaluacion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_EVALUACION"})) {
            int index = 1;
            ps.setLong(index++, registro.getIdExpediente());
            setLongOrNull(ps, index++, idResultado);
            setBooleanNumberOrNull(ps, index++, registro.getCorresponde());
            setLongOrNull(ps, index++, idMotivoNoCorresponde);
            ps.setInt(index++, Boolean.TRUE.equals(registro.getIncorporado()) ? 1 : 0);
            ps.setInt(index++, registro.isRequiereReconstitucion() ? 1 : 0);
            ps.setInt(index++, registro.isTieneLegitimidad() ? 1 : 0);
            ps.setInt(index++, registro.isCumpleMediosProbatorios() ? 1 : 0);
            ps.setString(index++, limitar(registro.getFundamento(), 2000));
            setLongOrNull(ps, index, idUsuario);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el identificador generado de la evaluación.");
    }

    private void actualizarEvaluacion(
            Connection conn,
            Long idEvaluacion,
            AnalisisRegistroDTO registro,
            Long idResultado,
            Long idMotivoNoCorresponde,
            Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_evaluacion SET "
                + "id_tipo_resultado_evaluacion = ?, corresponde = ?, id_motivo_no_corresponde = ?, "
                + "incorporado = ?, requiere_reconstitucion = ?, tiene_legitimidad = ?, cumple_medios_probatorios = ?, "
                + "fundamento = ?, fecha_evaluacion = SYSTIMESTAMP, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente_evaluacion = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            setLongOrNull(ps, index++, idResultado);
            setBooleanNumberOrNull(ps, index++, registro.getCorresponde());
            setLongOrNull(ps, index++, idMotivoNoCorresponde);
            ps.setInt(index++, Boolean.TRUE.equals(registro.getIncorporado()) ? 1 : 0);
            ps.setInt(index++, registro.isRequiereReconstitucion() ? 1 : 0);
            ps.setInt(index++, registro.isTieneLegitimidad() ? 1 : 0);
            ps.setInt(index++, registro.isCumpleMediosProbatorios() ? 1 : 0);
            ps.setString(index++, limitar(registro.getFundamento(), 2000));
            setLongOrNull(ps, index++, idUsuario);
            ps.setLong(index, idEvaluacion);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar la evaluación del expediente.");
            }
        }
    }

    private void registrarDocumentoNoCorrespondeSiInformado(Connection conn, AnalisisRegistroDTO registro, Long idUsuario) throws SQLException {
        if (!hasText(registro.getNumeroDocumentoProveido())) {
            return;
        }
        String codigoTipo = TIPO_DOCUMENTO_HOJA_ENVIO.equalsIgnoreCase(registro.getTipoDocumentoNoCorrespondeCodigo())
                ? TIPO_DOCUMENTO_HOJA_ENVIO
                : TIPO_DOCUMENTO_PROVEIDO;
        String nombreTipo = hasText(registro.getTipoDocumentoNoCorrespondeNombre())
                ? registro.getTipoDocumentoNoCorrespondeNombre()
                : nombreTipoDocumentoNoCorresponde(codigoTipo);
        Long idTipoDocumento = requerirId(
                catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, codigoTipo), "tipo de documento " + codigoTipo);
        if (existeDocumentoNoCorresponde(conn, registro.getIdExpediente(), idTipoDocumento, registro.getNumeroDocumentoProveido())) {
            return;
        }
        String sql = "INSERT INTO expediente_documento ("
                + "id_expediente, id_tipo_documento_adjunto, nombre_documento, numero_documento, "
                + "fecha_documento, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, registro.getIdExpediente());
            ps.setLong(2, idTipoDocumento);
            ps.setString(3, nombreTipo);
            ps.setString(4, limitar(registro.getNumeroDocumentoProveido(), 100));
            ps.setDate(5, Date.valueOf(LocalDate.now()));
            setLongOrNull(ps, 6, idUsuario);
            ps.executeUpdate();
        }
    }

    private boolean existeDocumentoNoCorresponde(
            Connection conn, Long idExpediente, Long idTipoDocumento, String numeroDocumento) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expediente_documento WHERE id_expediente = ? AND id_tipo_documento_adjunto = ? "
                + "AND UPPER(TRIM(numero_documento)) = UPPER(TRIM(?)) AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idTipoDocumento);
            ps.setString(3, numeroDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private static String nombreTipoDocumentoNoCorresponde(String codigo) {
        return TIPO_DOCUMENTO_HOJA_ENVIO.equalsIgnoreCase(codigo) ? "Hoja de Envío" : "Proveído";
    }

    private void insertarObservacionAnalisis(
            Connection conn, Long idExpediente, ObservacionAnalisisDTO observacion, Long idUsuario) throws SQLException {
        if (observacion == null || !observacion.hasDescripcion()) {
            return;
        }
        Long idTipoObservacion = hasText(observacion.getTipoObservacionCodigo())
                ? requerirId(
                        catalogoLookupDAO.obtenerTipoObservacionId(conn, observacion.getTipoObservacionCodigo()),
                        "tipo de observación " + observacion.getTipoObservacionCodigo())
                : null;
        String sql = "INSERT INTO expediente_observacion ("
                + "id_expediente, id_tipo_observacion, origen_observacion, descripcion, "
                + "subsanada, fecha_observacion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, 'ANALISIS', ?, 0, SYSTIMESTAMP, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            setLongOrNull(ps, 2, idTipoObservacion);
            ps.setString(3, limitar(observacion.getDescripcion(), 1500));
            setLongOrNull(ps, 4, idUsuario);
            ps.executeUpdate();
        }
    }

    private static String comentarioAnalisis(AnalisisRegistroDTO registro) {
        String base = hasText(registro.getFundamento()) ? registro.getFundamento() : "Registro de resultado de análisis.";
        String comentario = hasText(registro.getResultadoNombre()) ? registro.getResultadoNombre() + ": " + base : base;
        if (hasText(registro.getNumeroDocumentoProveido())) {
            String tipo = hasText(registro.getTipoDocumentoNoCorrespondeNombre())
                    ? registro.getTipoDocumentoNoCorrespondeNombre()
                    : nombreTipoDocumentoNoCorresponde(registro.getTipoDocumentoNoCorrespondeCodigo());
            comentario += " " + tipo + ": " + registro.getNumeroDocumentoProveido() + ".";
        }
        return comentario;
    }

    private EvaluacionRegistrada obtenerEvaluacionRegistrada(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT * FROM ("
                + "SELECT ev.corresponde, ev.incorporado, ev.requiere_reconstitucion, "
                + "ev.tiene_legitimidad, ev.cumple_medios_probatorios, ev.fundamento, "
                + "NVL(ev.fecha_evaluacion, ev.creado_en) AS fecha_evaluacion, "
                + "tre.codigo AS resultado_codigo, tre.nombre AS resultado_nombre, "
                + "mnc.codigo AS motivo_codigo, mnc.nombre AS motivo_nombre, est.codigo AS estado_codigo "
                + "FROM expediente_evaluacion ev "
                + "JOIN expediente e ON e.id_expediente = ev.id_expediente "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "LEFT JOIN tipo_resultado_evaluacion tre ON tre.id_tipo_resultado_evaluacion = ev.id_tipo_resultado_evaluacion "
                + "LEFT JOIN motivo_no_corresponde mnc ON mnc.id_motivo_no_corresponde = ev.id_motivo_no_corresponde "
                + "WHERE ev.id_expediente = ? AND ev.activo = 1 "
                + "ORDER BY NVL(ev.modificado_en, ev.creado_en) DESC, ev.id_expediente_evaluacion DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Timestamp fecha = rs.getTimestamp("fecha_evaluacion");
                String resultadoCodigo = rs.getString("resultado_codigo");
                String resultadoNombre = rs.getString("resultado_nombre");
                String estadoActual = rs.getString("estado_codigo");
                // ev.id_tipo_resultado_evaluacion queda NULL cuando el resultado fue NO_CORRESPONDE u
                // OBSERVADO (ver requiereResultadoCatalogo); en ese caso el estado ACTUAL del
                // expediente es, por construcción de resolverEstadoDestinoResultado, el mismo código
                // que el resultado elegido — se usa como respaldo para repoblar el formulario.
                if (!hasText(resultadoCodigo)) {
                    resultadoCodigo = estadoActual;
                    if (ESTADO_NO_CORRESPONDE.equalsIgnoreCase(estadoActual)) {
                        resultadoNombre = "No corresponde a SDRERC";
                    } else if (ESTADO_OBSERVADO.equalsIgnoreCase(estadoActual)) {
                        resultadoNombre = "Observado / requiere subsanación";
                    } else {
                        resultadoNombre = estadoActual;
                    }
                }
                return new EvaluacionRegistrada(
                        getBooleanOrNull(rs, "corresponde"),
                        getBooleanOrNull(rs, "incorporado"),
                        rs.getInt("requiere_reconstitucion") == 1,
                        rs.getInt("tiene_legitimidad") == 1,
                        rs.getInt("cumple_medios_probatorios") == 1,
                        rs.getString("fundamento"),
                        resultadoCodigo,
                        resultadoNombre,
                        rs.getString("motivo_codigo"),
                        rs.getString("motivo_nombre"),
                        fecha == null ? null : fecha.toLocalDateTime().toLocalDate());
            }
        }
    }

    private ObservacionAnalisisDTO obtenerUltimaObservacionAnalisis(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT * FROM ("
                + "SELECT tob.codigo AS tipo_codigo, tob.nombre AS tipo_nombre, o.descripcion "
                + "FROM expediente_observacion o "
                + "LEFT JOIN tipo_observacion tob ON tob.id_tipo_observacion = o.id_tipo_observacion "
                + "WHERE o.id_expediente = ? AND o.activo = 1 AND UPPER(o.origen_observacion) = 'ANALISIS' "
                + "ORDER BY NVL(o.modificado_en, o.creado_en) DESC, o.id_expediente_observacion DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ObservacionAnalisisDTO(rs.getString("tipo_codigo"), rs.getString("tipo_nombre"), rs.getString("descripcion"));
            }
        }
    }

    private DocumentoNoCorrespondeInfo obtenerUltimoDocumentoNoCorresponde(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT codigo, nombre, numero_documento FROM ("
                + "SELECT td.codigo, td.nombre, d.numero_documento "
                + "FROM expediente_documento d "
                + "JOIN tipo_documento_adjunto td ON td.id_tipo_documento_adjunto = d.id_tipo_documento_adjunto "
                + "WHERE d.id_expediente = ? AND d.activo = 1 AND UPPER(td.codigo) IN ('PROVEIDO', 'HOJA_ENVIO') "
                + "ORDER BY NVL(d.modificado_en, d.creado_en) DESC, d.id_expediente_documento DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return DocumentoNoCorrespondeInfo.vacio();
                }
                return new DocumentoNoCorrespondeInfo(rs.getString("codigo"), rs.getString("nombre"), rs.getString("numero_documento"));
            }
        }
    }

    private static AnalisisDetalleDTO analisisVacio() {
        return new AnalisisDetalleDTO(false, "", "", null, null, false, false, false, "", "", "", "", "", "", null, null);
    }

    private static void setBooleanNumberOrNull(PreparedStatement ps, int index, Boolean value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setInt(index, value ? 1 : 0);
        }
    }

    private static Boolean getBooleanOrNull(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value == 1;
    }

    private static String limitar(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static final class EvaluacionRegistrada {
        private final Boolean corresponde;
        private final Boolean incorporado;
        private final boolean requiereReconstitucion;
        private final boolean tieneLegitimidad;
        private final boolean cumpleMediosProbatorios;
        private final String fundamento;
        private final String resultadoCodigo;
        private final String resultadoNombre;
        private final String motivoNoCorrespondeCodigo;
        private final String motivoNoCorrespondeNombre;
        private final LocalDate fechaEvaluacion;

        private EvaluacionRegistrada(
                Boolean corresponde,
                Boolean incorporado,
                boolean requiereReconstitucion,
                boolean tieneLegitimidad,
                boolean cumpleMediosProbatorios,
                String fundamento,
                String resultadoCodigo,
                String resultadoNombre,
                String motivoNoCorrespondeCodigo,
                String motivoNoCorrespondeNombre,
                LocalDate fechaEvaluacion) {
            this.corresponde = corresponde;
            this.incorporado = incorporado;
            this.requiereReconstitucion = requiereReconstitucion;
            this.tieneLegitimidad = tieneLegitimidad;
            this.cumpleMediosProbatorios = cumpleMediosProbatorios;
            this.fundamento = fundamento;
            this.resultadoCodigo = resultadoCodigo;
            this.resultadoNombre = resultadoNombre;
            this.motivoNoCorrespondeCodigo = motivoNoCorrespondeCodigo;
            this.motivoNoCorrespondeNombre = motivoNoCorrespondeNombre;
            this.fechaEvaluacion = fechaEvaluacion;
        }
    }

    private static final class DocumentoNoCorrespondeInfo {
        private final String codigo;
        private final String nombre;
        private final String numero;

        private DocumentoNoCorrespondeInfo(String codigo, String nombre, String numero) {
            this.codigo = hasText(codigo) ? codigo : TIPO_DOCUMENTO_PROVEIDO;
            this.nombre = nombre == null ? "" : nombre;
            this.numero = numero == null ? "" : numero;
        }

        private static DocumentoNoCorrespondeInfo vacio() {
            return new DocumentoNoCorrespondeInfo(TIPO_DOCUMENTO_PROVEIDO, "Proveído", "");
        }
    }

    private static final class Transicion {
        private final Long idEtapaDestino;
        private final Long idEstadoDestino;

        private Transicion(Long idEtapaDestino, Long idEstadoDestino) {
            this.idEtapaDestino = idEtapaDestino;
            this.idEstadoDestino = idEstadoDestino;
        }
    }

    private AnalisisExpedienteDTO map(Connection conn, ResultSet rs) throws SQLException {
        Long idExpediente = getLongOrNull(rs, "id_expediente");
        Date fechaVencimiento = rs.getDate("fecha_vencimiento");
        return new AnalisisExpedienteDTO(
                idExpediente,
                rs.getString("numero_expediente"),
                rs.getString("numero_expediente_sgd"),
                rs.getString("procedimiento"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                rs.getString("responsable"),
                getLongOrNull(rs, "id_responsable"),
                getLongOrNull(rs, "id_equipo_responsable"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                toLocalDate(fechaVencimiento),
                toLocalDateTime(rs.getTimestamp("fecha_registro")),
                calendarioLaboralService.calcularDiasHabilesRestantes(conn, idExpediente, fechaVencimiento),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getInt("grupo_familiar") == 1,
                rs.getInt("asociados_confirmados"),
                rs.getInt("documentos_analizados"));
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
                        getLongOrNull(rs, "id_etapa_actual"),
                        getLongOrNull(rs, "id_estado_actual"),
                        rs.getString("etapa_codigo"),
                        rs.getString("estado_codigo"));
            }
        }
    }

    private void validarTransicion(
            Connection conn, Long idEtapaOrigen, Long idEstadoOrigen, Long idEtapaDestino, Long idEstadoDestino) throws SQLException {
        String sql = "SELECT 1 FROM flujo f "
                + "JOIN flujo_transicion ft ON ft.id_flujo = f.id_flujo "
                + "WHERE f.codigo = ? AND f.activo = 1 "
                + "AND ft.codigo_accion = ? AND ft.activo = 1 "
                + "AND ft.id_etapa_origen = ? AND ft.id_estado_origen = ? "
                + "AND ft.id_etapa_destino = ? AND ft.id_estado_destino = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, CODIGO_FLUJO);
            ps.setString(2, CODIGO_ACCION_RECEPCION);
            ps.setLong(3, idEtapaOrigen);
            ps.setLong(4, idEstadoOrigen);
            ps.setLong(5, idEtapaDestino);
            ps.setLong(6, idEstadoDestino);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException(
                            "No existe transición activa ASIGNACION/ASIGNADO -> ANALISIS/RECIBIDO_POR_ABOGADO para RECEPCION_ASIGNACION.");
                }
            }
        }
    }

    private void actualizarEtapaEstado(
            Connection conn, Long idExpediente, Long idEtapaDestino, Long idEstadoDestino, Long idUsuarioActual) throws SQLException {
        String sql = "UPDATE expediente SET id_etapa_actual = ?, id_estado_actual = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEtapaDestino);
            ps.setLong(2, idEstadoDestino);
            setLongOrNull(ps, 3, idUsuarioActual);
            ps.setLong(4, idExpediente);
            ps.executeUpdate();
        }
    }

    private void insertarHistorial(Connection conn, Long idExpediente, Long idMovimiento, Long idUsuarioActual, String comentario) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_usuario_origen, comentario, activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            setLongOrNull(ps, 3, idUsuarioActual);
            ps.setString(4, comentario);
            setLongOrNull(ps, 5, idUsuarioActual);
            ps.executeUpdate();
        }
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
}
