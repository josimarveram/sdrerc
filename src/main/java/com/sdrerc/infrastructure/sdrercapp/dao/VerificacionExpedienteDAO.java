package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CalendarioLaboralService;
import com.sdrerc.domain.dto.sdrercapp.VerificacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.VerificacionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.VerificacionResultadoDTO;
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

public class VerificacionExpedienteDAO {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String ETAPA_VERIFICACION = "VERIFICACION";
    private static final String ETAPA_ANALISIS = "ANALISIS";
    private static final String ETAPA_FIRMA = "FIRMA_EMISION";
    private static final String ETAPA_EJECUCION = "EJECUCION";
    private static final String ESTADO_EN_EJECUCION = "EN_EJECUCION";
    private static final String ESTADO_EN_VERIFICACION = "EN_VERIFICACION";
    private static final String ESTADO_REQUIERE_CORRECCION = "REQUIERE_CORRECCION";
    private static final String ESTADO_DOCUMENTO_INCONSISTENTE = "DOCUMENTO_INCONSISTENTE";
    private static final String ESTADO_VERIFICADO = "VERIFICADO";
    private static final String ESTADO_ATENDIDO = "ATENDIDO";
    private static final String ESTADO_ANALISIS_OBSERVADO = "OBSERVADO";
    private static final String ESTADO_PARA_FIRMA = "PARA_FIRMA";
    private static final String ACCION_APROBACION = "APROBACION_VERIFICACION";
    private static final String ACCION_OBSERVACION = "REGISTRO_OBSERVACION_VERIFICACION";
    private static final String ACCION_DOCUMENTO_INCONSISTENTE = "REVERSION_ESTADO_DOCUMENTO";
    private static final String ACCION_DEVOLUCION_ANALISIS = "DEVOLUCION_A_ANALISIS";
    private static final String ACCION_ENVIO_FIRMA = "ENVIO_FIRMA";

    private final CatalogoLookupDAO catalogoLookupDAO;
    private final DocumentoAnalisisDAO documentoAnalisisDAO;
    private final ObservacionExpedienteDAO observacionExpedienteDAO;
    private final CalendarioLaboralService calendarioLaboralService = new CalendarioLaboralService();

    public VerificacionExpedienteDAO() {
        this(new CatalogoLookupDAO(), new DocumentoAnalisisDAO(), new ObservacionExpedienteDAO());
    }

    public VerificacionExpedienteDAO(
            CatalogoLookupDAO catalogoLookupDAO,
            DocumentoAnalisisDAO documentoAnalisisDAO,
            ObservacionExpedienteDAO observacionExpedienteDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.documentoAnalisisDAO = documentoAnalisisDAO;
        this.observacionExpedienteDAO = observacionExpedienteDAO;
    }

    public List<VerificacionExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
        return buscarExpedientes(textoLibre, estadoCodigo, null, null, limite);
    }

    public List<VerificacionExpedienteDTO> buscarExpedientes(
            String textoLibre,
            String estadoCodigo,
            LocalDate fechaSolicitudDesde,
            LocalDate fechaSolicitudHasta,
            int limite) throws SQLException {
        List<Object> params = new ArrayList<Object>();
        boolean soportaGrupoFamiliar;
        try (Connection connSoporte = SdrercAppConnection.getConnection()) {
            soportaGrupoFamiliar = soportaGrupoFamiliar(connSoporte);
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("SELECT DISTINCT e.id_expediente, e.numero_expediente, esol.numero_expediente_sgd, e.numero_tramite_documentario, ");
        sql.append("(SELECT MAX(da2.id_documento_analizado) KEEP (DENSE_RANK LAST ORDER BY da2.fecha_documento NULLS FIRST, da2.id_documento_analizado) ");
        sql.append(" FROM expediente_documento_analizado da2 ");
        sql.append(" LEFT JOIN estado_documento ed2 ON ed2.id_estado_documento = da2.id_estado_documento ");
        sql.append(" WHERE da2.id_expediente = e.id_expediente AND da2.activo = 1 ");
        sql.append(" AND UPPER(NVL(ed2.codigo, '')) IN ('EN_DESPACHO', 'EMITIDO')) AS id_documento_pendiente, ");
        sql.append("(SELECT MAX(tda2.nombre) KEEP (DENSE_RANK LAST ORDER BY da2.fecha_documento NULLS FIRST, da2.id_documento_analizado) ");
        sql.append(" FROM expediente_documento_analizado da2 ");
        sql.append(" LEFT JOIN estado_documento ed2 ON ed2.id_estado_documento = da2.id_estado_documento ");
        sql.append(" LEFT JOIN tipo_documento_adjunto tda2 ON tda2.id_tipo_documento_adjunto = da2.id_tipo_documento_adjunto ");
        sql.append(" WHERE da2.id_expediente = e.id_expediente AND da2.activo = 1 ");
        sql.append(" AND UPPER(NVL(ed2.codigo, '')) IN ('EN_DESPACHO', 'EMITIDO')) AS tipo_documento_pendiente, ");
        sql.append("esol.asunto AS procedimiento, p.tipo_documento AS tipo_documento_titular, p.numero_documento AS numero_documento_titular, ");
        sql.append("(SELECT MIN(ed.numero_documento) KEEP (DENSE_RANK FIRST ORDER BY ed.id_expediente_documento) ");
        sql.append(" FROM expediente_documento ed WHERE ed.id_expediente = e.id_expediente ");
        sql.append(" AND TRIM(ed.numero_documento) IS NOT NULL) AS numero_documento, ");
        sql.append("(SELECT MIN(ed.nombre_documento) KEEP (DENSE_RANK FIRST ORDER BY ed.id_expediente_documento) ");
        sql.append(" FROM expediente_documento ed WHERE ed.id_expediente = e.id_expediente ");
        sql.append(" AND TRIM(ed.nombre_documento) IS NOT NULL) AS tipo_documento, ");
        sql.append("ta.nombre AS tipo_acta, ea.numero_acta, ").append(nombrePersona("p")).append(" AS titular, ");
        sql.append(nombrePersona("ps")).append(" AS solicitante, ps.tipo_documento AS solicitante_tipo_documento, ");
        sql.append("ps.numero_documento AS numero_documento_solicitante, ps.correo_electronico AS solicitante_correo, ");
        sql.append("ps.telefono AS solicitante_telefono, ps.direccion AS solicitante_direccion, ");
        sql.append("ps.departamento AS solicitante_departamento, ps.provincia AS solicitante_provincia, ps.distrito AS solicitante_distrito, ");
        sql.append("cr.nombre AS canal_ingreso, esol.observacion AS observacion_solicitud, ");
        if (soportaGrupoFamiliar) {
            sql.append("NVL(esol.grupo_familiar, 0) AS grupo_familiar, ");
            sql.append("esol.criterio_grupo_familiar, esol.observacion_grupo_familiar, ");
        } else {
            sql.append("0 AS grupo_familiar, CAST(NULL AS VARCHAR2(80)) AS criterio_grupo_familiar, ");
            sql.append("CAST(NULL AS VARCHAR2(500)) AS observacion_grupo_familiar, ");
        }
        sql.append("esol.fecha_recepcion, e.fecha_vencimiento, ");
        sql.append("e.fecha_registro, e.fecha_ultimo_movimiento, ");
        sql.append("(SELECT MAX(h.fecha_movimiento) FROM expediente_historial h ");
        sql.append("JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = h.id_tipo_movimiento ");
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 ");
        sql.append("AND tm.codigo IN ('ENVIO_VERIFICACION', 'REENVIO_VERIFICACION')) AS fecha_envio_verificacion, ");
        sql.append("ur.nombre_completo AS responsable, eq.nombre AS equipo, ");
        sql.append("(SELECT MAX(ua.nombre_completo) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) ");
        sql.append("FROM expediente_evaluacion ev LEFT JOIN usuario ua ON ua.id_usuario = ev.creado_por ");
        sql.append("WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS responsable_analisis, ");
        sql.append("(SELECT MAX(eqa.nombre) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) ");
        sql.append("FROM expediente_evaluacion ev ");
        sql.append("LEFT JOIN equipo_usuario eua ON eua.id_usuario = ev.creado_por AND eua.activo = 1 ");
        sql.append("LEFT JOIN equipo eqa ON eqa.id_equipo = eua.id_equipo AND eqa.activo = 1 ");
        sql.append("WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS equipo_analisis, ");
        sql.append("et.codigo AS etapa_codigo, est.codigo AS estado_codigo, ");
        sql.append("UPPER(NVL(").append(nombrePersona("p")).append(", 'ZZZ')) AS orden_titular, ");
        sql.append("(SELECT COUNT(*) FROM expediente_observacion o WHERE o.id_expediente = e.id_expediente AND o.subsanada = 0 AND o.activo = 1) AS observaciones_pendientes, ");
        sql.append("(SELECT COUNT(*) FROM expediente_relacion r WHERE r.activo = 1 AND (r.id_expediente_principal = e.id_expediente OR r.id_expediente_relacionado = e.id_expediente)) AS relaciones_confirmadas, ");
        sql.append("(SELECT COUNT(*) FROM expediente_documento_analizado da WHERE da.id_expediente = e.id_expediente AND da.activo = 1) AS documentos_analizados, ");
        sql.append("(SELECT MAX(NVL(tre.nombre, tre.codigo)) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) ");
        sql.append("FROM expediente_evaluacion ev LEFT JOIN tipo_resultado_evaluacion tre ON tre.id_tipo_resultado_evaluacion = ev.id_tipo_resultado_evaluacion ");
        sql.append("WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS ultimo_resultado, ");
        sql.append("(SELECT MAX(ev.fundamento) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) ");
        sql.append("FROM expediente_evaluacion ev WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS fundamento_analisis, ");
        sql.append("(SELECT MAX(o.descripcion) KEEP (DENSE_RANK LAST ORDER BY o.fecha_observacion) ");
        sql.append("FROM expediente_observacion o WHERE o.id_expediente = e.id_expediente AND o.activo = 1 ");
        sql.append("AND UPPER(o.origen_observacion) = 'VERIFICACION') AS ultima_observacion_verificacion, ");
        sql.append("NVL(e.requiere_publicacion, 0) AS requiere_publicacion, ");
        sql.append("(SELECT MAX(pub.fecha_publicacion) KEEP (DENSE_RANK LAST ORDER BY pub.creado_en, pub.id_expediente_publicacion) ");
        sql.append("FROM expediente_publicacion pub WHERE pub.id_expediente = e.id_expediente AND pub.activo = 1) AS fecha_publicacion, ");
        sql.append("tr.nombre AS tipo_documento_emitido, res.numero_resolucion AS numero_documento_emitido, ");
        sql.append("res.fecha_resolucion AS fecha_documento_emitido, res.fecha_firma AS fecha_firma_documento, ");
        sql.append("(SELECT COUNT(*) FROM expediente_documento_analizado da ");
        sql.append("LEFT JOIN tipo_documento_adjunto tda ON tda.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto ");
        sql.append("WHERE da.id_expediente = e.id_expediente AND da.activo = 1 ");
        sql.append("AND (UPPER(NVL(tda.codigo, '')) LIKE '%EDICTO%' OR UPPER(NVL(tda.nombre, '')) LIKE '%EDICTO%')) AS cartas_edicto, ");
        sql.append("CASE WHEN EXISTS (");
        sql.append("SELECT 1 FROM flujo f JOIN flujo_transicion ft ON ft.id_flujo = f.id_flujo ");
        sql.append("JOIN etapa_expediente edn ON edn.id_etapa = ft.id_etapa_destino ");
        sql.append("JOIN estado_expediente sdn ON sdn.id_estado = ft.id_estado_destino ");
        sql.append("WHERE f.codigo = 'SDRERC_TO_BE' AND f.activo = 1 AND ft.activo = 1 ");
        sql.append("AND ft.id_etapa_origen = e.id_etapa_actual AND ft.id_estado_origen = e.id_estado_actual ");
        sql.append("AND ft.codigo_accion = 'DERIVACION_A_NOTIFICACION' ");
        sql.append("AND edn.codigo = 'NOTIFICACION' AND sdn.codigo = 'EN_NOTIFICACION'");
        sql.append(") THEN 1 ELSE 0 END AS puede_derivar_notificacion ");
        sql.append("FROM expediente e ");
        sql.append("JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual ");
        sql.append("JOIN estado_expediente est ON est.id_estado = e.id_estado_actual ");
        sql.append("LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual ");
        sql.append("LEFT JOIN equipo eq ON eq.id_equipo = e.id_equipo_responsable_actual ");
        sql.append("LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 ");
        sql.append("LEFT JOIN canal_recepcion cr ON cr.id_canal_recepcion = esol.id_canal_recepcion ");
        sql.append("LEFT JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1 ");
        sql.append("LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta ");
        sql.append("LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' ");
        sql.append("LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 ");
        sql.append("LEFT JOIN persona ps ON ps.id_persona = esol.id_persona_solicitante AND ps.activo = 1 ");
        sql.append("LEFT JOIN (SELECT id_expediente, ");
        sql.append("MAX(id_expediente_resolucion) KEEP (DENSE_RANK LAST ORDER BY creado_en, id_expediente_resolucion) AS id_expediente_resolucion ");
        sql.append("FROM expediente_resolucion WHERE activo = 1 GROUP BY id_expediente) res_pick ");
        sql.append("ON res_pick.id_expediente = e.id_expediente ");
        sql.append("LEFT JOIN expediente_resolucion res ON res.id_expediente_resolucion = res_pick.id_expediente_resolucion ");
        sql.append("LEFT JOIN tipo_resolucion tr ON tr.id_tipo_resolucion = res.id_tipo_resolucion ");
        sql.append("WHERE e.activo = 1 AND (et.codigo IN (?, ?) OR (et.codigo = ? AND est.codigo = ?)) ");
        params.add(ETAPA_VERIFICACION);
        params.add(ETAPA_FIRMA);
        params.add(ETAPA_ANALISIS);
        params.add(ESTADO_ATENDIDO);

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
            sql.append("OR UPPER(NVL(e.numero_tramite_documentario, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(esol.asunto, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(ea.numero_acta, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(p.numero_documento, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(").append(nombrePersona("p")).append(", '')) LIKE ? ");
            sql.append("OR UPPER(NVL(esol.numero_expediente_sgd, '')) LIKE ? ");
            sql.append(") ");
            for (int i = 0; i < 7; i++) {
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
                List<VerificacionExpedienteDTO> expedientes = new ArrayList<VerificacionExpedienteDTO>();
                while (rs.next()) {
                    expedientes.add(map(conn, rs));
                }
                return expedientes;
            }
        }
    }

    public VerificacionResultadoDTO registrarVerificacion(VerificacionRegistroDTO registro, Long idUsuario) throws SQLException {
        if (ACCION_APROBACION.equalsIgnoreCase(registro.getAccionCodigo())) {
            return aprobarVerificacion(registro.getIdExpediente(), registro.getComentario(), idUsuario);
        }
        if (ACCION_OBSERVACION.equalsIgnoreCase(registro.getAccionCodigo())) {
            return registrarObservacion(
                    registro,
                    ACCION_OBSERVACION,
                    ESTADO_REQUIERE_CORRECCION,
                    "La observación de verificación fue registrada correctamente.",
                    idUsuario);
        }
        if (ACCION_DOCUMENTO_INCONSISTENTE.equalsIgnoreCase(registro.getAccionCodigo())) {
            return registrarObservacion(
                    registro,
                    ACCION_DOCUMENTO_INCONSISTENTE,
                    ESTADO_DOCUMENTO_INCONSISTENTE,
                    "El documento inconsistente fue registrado correctamente.",
                    idUsuario);
        }
        throw new SQLException("La acción de verificación no está soportada: " + registro.getAccionCodigo() + ".");
    }

    public VerificacionResultadoDTO aprobarVerificacion(Long idExpediente, String comentario, Long idUsuario) throws SQLException {
        return moverExpediente(
                idExpediente,
                ACCION_APROBACION,
                ETAPA_VERIFICACION,
                ESTADO_EN_VERIFICACION,
                ETAPA_VERIFICACION,
                ESTADO_VERIFICADO,
                comentario,
                idUsuario,
                true,
                "La verificación fue aprobada correctamente.");
    }

    public VerificacionResultadoDTO aprobarVerificacionDirecta(Long idExpediente, String comentario, Long idUsuario) throws SQLException {
        return moverExpediente(
                idExpediente,
                ACCION_APROBACION,
                ETAPA_VERIFICACION,
                ESTADO_EN_VERIFICACION,
                ETAPA_EJECUCION,
                ESTADO_EN_EJECUCION,
                comentario,
                idUsuario,
                true,
                "El expediente fue aprobado y enviado a Ejecución.");
    }

    public VerificacionResultadoDTO registrarObservacionYDevolverAnalisis(
            VerificacionRegistroDTO registro, Long idUsuario) throws SQLException {
        registrarObservacion(
                registro,
                ACCION_OBSERVACION,
                ESTADO_REQUIERE_CORRECCION,
                "La observación de verificación fue registrada correctamente.",
                idUsuario);
        VerificacionRegistroDTO devolucion = new VerificacionRegistroDTO(
                registro.getIdExpediente(),
                ACCION_DEVOLUCION_ANALISIS,
                registro.getResultadoNombre(),
                registro.getComentario(),
                registro.getObservacion());
        return devolverAnalisis(devolucion, idUsuario);
    }

    public VerificacionResultadoDTO aprobarVerificacionConDestino(
            Long idExpediente,
            String comentario,
            Long idEquipoDestino,
            Long idUsuarioDestino,
            Long idUsuario) throws SQLException {
        if (idEquipoDestino == null || idUsuarioDestino == null) {
            throw new SQLException("Seleccione equipo destino y usuario destino para registrar la verificación.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, idExpediente);
                if (!ETAPA_VERIFICACION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !ESTADO_EN_VERIFICACION.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente ya no se encuentra en "
                            + ETAPA_VERIFICACION + " / " + ESTADO_EN_VERIFICACION + ".");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_APROBACION,
                        ETAPA_VERIFICACION,
                        ESTADO_EN_VERIFICACION,
                        ETAPA_VERIFICACION,
                        ESTADO_VERIFICADO);
                validarRequisitosTransicion(conn, transicion, comentario, idExpediente, true);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_APROBACION), "movimiento " + ACCION_APROBACION);
                actualizarExpedienteConDestino(
                        conn,
                        idExpediente,
                        transicion.idEtapaDestino,
                        transicion.idEstadoDestino,
                        idEquipoDestino,
                        idUsuarioDestino,
                        idUsuario);
                insertarHistorial(
                        conn,
                        idExpediente,
                        idMovimiento,
                        expediente.idEtapa,
                        expediente.idEstado,
                        transicion.idEtapaDestino,
                        transicion.idEstadoDestino,
                        idUsuario,
                        idUsuarioDestino,
                        idEquipoDestino,
                        null,
                        null,
                        comentarioMovimiento(ACCION_APROBACION, comentario),
                        ACCION_APROBACION);
                conn.commit();
                return new VerificacionResultadoDTO(
                        idExpediente,
                        expediente.numeroExpediente,
                        ACCION_APROBACION,
                        ETAPA_VERIFICACION,
                        ESTADO_VERIFICADO,
                        "La verificación fue aprobada y enviada al equipo destino correctamente.");
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

    public VerificacionResultadoDTO enviarFirma(Long idExpediente, String comentario, Long idUsuario) throws SQLException {
        return moverExpediente(
                idExpediente,
                ACCION_ENVIO_FIRMA,
                ETAPA_VERIFICACION,
                ESTADO_VERIFICADO,
                ETAPA_FIRMA,
                ESTADO_PARA_FIRMA,
                comentario,
                idUsuario,
                true,
                "El expediente continúa con la preparación del documento emitido.");
    }

    public VerificacionResultadoDTO devolverAnalisis(VerificacionRegistroDTO registro, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, registro.getIdExpediente());
                if (!ETAPA_VERIFICACION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !(ESTADO_REQUIERE_CORRECCION.equalsIgnoreCase(expediente.estadoCodigo)
                        || ESTADO_DOCUMENTO_INCONSISTENTE.equalsIgnoreCase(expediente.estadoCodigo))) {
                    throw new SQLException("El expediente debe estar en corrección o documento inconsistente para devolverlo a Análisis.");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_DEVOLUCION_ANALISIS,
                        ETAPA_VERIFICACION,
                        expediente.estadoCodigo,
                        ETAPA_ANALISIS,
                        ESTADO_ANALISIS_OBSERVADO);
                validarRequisitosTransicion(conn, transicion, registro.getComentario(), registro.getIdExpediente(), true);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_DEVOLUCION_ANALISIS), "movimiento " + ACCION_DEVOLUCION_ANALISIS);
                observacionExpedienteDAO.insertarObservacionVerificacion(conn, registro.getIdExpediente(), registro.getObservacion(), idUsuario);
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
                        comentarioMovimiento(ACCION_DEVOLUCION_ANALISIS, registro.getComentario()),
                        registro.getAccionCodigo());
                conn.commit();
                return new VerificacionResultadoDTO(
                        registro.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_DEVOLUCION_ANALISIS,
                        ETAPA_ANALISIS,
                        ESTADO_ANALISIS_OBSERVADO,
                        "El expediente fue devuelto a Análisis para corrección.");
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

    private VerificacionResultadoDTO registrarObservacion(
            VerificacionRegistroDTO registro,
            String accionCodigo,
            String estadoDestinoCodigo,
            String mensaje,
            Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, registro.getIdExpediente());
                if (!ETAPA_VERIFICACION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !ESTADO_EN_VERIFICACION.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente debe estar En verificación para registrar observaciones.");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        accionCodigo,
                        ETAPA_VERIFICACION,
                        ESTADO_EN_VERIFICACION,
                        ETAPA_VERIFICACION,
                        estadoDestinoCodigo);
                validarRequisitosTransicion(conn, transicion, registro.getComentario(), registro.getIdExpediente(), true);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, accionCodigo), "movimiento " + accionCodigo);
                observacionExpedienteDAO.insertarObservacionVerificacion(conn, registro.getIdExpediente(), registro.getObservacion(), idUsuario);
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
                        comentarioMovimiento(accionCodigo, registro.getComentario()),
                        registro.getResultadoNombre());
                conn.commit();
                return new VerificacionResultadoDTO(
                        registro.getIdExpediente(),
                        expediente.numeroExpediente,
                        accionCodigo,
                        ETAPA_VERIFICACION,
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

    private VerificacionResultadoDTO moverExpediente(
            Long idExpediente,
            String accionCodigo,
            String etapaOrigenCodigo,
            String estadoOrigenCodigo,
            String etapaDestinoCodigo,
            String estadoDestinoCodigo,
            String comentario,
            Long idUsuario,
            boolean exigirEvaluacion,
            String mensaje) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, idExpediente);
                if (!etapaOrigenCodigo.equalsIgnoreCase(expediente.etapaCodigo)
                        || !estadoOrigenCodigo.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente ya no se encuentra en "
                            + etapaOrigenCodigo + " / " + estadoOrigenCodigo + ".");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        accionCodigo,
                        etapaOrigenCodigo,
                        estadoOrigenCodigo,
                        etapaDestinoCodigo,
                        estadoDestinoCodigo);
                validarRequisitosTransicion(conn, transicion, comentario, idExpediente, exigirEvaluacion);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, accionCodigo), "movimiento " + accionCodigo);
                actualizarExpediente(conn, idExpediente, transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario);
                insertarHistorial(
                        conn,
                        idExpediente,
                        idMovimiento,
                        expediente.idEtapa,
                        expediente.idEstado,
                        transicion.idEtapaDestino,
                        transicion.idEstadoDestino,
                        idUsuario,
                        expediente.idUsuarioResponsable,
                        expediente.idEquipoResponsable,
                        null,
                        null,
                        comentarioMovimiento(accionCodigo, comentario),
                        accionCodigo);
                conn.commit();
                return new VerificacionResultadoDTO(
                        idExpediente,
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

    private void validarRequisitosTransicion(
            Connection conn,
            Transicion transicion,
            String comentario,
            Long idExpediente,
            boolean exigirEvaluacion) throws SQLException {
        if (transicion.requiereComentario && !hasText(comentario)) {
            throw new SQLException("Ingrese el sustento requerido para esta acción.");
        }
        if (exigirEvaluacion && !tieneEvaluacionActiva(conn, idExpediente)) {
            throw new SQLException("No existe análisis registrado para el expediente seleccionado.");
        }
        if (transicion.requiereDocumento && documentoAnalisisDAO.contarPorExpediente(conn, idExpediente) <= 0) {
            throw new SQLException("No existen documentos analizados para sustentar la verificación.");
        }
    }

    private VerificacionExpedienteDTO map(Connection conn, ResultSet rs) throws SQLException {
        return new VerificacionExpedienteDTO(
                getLongOrNull(rs, "id_expediente"),
                rs.getString("numero_expediente"),
                rs.getString("numero_expediente_sgd"),
                rs.getString("numero_tramite_documentario"),
                rs.getString("procedimiento"),
                rs.getString("numero_documento"),
                rs.getString("tipo_documento"),
                rs.getString("numero_documento_titular"),
                rs.getString("tipo_documento_titular"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                rs.getString("solicitante"),
                rs.getString("solicitante_tipo_documento"),
                rs.getString("numero_documento_solicitante"),
                rs.getString("solicitante_correo"),
                rs.getString("solicitante_telefono"),
                rs.getString("solicitante_departamento"),
                rs.getString("solicitante_provincia"),
                rs.getString("solicitante_distrito"),
                rs.getString("solicitante_direccion"),
                rs.getString("canal_ingreso"),
                rs.getString("observacion_solicitud"),
                rs.getInt("grupo_familiar") == 1,
                rs.getString("criterio_grupo_familiar"),
                rs.getString("observacion_grupo_familiar"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                toLocalDate(rs.getDate("fecha_vencimiento")),
                calendarioLaboralService.calcularDiasHabilesRestantes(conn, rs.getDate("fecha_vencimiento")),
                toLocalDateTime(rs.getTimestamp("fecha_envio_verificacion")),
                toLocalDateTime(rs.getTimestamp("fecha_ultimo_movimiento")),
                rs.getString("responsable"),
                rs.getString("equipo"),
                rs.getString("responsable_analisis"),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getInt("observaciones_pendientes") > 0,
                rs.getInt("relaciones_confirmadas"),
                rs.getInt("documentos_analizados"),
                rs.getString("ultimo_resultado"),
                rs.getString("fundamento_analisis"),
                rs.getString("ultima_observacion_verificacion"),
                rs.getInt("requiere_publicacion") == 1,
                toLocalDate(rs.getDate("fecha_publicacion")),
                rs.getString("tipo_documento_emitido"),
                rs.getString("numero_documento_emitido"),
                toLocalDate(rs.getDate("fecha_documento_emitido")),
                toLocalDateTime(rs.getTimestamp("fecha_firma_documento")),
                rs.getInt("cartas_edicto") > 0,
                rs.getInt("puede_derivar_notificacion") == 1,
                getLongOrNull(rs, "id_documento_pendiente"),
                rs.getString("tipo_documento_pendiente"),
                rs.getString("equipo_analisis"));
    }

    private static boolean soportaGrupoFamiliar(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_SOLICITUD' AND column_name = 'GRUPO_FAMILIAR'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private boolean tieneEvaluacionActiva(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expediente_evaluacion WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
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

    private void actualizarExpedienteConDestino(
            Connection conn,
            Long idExpediente,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idEquipoDestino,
            Long idUsuarioDestino,
            Long idUsuarioModificador) throws SQLException {
        String sql = "UPDATE expediente SET "
                + "id_etapa_actual = ?, id_estado_actual = ?, "
                + "id_equipo_responsable_actual = ?, id_usuario_responsable_actual = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEtapaDestino);
            ps.setLong(2, idEstadoDestino);
            ps.setLong(3, idEquipoDestino);
            ps.setLong(4, idUsuarioDestino);
            setLongOrNull(ps, 5, idUsuarioModificador);
            ps.setLong(6, idExpediente);
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

    private Long requerirId(Long value, String descripcion) throws SQLException {
        if (value == null) {
            throw new SQLException("No se encontró el catálogo requerido: " + descripcion + ".");
        }
        return value;
    }

    private static String comentarioMovimiento(String accionCodigo, String comentario) {
        if (hasText(comentario)) {
            return comentario.trim();
        }
        if (ACCION_APROBACION.equals(accionCodigo)) {
            return "Verificación aprobada.";
        }
        if (ACCION_ENVIO_FIRMA.equals(accionCodigo)) {
            return "Expediente preparado para emisión documental.";
        }
        if (ACCION_OBSERVACION.equals(accionCodigo)) {
            return "Observación de verificación registrada.";
        }
        if (ACCION_DOCUMENTO_INCONSISTENTE.equals(accionCodigo)) {
            return "Documento inconsistente registrado en verificación.";
        }
        if (ACCION_DEVOLUCION_ANALISIS.equals(accionCodigo)) {
            return "Devolución a Análisis por corrección.";
        }
        return accionCodigo;
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
            // El error original se reporta al usuario; el rollback fallido no debe ocultarlo.
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
