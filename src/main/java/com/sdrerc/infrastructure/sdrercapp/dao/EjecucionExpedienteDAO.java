package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CalendarioLaboralService;
import com.sdrerc.domain.dto.sdrercapp.EjecucionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionReversionDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
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

public class EjecucionExpedienteDAO {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String ETAPA_EJECUCION = "EJECUCION";
    private static final String ETAPA_ANALISIS = "ANALISIS";
    private static final String ETAPA_NOTIFICACION = "NOTIFICACION";
    private static final String ESTADO_EN_EJECUCION = "EN_EJECUCION";
    private static final String ESTADO_EJECUTADO = "EJECUTADO";
    private static final String ESTADO_DOCUMENTO_INCONSISTENTE = "DOCUMENTO_INCONSISTENTE";
    private static final String ESTADO_REQUIERE_CORRECCION = "REQUIERE_CORRECCION";
    private static final String ESTADO_INDAGATORIO = "INDAGATORIO";
    private static final String ESTADO_ANALISIS_OBSERVADO = "OBSERVADO";
    private static final String ESTADO_EN_NOTIFICACION = "EN_NOTIFICACION";
    private static final String ACCION_INICIO_EJECUCION = "INICIO_EJECUCION";
    private static final String ACCION_OBSERVACION_EJECUCION = "OBSERVACION_EJECUCION";
    private static final String ACCION_DOCUMENTO_INCONSISTENTE = "REVERSION_ESTADO_DOCUMENTO_EJECUCION";
    private static final String ACCION_DEVOLUCION_ANALISIS = "DEVOLUCION_A_ANALISIS";
    private static final String ACCION_DERIVACION_NOTIFICACION = "DERIVACION_A_NOTIFICACION";

    private final CatalogoLookupDAO catalogoLookupDAO;
    private final ResolucionDocumentoDAO resolucionDocumentoDAO;
    private final CalendarioLaboralService calendarioLaboralService = new CalendarioLaboralService();

    public EjecucionExpedienteDAO() {
        this(new CatalogoLookupDAO(), new ResolucionDocumentoDAO());
    }

    public EjecucionExpedienteDAO(
            CatalogoLookupDAO catalogoLookupDAO,
            ResolucionDocumentoDAO resolucionDocumentoDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.resolucionDocumentoDAO = resolucionDocumentoDAO;
    }

    public List<EjecucionExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("SELECT DISTINCT e.id_expediente, e.numero_expediente, e.numero_tramite_documentario, ");
        sql.append("esol.asunto AS procedimiento, p.tipo_documento, ");
        sql.append("ta.nombre AS tipo_acta, ea.numero_acta, ").append(nombrePersona("p")).append(" AS titular, ");
        sql.append("esol.fecha_recepcion, e.fecha_vencimiento, ");
        sql.append("e.fecha_ultimo_movimiento, ");
        sql.append("(SELECT MAX(h.fecha_movimiento) FROM expediente_historial h ");
        sql.append("JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = h.id_tipo_movimiento ");
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 ");
        sql.append("AND tm.codigo = 'REGISTRO_NUMERO_RESOLUCION') AS fecha_ingreso_ejecucion, ");
        sql.append("ur.nombre_completo AS responsable, eq.nombre AS equipo, ");
        sql.append("et.codigo AS etapa_codigo, est.codigo AS estado_codigo, ");
        sql.append("UPPER(NVL(").append(nombrePersona("p")).append(", 'ZZZ')) AS orden_titular, ");
        sql.append("(SELECT MAX(NVL(tre.nombre, tre.codigo)) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) ");
        sql.append("FROM expediente_evaluacion ev LEFT JOIN tipo_resultado_evaluacion tre ON tre.id_tipo_resultado_evaluacion = ev.id_tipo_resultado_evaluacion ");
        sql.append("WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS resultado_analisis, ");
        sql.append("(SELECT MAX(ev.fundamento) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) ");
        sql.append("FROM expediente_evaluacion ev WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS fundamento_analisis, ");
        sql.append("(SELECT CASE WHEN COUNT(*) > 0 THEN 'Verificación aprobada' ELSE '' END ");
        sql.append("FROM expediente_historial h JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = h.id_tipo_movimiento ");
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 AND tm.codigo = 'APROBACION_VERIFICACION') AS resultado_verificacion, ");
        sql.append("(SELECT MAX(o.descripcion) KEEP (DENSE_RANK LAST ORDER BY o.fecha_observacion) ");
        sql.append("FROM expediente_observacion o WHERE o.id_expediente = e.id_expediente AND o.activo = 1) AS ultima_observacion, ");
        sql.append("(SELECT COUNT(*) FROM expediente_documento d WHERE d.id_expediente = e.id_expediente AND d.activo = 1) AS documentos, ");
        sql.append("(SELECT COUNT(*) FROM expediente_relacion r WHERE r.activo = 1 AND (r.id_expediente_principal = e.id_expediente OR r.id_expediente_relacionado = e.id_expediente)) AS relaciones_confirmadas, ");
        sql.append("res.id_expediente_resolucion, tr.nombre AS tipo_resolucion, res.numero_resolucion, res.fecha_resolucion, ");
        sql.append("(SELECT LISTAGG(ap.codigo_accion, ',') WITHIN GROUP (ORDER BY ap.codigo_accion) ");
        sql.append("FROM vw_expediente_acciones_permitidas ap WHERE ap.id_expediente = e.id_expediente) AS acciones_permitidas ");
        sql.append("FROM expediente e ");
        sql.append("JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual ");
        sql.append("JOIN estado_expediente est ON est.id_estado = e.id_estado_actual ");
        sql.append("LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual ");
        sql.append("LEFT JOIN equipo eq ON eq.id_equipo = e.id_equipo_responsable_actual ");
        sql.append("LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 ");
        sql.append("LEFT JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1 ");
        sql.append("LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta ");
        sql.append("LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' ");
        sql.append("LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 ");
        sql.append("LEFT JOIN (SELECT id_expediente, ");
        sql.append("MAX(id_expediente_resolucion) KEEP (DENSE_RANK LAST ORDER BY creado_en, id_expediente_resolucion) AS id_expediente_resolucion ");
        sql.append("FROM expediente_resolucion WHERE activo = 1 GROUP BY id_expediente) res_pick ");
        sql.append("ON res_pick.id_expediente = e.id_expediente ");
        sql.append("LEFT JOIN expediente_resolucion res ON res.id_expediente_resolucion = res_pick.id_expediente_resolucion ");
        sql.append("LEFT JOIN tipo_resolucion tr ON tr.id_tipo_resolucion = res.id_tipo_resolucion ");
        sql.append("WHERE e.activo = 1 AND et.codigo = ? ");
        params.add(ETAPA_EJECUCION);

        if (hasText(estadoCodigo) && !"TODOS".equalsIgnoreCase(estadoCodigo)) {
            sql.append("AND UPPER(est.codigo) = ? ");
            params.add(estadoCodigo.trim().toUpperCase(Locale.ROOT));
        }

        if (hasText(textoLibre)) {
            String pattern = "%" + textoLibre.trim().toUpperCase(Locale.ROOT) + "%";
            sql.append("AND (UPPER(NVL(e.numero_expediente, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(e.numero_tramite_documentario, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(esol.asunto, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(ea.numero_acta, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(res.numero_resolucion, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(p.numero_documento, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(").append(nombrePersona("p")).append(", '')) LIKE ? ");
            sql.append("OR UPPER(NVL(esol.numero_expediente_sgd, '')) LIKE ?) ");
            for (int i = 0; i < 8; i++) {
                params.add(pattern);
            }
        }

        sql.append("ORDER BY fecha_vencimiento ASC NULLS LAST, orden_titular ASC, id_expediente ASC");
        sql.append(") WHERE ROWNUM <= ?");
        params.add(normalizarLimite(limite));

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<EjecucionExpedienteDTO> expedientes = new ArrayList<EjecucionExpedienteDTO>();
                while (rs.next()) {
                    expedientes.add(map(conn, rs));
                }
                return expedientes;
            }
        }
    }

    public EjecucionResultadoDTO registrarEjecucion(EjecucionRegistroDTO registro, Long idUsuario) throws SQLException {
        return moverDesdeEjecucion(
                registro,
                ACCION_INICIO_EJECUCION,
                ESTADO_EN_EJECUCION,
                ETAPA_EJECUCION,
                ESTADO_EJECUTADO,
                true,
                idUsuario,
                "La ejecución fue registrada correctamente.");
    }

    public EjecucionResultadoDTO registrarObservacion(EjecucionRegistroDTO registro, Long idUsuario) throws SQLException {
        return registrarObservacionEjecucion(
                registro,
                ACCION_OBSERVACION_EJECUCION,
                ESTADO_REQUIERE_CORRECCION,
                idUsuario,
                "La observación de ejecución fue registrada correctamente.");
    }

    public EjecucionResultadoDTO registrarDocumentoInconsistente(EjecucionRegistroDTO registro, Long idUsuario) throws SQLException {
        return registrarObservacionEjecucion(
                registro,
                ACCION_DOCUMENTO_INCONSISTENTE,
                ESTADO_DOCUMENTO_INCONSISTENTE,
                idUsuario,
                "El documento inconsistente fue registrado correctamente.");
    }

    public EjecucionResultadoDTO derivarNotificacion(EjecucionRegistroDTO registro, Long idUsuario) throws SQLException {
        return moverDesdeEjecucion(
                registro,
                ACCION_DERIVACION_NOTIFICACION,
                ESTADO_EJECUTADO,
                ETAPA_NOTIFICACION,
                ESTADO_EN_NOTIFICACION,
                false,
                idUsuario,
                "El expediente fue derivado a Notificación.");
    }

    public EjecucionResultadoDTO revertirAnalisis(EjecucionReversionDTO reversion, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, reversion.getIdExpediente());
                if (!ETAPA_EJECUCION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !(ESTADO_DOCUMENTO_INCONSISTENTE.equalsIgnoreCase(expediente.estadoCodigo)
                        || ESTADO_REQUIERE_CORRECCION.equalsIgnoreCase(expediente.estadoCodigo))) {
                    throw new SQLException("El expediente debe estar con observación o documento inconsistente para devolverlo a Análisis.");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_DEVOLUCION_ANALISIS,
                        ETAPA_EJECUCION,
                        expediente.estadoCodigo,
                        ETAPA_ANALISIS,
                        ESTADO_ANALISIS_OBSERVADO);
                validarRequisitosTransicion(conn, transicion, reversion.getComentario(), reversion.getIdExpediente(), true);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_DEVOLUCION_ANALISIS), "movimiento " + ACCION_DEVOLUCION_ANALISIS);
                insertarObservacionEjecucion(
                        conn,
                        reversion.getIdExpediente(),
                        reversion.getTipoObservacionCodigo(),
                        reversion.getMotivoCorreccionCodigo(),
                        reversion.getMotivoReversion(),
                        idUsuario);
                actualizarExpediente(conn, reversion.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario);
                insertarHistorial(
                        conn,
                        reversion.getIdExpediente(),
                        idMovimiento,
                        expediente.idEtapa,
                        expediente.idEstado,
                        transicion.idEtapaDestino,
                        transicion.idEstadoDestino,
                        idUsuario,
                        expediente.idUsuarioResponsable,
                        expediente.idEquipoResponsable,
                        "EXPEDIENTE_OBSERVACION",
                        null,
                        comentarioMovimiento(ACCION_DEVOLUCION_ANALISIS, reversion.getComentario(), "Devolución a Análisis desde Ejecución."),
                        limitar(reversion.getMotivoReversion(), 1000));
                conn.commit();
                return new EjecucionResultadoDTO(
                        reversion.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_DEVOLUCION_ANALISIS,
                        ETAPA_ANALISIS,
                        ESTADO_ANALISIS_OBSERVADO,
                        "El expediente fue devuelto a Análisis.");
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private EjecucionResultadoDTO registrarObservacionEjecucion(
            EjecucionRegistroDTO registro,
            String accionCodigo,
            String estadoDestinoCodigo,
            Long idUsuario,
            String mensaje) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, registro.getIdExpediente());
                if (!ETAPA_EJECUCION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !ESTADO_EN_EJECUCION.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente debe estar en Ejecución para registrar la observación.");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        accionCodigo,
                        ETAPA_EJECUCION,
                        ESTADO_EN_EJECUCION,
                        ETAPA_EJECUCION,
                        estadoDestinoCodigo);
                validarRequisitosTransicion(conn, transicion, registro.getComentario(), registro.getIdExpediente(), true);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, accionCodigo), "movimiento " + accionCodigo);
                insertarObservacionEjecucion(
                        conn,
                        registro.getIdExpediente(),
                        registro.getTipoObservacionCodigo(),
                        registro.getMotivoCorreccionCodigo(),
                        registro.getComentario(),
                        idUsuario);
                actualizarExpediente(conn, registro.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario);
                insertarHistorial(
                        conn,
                        registro.getIdExpediente(),
                        idMovimiento,
                        expediente.idEtapa,
                        expediente.idEstado,
                        transicion.idEtapaDestino,
                        transicion.idEstadoDestino,
                        idUsuario,
                        expediente.idUsuarioResponsable,
                        expediente.idEquipoResponsable,
                        "EXPEDIENTE_OBSERVACION",
                        null,
                        comentarioMovimiento(accionCodigo, registro.getComentario(), mensaje),
                        registro.getResultadoCodigo());
                conn.commit();
                return new EjecucionResultadoDTO(
                        registro.getIdExpediente(),
                        expediente.numeroExpediente,
                        accionCodigo,
                        ETAPA_EJECUCION,
                        estadoDestinoCodigo,
                        mensaje);
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private EjecucionResultadoDTO moverDesdeEjecucion(
            EjecucionRegistroDTO registro,
            String accionCodigo,
            String estadoOrigenCodigo,
            String etapaDestinoCodigo,
            String estadoDestinoCodigo,
            boolean requiereResolucion,
            Long idUsuario,
            String mensaje) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, registro.getIdExpediente());
                if (!ETAPA_EJECUCION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !estadoOrigenCodigo.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente ya no se encuentra en Ejecución / " + estadoOrigenCodigo + ".");
                }
                Long idResolucion = resolucionDocumentoDAO.obtenerIdResolucionActiva(conn, registro.getIdExpediente());
                if (requiereResolucion && idResolucion == null) {
                    throw new SQLException("El expediente no tiene resolución activa para ejecutar.");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        accionCodigo,
                        ETAPA_EJECUCION,
                        estadoOrigenCodigo,
                        etapaDestinoCodigo,
                        estadoDestinoCodigo);
                validarRequisitosTransicion(conn, transicion, registro.getComentario(), registro.getIdExpediente(), false);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, accionCodigo), "movimiento " + accionCodigo);
                actualizarExpediente(conn, registro.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario);
                insertarHistorial(
                        conn,
                        registro.getIdExpediente(),
                        idMovimiento,
                        expediente.idEtapa,
                        expediente.idEstado,
                        transicion.idEtapaDestino,
                        transicion.idEstadoDestino,
                        idUsuario,
                        expediente.idUsuarioResponsable,
                        expediente.idEquipoResponsable,
                        idResolucion == null ? "EXPEDIENTE" : "EXPEDIENTE_RESOLUCION",
                        idResolucion,
                        comentarioMovimiento(accionCodigo, registro.getComentario(), mensaje),
                        motivoEjecucion(registro));
                conn.commit();
                return new EjecucionResultadoDTO(
                        registro.getIdExpediente(),
                        expediente.numeroExpediente,
                        accionCodigo,
                        etapaDestinoCodigo,
                        estadoDestinoCodigo,
                        mensaje);
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private String motivoEjecucion(EjecucionRegistroDTO registro) {
        StringBuilder sb = new StringBuilder();
        if (hasText(registro.getResultadoCodigo())) {
            sb.append(registro.getResultadoCodigo());
        }
        if (registro.getFechaEjecucion() != null) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append("Fecha ejecución: ").append(registro.getFechaEjecucion());
        }
        return sb.length() == 0 ? registro.getAccionCodigo() : sb.toString();
    }

    private void insertarObservacionEjecucion(
            Connection conn,
            Long idExpediente,
            String tipoObservacionCodigo,
            String motivoCorreccionCodigo,
            String descripcion,
            Long idUsuarioCreador) throws SQLException {
        if (!hasText(descripcion)) {
            return;
        }
        Long idTipoObservacion = null;
        if (hasText(tipoObservacionCodigo)) {
            idTipoObservacion = catalogoLookupDAO.obtenerTipoObservacionId(conn, tipoObservacionCodigo);
            if (idTipoObservacion == null) {
                throw new SQLException("No se encontró el tipo de observación: " + tipoObservacionCodigo + ".");
            }
        }
        Long idMotivoCorreccion = null;
        if (hasText(motivoCorreccionCodigo)) {
            idMotivoCorreccion = catalogoLookupDAO.obtenerMotivoCorreccionId(conn, motivoCorreccionCodigo);
            if (idMotivoCorreccion == null) {
                throw new SQLException("No se encontró el motivo de corrección: " + motivoCorreccionCodigo + ".");
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
            setLongOrNull(ps, 5, idUsuarioCreador);
            ps.executeUpdate();
        }
    }

    private void validarRequisitosTransicion(
            Connection conn,
            Transicion transicion,
            String comentario,
            Long idExpediente,
            boolean requiereComentarioNegocio) throws SQLException {
        if ((transicion.requiereComentario || requiereComentarioNegocio) && !hasText(comentario)) {
            throw new SQLException("Ingrese el sustento requerido para esta acción.");
        }
        if (transicion.requiereDocumento && !tieneDocumentoSoporte(conn, idExpediente)) {
            throw new SQLException("No existe documento o resolución registrada para sustentar la acción.");
        }
    }

    private boolean tieneDocumentoSoporte(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT ("
                + "(SELECT COUNT(*) FROM expediente_documento d WHERE d.id_expediente = ? AND d.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_documento_analizado da WHERE da.id_expediente = ? AND da.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_resolucion r WHERE r.id_expediente = ? AND r.activo = 1)"
                + ") AS total FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idExpediente);
            ps.setLong(3, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
            }
        }
    }

    private void actualizarExpediente(
            Connection conn,
            Long idExpediente,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idUsuarioModificador) throws SQLException {
        String sql = "UPDATE expediente SET "
                + "id_etapa_actual = ?, id_estado_actual = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEtapaDestino);
            ps.setLong(2, idEstadoDestino);
            setLongOrNull(ps, 3, idUsuarioModificador);
            ps.setLong(4, idExpediente);
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
            Long idUsuarioOrigen,
            Long idUsuarioDestino,
            Long idEquipoDestino,
            String tablaRelacionada,
            Long idRegistroRelacionado,
            String comentario,
            String motivo) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, "
                + "id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino, "
                + "id_usuario_origen, id_usuario_destino, id_equipo_destino, "
                + "tabla_relacionada, id_registro_relacionado, comentario, motivo, activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            setLongOrNull(ps, 3, idEtapaOrigen);
            setLongOrNull(ps, 4, idEstadoOrigen);
            ps.setLong(5, idEtapaDestino);
            ps.setLong(6, idEstadoDestino);
            setLongOrNull(ps, 7, idUsuarioOrigen);
            setLongOrNull(ps, 8, idUsuarioDestino);
            setLongOrNull(ps, 9, idEquipoDestino);
            ps.setString(10, tablaRelacionada);
            setLongOrNull(ps, 11, idRegistroRelacionado);
            ps.setString(12, limitar(comentario, 2000));
            ps.setString(13, limitar(motivo, 1000));
            setLongOrNull(ps, 14, idUsuarioOrigen);
            ps.executeUpdate();
        }
    }

    private Transicion requerirTransicion(
            Connection conn,
            String accionCodigo,
            String etapaOrigenCodigo,
            String estadoOrigenCodigo,
            String etapaDestinoCodigo,
            String estadoDestinoCodigo) throws SQLException {
        String sql = "SELECT ft.id_etapa_origen, ft.id_estado_origen, ft.id_etapa_destino, ft.id_estado_destino, "
                + "ft.requiere_comentario, ft.requiere_documento "
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
                    throw new SQLException("No existe transición activa "
                            + etapaOrigenCodigo + "/" + estadoOrigenCodigo + " -> "
                            + etapaDestinoCodigo + "/" + estadoDestinoCodigo
                            + " para " + accionCodigo + " en " + CODIGO_FLUJO + ".");
                }
                return new Transicion(
                        getLongOrNull(rs, "id_etapa_origen"),
                        getLongOrNull(rs, "id_estado_origen"),
                        getLongOrNull(rs, "id_etapa_destino"),
                        getLongOrNull(rs, "id_estado_destino"),
                        rs.getInt("requiere_comentario") == 1,
                        rs.getInt("requiere_documento") == 1);
            }
        }
    }

    private ExpedienteBloqueado bloquearExpediente(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT e.id_expediente, e.numero_expediente, e.id_etapa_actual, e.id_estado_actual, "
                + "e.id_usuario_responsable_actual, e.id_equipo_responsable_actual, "
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
                return new ExpedienteBloqueado(
                        getLongOrNull(rs, "id_expediente"),
                        rs.getString("numero_expediente"),
                        getLongOrNull(rs, "id_etapa_actual"),
                        getLongOrNull(rs, "id_estado_actual"),
                        getLongOrNull(rs, "id_usuario_responsable_actual"),
                        getLongOrNull(rs, "id_equipo_responsable_actual"),
                        rs.getString("etapa_codigo"),
                        rs.getString("estado_codigo"));
            }
        }
    }

    private EjecucionExpedienteDTO map(Connection conn, ResultSet rs) throws SQLException {
        return new EjecucionExpedienteDTO(
                getLongOrNull(rs, "id_expediente"),
                rs.getString("numero_expediente"),
                rs.getString("numero_tramite_documentario"),
                rs.getString("procedimiento"),
                rs.getString("tipo_documento"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                calendarioLaboralService.calcularDiasHabilesRestantes(conn, rs.getDate("fecha_vencimiento")),
                toLocalDateTime(rs.getTimestamp("fecha_ingreso_ejecucion")),
                toLocalDateTime(rs.getTimestamp("fecha_ultimo_movimiento")),
                rs.getString("responsable"),
                rs.getString("equipo"),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getString("resultado_analisis"),
                rs.getString("fundamento_analisis"),
                rs.getString("resultado_verificacion"),
                rs.getString("ultima_observacion"),
                rs.getInt("documentos"),
                rs.getInt("relaciones_confirmadas"),
                getLongOrNull(rs, "id_expediente_resolucion"),
                rs.getString("tipo_resolucion"),
                rs.getString("numero_resolucion"),
                toLocalDate(rs.getDate("fecha_resolucion")),
                rs.getString("acciones_permitidas"));
    }

    private Long requerirId(Long value, String descripcion) throws SQLException {
        if (value == null) {
            throw new SQLException("No se encontró el catálogo requerido: " + descripcion + ".");
        }
        return value;
    }

    private static String comentarioMovimiento(String accionCodigo, String comentario, String defecto) {
        if (hasText(comentario)) {
            return comentario.trim();
        }
        return hasText(defecto) ? defecto : accionCodigo;
    }

    private static String nombrePersona(String alias) {
        return "TRIM(NVL(" + alias + ".razon_social, TRIM(NVL(" + alias + ".nombres, '') || ' ' || NVL(" + alias + ".apellidos, ''))))";
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

    private void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // El error original se conserva para el usuario.
        }
    }

    private static class Transicion {

        private final Long idEtapaOrigen;
        private final Long idEstadoOrigen;
        private final Long idEtapaDestino;
        private final Long idEstadoDestino;
        private final boolean requiereComentario;
        private final boolean requiereDocumento;

        private Transicion(
                Long idEtapaOrigen,
                Long idEstadoOrigen,
                Long idEtapaDestino,
                Long idEstadoDestino,
                boolean requiereComentario,
                boolean requiereDocumento) {
            this.idEtapaOrigen = idEtapaOrigen;
            this.idEstadoOrigen = idEstadoOrigen;
            this.idEtapaDestino = idEtapaDestino;
            this.idEstadoDestino = idEstadoDestino;
            this.requiereComentario = requiereComentario;
            this.requiereDocumento = requiereDocumento;
        }
    }

    private static class ExpedienteBloqueado {

        private final Long idExpediente;
        private final String numeroExpediente;
        private final Long idEtapa;
        private final Long idEstado;
        private final Long idUsuarioResponsable;
        private final Long idEquipoResponsable;
        private final String etapaCodigo;
        private final String estadoCodigo;

        private ExpedienteBloqueado(
                Long idExpediente,
                String numeroExpediente,
                Long idEtapa,
                Long idEstado,
                Long idUsuarioResponsable,
                Long idEquipoResponsable,
                String etapaCodigo,
                String estadoCodigo) {
            this.idExpediente = idExpediente;
            this.numeroExpediente = numeroExpediente == null ? "" : numeroExpediente;
            this.idEtapa = idEtapa;
            this.idEstado = idEstado;
            this.idUsuarioResponsable = idUsuarioResponsable;
            this.idEquipoResponsable = idEquipoResponsable;
            this.etapaCodigo = etapaCodigo == null ? "" : etapaCodigo;
            this.estadoCodigo = estadoCodigo == null ? "" : estadoCodigo;
        }
    }
}
