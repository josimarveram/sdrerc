package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.application.CalendarioLaboralService;
import com.sdrerc.v3.application.CorrelativoExpedienteService;
import com.sdrerc.v3.domain.CargaInicialFilaDTO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * DAO autocontenido del batch de carga inicial (migración histórica, ver v3/CLAUDE.md,
 * Incremento 1 = Registro + Asignación). Deliberadamente NO extiende ni modifica
 * {@link ExpedienteRegistroDAO}/{@link AsignacionExpedienteDAO} (ambos ya en producción, con
 * validaciones pensadas para un formulario interactivo en vivo) — cada método de este DAO trabaja
 * con la fecha histórica explícita de la fila, y sin las validaciones de "equipo activo"/"abogado
 * asignable al equipo elegido en pantalla", que no aplican a un hecho histórico ya ocurrido. Sí
 * reutiliza los Services/DAOs reales de catálogo y correlativo ({@link CatalogoLookupDAO},
 * {@link CorrelativoExpedienteService}, {@link CalendarioLaboralService}) — no se reimplementa
 * ninguna regla de negocio de esos 3.
 *
 * <p>Una fila = una transacción propia (aislamiento por fila, ver {@code CargaInicialImportService},
 * que es quien hace el loop). El procesamiento debe ser estrictamente secuencial (no paralelo):
 * el cálculo del siguiente correlativo de expediente (para DATA DIARIA UNIFICADA) lee el máximo ya
 * confirmado en base, así que 2 filas procesadas en paralelo podrían competir por el mismo número.</p>
 *
 * <p>Regla de confidencialidad: ningún método de esta clase loguea/imprime valores de fila — el
 * {@link ResultadoFila} que devuelve {@link #procesarFila} solo trae motivos estructurales.</p>
 */
@Repository
public class CargaInicialDAO {

    private static final String CODIGO_ETAPA_REGISTRO = "REGISTRO";
    private static final String CODIGO_ESTADO_REGISTRADO = "REGISTRADO";
    private static final String CODIGO_ETAPA_ASIGNACION = "ASIGNACION";
    private static final String CODIGO_ESTADO_ASIGNADO = "ASIGNADO";
    private static final String CODIGO_MOVIMIENTO_REGISTRO = "IMPORTACION_CARGA_INICIAL";
    private static final String CODIGO_MOVIMIENTO_ASIGNACION = "ASIGNACION_CARGA_INICIAL";
    private static final String CODIGO_CANAL_OR_PASIVO = "OR_PASIVO";

    // Incremento 2 (Análisis + paso técnico por Verificación + Ejecución, ver v3/CLAUDE.md).
    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String CODIGO_ETAPA_ANALISIS = "ANALISIS";
    private static final String CODIGO_ESTADO_RECIBIDO_POR_ABOGADO = "RECIBIDO_POR_ABOGADO";
    private static final String CODIGO_ESTADO_ATENDIDO = "ATENDIDO";
    private static final String CODIGO_ESTADO_EN_ABANDONO_ANALISIS = "EN_ABANDONO";
    private static final String CODIGO_ESTADO_NO_CORRESPONDE = "NO_CORRESPONDE";
    private static final String CODIGO_ESTADO_OBSERVACION_ADMINISTRATIVA = "OBSERVACION_ADMINISTRATIVA";
    private static final String CODIGO_ETAPA_VERIFICACION = "VERIFICACION";
    private static final String CODIGO_ESTADO_EN_VERIFICACION = "EN_VERIFICACION";
    private static final String CODIGO_ETAPA_EJECUCION = "EJECUCION";
    private static final String CODIGO_ESTADO_EN_EJECUCION = "EN_EJECUCION";
    private static final String CODIGO_ESTADO_EJECUTADO = "EJECUTADO";
    private static final String CODIGO_ESTADO_REQUIERE_CORRECCION = "REQUIERE_CORRECCION";
    private static final String CODIGO_ACCION_RECEPCION_ASIGNACION = "RECEPCION_ASIGNACION";
    private static final String CODIGO_ACCION_REGISTRO_RESULTADO_ANALISIS = "REGISTRO_RESULTADO_ANALISIS";
    private static final String CODIGO_ACCION_ENVIO_VERIFICACION = "ENVIO_VERIFICACION";
    private static final String CODIGO_ACCION_APROBACION_VERIFICACION = "APROBACION_VERIFICACION";
    private static final String CODIGO_ACCION_INICIO_EJECUCION = "INICIO_EJECUCION";
    private static final String CODIGO_ACCION_OBSERVACION_EJECUCION = "OBSERVACION_EJECUCION";

    // Incremento 3 (Notificación, ver v3/CLAUDE.md).
    private static final String CODIGO_ETAPA_NOTIFICACION = "NOTIFICACION";
    private static final String CODIGO_ESTADO_POR_ASIGNAR = "POR_ASIGNAR";
    private static final String CODIGO_ESTADO_EN_NOTIFICACION = "EN_NOTIFICACION";
    private static final String CODIGO_ACCION_DERIVACION_NOTIFICACION = "DERIVACION_A_NOTIFICACION";
    private static final String CODIGO_EQUIPO_NOTIFICACION = "EQ_NOTIFICACION";
    private static final String CODIGO_ESTADO_DOCUMENTO_EMITIDO = "EMITIDO";
    private static final String CODIGO_TIPO_NOTIF_VIRTUAL = "VIRTUAL";
    private static final String CODIGO_TIPO_NOTIF_PRESENCIAL_2 = "PRESENCIAL_2";
    private static final String CODIGO_ESTADO_NOTIF_EXITOSA = "EXITOSA";
    private static final String CODIGO_ESTADO_NOTIF_FALLIDA = "FALLIDA";
    private static final String CODIGO_ESTADO_CARGO_RECIBIDO = "CARGO_RECIBIDO";

    public static final String ESTADO_IMPORTADO = "IMPORTADO";
    public static final String ESTADO_INCOMPLETO = "INCOMPLETO";
    public static final String ESTADO_ERROR = "ERROR";

    private final DataSource dataSource;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final CorrelativoExpedienteService correlativoExpedienteService;
    private final CalendarioLaboralService calendarioLaboralService;

    public CargaInicialDAO(
            DataSource dataSource,
            CatalogoLookupDAO catalogoLookupDAO,
            CorrelativoExpedienteService correlativoExpedienteService,
            CalendarioLaboralService calendarioLaboralService) {
        this.dataSource = dataSource;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.correlativoExpedienteService = correlativoExpedienteService;
        this.calendarioLaboralService = calendarioLaboralService;
    }

    public record ResultadoFila(Long idExpediente, String etapaAlcanzada, String estado, String mensaje) {
    }

    /** Registra 1 fila de auditoría en CARGA_INICIAL_LOG (script 111). Conexión propia, fuera de la
     * transacción de {@link #procesarFila} — un fallo al auditar nunca debe revertir un expediente
     * ya confirmado. */
    public void registrarLog(String hojaOrigen, int filaExcel, ResultadoFila resultado, Long idUsuarioActual) {
        String sql = "INSERT INTO carga_inicial_log ("
                + "hoja_origen, fila_excel, id_expediente_resultante, etapa_alcanzada, estado, mensaje, id_usuario_proceso"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hojaOrigen);
            ps.setInt(2, filaExcel);
            setLongOrNull(ps, 3, resultado.idExpediente());
            ps.setString(4, resultado.etapaAlcanzada());
            ps.setString(5, resultado.estado());
            ps.setString(6, limitar(resultado.mensaje(), 500));
            setLongOrNull(ps, 7, idUsuarioActual);
            ps.executeUpdate();
        } catch (SQLException ex) {
            // El log es auditoría de mejor esfuerzo: si CARGA_INICIAL_LOG no existe todavía (script
            // 111 sin ejecutar) o falla por cualquier motivo, no debe interrumpir el batch — el
            // resultado real de la fila ya quedó confirmado (o no) en su propia transacción.
        }
    }

    /**
     * Busca, en {@code CARGA_INICIAL_LOG}, el expediente ya creado por la 1ra pasada para una fila
     * puntual (por hoja+fila de Excel) — la más reciente si hay más de un registro para esa fila
     * (nunca debería haberlo salvo tras un {@link #reintentarAsignacion}, que agrega su propia fila
     * de log sin tocar la original). Null si esa fila nunca llegó a crear expediente (quedó en ERROR
     * en la 1ra pasada) o si el log no tiene registro de ella.
     */
    public Long buscarExpedienteResultante(String hojaOrigen, int filaExcel) {
        String sql = "SELECT * FROM (SELECT id_expediente_resultante FROM carga_inicial_log "
                + "WHERE hoja_origen = ? AND fila_excel = ? AND id_expediente_resultante IS NOT NULL "
                + "ORDER BY id_carga_inicial_log DESC) WHERE ROWNUM = 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hojaOrigen);
            ps.setInt(2, filaExcel);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return rs.wasNull() ? null : id;
                }
                return null;
            }
        } catch (SQLException ex) {
            return null;
        }
    }

    /**
     * Segunda pasada, exclusiva de Asignación — NO vuelve a tocar Registro (persona/solicitud/acta/
     * documento ya insertados por {@link #procesarFila} quedan intactos). Reintenta resolver el
     * abogado de una fila ya procesada (con el matching mejorado: tildes/coma/espacios, alias de
     * typos conocidos, e incluye USUARIO inactivos — ver {@link #resolverAbogadoPorNombre}) y, si
     * ahora resuelve abogado+equipo, recién ahí inserta la asignación real.
     *
     * <p>Idempotente y segura de reejecutar: si el expediente ya salió de etapa REGISTRO (porque esta
     * misma fila ya se asignó en una pasada anterior, o porque alguien ya lo tocó manualmente desde
     * la propia UI de Asignación mientras tanto), no hace nada — nunca pisa una asignación real ya
     * hecha por otro medio.</p>
     */
    public ResultadoFila reintentarAsignacion(CargaInicialFilaDTO fila, Long idExpediente, Long idUsuarioActual) {
        if (!hasText(fila.getAsignadoAAbogado())) {
            return new ResultadoFila(idExpediente, null, ESTADO_INCOMPLETO, "Sin abogado informado en el Excel.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String etapaActual = obtenerEtapaActualCodigo(conn, idExpediente);
                if (!CODIGO_ETAPA_REGISTRO.equals(etapaActual)) {
                    conn.rollback();
                    conn.setAutoCommit(previousAutoCommit);
                    return new ResultadoFila(idExpediente, etapaActual, ESTADO_INCOMPLETO,
                            "Expediente ya no está en REGISTRO, se omite (ya fue asignado o tocado manualmente).");
                }

                Long idAbogado = resolverAbogadoPorNombre(conn, fila.getAsignadoAAbogado());
                if (idAbogado == null) {
                    conn.rollback();
                    conn.setAutoCommit(previousAutoCommit);
                    return new ResultadoFila(idExpediente, CODIGO_ETAPA_REGISTRO, ESTADO_INCOMPLETO,
                            "Abogado sigue sin resolverse por nombre (ni con alias ni entre inactivos).");
                }
                Long idEquipo = resolverEquipoUnicoDelAbogado(conn, idAbogado);
                if (idEquipo == null) {
                    conn.rollback();
                    conn.setAutoCommit(previousAutoCommit);
                    return new ResultadoFila(idExpediente, CODIGO_ETAPA_REGISTRO, ESTADO_INCOMPLETO,
                            "Abogado resuelto pero sin equipo activo único.");
                }

                Long idEtapaRegistro = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, CODIGO_ETAPA_REGISTRO), "etapa REGISTRO");
                Long idEstadoRegistrado = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, CODIGO_ESTADO_REGISTRADO), "estado REGISTRADO");
                Long idEtapaAsignacion = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, CODIGO_ETAPA_ASIGNACION), "etapa ASIGNACION");
                Long idEstadoAsignado = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, CODIGO_ESTADO_ASIGNADO), "estado ASIGNADO");
                Long idMovimientoAsignacion = requerirId(
                        catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_ASIGNACION), "movimiento ASIGNACION_CARGA_INICIAL");
                LocalDate fechaAsignacion = fila.getFechaAsignado() != null ? fila.getFechaAsignado() : fila.getFechaSolicitud();

                String numeroHojaEnvio = fila.getNumeroHojaEnvioAsignacion();
                boolean hojaEnvioOmitida = existeHojaEnvioActiva(conn, numeroHojaEnvio);
                if (hojaEnvioOmitida) {
                    numeroHojaEnvio = null;
                }

                insertarAsignacion(conn, idExpediente, idAbogado, idEquipo, idEtapaAsignacion, fechaAsignacion, numeroHojaEnvio);
                actualizarExpedienteResponsable(conn, idExpediente, idEtapaAsignacion, idEstadoAsignado, idAbogado, idEquipo);
                insertarHistorial(
                        conn, idExpediente, idMovimientoAsignacion, fechaAsignacion,
                        idEtapaAsignacion, idEstadoAsignado, idEtapaRegistro, idEstadoRegistrado,
                        "Asignación por carga inicial (segunda pasada, matching de abogado mejorado).", idUsuarioActual);

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                if (hojaEnvioOmitida) {
                    return new ResultadoFila(idExpediente, CODIGO_ETAPA_ASIGNACION, ESTADO_INCOMPLETO,
                            "N° de hoja de envío ya registrado en otro expediente (duplicado histórico), se omitió.");
                }
                return new ResultadoFila(idExpediente, CODIGO_ETAPA_ASIGNACION, ESTADO_IMPORTADO, null);
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                return new ResultadoFila(idExpediente, null, ESTADO_ERROR, ex.getMessage());
            }
        } catch (SQLException ex) {
            return new ResultadoFila(idExpediente, null, ESTADO_ERROR, ex.getMessage());
        }
    }

    /**
     * {@code UX_EXP_ASIG_HOJA_ENVIO_ACT} es un índice único (case/trim-insensitive) sobre CUALQUIER
     * fila no eliminada lógicamente de {@code EXPEDIENTE_ASIGNACION} (columna {@code ACTIVO}, no
     * {@code ACTIVA}) — el mismo N° de hoja de envío nunca puede repetirse en todo el historial, ni
     * siquiera contra una asignación ya superada. Verificado contra 18 casos reales del Excel de
     * carga inicial (26/08/2026): cada uno ya existía exactamente 1 vez en OTRO expediente — una
     * duplicación real del propio histórico, no un error del batch. Se omite el valor (asignación
     * igual se registra, sin ese dato) en vez de bloquear toda la fila por este motivo secundario.
     */
    private boolean existeHojaEnvioActiva(Connection conn, String numeroHojaEnvio) throws SQLException {
        if (!hasText(numeroHojaEnvio)) {
            return false;
        }
        String sql = "SELECT COUNT(1) FROM expediente_asignacion "
                + "WHERE activo = 1 AND UPPER(TRIM(numero_hoja_envio)) = UPPER(TRIM(?))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroHojaEnvio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Reconoce los 3 formatos reales encontrados en la columna "N EXPEDIENTE DIGITAL" del Excel de
     * carga inicial (26/08/2026, ver v3/CLAUDE.md): {@code DUPLICADO_EXP 018}, {@code DUPLICIDAD_EXP
     * 0329}, {@code SDRERC_EXP_2026_000 018} — en los 3 casos el número que sigue (sin importar
     * cuántos dígitos/ceros a la izquierda tenga) ES el correlativo real que le corresponde a esta
     * fila, confirmado explícitamente por el usuario: esta fila es un duplicado de otro expediente ya
     * numerado con ese correlativo, y por la regla ya vigente en el sistema ("duplicado asociado
     * hereda número de expediente", ver CLAUDE.md raíz, sección Asignación) DEBE compartir el mismo
     * {@code numero_expediente}, no recibir uno nuevo. Valores sin ninguno de estos 3 prefijos (ej.
     * "RENIEC", un nombre de persona, "GGG") no matchean y se ignoran — nunca se adivina.
     */
    private static final Pattern PATRON_NUMERO_EXPEDIENTE_DIGITAL = Pattern.compile(
            "(?:DUPLICADO_EXP|DUPLICIDAD_EXP|SDRERC_EXP_\\d{4}_0+)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    private Long extraerCorrelativoExpedienteDigital(String valor) {
        if (!hasText(valor)) {
            return null;
        }
        Matcher m = PATRON_NUMERO_EXPEDIENTE_DIGITAL.matcher(valor.trim());
        if (!m.find()) {
            return null;
        }
        try {
            return Long.parseLong(m.group(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Corrige {@code numero_expediente} de un expediente YA CREADO (por {@link #procesarFila}) cuando
     * la fila trae un marcador reconocible de "N EXPEDIENTE DIGITAL" — nunca crea/borra nada más,
     * solo el UPDATE puntual de ese campo. Sin transacción explícita (una sola sentencia).
     */
    public ResultadoFila corregirNumeroExpedienteDigital(CargaInicialFilaDTO fila, Long idExpediente) {
        Long correlativo = extraerCorrelativoExpedienteDigital(fila.getNumeroExpedienteDigital());
        if (correlativo == null) {
            return new ResultadoFila(idExpediente, null, ESTADO_INCOMPLETO,
                    "N EXPEDIENTE DIGITAL sin patrón reconocible, no se corrigió el número.");
        }
        int anio = fila.getFechaSolicitud() != null ? fila.getFechaSolicitud().getYear() : correlativoExpedienteService.anioActual();
        String numeroCorregido = correlativoExpedienteService.generar(anio, correlativo.intValue());
        String sql = "UPDATE expediente SET numero_expediente = ? WHERE id_expediente = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroCorregido);
            ps.setLong(2, idExpediente);
            ps.executeUpdate();
            return new ResultadoFila(idExpediente, null, ESTADO_IMPORTADO,
                    "N° de expediente corregido a " + numeroCorregido + " (duplicado según N EXPEDIENTE DIGITAL).");
        } catch (SQLException ex) {
            return new ResultadoFila(idExpediente, null, ESTADO_ERROR, ex.getMessage());
        }
    }

    private String obtenerEtapaActualCodigo(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT ee.codigo FROM expediente e "
                + "JOIN etapa_expediente ee ON ee.id_etapa = e.id_etapa_actual "
                + "WHERE e.id_expediente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private record EtapaEstado(String etapaCodigo, String estadoCodigo) {
    }

    private EtapaEstado bloquearYObtenerEtapaEstado(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT ee.codigo AS etapa_codigo, es.codigo AS estado_codigo FROM expediente e "
                + "JOIN etapa_expediente ee ON ee.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente es ON es.id_estado = e.id_estado_actual "
                + "WHERE e.id_expediente = ? FOR UPDATE OF e.id_etapa_actual";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Expediente no encontrado: " + idExpediente + ".");
                }
                return new EtapaEstado(rs.getString("etapa_codigo"), rs.getString("estado_codigo"));
            }
        }
    }

    private record TransicionIds(Long idEtapaDestino, Long idEstadoDestino) {
    }

    /**
     * Verifica que exista una transición ACTIVA en {@code flujo_transicion} para la acción/origen/
     * destino dados y resuelve los ids del destino — null si no existe (el llamador decide si eso es
     * un dato incompleto o un error real). A diferencia de los DAOs operativos reales (Análisis/
     * Verificación/Ejecución), este método NO reproduce {@code validarRequisitosTransicion}
     * (exigir documentos analizados, comentario obligatorio, etc.) — decisión deliberada: el batch
     * reconstruye HECHOS históricos ya ocurridos, no valida una acción de un formulario en vivo.
     */
    private TransicionIds resolverTransicion(
            Connection conn, String accionCodigo, String etapaOrigenCodigo, String estadoOrigenCodigo,
            String etapaDestinoCodigo, String estadoDestinoCodigo) throws SQLException {
        Long idEtapaDestino = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, etapaDestinoCodigo), "etapa " + etapaDestinoCodigo);
        Long idEstadoDestino = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, estadoDestinoCodigo), "estado " + estadoDestinoCodigo);
        String sql = "SELECT 1 FROM flujo f "
                + "JOIN flujo_transicion ft ON ft.id_flujo = f.id_flujo "
                + "JOIN etapa_expediente eo ON eo.id_etapa = ft.id_etapa_origen "
                + "JOIN estado_expediente so ON so.id_estado = ft.id_estado_origen "
                + "WHERE f.codigo = ? AND f.activo = 1 AND ft.activo = 1 AND ft.codigo_accion = ? "
                + "AND eo.codigo = ? AND so.codigo = ? AND ft.id_etapa_destino = ? AND ft.id_estado_destino = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, CODIGO_FLUJO);
            ps.setString(2, accionCodigo);
            ps.setString(3, etapaOrigenCodigo);
            ps.setString(4, estadoOrigenCodigo);
            ps.setLong(5, idEtapaDestino);
            ps.setLong(6, idEstadoDestino);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
            }
        }
        return new TransicionIds(idEtapaDestino, idEstadoDestino);
    }

    private TransicionIds requerirTransicionCargaInicial(
            Connection conn, String accionCodigo, String etapaOrigenCodigo, String estadoOrigenCodigo,
            String etapaDestinoCodigo, String estadoDestinoCodigo) throws SQLException {
        TransicionIds t = resolverTransicion(conn, accionCodigo, etapaOrigenCodigo, estadoOrigenCodigo, etapaDestinoCodigo, estadoDestinoCodigo);
        if (t == null) {
            throw new SQLException("No existe transición activa " + etapaOrigenCodigo + "/" + estadoOrigenCodigo
                    + " -> " + etapaDestinoCodigo + "/" + estadoDestinoCodigo + " para " + accionCodigo + ".");
        }
        return t;
    }

    private void actualizarEtapaEstadoExpediente(Connection conn, Long idExpediente, Long idEtapaDestino, Long idEstadoDestino) throws SQLException {
        String sql = "UPDATE expediente SET id_etapa_actual = ?, id_estado_actual = ?, fecha_ultimo_movimiento = SYSTIMESTAMP WHERE id_expediente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEtapaDestino);
            ps.setLong(2, idEstadoDestino);
            ps.setLong(3, idExpediente);
            ps.executeUpdate();
        }
    }

    private void insertarEvaluacionCargaInicial(
            Connection conn, Long idExpediente, Long idTipoResultadoEvaluacion, LocalDate fechaEvaluacion, Long idUsuarioActual) throws SQLException {
        insertarEvaluacionCargaInicialConFundamento(
                conn, idExpediente, idTipoResultadoEvaluacion, "Registrado por carga inicial (migración histórica).", fechaEvaluacion, idUsuarioActual);
    }

    /**
     * Variante para el caso "Resolución sin calificación" (pedido explícito del usuario, 26/08/2026):
     * {@code id_tipo_resultado_evaluacion} queda NULL (columna nullable) — no hay un resultado legal
     * real que registrar cuando {@code EJECUCIONES.TIPO DE CALIFICACION} viene vacío, pero el
     * expediente igual tiene Resolución+Procedimiento+Estado de ejecución, suficiente para avanzarlo.
     */
    private void insertarEvaluacionCargaInicialConFundamento(
            Connection conn, Long idExpediente, Long idTipoResultadoEvaluacionOrNull, String fundamento,
            LocalDate fechaEvaluacion, Long idUsuarioActual) throws SQLException {
        String sql = "INSERT INTO expediente_evaluacion ("
                + "id_expediente, id_tipo_resultado_evaluacion, corresponde, incorporado, requiere_reconstitucion, "
                + "tiene_legitimidad, cumple_medios_probatorios, fundamento, fecha_evaluacion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, 1, 1, 0, 1, 1, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            setLongOrNull(ps, 2, idTipoResultadoEvaluacionOrNull);
            ps.setString(3, fundamento);
            ps.setDate(4, fechaEvaluacion == null ? null : Date.valueOf(fechaEvaluacion));
            setLongOrNull(ps, 5, idUsuarioActual);
            ps.executeUpdate();
        }
    }

    private void insertarObservacionEjecucionCargaInicial(Connection conn, Long idExpediente, String descripcion, Long idUsuarioActual) throws SQLException {
        String sql = "INSERT INTO expediente_observacion (id_expediente, origen_observacion, descripcion, subsanada, fecha_observacion, activo, creado_por, creado_en) "
                + "VALUES (?, 'EJECUCION', ?, 0, SYSTIMESTAMP, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, limitar(descripcion, 1500));
            setLongOrNull(ps, 3, idUsuarioActual);
            ps.executeUpdate();
        }
    }

    /**
     * Traduce el texto libre de "EJECUCIONES.TIPO DE CALIFICACION" al código real de
     * {@code tipo_resultado_evaluacion} (12 códigos activos tras el script 112: EDICTO, EN_ABANDONO,
     * FALTA_SUSTENTO, IMPROCEDENTE, INDAGATORIO, NO_CORRESPONDE, OBSERVACION_ADMINISTRATIVA,
     * PRECISAR_PRETENSION, PROCEDENTE, PROCEDENTE_EN_PARTE, DESISTIMIENTO, INFUNDADO) por CONTENCIÓN
     * normalizada (mismo criterio ya usado en Carga diaria para Centro de Atención/Municipalidad) —
     * tolera typos reales ya verificados ("PROCEENTE EN PARTE") y texto con datos adicionales
     * ("PROCEDENTE/ MUNICIPALIDAD"). Orden de comprobación deliberado: IMPROCEDENTE y "EN PARTE" se
     * evalúan antes que PROCEDENTE (subcadena). Null si no matchea ningún código — nunca se adivina.
     */
    private String mapearResultadoAnalisis(String textoBruto) {
        if (!hasText(textoBruto)) {
            return null;
        }
        String t = normalizarJava(textoBruto);
        if (t.contains("IMPROCEDENTE")) {
            return "IMPROCEDENTE";
        }
        if (t.contains("PROCEDENTE EN PARTE") || t.contains("PROCEENTE EN PARTE")) {
            return "PROCEDENTE_EN_PARTE";
        }
        if (t.contains("PROCEDENTE") || t.contains("PROCEENTE")) {
            return "PROCEDENTE";
        }
        if (t.contains("ABANDONO")) {
            return "EN_ABANDONO";
        }
        if (t.contains("DESISTIMIENTO") || t.contains("DESESTIMIENTO")) {
            return "DESISTIMIENTO";
        }
        if (t.contains("INFUNDADO")) {
            return "INFUNDADO";
        }
        if (t.contains("EDICTO")) {
            return "EDICTO";
        }
        if (t.contains("FALTA SUSTENTO") || t.contains("FALTA DE SUSTENTO")) {
            return "FALTA_SUSTENTO";
        }
        if (t.contains("INDAGATORIO")) {
            return "INDAGATORIO";
        }
        if (t.contains("NO CORRESPONDE")) {
            return "NO_CORRESPONDE";
        }
        if (t.contains("OBSERVACION ADMINISTRATIVA")) {
            return "OBSERVACION_ADMINISTRATIVA";
        }
        if (t.contains("PRECISAR PRETENSION") || t.contains("PRECISAR LA PRETENSION")) {
            return "PRECISAR_PRETENSION";
        }
        return null;
    }

    /**
     * De los 12 códigos posibles de {@link #mapearResultadoAnalisis}, solo 5 estados de
     * {@code estado_expediente} (etapa ANALISIS) son destinos válidos de
     * {@code REGISTRO_RESULTADO_ANALISIS} desde RECIBIDO_POR_ABOGADO: ATENDIDO/OBSERVADO/
     * NO_CORRESPONDE/EN_ABANDONO/OBSERVACION_ADMINISTRATIVA (verificado contra Oracle real). Un
     * resultado ya de por sí resolutivo (PROCEDENTE/IMPROCEDENTE/PROCEDENTE_EN_PARTE/INFUNDADO/
     * EDICTO/FALTA_SUSTENTO/INDAGATORIO/PRECISAR_PRETENSION) avanza a ATENDIDO (sigue el camino real
     * hacia Verificación/Ejecución); DESISTIMIENTO no tiene un estado propio, se mapea al más cercano
     * semánticamente (EN_ABANDONO — el ciudadano desistió, el caso no se resuelve). No replica la
     * inconsistencia real de V2 (donde el resultado EN_ABANDONO nunca produce el estado EN_ABANDONO
     * del expediente pese a ser una transición válida) — para reconstruir historia, se usa la
     * transición real y válida.
     */
    private String mapearEstadoDestinoAnalisis(String resultadoCodigo) {
        if (CODIGO_ESTADO_NO_CORRESPONDE.equals(resultadoCodigo)) {
            return CODIGO_ESTADO_NO_CORRESPONDE;
        }
        if (CODIGO_ESTADO_OBSERVACION_ADMINISTRATIVA.equals(resultadoCodigo)) {
            return CODIGO_ESTADO_OBSERVACION_ADMINISTRATIVA;
        }
        if (CODIGO_ESTADO_EN_ABANDONO_ANALISIS.equals(resultadoCodigo) || "DESISTIMIENTO".equals(resultadoCodigo)) {
            return CODIGO_ESTADO_EN_ABANDONO_ANALISIS;
        }
        return CODIGO_ESTADO_ATENDIDO;
    }

    /**
     * Traduce "EJECUCIONES.ESTADO" a uno de 4 grupos de acción, decisión confirmada explícitamente
     * por el usuario ("Agrupar por resultado real", 26/08/2026): EJECUTAR (EJECUTADO/CULMINACION_EN_
     * LINEA/NO_CORRESPONDE_EJECUTAR → aplica INICIO_EJECUCION real), OBSERVAR (RESOLUCION_OBSERVADA →
     * aplica OBSERVACION_EJECUCION), PENDIENTE (PENDIENTE_POR_EJECUTAR/POR_EJECUTAR_POR_OREC → se
     * queda En Ejecución, sin forzar avance) y DETENER (ABANDONO/ANULADO → se queda En Ejecución
     * también, ninguno de los 4 estados reales de Ejecución representa "abandonado/anulado"). Null si
     * no matchea ningún grupo.
     */
    private String clasificarGrupoEjecucion(String textoBruto) {
        if (!hasText(textoBruto)) {
            return null;
        }
        String t = normalizarJava(textoBruto);
        if (t.contains("ABANDONO")) {
            return "DETENER";
        }
        if (t.contains("ANULA")) {
            return "DETENER";
        }
        if (t.contains("OBSERVA")) {
            return "OBSERVAR";
        }
        if (t.contains("EJECUTADO")) {
            return "EJECUTAR";
        }
        if (t.contains("CULMINACION")) {
            return "EJECUTAR";
        }
        if (t.contains("NO CORRESPONDE")) {
            return "EJECUTAR";
        }
        if (t.contains("PENDIENTE")) {
            return "PENDIENTE";
        }
        if (t.contains("OREC")) {
            return "PENDIENTE";
        }
        return null;
    }

    /**
     * Incremento 2 (Análisis + paso técnico por Verificación + Ejecución, ver v3/CLAUDE.md):
     * segunda/tercera capa sobre un expediente YA CREADO por {@link #procesarFila} y ya en
     * ASIGNACION/ASIGNADO. Avanza: Recepción (RECEPCION_ASIGNACION) → resultado de análisis
     * (REGISTRO_RESULTADO_ANALISIS, mapeado desde "EJECUCIONES.TIPO DE CALIFICACION") → si el
     * resultado es resolutivo (ATENDIDO), paso técnico por Verificación (ENVIO_VERIFICACION +
     * APROBACION_VERIFICACION, sin intervención real de un verificador — mismo criterio ya acordado
     * en el plan: "usar EJECUCIONES.FECHA DE EMISIÓN como aproximación para esas 3 transiciones") →
     * Ejecución (INICIO_EJECUCION u OBSERVACION_EJECUCION, según "EJECUCIONES.ESTADO"). Notificación
     * queda deliberadamente diferida (requeriría sintetizar un documento analizado, fuera de alcance
     * — decisión confirmada explícitamente por el usuario). El valor crudo del Excel NUNCA se
     * persiste en el historial (solo el código ya mapeado) — dato de clasificación de negocio, no de
     * ciudadano, pero se prefiere no repetir texto libre del archivo en ningún registro nuevo.
     */
    public ResultadoFila avanzarAnalisisVerificacionEjecucion(CargaInicialFilaDTO fila, Long idExpediente, Long idUsuarioActual) {
        if (idExpediente == null) {
            return new ResultadoFila(null, null, ESTADO_INCOMPLETO, "Sin expediente creado en la 1ra/2da pasada, se omite.");
        }
        String numeroResolucionPreview = hasText(fila.getResolucionLimpia()) ? fila.getResolucionLimpia() : fila.getNumeroResolucion();
        // Camino alterno "Resolución sin calificación" (pedido explícito del usuario, 26/08/2026,
        // ver v3/CLAUDE.md): si EJECUCIONES.TIPO DE CALIFICACION viene vacío pero SÍ hay N° de
        // Resolución y EJECUCIONES.ESTADO, igual se avanza — ver más abajo.
        boolean puedeAvanzarSinCalificacion = hasText(numeroResolucionPreview) && hasText(fila.getEstadoEjecucionTexto());
        if (!hasText(fila.getResultadoAnalisisTexto()) && !puedeAvanzarSinCalificacion) {
            return new ResultadoFila(idExpediente, null, ESTADO_INCOMPLETO,
                    "Sin resultado de análisis en el Excel (EJECUCIONES.TIPO DE CALIFICACION), no se avanzó.");
        }
        LocalDate fechaBase = fila.getFechaEmisionAnalisis() != null
                ? fila.getFechaEmisionAnalisis()
                : (fila.getFechaAsignado() != null ? fila.getFechaAsignado() : fila.getFechaSolicitud());
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                EtapaEstado inicial = bloquearYObtenerEtapaEstado(conn, idExpediente);
                if (!CODIGO_ETAPA_ASIGNACION.equals(inicial.etapaCodigo()) || !CODIGO_ESTADO_ASIGNADO.equals(inicial.estadoCodigo())) {
                    conn.rollback();
                    conn.setAutoCommit(previousAutoCommit);
                    return new ResultadoFila(idExpediente, inicial.etapaCodigo(), ESTADO_INCOMPLETO,
                            "Expediente no está en ASIGNACION/ASIGNADO (sigue sin abogado/equipo resuelto), no se avanzó a Análisis.");
                }

                // Paso 1: Recepción por el abogado (ASIGNACION/ASIGNADO -> ANALISIS/RECIBIDO_POR_ABOGADO).
                TransicionIds tRecepcion = requerirTransicionCargaInicial(
                        conn, CODIGO_ACCION_RECEPCION_ASIGNACION, CODIGO_ETAPA_ASIGNACION, CODIGO_ESTADO_ASIGNADO,
                        CODIGO_ETAPA_ANALISIS, CODIGO_ESTADO_RECIBIDO_POR_ABOGADO);
                actualizarEtapaEstadoExpediente(conn, idExpediente, tRecepcion.idEtapaDestino(), tRecepcion.idEstadoDestino());
                insertarHistorialCargaInicial2(
                        conn, idExpediente, CODIGO_ACCION_RECEPCION_ASIGNACION, fechaBase,
                        tRecepcion.idEtapaDestino(), tRecepcion.idEstadoDestino(),
                        CODIGO_ETAPA_ASIGNACION, CODIGO_ESTADO_ASIGNADO,
                        "Recepción por carga inicial.", idUsuarioActual);

                // Paso 2: resultado de análisis.
                String resultadoCodigo = mapearResultadoAnalisis(fila.getResultadoAnalisisTexto());
                if (resultadoCodigo == null) {
                    if (!puedeAvanzarSinCalificacion) {
                        conn.commit();
                        conn.setAutoCommit(previousAutoCommit);
                        return new ResultadoFila(idExpediente, CODIGO_ETAPA_ANALISIS, ESTADO_INCOMPLETO,
                                "Resultado de análisis no reconocido, expediente queda Recibido en Análisis.");
                    }
                    // Camino alterno "Resolución sin calificación" (pedido explícito del usuario,
                    // 26/08/2026, ver v3/CLAUDE.md): sin EJECUCIONES.TIPO DE CALIFICACION no hay
                    // resultado legal que registrar, pero SÍ hay Resolución+Procedimiento+Estado —
                    // se sintetiza igual el documento de Resolución (tipo real vía PROCEDIMIENTO) y se
                    // fuerza ATENDIDO (evaluación sin id_tipo_resultado_evaluacion) para que el
                    // expediente avance por el mismo paso técnico y aterrice donde indique
                    // EJECUCIONES.ESTADO — nunca se sintetiza Carta de notificación después (no hay
                    // forma de saber qué tipo de carta corresponde sin la calificación real).
                    String codigoResolucion = mapearTipoResolucion(fila.getProcedimientoEjecuciones());
                    obtenerOInsertarDocumentoAnalizadoCargaInicial(conn, idExpediente, codigoResolucion, numeroResolucionPreview, fechaBase, idUsuarioActual);
                    insertarEvaluacionCargaInicialConFundamento(
                            conn, idExpediente, null,
                            "Registrado por carga inicial (migración histórica) — EJECUCIONES.TIPO DE CALIFICACION no informado; "
                                    + "se avanzó con N° de Resolución + Procedimiento + Estado de ejecución.",
                            fechaBase, idUsuarioActual);
                    TransicionIds tResultadoForzado = requerirTransicionCargaInicial(
                            conn, CODIGO_ACCION_REGISTRO_RESULTADO_ANALISIS, CODIGO_ETAPA_ANALISIS, CODIGO_ESTADO_RECIBIDO_POR_ABOGADO,
                            CODIGO_ETAPA_ANALISIS, CODIGO_ESTADO_ATENDIDO);
                    actualizarEtapaEstadoExpediente(conn, idExpediente, tResultadoForzado.idEtapaDestino(), tResultadoForzado.idEstadoDestino());
                    insertarHistorialCargaInicial2(
                            conn, idExpediente, CODIGO_ACCION_REGISTRO_RESULTADO_ANALISIS, fechaBase,
                            tResultadoForzado.idEtapaDestino(), tResultadoForzado.idEstadoDestino(),
                            CODIGO_ETAPA_ANALISIS, CODIGO_ESTADO_RECIBIDO_POR_ABOGADO,
                            "Resultado de análisis por carga inicial: sin calificación informada, se avanza vía Resolución.", idUsuarioActual);
                    ResultadoFila resultadoSinCalificacion = avanzarVerificacionYEjecucion(conn, fila, idExpediente, idUsuarioActual, fechaBase);
                    conn.commit();
                    conn.setAutoCommit(previousAutoCommit);
                    return resultadoSinCalificacion;
                }
                Long idResultado = requerirId(
                        catalogoLookupDAO.obtenerTipoResultadoEvaluacionId(conn, resultadoCodigo), "resultado " + resultadoCodigo);
                insertarEvaluacionCargaInicial(conn, idExpediente, idResultado, fechaBase, idUsuarioActual);

                String estadoDestinoAnalisis = mapearEstadoDestinoAnalisis(resultadoCodigo);
                TransicionIds tResultado = requerirTransicionCargaInicial(
                        conn, CODIGO_ACCION_REGISTRO_RESULTADO_ANALISIS, CODIGO_ETAPA_ANALISIS, CODIGO_ESTADO_RECIBIDO_POR_ABOGADO,
                        CODIGO_ETAPA_ANALISIS, estadoDestinoAnalisis);
                actualizarEtapaEstadoExpediente(conn, idExpediente, tResultado.idEtapaDestino(), tResultado.idEstadoDestino());
                insertarHistorialCargaInicial2(
                        conn, idExpediente, CODIGO_ACCION_REGISTRO_RESULTADO_ANALISIS, fechaBase,
                        tResultado.idEtapaDestino(), tResultado.idEstadoDestino(),
                        CODIGO_ETAPA_ANALISIS, CODIGO_ESTADO_RECIBIDO_POR_ABOGADO,
                        "Resultado de análisis por carga inicial: " + resultadoCodigo + ".", idUsuarioActual);

                if (!CODIGO_ESTADO_ATENDIDO.equals(estadoDestinoAnalisis)) {
                    conn.commit();
                    conn.setAutoCommit(previousAutoCommit);
                    return new ResultadoFila(idExpediente, CODIGO_ETAPA_ANALISIS, ESTADO_IMPORTADO,
                            "Resultado " + resultadoCodigo + ": expediente detenido en Análisis (no procede a Verificación/Ejecución).");
                }

                ResultadoFila resultadoFinal = avanzarVerificacionYEjecucion(conn, fila, idExpediente, idUsuarioActual, fechaBase);
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return resultadoFinal;
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                return new ResultadoFila(idExpediente, null, ESTADO_ERROR, ex.getMessage());
            }
        } catch (SQLException ex) {
            return new ResultadoFila(idExpediente, null, ESTADO_ERROR, ex.getMessage());
        }
    }

    /**
     * Paso técnico por Verificación (sin intervención real de un verificador) + Ejecución, extraído
     * como helper compartido (26/08/2026, ver v3/CLAUDE.md) por el camino normal (resultado ATENDIDO)
     * y el camino alterno "Resolución sin calificación" (evaluación forzada a ATENDIDO sin resultado
     * real). No maneja {@code commit}/{@code setAutoCommit} — eso queda a cargo del llamador, que ya
     * tiene la transacción abierta.
     */
    private ResultadoFila avanzarVerificacionYEjecucion(
            Connection conn, CargaInicialFilaDTO fila, Long idExpediente, Long idUsuarioActual, LocalDate fechaBase) throws SQLException {
        // Paso 3: paso técnico por Verificación (sin intervención real de un verificador).
        TransicionIds tEnvioVerif = requerirTransicionCargaInicial(
                conn, CODIGO_ACCION_ENVIO_VERIFICACION, CODIGO_ETAPA_ANALISIS, CODIGO_ESTADO_ATENDIDO,
                CODIGO_ETAPA_VERIFICACION, CODIGO_ESTADO_EN_VERIFICACION);
        actualizarEtapaEstadoExpediente(conn, idExpediente, tEnvioVerif.idEtapaDestino(), tEnvioVerif.idEstadoDestino());
        insertarHistorialCargaInicial2(
                conn, idExpediente, CODIGO_ACCION_ENVIO_VERIFICACION, fechaBase,
                tEnvioVerif.idEtapaDestino(), tEnvioVerif.idEstadoDestino(),
                CODIGO_ETAPA_ANALISIS, CODIGO_ESTADO_ATENDIDO,
                "Envío a Verificación (paso técnico) por carga inicial.", idUsuarioActual);

        TransicionIds tAprobVerif = requerirTransicionCargaInicial(
                conn, CODIGO_ACCION_APROBACION_VERIFICACION, CODIGO_ETAPA_VERIFICACION, CODIGO_ESTADO_EN_VERIFICACION,
                CODIGO_ETAPA_EJECUCION, CODIGO_ESTADO_EN_EJECUCION);
        actualizarEtapaEstadoExpediente(conn, idExpediente, tAprobVerif.idEtapaDestino(), tAprobVerif.idEstadoDestino());
        insertarHistorialCargaInicial2(
                conn, idExpediente, CODIGO_ACCION_APROBACION_VERIFICACION, fechaBase,
                tAprobVerif.idEtapaDestino(), tAprobVerif.idEstadoDestino(),
                CODIGO_ETAPA_VERIFICACION, CODIGO_ESTADO_EN_VERIFICACION,
                "Aprobación de Verificación (paso técnico) por carga inicial.", idUsuarioActual);

        // Paso 4: Ejecución.
        if (!hasText(fila.getEstadoEjecucionTexto())) {
            return new ResultadoFila(idExpediente, CODIGO_ETAPA_EJECUCION, ESTADO_INCOMPLETO,
                    "Sin dato de EJECUCIONES.ESTADO, expediente queda En Ejecución pendiente.");
        }
        String grupoEjecucion = clasificarGrupoEjecucion(fila.getEstadoEjecucionTexto());
        if (grupoEjecucion == null) {
            return new ResultadoFila(idExpediente, CODIGO_ETAPA_EJECUCION, ESTADO_INCOMPLETO,
                    "Estado de ejecución no reconocido, expediente queda En Ejecución pendiente.");
        }
        if ("PENDIENTE".equals(grupoEjecucion) || "DETENER".equals(grupoEjecucion)) {
            return new ResultadoFila(idExpediente, CODIGO_ETAPA_EJECUCION, ESTADO_INCOMPLETO,
                    "Estado de ejecución '" + grupoEjecucion + "', expediente queda En Ejecución (no se fuerza avance).");
        }

        LocalDate fechaEjecucion = fila.getFechaEjecucion() != null ? fila.getFechaEjecucion() : fechaBase;
        if ("EJECUTAR".equals(grupoEjecucion)) {
            TransicionIds tEjec = requerirTransicionCargaInicial(
                    conn, CODIGO_ACCION_INICIO_EJECUCION, CODIGO_ETAPA_EJECUCION, CODIGO_ESTADO_EN_EJECUCION,
                    CODIGO_ETAPA_EJECUCION, CODIGO_ESTADO_EJECUTADO);
            actualizarEtapaEstadoExpediente(conn, idExpediente, tEjec.idEtapaDestino(), tEjec.idEstadoDestino());
            insertarHistorialCargaInicial2(
                    conn, idExpediente, CODIGO_ACCION_INICIO_EJECUCION, fechaEjecucion,
                    tEjec.idEtapaDestino(), tEjec.idEstadoDestino(),
                    CODIGO_ETAPA_EJECUCION, CODIGO_ESTADO_EN_EJECUCION,
                    "Ejecución registrada por carga inicial.", idUsuarioActual);
            return new ResultadoFila(idExpediente, CODIGO_ETAPA_EJECUCION, ESTADO_IMPORTADO, "Expediente Ejecutado.");
        }

        // grupoEjecucion == "OBSERVAR"
        TransicionIds tObs = requerirTransicionCargaInicial(
                conn, CODIGO_ACCION_OBSERVACION_EJECUCION, CODIGO_ETAPA_EJECUCION, CODIGO_ESTADO_EN_EJECUCION,
                CODIGO_ETAPA_EJECUCION, CODIGO_ESTADO_REQUIERE_CORRECCION);
        insertarObservacionEjecucionCargaInicial(conn, idExpediente, "Resolución observada (carga inicial, EJECUCIONES.ESTADO).", idUsuarioActual);
        actualizarEtapaEstadoExpediente(conn, idExpediente, tObs.idEtapaDestino(), tObs.idEstadoDestino());
        insertarHistorialCargaInicial2(
                conn, idExpediente, CODIGO_ACCION_OBSERVACION_EJECUCION, fechaEjecucion,
                tObs.idEtapaDestino(), tObs.idEstadoDestino(),
                CODIGO_ETAPA_EJECUCION, CODIGO_ESTADO_EN_EJECUCION,
                "Observación de ejecución por carga inicial.", idUsuarioActual);
        return new ResultadoFila(idExpediente, CODIGO_ETAPA_EJECUCION, ESTADO_IMPORTADO, "Resolución observada (requiere corrección).");
    }

    /** Variante de {@link #insertarHistorial} que resuelve ids de etapa/estado/movimiento a partir de códigos (Incremento 2). */
    private void insertarHistorialCargaInicial2(
            Connection conn, Long idExpediente, String codigoAccion, LocalDate fechaMovimiento,
            Long idEtapaDestino, Long idEstadoDestino, String etapaOrigenCodigo, String estadoOrigenCodigo,
            String comentario, Long idUsuarioActual) throws SQLException {
        Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, codigoAccion), "movimiento " + codigoAccion);
        Long idEtapaOrigen = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, etapaOrigenCodigo), "etapa " + etapaOrigenCodigo);
        Long idEstadoOrigen = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, estadoOrigenCodigo), "estado " + estadoOrigenCodigo);
        insertarHistorial(conn, idExpediente, idMovimiento, fechaMovimiento, idEtapaDestino, idEstadoDestino, idEtapaOrigen, idEstadoOrigen, comentario, idUsuarioActual);
    }

    /**
     * Traduce "EJECUCIONES.PROCEDIMIENTO" al tipo de Resolución real del catálogo, decisión pedida
     * explícitamente por el usuario (26/08/2026) — verificado contra los valores reales del Excel
     * (RECTIFICACION 1589, RECONSTITUCIÓN 57, CANCELACIÓN/CANCELACIÓN DE OFICIO 51, resto 8 casos
     * sueltos ACTUALIZACION DE DATOS/APELACION/RECONSIDERACION sin tipo de resolución propio en el
     * catálogo, caen al código genérico {@code RESOLUCION}).
     */
    private String mapearTipoResolucion(String procedimientoBruto) {
        String t = normalizarJava(procedimientoBruto);
        if (t.contains("RECTIFICACION")) {
            return "ANALISIS_DOC_16_RESOLUCION_RECTIFICACION";
        }
        if (t.contains("RECONSTITUCION")) {
            return "ANALISIS_DOC_15_RESOLUCION_RECONSTITUCION";
        }
        if (t.contains("CANCELACION")) {
            return "ANALISIS_DOC_13_RESOLUCION_CANCELACION";
        }
        return "RESOLUCION";
    }

    /**
     * Traduce el {@code resultadoCodigo} ya mapeado (ver {@link #mapearResultadoAnalisis}) al tipo de
     * Carta de notificación real del catálogo — {@code ANALISIS_DOC_17/18/19} (Improcedente/Procedente/
     * Procedente en parte) ya existían; {@code ANALISIS_DOC_01_CARTA_ABANDONO} también ya existía;
     * {@code ANALISIS_DOC_22/23} (Desistimiento/Infundado) se agregaron en el script 113 (26/08/2026,
     * pedido explícito del usuario). Null para códigos sin carta de notificación real (NO_CORRESPONDE/
     * OBSERVACION_ADMINISTRATIVA — nunca aparecieron en los datos reales de EJECUCIONES, verificado).
     */
    private String mapearTipoCartaNotificacion(String resultadoCodigo) {
        return switch (resultadoCodigo) {
            case "PROCEDENTE" -> "ANALISIS_DOC_18_CARTA_PROCEDENTE";
            case "IMPROCEDENTE" -> "ANALISIS_DOC_17_CARTA_IMPROCEDENTE";
            case "PROCEDENTE_EN_PARTE" -> "ANALISIS_DOC_19_CARTA_PROCEDENTE_EN_PARTE";
            case "EN_ABANDONO" -> "ANALISIS_DOC_01_CARTA_ABANDONO";
            case "DESISTIMIENTO" -> "ANALISIS_DOC_22_CARTA_DESISTIMIENTO";
            case "INFUNDADO" -> "ANALISIS_DOC_23_CARTA_INFUNDADO";
            default -> null;
        };
    }

    private Long insertarDocumentoAnalizadoCargaInicial(
            Connection conn, Long idExpediente, String codigoTipoDocumento, String numeroDocumento,
            LocalDate fechaDocumento, Long idUsuarioActual) throws SQLException {
        Long idTipoDocumento = requerirId(catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, codigoTipoDocumento), "tipo de documento " + codigoTipoDocumento);
        Long idEstadoDocumento = requerirId(catalogoLookupDAO.obtenerEstadoDocumentoId(conn, CODIGO_ESTADO_DOCUMENTO_EMITIDO), "estado documento " + CODIGO_ESTADO_DOCUMENTO_EMITIDO);
        String sql = "INSERT INTO expediente_documento_analizado ("
                + "id_expediente, nivel, orden, id_tipo_documento_adjunto, id_estado_documento, "
                + "fecha_documento, numero_documento, requiere_respuesta, activo, creado_por, creado_en"
                + ") VALUES (?, 0, 0, ?, ?, ?, ?, 0, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_DOCUMENTO_ANALIZADO"})) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idTipoDocumento);
            ps.setLong(3, idEstadoDocumento);
            ps.setDate(4, fechaDocumento == null ? null : Date.valueOf(fechaDocumento));
            ps.setString(5, limitar(numeroDocumento, 120));
            setLongOrNull(ps, 6, idUsuarioActual);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "expediente_documento_analizado");
        }
    }

    /**
     * Idempotencia real (bug corregido 26/08/2026): un expediente detenido en {@code ANALISIS/
     * EN_ABANDONO} nunca cambia de etapa/estado en este método, así que reintentar el pase (ej. tras
     * corregir un bug que afectaba a OTRAS filas) volvía a sintetizar una 2da copia idéntica de
     * Resolución+Carta para esos expedientes — confirmado con 3 pares de documentos duplicados reales
     * tras el reintento del 26/08/2026 (limpiados manualmente, ver v3/CLAUDE.md). Antes de insertar,
     * reutiliza el documento activo ya existente de ese mismo tipo si lo hay, en vez de asumir que
     * cada invocación de este método sobre un expediente es la primera.
     */
    private Long obtenerOInsertarDocumentoAnalizadoCargaInicial(
            Connection conn, Long idExpediente, String codigoTipoDocumento, String numeroDocumento,
            LocalDate fechaDocumento, Long idUsuarioActual) throws SQLException {
        Long idExistente = obtenerIdDocumentoActivoDeTipo(conn, idExpediente, codigoTipoDocumento);
        if (idExistente != null) {
            return idExistente;
        }
        return insertarDocumentoAnalizadoCargaInicial(conn, idExpediente, codigoTipoDocumento, numeroDocumento, fechaDocumento, idUsuarioActual);
    }

    private Long obtenerIdDocumentoActivoDeTipo(Connection conn, Long idExpediente, String codigoTipoDocumento) throws SQLException {
        String sql = "SELECT eda.id_documento_analizado FROM expediente_documento_analizado eda "
                + "JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = eda.id_tipo_documento_adjunto "
                + "WHERE eda.id_expediente = ? AND eda.activo = 1 AND tda.codigo = ? AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, codigoTipoDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private Long obtenerIdEquipoPorCodigo(Connection conn, String codigo) throws SQLException {
        String sql = "SELECT id_equipo FROM equipo WHERE codigo = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    /**
     * Registra un intento de notificación ya resuelto (auto-contenido, no reutiliza
     * {@code DocumentoAnalisisDAO} — misma filosofía del resto de esta clase) — inserta
     * {@code EXPEDIENTE_NOTIFICACION} con el resultado ya conocido (histórico, no en vivo) y, si es
     * {@code EXITOSA}, además {@code EXPEDIENTE_CARGO_ACUSE} y marca
     * {@code expediente_documento_analizado.fecha_acuse}/{@code notificado}.
     */
    private void registrarIntentoNotificacionCargaInicial(
            Connection conn, Long idExpediente, Long idDocumentoAnalizado, int numeroIntento,
            String tipoNotificacionCodigo, String estadoNotificacionCodigo, String codigoNotificacion,
            LocalDate fechaEnvio, Long idUsuarioActual) throws SQLException {
        Long idTipoNotificacion = requerirId(catalogoLookupDAO.obtenerTipoNotificacionId(conn, tipoNotificacionCodigo), "tipo de notificación " + tipoNotificacionCodigo);
        Long idEstadoNotificacion = requerirId(catalogoLookupDAO.obtenerEstadoNotificacionId(conn, estadoNotificacionCodigo), "estado de notificación " + estadoNotificacionCodigo);
        String sql = "INSERT INTO expediente_notificacion ("
                + "id_expediente, id_documento_analizado, id_tipo_notificacion, id_estado_notificacion, "
                + "numero_intento, fecha_envio, resultado, requiere_publicacion, codigo_notificacion, "
                + "observacion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, ?, 1, ?, SYSTIMESTAMP)";
        Long idExpedienteNotificacion;
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_NOTIFICACION"})) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idDocumentoAnalizado);
            ps.setLong(3, idTipoNotificacion);
            ps.setLong(4, idEstadoNotificacion);
            ps.setInt(5, numeroIntento);
            ps.setTimestamp(6, fechaEnvio == null ? null : java.sql.Timestamp.valueOf(fechaEnvio.atStartOfDay()));
            ps.setString(7, estadoNotificacionCodigo);
            ps.setString(8, limitar(codigoNotificacion, 60));
            ps.setString(9, "Intento registrado por carga inicial (migración histórica).");
            setLongOrNull(ps, 10, idUsuarioActual);
            ps.executeUpdate();
            idExpedienteNotificacion = obtenerGeneratedKey(ps, "expediente_notificacion");
        }
        if (!CODIGO_ESTADO_NOTIF_EXITOSA.equals(estadoNotificacionCodigo)) {
            return;
        }
        Long idEstadoCargo = requerirId(catalogoLookupDAO.obtenerEstadoCargoAcuseId(conn, CODIGO_ESTADO_CARGO_RECIBIDO), "estado de cargo " + CODIGO_ESTADO_CARGO_RECIBIDO);
        LocalDate fechaAcuse = fechaEnvio != null ? fechaEnvio : LocalDate.now();
        String sqlCargo = "INSERT INTO expediente_cargo_acuse ("
                + "id_expediente, id_expediente_notificacion, id_estado_cargo_acuse, fecha_recepcion, "
                + "recibido_por, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sqlCargo)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idExpedienteNotificacion);
            ps.setLong(3, idEstadoCargo);
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(fechaAcuse.atStartOfDay()));
            ps.setString(5, limitar(codigoNotificacion, 250));
            setLongOrNull(ps, 6, idUsuarioActual);
            ps.executeUpdate();
        }
        String sqlAcuseDocumento = "UPDATE expediente_documento_analizado SET "
                + "fecha_acuse = ?, notificado = 1, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sqlAcuseDocumento)) {
            ps.setDate(1, Date.valueOf(fechaAcuse));
            setLongOrNull(ps, 2, idUsuarioActual);
            ps.setLong(3, idDocumentoAnalizado);
            ps.executeUpdate();
        }
    }

    /**
     * Incremento 3 (Notificación, ver v3/CLAUDE.md 26/08/2026): sobre un expediente YA AVANZADO por
     * {@link #avanzarAnalisisVerificacionEjecucion}, sintetiza los documentos reales (Resolución +
     * Carta de notificación, marcados EMITIDO) y, solo para el subconjunto que llegó a
     * EJECUCION/EJECUTADO (resultado PROCEDENTE/IMPROCEDENTE/PROCEDENTE_EN_PARTE), deriva a
     * Notificación y registra el/los intento(s) reales de notificación al ciudadano usando las
     * columnas {@code EJECUCIONES.MEDIO DE NOTIFICACION}/{@code FECHA EMISION 1°/2° CARTA}/
     * {@code 1°/2° NUMERO CARTA} ya presentes en el Excel.
     *
     * <p>Para expedientes detenidos en ANALISIS/EN_ABANDONO (resultado EN_ABANDONO/DESISTIMIENTO/
     * INFUNDADO, decisión confirmada explícitamente por el usuario): solo se sintetiza la carta
     * (marcada EMITIDO), sin ningún intento de notificación — no existe una transición real
     * ANALISIS/EN_ABANDONO → NOTIFICACION en el catálogo, así que esos expedientes NUNCA avanzan de
     * etapa en este paso, solo ganan su documento.</p>
     *
     * <p>Simplificación deliberada, documentada explícitamente (no un descuido): el intento de
     * notificación se marca directamente {@code EXITOSA} (ubicado) sin ceremonia de validación por
     * EQ_VALIDACION (el ciclo real completo de un documento FINAL en V2/V3 pasa primero por esa
     * bandeja) — el batch reconstruye el DESENLACE ya conocido (el Excel confirma que la carta se
     * emitió con éxito), no simula paso a paso el flujo operativo en vivo. Igual criterio para el
     * salto directo POR_ASIGNAR → EN_NOTIFICACION (UPDATE directo, sin flujo_transicion — mismo patrón
     * que usa el DAO real de producción {@code DocumentoAnalisisDAO#asignarNotificacionMultiple}, que
     * tampoco valida esa transición puntual contra el catálogo).</p>
     */
    public ResultadoFila avanzarNotificacion(CargaInicialFilaDTO fila, Long idExpediente, Long idUsuarioActual) {
        if (idExpediente == null) {
            return new ResultadoFila(null, null, ESTADO_INCOMPLETO, "Sin expediente creado en pasadas anteriores, se omite.");
        }
        String resultadoCodigo = mapearResultadoAnalisis(fila.getResultadoAnalisisTexto());
        if (resultadoCodigo == null) {
            return new ResultadoFila(idExpediente, null, ESTADO_INCOMPLETO, "Sin resultado de análisis reconocido, no se sintetizan documentos.");
        }
        String codigoCarta = mapearTipoCartaNotificacion(resultadoCodigo);
        StringBuilder incompletos = new StringBuilder();
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                EtapaEstado actual = bloquearYObtenerEtapaEstado(conn, idExpediente);
                LocalDate fechaResolucion = fila.getFechaEmisionAnalisis() != null ? fila.getFechaEmisionAnalisis() : fila.getFechaEjecucion();
                String numeroResolucion = hasText(fila.getResolucionLimpia()) ? fila.getResolucionLimpia() : fila.getNumeroResolucion();

                boolean detenidoEnAbandono = CODIGO_ETAPA_ANALISIS.equals(actual.etapaCodigo()) && CODIGO_ESTADO_EN_ABANDONO_ANALISIS.equals(actual.estadoCodigo());
                boolean listoParaNotificar = CODIGO_ETAPA_EJECUCION.equals(actual.etapaCodigo()) && CODIGO_ESTADO_EJECUTADO.equals(actual.estadoCodigo());

                if (!detenidoEnAbandono && !listoParaNotificar) {
                    conn.rollback();
                    conn.setAutoCommit(previousAutoCommit);
                    return new ResultadoFila(idExpediente, actual.etapaCodigo(), ESTADO_INCOMPLETO,
                            "Expediente en " + actual.etapaCodigo() + "/" + actual.estadoCodigo() + ", no aplica síntesis de Notificación en este pase.");
                }

                if (hasText(numeroResolucion)) {
                    String codigoResolucion = mapearTipoResolucion(fila.getProcedimientoEjecuciones());
                    obtenerOInsertarDocumentoAnalizadoCargaInicial(conn, idExpediente, codigoResolucion, numeroResolucion, fechaResolucion, idUsuarioActual);
                } else {
                    incompletos.append("Sin N° de resolución en el Excel. ");
                }

                Long idDocumentoCarta = null;
                if (codigoCarta != null) {
                    String numeroCarta = hasText(fila.getNumero1Carta()) ? fila.getNumero1Carta() : numeroResolucion;
                    LocalDate fechaCarta = fila.getFechaEmision1Carta() != null ? fila.getFechaEmision1Carta() : fechaResolucion;
                    idDocumentoCarta = obtenerOInsertarDocumentoAnalizadoCargaInicial(conn, idExpediente, codigoCarta, numeroCarta, fechaCarta, idUsuarioActual);
                } else {
                    incompletos.append("Resultado " + resultadoCodigo + " sin carta de notificación mapeada. ");
                }

                if (detenidoEnAbandono) {
                    conn.commit();
                    conn.setAutoCommit(previousAutoCommit);
                    String estado = incompletos.length() > 0 ? ESTADO_INCOMPLETO : ESTADO_IMPORTADO;
                    return new ResultadoFila(idExpediente, CODIGO_ETAPA_ANALISIS, estado,
                            ("Documento(s) sintetizado(s) en Análisis (expediente detenido, sin Notificación). " + incompletos).trim());
                }

                // listoParaNotificar: deriva a Notificación y registra el/los intento(s) reales.
                // EXPEDIENTE_HISTORIAL.FECHA_MOVIMIENTO es NOT NULL — fila.getFechaEjecucion() puede
                // venir vacío pese a que "EJECUCIONES.ESTADO" sí tenía dato (96 casos reales
                // confirmados, ORA-01400, 26/08/2026): se cae a fechaResolucion (ya con su propio
                // fallback) y, en último caso, a la fecha del proceso.
                LocalDate fechaDerivacion = fila.getFechaEjecucion() != null
                        ? fila.getFechaEjecucion()
                        : (fechaResolucion != null ? fechaResolucion : LocalDate.now());
                TransicionIds tDerivacion = requerirTransicionCargaInicial(
                        conn, CODIGO_ACCION_DERIVACION_NOTIFICACION, CODIGO_ETAPA_EJECUCION, CODIGO_ESTADO_EJECUTADO,
                        CODIGO_ETAPA_NOTIFICACION, CODIGO_ESTADO_POR_ASIGNAR);
                actualizarEtapaEstadoExpediente(conn, idExpediente, tDerivacion.idEtapaDestino(), tDerivacion.idEstadoDestino());
                insertarHistorialCargaInicial2(
                        conn, idExpediente, CODIGO_ACCION_DERIVACION_NOTIFICACION, fechaDerivacion,
                        tDerivacion.idEtapaDestino(), tDerivacion.idEstadoDestino(),
                        CODIGO_ETAPA_EJECUCION, CODIGO_ESTADO_EJECUTADO,
                        "Derivación a Notificación por carga inicial.", idUsuarioActual);

                // Salto directo POR_ASIGNAR -> EN_NOTIFICACION (mismo patrón que el DAO real de
                // asignación de notificación: UPDATE directo, sin flujo_transicion para este sub-paso).
                Long idEstadoEnNotificacion = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, CODIGO_ESTADO_EN_NOTIFICACION), "estado " + CODIGO_ESTADO_EN_NOTIFICACION);
                Long idEtapaNotificacion = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, CODIGO_ETAPA_NOTIFICACION), "etapa " + CODIGO_ETAPA_NOTIFICACION);
                Long idEquipoNotificacion = obtenerIdEquipoPorCodigo(conn, CODIGO_EQUIPO_NOTIFICACION);
                Long idNotificador = hasText(fila.getAsignadoNotificacion()) ? resolverAbogadoPorNombre(conn, fila.getAsignadoNotificacion()) : null;
                if (hasText(fila.getAsignadoNotificacion()) && idNotificador == null) {
                    incompletos.append("Notificador no resuelto por nombre. ");
                }
                actualizarEtapaEstadoExpediente(conn, idExpediente, idEtapaNotificacion, idEstadoEnNotificacion);
                if (idNotificador != null || idEquipoNotificacion != null) {
                    String sqlResponsable = "UPDATE expediente SET id_usuario_responsable_actual = ?, id_equipo_responsable_actual = ? WHERE id_expediente = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlResponsable)) {
                        setLongOrNull(ps, 1, idNotificador);
                        setLongOrNull(ps, 2, idEquipoNotificacion);
                        ps.setLong(3, idExpediente);
                        ps.executeUpdate();
                    }
                }

                // Modalidad: "SITD" (~99.7% de los casos reales) -> VIRTUAL; cualquier otro valor no
                // vacío -> igual VIRTUAL para el 1er intento (sigue siendo un medio del sistema, no
                // presencial) — solo el 2do intento (cuando existe) se marca PRESENCIAL_2, replicando
                // el criterio real de V2 (intento 1 virtual, intento 2 presencial).
                boolean tieneSegundoIntento = fila.getFechaElaboracion2Carta() != null || hasText(fila.getNumero2Carta());
                String codigoNotif1 = hasText(fila.getNumero1Carta()) ? fila.getNumero1Carta() : numeroResolucion;
                LocalDate fechaEnvio1 = fila.getFechaEmision1Carta() != null ? fila.getFechaEmision1Carta() : fechaResolucion;
                String estadoIntento1 = tieneSegundoIntento ? CODIGO_ESTADO_NOTIF_FALLIDA : CODIGO_ESTADO_NOTIF_EXITOSA;
                registrarIntentoNotificacionCargaInicial(
                        conn, idExpediente, idDocumentoCarta, 1, CODIGO_TIPO_NOTIF_VIRTUAL, estadoIntento1, codigoNotif1, fechaEnvio1, idUsuarioActual);

                if (tieneSegundoIntento) {
                    String codigoNotif2 = hasText(fila.getNumero2Carta()) ? fila.getNumero2Carta() : codigoNotif1;
                    LocalDate fechaEnvio2 = fila.getFechaElaboracion2Carta() != null ? fila.getFechaElaboracion2Carta() : fechaEnvio1;
                    registrarIntentoNotificacionCargaInicial(
                            conn, idExpediente, idDocumentoCarta, 2, CODIGO_TIPO_NOTIF_PRESENCIAL_2, CODIGO_ESTADO_NOTIF_EXITOSA, codigoNotif2, fechaEnvio2, idUsuarioActual);
                }

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                String estado = incompletos.length() > 0 ? ESTADO_INCOMPLETO : ESTADO_IMPORTADO;
                return new ResultadoFila(idExpediente, CODIGO_ETAPA_NOTIFICACION, estado,
                        (incompletos.length() > 0 ? incompletos.toString().trim() : "Notificación registrada correctamente."));
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                return new ResultadoFila(idExpediente, null, ESTADO_ERROR, ex.getMessage());
            }
        } catch (SQLException ex) {
            return new ResultadoFila(idExpediente, null, ESTADO_ERROR, ex.getMessage());
        }
    }

    public ResultadoFila procesarFila(CargaInicialFilaDTO fila, Long idUsuarioActual) {
        if (!hasText(fila.getTitular())) {
            return new ResultadoFila(null, null, ESTADO_ERROR, "Fila sin titular.");
        }

        // "Sin número de acta" YA NO bloquea la creación del expediente (pedido explícito del
        // usuario, 26/08/2026) — EXPEDIENTE_ACTA.numero_acta es nullable, así que se registra igual
        // (sin ese dato) y queda marcado INCOMPLETO en vez de ERROR.
        StringBuilder incompletos = new StringBuilder();
        if (!hasText(fila.getNumeroActa())) {
            incompletos.append("Sin número de acta. ");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idEtapaRegistro = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, CODIGO_ETAPA_REGISTRO), "etapa REGISTRO");
                Long idEstadoRegistrado = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, CODIGO_ESTADO_REGISTRADO), "estado REGISTRADO");
                Long idMovimientoRegistro = requerirId(
                        catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_REGISTRO), "movimiento IMPORTACION_CARGA_INICIAL");

                // Persona única (titular = solicitante): el Excel no trae un nombre de solicitante
                // distinto del titular en ninguna de las 2 hojas (ver v3/CLAUDE.md) — el único dato de
                // identidad disponible es "DNI SOLICITANTE" (solo DATA DIARIA UNIFICADA), que se asocia
                // a esta misma persona en vez de inventar un segundo registro sin nombre.
                Long idPersona = resolverOInsertarPersona(conn, fila.getTitular(), fila.getDniSolicitante());

                Long idCanal = null;
                if (fila.esPasivoOr()) {
                    idCanal = catalogoLookupDAO.obtenerCanalRecepcionId(conn, CODIGO_CANAL_OR_PASIVO);
                } else if (hasText(fila.getCanalRecepcion())) {
                    String codigoCanal = resolverCodigoCanalRecepcion(fila.getCanalRecepcion());
                    idCanal = catalogoLookupDAO.obtenerCanalRecepcionId(conn, codigoCanal);
                    if (idCanal == null) {
                        incompletos.append("Canal no reconocido. ");
                    }
                }

                String numeroExpediente;
                if (fila.esPasivoOr() && hasText(fila.getNumeroExpedienteLegado())) {
                    numeroExpediente = fila.getNumeroExpedienteLegado().trim();
                    if (existeNumeroExpediente(conn, numeroExpediente)) {
                        conn.rollback();
                        conn.setAutoCommit(previousAutoCommit);
                        return new ResultadoFila(null, null, ESTADO_ERROR, "N° de expediente legado ya registrado.");
                    }
                } else {
                    int anio = fila.getFechaSolicitud() != null ? fila.getFechaSolicitud().getYear() : correlativoExpedienteService.anioActual();
                    int siguienteCorrelativo = obtenerSiguienteCorrelativo(conn, anio) + 1;
                    numeroExpediente = correlativoExpedienteService.generar(anio, siguienteCorrelativo);
                    if (fila.esPasivoOr()) {
                        incompletos.append("Sin N° de expediente legado, se generó uno nuevo. ");
                    }
                }

                Date fechaVencimiento = fila.getFechaSolicitud() == null
                        ? null
                        : Date.valueOf(calendarioLaboralService.calcularFechaVencimientoSolicitud(
                                conn, fila.getFechaSolicitud(), fila.getTipoSolicitud()));

                Long idExpediente = insertarExpediente(conn, numeroExpediente, fila.getFechaSolicitud(), fechaVencimiento);
                insertarSolicitud(conn, idExpediente, idCanal, idPersona, fila);
                insertarActa(conn, idExpediente, fila);
                insertarDocumento(conn, idExpediente, fila);
                insertarExpedientePersona(conn, idExpediente, idPersona, "TITULAR");
                insertarExpedientePersona(conn, idExpediente, idPersona, "REMITENTE");
                insertarHistorial(
                        conn, idExpediente, idMovimientoRegistro, fila.getFechaSolicitud(),
                        idEtapaRegistro, idEstadoRegistrado, null, null,
                        notaRegistro(fila), idUsuarioActual);

                String etapaAlcanzada = CODIGO_ETAPA_REGISTRO;
                if (hasText(fila.getAsignadoAAbogado())) {
                    Long idAbogado = resolverAbogadoPorNombre(conn, fila.getAsignadoAAbogado());
                    Long idEquipo = idAbogado == null ? null : resolverEquipoUnicoDelAbogado(conn, idAbogado);
                    if (idAbogado == null) {
                        incompletos.append("Abogado no resuelto por nombre exacto contra USUARIO. ");
                    } else if (idEquipo == null) {
                        incompletos.append("Abogado sin equipo activo único. ");
                    } else {
                        Long idEtapaAsignacion = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, CODIGO_ETAPA_ASIGNACION), "etapa ASIGNACION");
                        Long idEstadoAsignado = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, CODIGO_ESTADO_ASIGNADO), "estado ASIGNADO");
                        Long idMovimientoAsignacion = requerirId(
                                catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_ASIGNACION), "movimiento ASIGNACION_CARGA_INICIAL");
                        LocalDate fechaAsignacion = fila.getFechaAsignado() != null ? fila.getFechaAsignado() : fila.getFechaSolicitud();

                        String numeroHojaEnvio = fila.getNumeroHojaEnvioAsignacion();
                        if (existeHojaEnvioActiva(conn, numeroHojaEnvio)) {
                            incompletos.append("N° de hoja de envío ya registrado en otro expediente (duplicado histórico), se omitió. ");
                            numeroHojaEnvio = null;
                        }

                        insertarAsignacion(conn, idExpediente, idAbogado, idEquipo, idEtapaAsignacion, fechaAsignacion, numeroHojaEnvio);
                        actualizarExpedienteResponsable(conn, idExpediente, idEtapaAsignacion, idEstadoAsignado, idAbogado, idEquipo);
                        insertarHistorial(
                                conn, idExpediente, idMovimientoAsignacion, fechaAsignacion,
                                idEtapaAsignacion, idEstadoAsignado, idEtapaRegistro, idEstadoRegistrado,
                                "Asignación por carga inicial.", idUsuarioActual);
                        etapaAlcanzada = CODIGO_ETAPA_ASIGNACION;
                    }
                }

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);

                String estado = incompletos.length() > 0 ? ESTADO_INCOMPLETO : ESTADO_IMPORTADO;
                String mensaje = incompletos.length() > 0 ? incompletos.toString().trim() : null;
                return new ResultadoFila(idExpediente, etapaAlcanzada, estado, mensaje);
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                return new ResultadoFila(null, null, ESTADO_ERROR, ex.getMessage());
            }
        } catch (SQLException ex) {
            return new ResultadoFila(null, null, ESTADO_ERROR, ex.getMessage());
        }
    }

    private Long resolverOInsertarPersona(Connection conn, String nombre, String numeroDocumentoBruto) throws SQLException {
        String numeroDocumento = normalizarDocumento(numeroDocumentoBruto);
        String tipoDocumento = numeroDocumento == null ? null : "DNI";
        if (numeroDocumento != null) {
            String sqlBuscar = "SELECT id_persona FROM persona "
                    + "WHERE activo = 1 AND tipo_documento = ? AND numero_documento = ? AND ROWNUM = 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlBuscar)) {
                ps.setString(1, tipoDocumento);
                ps.setString(2, numeroDocumento);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        if (!rs.wasNull()) {
                            return id;
                        }
                    }
                }
            }
        }
        String sqlInsertar = "INSERT INTO persona (tipo_documento, numero_documento, razon_social, activo) VALUES (?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsertar, new String[]{"ID_PERSONA"})) {
            ps.setString(1, tipoDocumento);
            ps.setString(2, numeroDocumento);
            ps.setString(3, limitar(nombre, 250));
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "persona");
        }
    }

    private String normalizarDocumento(String value) {
        if (!hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if ("SIN DNI".equalsIgnoreCase(trimmed) || "S/N".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }

    private boolean existeNumeroExpediente(Connection conn, String numeroExpediente) throws SQLException {
        String sql = "SELECT COUNT(1) FROM expediente WHERE numero_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private int obtenerSiguienteCorrelativo(Connection conn, int anio) throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(numero_expediente, -6))), 0) AS correlativo "
                + "FROM expediente "
                + "WHERE numero_expediente IS NOT NULL AND activo = 1 "
                + "AND numero_expediente LIKE ? "
                + "AND REGEXP_LIKE(numero_expediente, '^SDRERC[-_]EXP[-_][0-9]{4}[-_][0-9]{6}$')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "SDRERC-EXP-" + anio + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("correlativo") : 0;
            }
        }
    }

    private Long insertarExpediente(Connection conn, String numeroExpediente, LocalDate fechaRegistro, Date fechaVencimiento) throws SQLException {
        String sql = "INSERT INTO expediente ("
                + "numero_expediente, id_etapa_actual, id_estado_actual, "
                + "fecha_registro, fecha_ultimo_movimiento, fecha_vencimiento, prioridad, requiere_publicacion, "
                + "expediente_digital_completo, archivado, cerrado, activo"
                + ") VALUES (?, (SELECT id_etapa FROM etapa_expediente WHERE codigo = '" + CODIGO_ETAPA_REGISTRO + "'), "
                + "(SELECT id_estado FROM estado_expediente se JOIN etapa_expediente ee ON ee.id_etapa = se.id_etapa "
                + "WHERE ee.codigo = '" + CODIGO_ETAPA_REGISTRO + "' AND se.codigo = '" + CODIGO_ESTADO_REGISTRADO + "'), "
                + "?, ?, ?, 'NORMAL', 0, 0, 0, 0, 1)";
        Date fechaRegistroSql = fechaRegistro == null ? null : Date.valueOf(fechaRegistro);
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE"})) {
            ps.setString(1, numeroExpediente);
            ps.setDate(2, fechaRegistroSql);
            ps.setDate(3, fechaRegistroSql);
            ps.setDate(4, fechaVencimiento);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "expediente");
        }
    }

    private void insertarSolicitud(Connection conn, Long idExpediente, Long idCanal, Long idPersonaSolicitante, CargaInicialFilaDTO fila) throws SQLException {
        String sql = "INSERT INTO expediente_solicitud ("
                + "id_expediente, id_canal_recepcion, id_persona_solicitante, numero_tramite_documentario, "
                + "fecha_recepcion, asunto, observacion, es_tramite_virtual, potencial_duplicado, activo"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setLong(idx++, idExpediente);
            setLongOrNull(ps, idx++, idCanal);
            setLongOrNull(ps, idx++, idPersonaSolicitante);
            ps.setString(idx++, limitar(fila.getNumeroTramiteWeb(), 60));
            ps.setDate(idx++, fila.getFechaSolicitud() == null ? null : Date.valueOf(fila.getFechaSolicitud()));
            ps.setString(idx++, limitar(fila.getTipoSolicitud(), 200));
            ps.setString(idx++, limitar(notaObservacion(fila), 1000));
            ps.executeUpdate();
        }
    }

    private void insertarActa(Connection conn, Long idExpediente, CargaInicialFilaDTO fila) throws SQLException {
        Long idTipoActa = hasText(fila.getTipoActa())
                ? catalogoLookupDAO.obtenerTipoActaIdPorCodigoONombre(conn, fila.getTipoActa())
                : null;
        String sql = "INSERT INTO expediente_acta (id_expediente, id_tipo_acta, numero_acta, anio_acta, activo) VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            setLongOrNull(ps, 2, idTipoActa);
            ps.setString(3, limitar(fila.getNumeroActa(), 60));
            if (fila.getFechaSolicitud() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, fila.getFechaSolicitud().getYear());
            }
            ps.executeUpdate();
        }
    }

    private void insertarDocumento(Connection conn, Long idExpediente, CargaInicialFilaDTO fila) throws SQLException {
        if (!hasText(fila.getTipoDocumento())) {
            return;
        }
        String sql = "INSERT INTO expediente_documento (id_expediente, nombre_documento, numero_documento, fecha_documento, activo) "
                + "VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, limitar(fila.getTipoDocumento(), 200));
            ps.setString(3, limitar(fila.getNumeroDocumento(), 100));
            ps.setDate(4, fila.getFechaSolicitud() == null ? null : Date.valueOf(fila.getFechaSolicitud()));
            ps.executeUpdate();
        }
    }

    private void insertarExpedientePersona(Connection conn, Long idExpediente, Long idPersona, String tipoRelacion) throws SQLException {
        String sql = "INSERT INTO expediente_persona (id_expediente, id_persona, tipo_relacion_persona, activo) VALUES (?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idPersona);
            ps.setString(3, tipoRelacion);
            ps.executeUpdate();
        }
    }

    private void insertarHistorial(
            Connection conn,
            Long idExpediente,
            Long idTipoMovimiento,
            LocalDate fechaMovimiento,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idEtapaOrigen,
            Long idEstadoOrigen,
            String comentario,
            Long idUsuarioActual) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_etapa_origen, id_estado_origen, "
                + "id_etapa_destino, id_estado_destino, id_usuario_origen, tabla_relacionada, id_registro_relacionado, "
                + "comentario, motivo, activo"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'EXPEDIENTE', ?, ?, 'CARGA_INICIAL', 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idTipoMovimiento);
            ps.setTimestamp(3, fechaMovimiento == null ? null : java.sql.Timestamp.valueOf(fechaMovimiento.atStartOfDay()));
            setLongOrNull(ps, 4, idEtapaOrigen);
            setLongOrNull(ps, 5, idEstadoOrigen);
            ps.setLong(6, idEtapaDestino);
            ps.setLong(7, idEstadoDestino);
            setLongOrNull(ps, 8, idUsuarioActual);
            ps.setLong(9, idExpediente);
            ps.setString(10, limitar(comentario, 500));
            ps.executeUpdate();
        }
    }

    private Long insertarAsignacion(
            Connection conn, Long idExpediente, Long idAbogado, Long idEquipo, Long idEtapa,
            LocalDate fechaAsignacion, String numeroHojaEnvio) throws SQLException {
        String sql = "INSERT INTO expediente_asignacion ("
                + "id_expediente, id_usuario_asignado, id_equipo_asignado, id_etapa, fecha_asignacion, "
                + "activa, es_abogado_principal, es_reasignacion_excepcional, numero_hoja_envio, motivo, activo"
                + ") VALUES (?, ?, ?, ?, ?, 1, 1, 0, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_ASIGNACION"})) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idAbogado);
            ps.setLong(3, idEquipo);
            ps.setLong(4, idEtapa);
            ps.setTimestamp(5, fechaAsignacion == null ? null : java.sql.Timestamp.valueOf(fechaAsignacion.atStartOfDay()));
            ps.setString(6, limitar(numeroHojaEnvio, 120));
            ps.setString(7, "Carga inicial (migración histórica).");
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "expediente_asignacion");
        }
    }

    private void actualizarExpedienteResponsable(
            Connection conn, Long idExpediente, Long idEtapaDestino, Long idEstadoDestino, Long idAbogado, Long idEquipo) throws SQLException {
        String sql = "UPDATE expediente SET "
                + "id_etapa_actual = ?, id_estado_actual = ?, id_usuario_responsable_actual = ?, "
                + "id_usuario_abogado_inicial = NVL(id_usuario_abogado_inicial, ?), id_equipo_responsable_actual = ? "
                + "WHERE id_expediente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEtapaDestino);
            ps.setLong(2, idEstadoDestino);
            ps.setLong(3, idAbogado);
            ps.setLong(4, idAbogado);
            ps.setLong(5, idEquipo);
            ps.setLong(6, idExpediente);
            ps.executeUpdate();
        }
    }

    /**
     * Alias explícitos de solo-comparación (nunca tocan el dato maestro de USUARIO), verificados
     * contra los ~135 casos reales del Excel de carga inicial que ni siquiera normalizados (tildes/
     * coma/espacios) calzaban contra ningún USUARIO — mismo patrón/criterio ya usado para Centro de
     * Atención/Municipalidad en Carga diaria ("ALIASES_TIPO_SOLICITANTE"): typos reales y consistentes
     * en el Excel (STIVEN→STEVEN, AXCEL→AXEL, CYNTHIA→CINTHIA, MORVELI→MORLEVI), un nombre de casada
     * distinto ("DE YEPEZ"), 2 nombres incompletos (falta un segundo nombre) y 1 caso de espacio
     * faltante que la normalización (que solo colapsa espacios múltiples, nunca los elimina todos)
     * no cubre ("TUPAC YUPANQUI" vs "TUPACYUPANQUI"). Claves en el mismo espacio normalizado que
     * {@link #sqlNormalizarNombre}, para no depender de mayúsculas/tildes al comparar.
     */
    private static final Map<String, String> ALIASES_ABOGADO = Map.ofEntries(
            Map.entry("CABANA AGUILAR PAUL STEVEN", "CABANA AGUILAR PAUL STIVEN"),
            Map.entry("CORTEZ PITA ALEXANDRA", "CORTEZ PITA ALEXANDRA KAREN"),
            Map.entry("FERNANDEZ OBANDO GINO AXEL", "FERNANDEZ OBANDO GINO AXCEL"),
            Map.entry("ILAVE VELASCO DE YEPEZ CAROLINE BERTA", "ILAVE VELASCO CAROLINE BERTA"),
            Map.entry("SALAZAR QUINONES TERESA DILAINES", "SALAZAR QUINONEZ TERESA DILAINES"),
            Map.entry("CURI FIGUEREDO JOYCE CINTHIA", "CURI FIGUEREDO JOYCE CYNTHIA"),
            Map.entry("JIMENEZ MORLEVI YENI", "JIMENEZ MORVELI YENI"),
            Map.entry("MOROCHO MARCHAN SILVIA", "MOROCHO MARCHAN SILVIA JANET"),
            Map.entry("TUPAC YUPANQUI GARAGATTI NATALI INES ALEJANDRA", "TUPACYUPANQUI GARAGATTI NATALI INES ALEJANDRA"));

    /**
     * Resuelve por nombre tolerando tildes/diéresis, comas y espacios múltiples (verificado contra
     * casos reales del Excel de carga inicial: "FERNANDEZ FERNANDEZ SERGIO ORLANDO" vs "FERNANDEZ
     * FERNANDEZ, SERGIO ORLANDO" en USUARIO, "ARESTEGUI..." vs "ARÉSTEGUI..." — mismo patrón de
     * normalización SQL ya usado en {@code ExpedienteRelacionadoDAO#normalizarPersona}), más
     * {@link #ALIASES_ABOGADO} para typos puntuales ya verificados. Sigue exigiendo coincidencia
     * única — nunca fuzzy/aproximado por similitud.
     *
     * <p><b>Incluye USUARIO inactivos a propósito</b> (decisión explícita del usuario, 26/08/2026):
     * gran parte del personal que aparece en el Excel histórico ya no está activo hoy — el caso
     * igual se asigna a esa persona (queda en su cola aunque no pueda iniciar sesión, hasta que
     * alguien lo reasigne manualmente desde la propia bandeja de Asignación). Antes de esta decisión
     * el filtro exigía {@code activo = 1}, lo que dejaba sin resolver ~56% de las filas con abogado
     * informado.</p>
     */
    private Long resolverAbogadoPorNombre(Connection conn, String nombreCompleto) throws SQLException {
        String buscado = ALIASES_ABOGADO.getOrDefault(normalizarJava(nombreCompleto), nombreCompleto);
        String sql = "SELECT id_usuario FROM usuario WHERE "
                + sqlNormalizarNombre("nombre_completo") + " = " + sqlNormalizarNombre("?");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, buscado);
            try (ResultSet rs = ps.executeQuery()) {
                Long id = null;
                int matches = 0;
                while (rs.next()) {
                    matches++;
                    id = rs.getLong(1);
                }
                return matches == 1 ? id : null;
            }
        }
    }

    private static String sqlNormalizarNombre(String columnaOParametro) {
        return "TRANSLATE(UPPER(TRIM(REGEXP_REPLACE(REPLACE(" + columnaOParametro + ", ',', ' '), '[[:space:]]+', ' '))), "
                + "'ÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÛÑÇ', 'AAAAAEEEEIIIIOOOOOUUUUNC')";
    }

    /** Réplica en Java de {@link #sqlNormalizarNombre}, usada solo para la clave de {@link #ALIASES_ABOGADO}. */
    private static String normalizarJava(String value) {
        if (value == null) {
            return "";
        }
        String sinComas = value.replace(",", " ");
        String descompuesto = java.text.Normalizer.normalize(sinComas, java.text.Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder();
        boolean prevEspacio = false;
        for (char c : descompuesto.toCharArray()) {
            if (Character.getType(c) == Character.NON_SPACING_MARK) {
                continue;
            }
            char up = Character.toUpperCase(c);
            if (Character.isWhitespace(up)) {
                if (!prevEspacio) {
                    sb.append(' ');
                }
                prevEspacio = true;
            } else {
                sb.append(up);
                prevEspacio = false;
            }
        }
        return sb.toString().trim();
    }

    /**
     * Devuelve el equipo activo único del abogado — salvo el caso {@code EQ_ANALISIS}+{@code
     * EQ_EJECUCION} (los únicos 2 equipos posibles para un abogado con exactamente esa combinación,
     * por el espejado documentado en {@code 68_espejar_equipo_ejecucion_analisis.sql}), donde se
     * prioriza {@code EQ_ANALISIS} — el Incremento 1 de carga inicial solo llega hasta la etapa
     * ASIGNACION, nunca hasta Ejecución, así que ese es el equipo real al que correspondería el
     * expediente en este punto del flujo. Cualquier otra combinación de 2+ equipos sigue sin
     * resolverse (nunca se adivina).
     */
    private Long resolverEquipoUnicoDelAbogado(Connection conn, Long idAbogado) throws SQLException {
        Map<String, Long> equiposPorCodigo = listarEquiposActivosPorCodigo(conn, idAbogado);
        if (equiposPorCodigo.size() == 1) {
            return equiposPorCodigo.values().iterator().next();
        }
        if (equiposPorCodigo.size() == 2
                && equiposPorCodigo.containsKey("EQ_ANALISIS")
                && equiposPorCodigo.containsKey("EQ_EJECUCION")) {
            return equiposPorCodigo.get("EQ_ANALISIS");
        }
        return null;
    }

    private Map<String, Long> listarEquiposActivosPorCodigo(Connection conn, Long idAbogado) throws SQLException {
        String sql = "SELECT eq.codigo, eq.id_equipo FROM equipo_usuario eu "
                + "JOIN equipo eq ON eq.id_equipo = eu.id_equipo "
                + "WHERE eu.id_usuario = ? AND eu.activo = 1";
        Map<String, Long> resultado = new java.util.LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idAbogado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.put(rs.getString(1), rs.getLong(2));
                }
            }
        }
        return resultado;
    }

    private String notaRegistro(CargaInicialFilaDTO fila) {
        StringBuilder sb = new StringBuilder("Registro por carga inicial.");
        if (hasText(fila.getResponsablePreevaluacion())) {
            sb.append(" Responsable preevaluación (Excel): ").append(fila.getResponsablePreevaluacion()).append('.');
        }
        return sb.toString();
    }

    private String notaObservacion(CargaInicialFilaDTO fila) {
        StringBuilder sb = new StringBuilder();
        if (hasText(fila.getTipoTramite())) {
            sb.append("Tipo trámite (Excel): ").append(fila.getTipoTramite()).append(". ");
        }
        if (hasText(fila.getDerivadoPor())) {
            sb.append("Derivado por: ").append(fila.getDerivadoPor()).append('.');
        }
        return sb.toString();
    }

    private String resolverCodigoCanalRecepcion(String value) {
        if (!hasText(value)) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT)
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
                .replaceAll("\\s+", " ");
        if ("MPV".equals(normalized) || "MESA PARTES VIRTUAL".equals(normalized) || "MESA DE PARTES VIRTUAL".equals(normalized)) {
            return "MESA_PARTES_VIRTUAL";
        }
        if ("MP PRESENCIAL".equals(normalized) || "MESA PARTES PRESENCIAL".equals(normalized) || "MESA DE PARTES PRESENCIAL".equals(normalized)) {
            return "MESA_PARTES_PRESENCIAL";
        }
        if ("OR".equals(normalized) || "OR PRESENCIAL".equals(normalized)) {
            return "OR_PRESENCIAL";
        }
        if ("OR PASIVO".equals(normalized) || "OFICINA REGISTRAL PASIVO".equals(normalized) || "PASIVO OR".equals(normalized)) {
            return CODIGO_CANAL_OR_PASIVO;
        }
        return normalized.replace(' ', '_');
    }

    private void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // El error original ya se reporta en el ResultadoFila; el rollback fallido no debe ocultarlo.
        }
    }

    private Long obtenerGeneratedKey(PreparedStatement ps, String entidad) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
        throw new SQLException("No se pudo obtener el identificador generado de " + entidad + ".");
    }

    private Long requerirId(Long value, String descripcion) throws SQLException {
        if (value == null) {
            throw new SQLException("No se encontró el catálogo requerido: " + descripcion + ".");
        }
        return value;
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

    private static String limitar(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
