package com.sdrerc.v3.infrastructure.dao;

import com.sdrerc.v3.application.CalendarioLaboralService;
import com.sdrerc.v3.application.CorrelativoExpedienteService;
import com.sdrerc.v3.domain.AsignacionExpedienteDTO;
import com.sdrerc.v3.domain.AsignacionHistorialDTO;
import com.sdrerc.v3.domain.AsignacionResultadoDTO;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

/**
 * Port de com.sdrerc.infrastructure.sdrercapp.dao.AsignacionExpedienteDAO (V2), acotado a los
 * primeros dos incrementos de la Fase 3: listado de la Bandeja Asignación
 * ({@link #buscarExpedientes}), la asignación inicial a equipo/abogado
 * ({@link #asignarExpedientes}) y la reasignación de un expediente ya asignado
 * ({@link #reasignarExpediente}, detrás de "Habilitar reasignación" en el frontend). También
 * incluye {@link #listarHistorialAsignaciones} y, portado después junto con la revisión del panel
 * "Asociar" (bloque "Decisión de número", 12/08/2026), {@link #generarNumeroExpediente}. Quedan
 * fuera de este port: reasignarDesdeCartaRespuesta, actualizarProcedimientoRegistral y
 * obtenerIdAbogadoActivoPorEquipo.
 *
 * <p>Diferencias deliberadas respecto al DAO de V2, no bugs:</p>
 * <ul>
 *   <li>Sin los chequeos {@code soportaNumeroHojaEnvio/soportaGrupoFamiliar/soportaUbigeoPersona}
 *   de V2 (detección de compatibilidad contra versiones antiguas del esquema): la BD compartida
 *   por V3 ya tiene esas columnas, así que se asumen presentes siempre.</li>
 *   <li>{@code posiblesRelacionados} queda fijo en 0 (en V2 requiere una consulta aparte de
 *   {@code ExpedienteRelacionadoDAO}, aún no portado) porque este incremento no incluye la
 *   pestaña "Asociar" ni la expansión de expedientes asociados en la grilla; distinto de
 *   {@code asociadosConfirmados}, que sí es una subconsulta barata ya incluida en el SELECT.</li>
 *   <li>No se llama a {@code expedienteRelacionadoDAO.sincronizarAsignacionAsociados} ni a
 *   {@code ExpedienteEstadoPropagacionDAO.propagarEstadoAAsociados} tras asignar (ambos atados al
 *   mismo alcance diferido de "Asociar"/duplicados): si el expediente asignado tiene duplicados
 *   confirmados desde V2, esos duplicados no heredan automáticamente el estado en este incremento.</li>
 *   <li>{@code resolverAutorHistorial} recibe {@code actorEsAdminSistema} ya resuelto por el
 *   caller desde {@code CurrentUser} (JWT), en vez de consultarlo con una query aparte como hace
 *   V2 vía {@code CatalogoLookupDAO.tieneRolAdminSistema}.</li>
 *   <li>{@link #buscarExpedientes} consulta la vista {@code vw_expediente_asignacion_detalle}
 *   (script {@code db/sdrerc_app/scripts/99_vista_expediente_asignacion_detalle.sql}, pendiente de
 *   ejecución manual) en vez de repetir el join completo como SQL inline en Java — mismo patrón ya
 *   usado por {@link ExpedienteBandejaDAO} sobre {@code vw_expediente_bandeja}. Los métodos de
 *   escritura ({@link #asignarExpedientes}, {@link #reasignarExpediente}) siguen con SQL directo:
 *   solo se movieron a vista los SELECT de listado, no los INSERT/UPDATE (ver nota de
 *   procedimientos almacenados en el plan de migración, sección 6 — decisión deliberada de no
 *   mover lógica de negocio/transaccional a la BD).</li>
 * </ul>
 */
@Repository
public class AsignacionExpedienteDAO {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String CODIGO_ETAPA_ORIGEN = "REGISTRO";
    private static final String CODIGO_ESTADO_ORIGEN = "REGISTRADO";
    private static final String CODIGO_ETAPA_DESTINO = "ASIGNACION";
    private static final String CODIGO_ESTADO_DESTINO = "ASIGNADO";
    private static final String CODIGO_MOVIMIENTO = "ASIGNACION_ABOGADO";
    private static final String CODIGO_MOVIMIENTO_REASIGNACION = "REASIGNACION_ABOGADO";
    private static final String TIPO_RELACION_DOCUMENTO_DUPLICADO_ASOCIADO = "DOCUMENTO_DUPLICADO_ASOCIADO";
    private static final String TIPO_RELACION_MISMA_ACTA_TITULAR = "MISMA_ACTA_TITULAR";
    private static final String CODIGO_MOVIMIENTO_GENERACION_NUMERO = "GENERACION_CODIGO_EXPEDIENTE";

    private final DataSource dataSource;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final CalendarioLaboralService calendarioLaboralService;
    private final CorrelativoExpedienteService correlativoExpedienteService;

    public AsignacionExpedienteDAO(
            DataSource dataSource,
            CatalogoLookupDAO catalogoLookupDAO,
            CalendarioLaboralService calendarioLaboralService,
            CorrelativoExpedienteService correlativoExpedienteService) {
        this.dataSource = dataSource;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.calendarioLaboralService = calendarioLaboralService;
        this.correlativoExpedienteService = correlativoExpedienteService;
    }

    /**
     * Port de com.sdrerc.infrastructure.sdrercapp.dao.AsignacionExpedienteDAO#generarNumeroExpediente
     * (V2): "Decisión de número" del panel "Asociar" — genera un número de expediente NUEVO e
     * independiente para una solicitud registrada sin número (REGISTRO/REGISTRADO), cuando el
     * usuario decide que NO corresponde asociarla a otro expediente como duplicado. Exige lock
     * pesimista, que el expediente siga sin número, y que no esté ya asociado como duplicado de
     * otro principal (`esDocumentoDuplicadoAsociado`).
     */
    public String generarNumeroExpediente(Long idExpediente, Long idUsuario) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para generar número.");
        }
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteParaNumero expediente = bloquearExpedienteParaNumero(conn, idExpediente);
                if (!CODIGO_ETAPA_ORIGEN.equalsIgnoreCase(expediente.etapaCodigo)
                        || !CODIGO_ESTADO_ORIGEN.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("Solo se puede generar número para registros en REGISTRO / REGISTRADO.");
                }
                if (hasText(expediente.numeroExpediente)) {
                    throw new SQLException("El expediente ya tiene número asignado: " + expediente.numeroExpediente + ".");
                }
                if (esDocumentoDuplicadoAsociado(conn, idExpediente)) {
                    throw new SQLException("Este registro ya está asociado al expediente principal y no requiere número independiente.");
                }

                int anio = correlativoExpedienteService.anioActual();
                int correlativo = obtenerUltimoCorrelativoExpedienteParaNumero(conn, anio) + 1;
                String numero = correlativoExpedienteService.generar(anio, correlativo);
                actualizarNumeroExpedienteGenerado(conn, idExpediente, numero, idUsuario);
                limpiarAlertaPotencialDuplicadoGeneracion(conn, idExpediente);
                Long idMovimiento = requerirId(
                        catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_GENERACION_NUMERO),
                        "movimiento GENERACION_CODIGO_EXPEDIENTE");
                insertarHistorialGeneracionNumero(conn, idExpediente, idMovimiento, expediente.idEtapa, expediente.idEstado, idUsuario, numero);

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return numero;
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

    private ExpedienteParaNumero bloquearExpedienteParaNumero(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT e.numero_expediente, e.id_etapa_actual, e.id_estado_actual, "
                + "et.codigo AS etapa_codigo, est.codigo AS estado_codigo "
                + "FROM expediente e "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "WHERE e.id_expediente = ? AND e.activo = 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El expediente seleccionado no existe o no está activo.");
                }
                return new ExpedienteParaNumero(
                        rs.getString("numero_expediente"),
                        getLongOrNull(rs, "id_etapa_actual"),
                        getLongOrNull(rs, "id_estado_actual"),
                        rs.getString("etapa_codigo"),
                        rs.getString("estado_codigo"));
            }
        }
    }

    private int obtenerUltimoCorrelativoExpedienteParaNumero(Connection conn, int anio) throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(numero_expediente, -6))), 0) AS correlativo "
                + "FROM expediente "
                + "WHERE numero_expediente IS NOT NULL "
                + "AND activo = 1 "
                + "AND (numero_expediente LIKE ? OR numero_expediente LIKE ?) "
                + "AND REGEXP_LIKE(numero_expediente, '^SDRERC[-_]EXP[-_][0-9]{4}[-_][0-9]{6}$')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "SDRERC-EXP-" + anio + "-%");
            ps.setString(2, "SDRERC_EXP_" + anio + "_%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("correlativo");
                }
            }
        }
        return 0;
    }

    private void actualizarNumeroExpedienteGenerado(Connection conn, Long idExpediente, String numeroExpediente, Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente SET "
                + "numero_expediente = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, "
                + "modificado_por = ?, "
                + "modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? "
                + "AND activo = 1 "
                + "AND (numero_expediente IS NULL OR TRIM(numero_expediente) IS NULL)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroExpediente);
            setLongOrNull(ps, 2, idUsuario);
            ps.setLong(3, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo generar número; el expediente pudo haber sido actualizado por otro usuario.");
            }
        }
    }

    /**
     * Al generar número independiente para una solicitud, el usuario decidió que NO corresponde
     * asociarla a otro expediente como duplicado; la alerta "Potencial duplicado" de la bandeja
     * (columna Alertas, calculada directo de expediente_solicitud.potencial_duplicado) queda
     * resuelta igual que cuando se asocia.
     */
    private void limpiarAlertaPotencialDuplicadoGeneracion(Connection conn, Long idExpediente) throws SQLException {
        String sql = "UPDATE expediente_solicitud SET potencial_duplicado = 0 "
                + "WHERE id_expediente = ? AND activo = 1 AND potencial_duplicado = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.executeUpdate();
        }
    }

    private void insertarHistorialGeneracionNumero(
            Connection conn,
            Long idExpediente,
            Long idMovimiento,
            Long idEtapa,
            Long idEstado,
            Long idUsuario,
            String numeroExpediente) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, "
                + "id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino, "
                + "id_usuario_origen, tabla_relacionada, id_registro_relacionado, comentario, motivo, activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, ?, ?, ?, 'EXPEDIENTE', ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            setLongOrNull(ps, 3, idEtapa);
            setLongOrNull(ps, 4, idEstado);
            setLongOrNull(ps, 5, idEtapa);
            setLongOrNull(ps, 6, idEstado);
            setLongOrNull(ps, 7, idUsuario);
            ps.setLong(8, idExpediente);
            ps.setString(9, "Generación de número de expediente desde Asignación para Reconsideración/Apelación: " + numeroExpediente + ".");
            ps.setString(10, CODIGO_MOVIMIENTO_GENERACION_NUMERO);
            setLongOrNull(ps, 11, idUsuario);
            ps.executeUpdate();
        }
    }

    private static class ExpedienteParaNumero {

        private final String numeroExpediente;
        private final Long idEtapa;
        private final Long idEstado;
        private final String etapaCodigo;
        private final String estadoCodigo;

        private ExpedienteParaNumero(
                String numeroExpediente,
                Long idEtapa,
                Long idEstado,
                String etapaCodigo,
                String estadoCodigo) {
            this.numeroExpediente = numeroExpediente == null ? "" : numeroExpediente;
            this.idEtapa = idEtapa;
            this.idEstado = idEstado;
            this.etapaCodigo = etapaCodigo == null ? "" : etapaCodigo;
            this.estadoCodigo = estadoCodigo == null ? "" : estadoCodigo;
        }
    }

    /**
     * Consulta {@code vw_expediente_asignacion_detalle} (script
     * {@code db/sdrerc_app/scripts/99_vista_expediente_asignacion_detalle.sql}, no ejecutado
     * todavía contra la BD — requiere autorización y ejecución manual antes de que este método
     * funcione) en vez de repetir el join completo en Java. Los filtros dinámicos por request
     * (texto libre, estado, rango de fechas, límite) se aplican sobre la vista, igual patrón que
     * {@link ExpedienteBandejaDAO#buscar} sobre {@code vw_expediente_bandeja}.
     */
    public List<AsignacionExpedienteDTO> buscarExpedientes(
            String textoLibre,
            String estadoCodigo,
            LocalDate fechaSolicitudDesde,
            LocalDate fechaSolicitudHasta,
            int limite) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("SELECT * FROM vw_expediente_asignacion_detalle WHERE 1 = 1 ");

        if (hasText(estadoCodigo) && !"TODOS".equalsIgnoreCase(estadoCodigo)) {
            sql.append("AND estado_codigo = ? ");
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
            sql.append("AND (");
            sql.append("UPPER(NVL(numero_expediente, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(numero_tramite_documentario, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(procedimiento, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(numero_acta, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(titular, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(solicitante, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(numero_documento_titular, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(observacion_solicitud, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(numero_expediente_sgd, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(observacion_grupo_familiar, '')) LIKE ? ");
            sql.append(") ");
            String pattern = "%" + textoLibre.trim().toUpperCase(Locale.ROOT) + "%";
            for (int i = 0; i < 10; i++) {
                params.add(pattern);
            }
        }

        sql.append("ORDER BY fecha_vencimiento ASC NULLS LAST, orden_titular ASC, id_expediente ASC");
        sql.append(") WHERE ROWNUM <= ?");
        params.add(normalizarLimite(limite));

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<AsignacionExpedienteDTO> expedientes = new ArrayList<>();
                while (rs.next()) {
                    expedientes.add(mapPendiente(conn, rs));
                }
                return expedientes;
            }
        }
    }

    public AsignacionResultadoDTO asignarExpedientes(
            List<Long> idsExpediente,
            Long idEquipoDestino,
            Long idAbogadoResponsable,
            String comentario,
            Long idUsuarioAsignador,
            boolean actorEsAdminSistema,
            Map<Long, String> hojasEnvioPorExpediente) throws SQLException {
        Set<Long> idsUnicos = normalizarIds(idsExpediente);
        if (idsUnicos.isEmpty()) {
            throw new IllegalArgumentException("Seleccione al menos un expediente para asignar.");
        }
        if (idEquipoDestino == null) {
            throw new IllegalArgumentException("Seleccione el equipo destino.");
        }
        if (idAbogadoResponsable == null) {
            throw new IllegalArgumentException("Seleccione el abogado responsable.");
        }
        Map<Long, String> hojasNormalizadas = normalizarHojasEnvio(idsUnicos, hojasEnvioPorExpediente);
        validarHojasEnvioUnicasEnSeleccion(hojasNormalizadas);

        List<String> detalles = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idEtapaOrigen = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, CODIGO_ETAPA_ORIGEN), "etapa REGISTRO");
                Long idEstadoOrigen = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, CODIGO_ESTADO_ORIGEN), "estado REGISTRADO");
                Long idEtapaDestino = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, CODIGO_ETAPA_DESTINO), "etapa ASIGNACION");
                Long idEstadoDestino = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, CODIGO_ESTADO_DESTINO), "estado ASIGNADO");
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO), "movimiento ASIGNACION_ABOGADO");
                validarTransicion(conn, idEtapaOrigen, idEstadoOrigen, idEtapaDestino, idEstadoDestino);
                validarEquipoActivo(conn, idEquipoDestino);
                validarAbogadoAsignable(conn, idAbogadoResponsable, idEquipoDestino);
                validarHojasEnvioNoRegistradas(conn, hojasNormalizadas);

                for (Long idExpediente : idsUnicos) {
                    ExpedienteBloqueado expediente = bloquearExpediente(conn, idExpediente);
                    if (!CODIGO_ETAPA_ORIGEN.equalsIgnoreCase(expediente.etapaCodigo)
                            || !CODIGO_ESTADO_ORIGEN.equalsIgnoreCase(expediente.estadoCodigo)) {
                        throw new IllegalStateException("El expediente ya se encuentra asignado.");
                    }
                    if (tieneAsignacionActiva(conn, idExpediente)) {
                        throw new IllegalStateException("El expediente ya se encuentra asignado.");
                    }
                    if (esDocumentoDuplicadoAsociado(conn, idExpediente)) {
                        throw new IllegalStateException("Este registro está asociado al expediente principal y no requiere asignación independiente.");
                    }
                    if (!hasText(expediente.numeroExpediente)) {
                        throw new IllegalStateException("El expediente seleccionado aún no tiene número. Asócielo a un expediente principal o genere número antes de asignarlo.");
                    }

                    Long idAsignacion = insertarAsignacion(
                            conn,
                            idExpediente,
                            idAbogadoResponsable,
                            idEquipoDestino,
                            idEtapaDestino,
                            hojasNormalizadas.get(idExpediente),
                            comentario,
                            idUsuarioAsignador,
                            false);
                    actualizarExpediente(
                            conn,
                            idExpediente,
                            idEtapaDestino,
                            idEstadoDestino,
                            idAbogadoResponsable,
                            idEquipoDestino,
                            idUsuarioAsignador);
                    insertarHistorial(
                            conn,
                            idExpediente,
                            idMovimiento,
                            idEtapaOrigen,
                            idEstadoOrigen,
                            idEtapaDestino,
                            idEstadoDestino,
                            idUsuarioAsignador,
                            actorEsAdminSistema,
                            idAbogadoResponsable,
                            idEquipoDestino,
                            idAsignacion,
                            comentario);
                    detalles.add(expediente.numeroExpediente + " asignado.");
                }

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
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

        return AsignacionResultadoDTO.exito(
                idsUnicos.size(),
                idsUnicos.size() + " expediente(s) asignado(s) correctamente.",
                detalles);
    }

    /**
     * Reasigna un expediente que ya tiene una asignación activa a otro equipo/abogado. A
     * diferencia de {@link #asignarExpedientes}: opera sobre un único expediente por llamada (el
     * frontend, igual que V2, hace un loop con manejo de error por-item cuando el usuario
     * selecciona varios expedientes ya asignados con "Habilitar reasignación" activo), exige hoja
     * de envío (no es opcional aquí), y no cambia etapa/estado del expediente (sigue en
     * ASIGNACION/ASIGNADO, solo cambia el responsable).
     */
    public AsignacionResultadoDTO reasignarExpediente(
            Long idExpediente,
            Long idEquipoNuevo,
            Long idAbogadoNuevo,
            String numeroHojaEnvioNuevo,
            String comentario,
            Long idUsuarioAsignador,
            boolean actorEsAdminSistema) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para reasignar.");
        }
        if (idEquipoNuevo == null) {
            throw new IllegalArgumentException("Seleccione el equipo destino.");
        }
        if (idAbogadoNuevo == null) {
            throw new IllegalArgumentException("Seleccione el abogado responsable.");
        }
        if (esHojaEnvioVacia(numeroHojaEnvioNuevo)) {
            throw new IllegalArgumentException("Ingrese una hoja de envío.");
        }
        String hojaEnvio = numeroHojaEnvioNuevo.trim();

        try (Connection conn = dataSource.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEdicionDatos expediente = bloquearExpedienteParaEdicionDatos(conn, idExpediente);
                if (!tieneAsignacionActiva(conn, idExpediente)) {
                    throw new IllegalStateException("El expediente no tiene una asignación activa para reasignar.");
                }
                validarHojaEnvioNoRegistrada(conn, hojaEnvio);
                validarEquipoActivo(conn, idEquipoNuevo);
                validarAbogadoAsignable(conn, idAbogadoNuevo, idEquipoNuevo);

                desactivarAsignacionesActivas(conn, idExpediente, idUsuarioAsignador);

                Long idAsignacion = insertarAsignacion(
                        conn,
                        idExpediente,
                        idAbogadoNuevo,
                        idEquipoNuevo,
                        expediente.idEtapa,
                        hojaEnvio,
                        comentario,
                        idUsuarioAsignador,
                        true);

                actualizarExpediente(
                        conn,
                        idExpediente,
                        expediente.idEtapa,
                        expediente.idEstado,
                        idAbogadoNuevo,
                        idEquipoNuevo,
                        idUsuarioAsignador);

                Long idMovimiento = requerirId(
                        catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_REASIGNACION),
                        "movimiento " + CODIGO_MOVIMIENTO_REASIGNACION);
                insertarHistorial(
                        conn,
                        idExpediente,
                        idMovimiento,
                        expediente.idEtapa,
                        expediente.idEstado,
                        expediente.idEtapa,
                        expediente.idEstado,
                        idUsuarioAsignador,
                        actorEsAdminSistema,
                        idAbogadoNuevo,
                        idEquipoNuevo,
                        idAsignacion,
                        hasText(comentario) ? comentario : "Reasignación de expediente a nuevo abogado responsable.");

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return AsignacionResultadoDTO.exito(
                        1,
                        expediente.numeroExpediente + " reasignado correctamente.",
                        java.util.Collections.singletonList(expediente.numeroExpediente + " reasignado."));
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

    /** Port literal del mismo nombre en V2 (sin el chequeo soportaNumeroHojaEnvio, ver Javadoc de clase). */
    public List<AsignacionHistorialDTO> listarHistorialAsignaciones(Long idExpediente) throws SQLException {
        List<AsignacionHistorialDTO> items = new ArrayList<>();
        if (idExpediente == null) {
            return items;
        }
        String sql = "SELECT axa.id_expediente_asignacion, u.nombre_completo AS abogado, eq.nombre AS equipo, "
                + "axa.numero_hoja_envio, axa.fecha_asignacion, axa.activa, axa.es_reasignacion_excepcional, axa.motivo, "
                + "uc.nombre_completo AS asignado_por "
                + "FROM expediente_asignacion axa "
                + "LEFT JOIN usuario u ON u.id_usuario = axa.id_usuario_asignado "
                + "LEFT JOIN equipo eq ON eq.id_equipo = axa.id_equipo_asignado "
                + "LEFT JOIN usuario uc ON uc.id_usuario = axa.creado_por "
                + "WHERE axa.id_expediente = ? AND axa.activo = 1 "
                + "ORDER BY axa.fecha_asignacion DESC, axa.id_expediente_asignacion DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new AsignacionHistorialDTO(
                            getLongOrNull(rs, "id_expediente_asignacion"),
                            rs.getString("abogado"),
                            rs.getString("equipo"),
                            rs.getString("numero_hoja_envio"),
                            toLocalDateTime(rs.getTimestamp("fecha_asignacion")),
                            rs.getInt("activa") == 1,
                            rs.getInt("es_reasignacion_excepcional") == 1,
                            rs.getString("motivo"),
                            rs.getString("asignado_por")));
                }
            }
        }
        return items;
    }

    private AsignacionExpedienteDTO mapPendiente(Connection conn, ResultSet rs) throws SQLException {
        Long idExpediente = getLongOrNull(rs, "id_expediente");
        return new AsignacionExpedienteDTO(
                idExpediente,
                rs.getString("numero_expediente"),
                rs.getString("numero_expediente_sgd"),
                rs.getString("numero_hoja_envio_asignacion"),
                rs.getString("numero_tramite_documentario"),
                rs.getString("canal_ingreso"),
                extraerValorObservacion(rs.getString("observacion_solicitud"), "Tipo de solicitud"),
                rs.getString("numero_documento"),
                rs.getString("tipo_documento"),
                rs.getString("procedimiento"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                rs.getString("solicitante"),
                rs.getString("solicitante_tipo_documento"),
                rs.getString("solicitante_numero_documento"),
                rs.getString("solicitante_correo"),
                rs.getString("solicitante_telefono"),
                rs.getString("solicitante_direccion"),
                rs.getString("solicitante_departamento"),
                rs.getString("solicitante_provincia"),
                rs.getString("solicitante_distrito"),
                rs.getString("equipo_asignado"),
                getLongOrNull(rs, "id_equipo_responsable"),
                rs.getString("abogado_asignado"),
                getLongOrNull(rs, "id_abogado_responsable"),
                rs.getString("tipo_documento_titular"),
                rs.getString("numero_documento_titular"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                toLocalDate(rs.getDate("fecha_vencimiento")),
                calendarioLaboralService.calcularDiasHabilesRestantes(conn, idExpediente, rs.getDate("fecha_vencimiento")),
                toLocalDateTime(rs.getTimestamp("fecha_registro")),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getInt("asignacion_activa") > 0,
                0,
                rs.getInt("asociados_confirmados"),
                rs.getInt("potencial_duplicado") > 0,
                rs.getString("observacion_solicitud"),
                rs.getInt("grupo_familiar") == 1,
                rs.getString("criterio_grupo_familiar"),
                rs.getString("observacion_grupo_familiar"),
                rs.getInt("alerta_grupo_familiar_activa") == 1);
    }

    private void validarTransicion(
            Connection conn,
            Long idEtapaOrigen,
            Long idEstadoOrigen,
            Long idEtapaDestino,
            Long idEstadoDestino) throws SQLException {
        String sql = "SELECT 1 FROM flujo f "
                + "JOIN flujo_transicion ft ON ft.id_flujo = f.id_flujo "
                + "WHERE f.codigo = ? AND f.activo = 1 "
                + "AND ft.codigo_accion = ? AND ft.activo = 1 "
                + "AND ft.id_etapa_origen = ? AND ft.id_estado_origen = ? "
                + "AND ft.id_etapa_destino = ? AND ft.id_estado_destino = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, CODIGO_FLUJO);
            ps.setString(2, CODIGO_MOVIMIENTO);
            ps.setLong(3, idEtapaOrigen);
            ps.setLong(4, idEstadoOrigen);
            ps.setLong(5, idEtapaDestino);
            ps.setLong(6, idEstadoDestino);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("No existe transición activa REGISTRO/REGISTRADO -> ASIGNACION/ASIGNADO para ASIGNACION_ABOGADO.");
                }
            }
        }
    }

    private void validarEquipoActivo(Connection conn, Long idEquipo) throws SQLException {
        String sql = "SELECT 1 FROM equipo WHERE id_equipo = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEquipo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("El equipo destino no está activo o no existe.");
                }
            }
        }
    }

    private void validarAbogadoAsignable(Connection conn, Long idUsuario, Long idEquipo) throws SQLException {
        String sql = "SELECT 1 FROM usuario u "
                + "JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario AND ur.activo = 1 "
                + "JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1 "
                + "JOIN equipo_usuario eu ON eu.id_usuario = u.id_usuario AND eu.activo = 1 "
                + "WHERE u.id_usuario = ? "
                + "AND eu.id_equipo = ? "
                + "AND u.activo = 1 "
                + "AND UPPER(u.estado) = 'ACTIVO' "
                + "AND UPPER(r.codigo) IN ('ABOGADO', 'ANALISTA')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ps.setLong(2, idEquipo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("El abogado seleccionado no está activo o no pertenece al equipo destino.");
                }
            }
        }
    }

    private ExpedienteBloqueado bloquearExpediente(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT e.id_expediente, e.numero_expediente, et.codigo AS etapa_codigo, est.codigo AS estado_codigo "
                + "FROM expediente e "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "WHERE e.id_expediente = ? AND e.activo = 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("El expediente seleccionado no existe o no está activo.");
                }
                return new ExpedienteBloqueado(
                        rs.getString("numero_expediente"),
                        rs.getString("etapa_codigo"),
                        rs.getString("estado_codigo"));
            }
        }
    }

    private ExpedienteEdicionDatos bloquearExpedienteParaEdicionDatos(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT e.id_expediente, e.numero_expediente, e.id_etapa_actual, e.id_estado_actual, "
                + "et.codigo AS etapa_codigo, est.codigo AS estado_codigo "
                + "FROM expediente e "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "WHERE e.id_expediente = ? AND e.activo = 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("El expediente seleccionado no existe o no está activo.");
                }
                return new ExpedienteEdicionDatos(
                        rs.getString("numero_expediente"),
                        getLongOrNull(rs, "id_etapa_actual"),
                        getLongOrNull(rs, "id_estado_actual"));
            }
        }
    }

    private void desactivarAsignacionesActivas(Connection conn, Long idExpediente, Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_asignacion SET activa = 0, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activa = 1 AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setLongOrNull(ps, 1, idUsuario);
            ps.setLong(2, idExpediente);
            ps.executeUpdate();
        }
    }

    private void validarHojaEnvioNoRegistrada(Connection conn, String hojaEnvio) throws SQLException {
        String normalizada = normalizarHojaEnvio(hojaEnvio);
        if (normalizada == null) {
            return;
        }
        String sql = "SELECT 1 FROM expediente_asignacion "
                + "WHERE activo = 1 "
                + "AND numero_hoja_envio IS NOT NULL "
                + "AND UPPER(TRIM(numero_hoja_envio)) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizada);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalStateException("El número de hoja de envío " + hojaEnvio.trim() + " ya se encuentra registrado en otra asignación.");
                }
            }
        }
    }

    private static boolean esHojaEnvioVacia(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean tieneAsignacionActiva(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT 1 FROM expediente_asignacion WHERE id_expediente = ? AND activa = 1 AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean esDocumentoDuplicadoAsociado(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT 1 FROM expediente_relacion "
                + "WHERE activo = 1 "
                + "AND id_expediente_relacionado = ? "
                + "AND UPPER(tipo_relacion) IN (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, TIPO_RELACION_DOCUMENTO_DUPLICADO_ASOCIADO);
            ps.setString(3, TIPO_RELACION_MISMA_ACTA_TITULAR);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Long insertarAsignacion(
            Connection conn,
            Long idExpediente,
            Long idAbogado,
            Long idEquipo,
            Long idEtapa,
            String numeroHojaEnvio,
            String comentario,
            Long idUsuarioAsignador,
            boolean esReasignacion) throws SQLException {
        String sql = "INSERT INTO expediente_asignacion ("
                + "id_expediente, id_usuario_asignado, id_equipo_asignado, id_etapa, fecha_asignacion, "
                + "activa, es_abogado_principal, es_reasignacion_excepcional, numero_hoja_envio, motivo, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, SYSTIMESTAMP, 1, 1, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_ASIGNACION"})) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idAbogado);
            ps.setLong(3, idEquipo);
            ps.setLong(4, idEtapa);
            ps.setInt(5, esReasignacion ? 1 : 0);
            ps.setString(6, limitar(numeroHojaEnvio, 120));
            ps.setString(7, limitar(comentario, 1000));
            setLongOrNull(ps, 8, idUsuarioAsignador);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "expediente_asignacion");
        }
    }

    private void actualizarExpediente(
            Connection conn,
            Long idExpediente,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idAbogadoResponsable,
            Long idEquipoDestino,
            Long idUsuarioModificador) throws SQLException {
        String sql = "UPDATE expediente SET "
                + "id_etapa_actual = ?, "
                + "id_estado_actual = ?, "
                + "id_usuario_responsable_actual = ?, "
                + "id_usuario_abogado_inicial = NVL(id_usuario_abogado_inicial, ?), "
                + "id_equipo_responsable_actual = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, "
                + "modificado_por = ?, "
                + "modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEtapaDestino);
            ps.setLong(2, idEstadoDestino);
            ps.setLong(3, idAbogadoResponsable);
            ps.setLong(4, idAbogadoResponsable);
            ps.setLong(5, idEquipoDestino);
            setLongOrNull(ps, 6, idUsuarioModificador);
            ps.setLong(7, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el expediente seleccionado.");
            }
        }
    }

    private void insertarHistorial(
            Connection conn,
            Long idExpediente,
            Long idMovimiento,
            Long idEtapaOrigen,
            Long idEstadoOrigen,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idUsuarioAsignador,
            boolean actorEsAdminSistema,
            Long idAbogadoResponsable,
            Long idEquipoDestino,
            Long idAsignacion,
            String comentario) throws SQLException {
        Long idAutorHistorial = resolverAutorHistorial(idUsuarioAsignador, actorEsAdminSistema, idAbogadoResponsable);
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, "
                + "id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino, "
                + "id_usuario_origen, id_usuario_destino, id_equipo_destino, "
                + "tabla_relacionada, id_registro_relacionado, comentario, motivo, activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, ?, ?, ?, ?, ?, 'EXPEDIENTE_ASIGNACION', ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            ps.setLong(3, idEtapaOrigen);
            ps.setLong(4, idEstadoOrigen);
            ps.setLong(5, idEtapaDestino);
            ps.setLong(6, idEstadoDestino);
            setLongOrNull(ps, 7, idAutorHistorial);
            ps.setLong(8, idAbogadoResponsable);
            ps.setLong(9, idEquipoDestino);
            ps.setLong(10, idAsignacion);
            ps.setString(11, comentario == null || comentario.trim().isEmpty()
                    ? "Asignación de expediente a abogado responsable."
                    : limitar(comentario.trim(), 2000));
            ps.setString(12, CODIGO_MOVIMIENTO);
            setLongOrNull(ps, 13, idAutorHistorial);
            ps.executeUpdate();
        }
    }

    /**
     * Si quien ejecuta la acción es ADMIN_SISTEMA, el historial no debe quedar a su nombre: se
     * sustituye por el usuario asignado (destino) de esa misma acción. Ver CLAUDE.md, sección
     * "Autor del historial cuando actúa ADMIN_SISTEMA".
     */
    private Long resolverAutorHistorial(Long idUsuarioActor, boolean actorEsAdminSistema, Long idUsuarioDestino) {
        if (idUsuarioDestino == null || !actorEsAdminSistema) {
            return idUsuarioActor;
        }
        return idUsuarioDestino;
    }

    private Map<Long, String> normalizarHojasEnvio(
            Set<Long> idsExpediente,
            Map<Long, String> hojasEnvioPorExpediente) {
        Map<Long, String> result = new LinkedHashMap<>();
        if (idsExpediente.isEmpty() || hojasEnvioPorExpediente == null) {
            return result;
        }
        for (Long idExpediente : idsExpediente) {
            String hoja = hojasEnvioPorExpediente.get(idExpediente);
            if (hasText(hoja)) {
                result.put(idExpediente, hoja.trim());
            }
        }
        return result;
    }

    private void validarHojasEnvioUnicasEnSeleccion(Map<Long, String> hojasEnvioPorExpediente) {
        Set<String> valores = new LinkedHashSet<>();
        for (String hoja : hojasEnvioPorExpediente.values()) {
            String normalizada = normalizarHojaEnvio(hoja);
            if (normalizada == null) {
                continue;
            }
            if (!valores.add(normalizada)) {
                throw new IllegalArgumentException("El número de hoja de envío " + hoja.trim() + " está duplicado en la selección.");
            }
        }
    }

    private void validarHojasEnvioNoRegistradas(
            Connection conn,
            Map<Long, String> hojasEnvioPorExpediente) throws SQLException {
        if (hojasEnvioPorExpediente.isEmpty()) {
            return;
        }
        String sql = "SELECT 1 FROM expediente_asignacion "
                + "WHERE activo = 1 "
                + "AND numero_hoja_envio IS NOT NULL "
                + "AND UPPER(TRIM(numero_hoja_envio)) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String hoja : hojasEnvioPorExpediente.values()) {
                String normalizada = normalizarHojaEnvio(hoja);
                if (normalizada == null) {
                    continue;
                }
                ps.setString(1, normalizada);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalStateException("El número de hoja de envío " + hoja.trim() + " ya se encuentra registrado en otra asignación.");
                    }
                }
            }
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

    private static Set<Long> normalizarIds(List<Long> ids) {
        Set<Long> result = new LinkedHashSet<>();
        if (ids == null) {
            return result;
        }
        for (Long id : ids) {
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

    private static String extraerValorObservacion(String observacion, String etiqueta) {
        if (observacion == null || etiqueta == null) {
            return null;
        }
        String prefix = etiqueta.trim().toLowerCase(Locale.ROOT) + ":";
        String[] partes = observacion.split("\\|");
        for (String parte : partes) {
            String limpia = parte == null ? "" : parte.trim();
            if (limpia.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                String valor = limpia.substring(prefix.length()).trim();
                return valor.isEmpty() ? null : valor;
            }
        }
        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static int normalizarLimite(int limite) {
        if (limite <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limite, MAX_LIMIT);
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

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static String limitar(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static String normalizarHojaEnvio(String value) {
        return value == null || value.trim().isEmpty()
                ? null
                : value.trim().toUpperCase(Locale.ROOT);
    }

    private void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // El error original se reporta al usuario; el rollback fallido no debe ocultarlo.
        }
    }

    private static class ExpedienteBloqueado {

        private final String numeroExpediente;
        private final String etapaCodigo;
        private final String estadoCodigo;

        private ExpedienteBloqueado(String numeroExpediente, String etapaCodigo, String estadoCodigo) {
            this.numeroExpediente = numeroExpediente == null ? "" : numeroExpediente;
            this.etapaCodigo = etapaCodigo == null ? "" : etapaCodigo;
            this.estadoCodigo = estadoCodigo == null ? "" : estadoCodigo;
        }
    }

    private static class ExpedienteEdicionDatos {

        private final String numeroExpediente;
        private final Long idEtapa;
        private final Long idEstado;

        private ExpedienteEdicionDatos(String numeroExpediente, Long idEtapa, Long idEstado) {
            this.numeroExpediente = numeroExpediente == null ? "" : numeroExpediente;
            this.idEtapa = idEtapa;
            this.idEstado = idEstado;
        }
    }
}
