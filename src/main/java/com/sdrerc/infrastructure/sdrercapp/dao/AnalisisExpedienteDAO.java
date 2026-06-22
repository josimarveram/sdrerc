package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CalendarioLaboralService;
import com.sdrerc.domain.dto.sdrercapp.AnalisisDetalleDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ObservacionAnalisisDTO;
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

public class AnalisisExpedienteDAO {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String ETAPA_ASIGNACION = "ASIGNACION";
    private static final String ETAPA_ANALISIS = "ANALISIS";
    private static final String ETAPA_VERIFICACION = "VERIFICACION";
    private static final String ETAPA_NOTIFICACION = "NOTIFICACION";
    private static final String ETAPA_CIERRE_ARCHIVO = "CIERRE_ARCHIVO";
    private static final String ESTADO_ASIGNADO = "ASIGNADO";
    private static final String ESTADO_RECIBIDO = "RECIBIDO_POR_ABOGADO";
    private static final String ESTADO_ATENDIDO = "ATENDIDO";
    private static final String ESTADO_OBSERVADO = "OBSERVADO";
    private static final String ESTADO_SUBSANADO = "SUBSANADO";
    private static final String ESTADO_NO_CORRESPONDE = "NO_CORRESPONDE";
    private static final String ESTADO_EN_ABANDONO = "EN_ABANDONO";
    private static final String ESTADO_OBSERVACION_ADMINISTRATIVA = "OBSERVACION_ADMINISTRATIVA";
    private static final String ESTADO_EN_VERIFICACION = "EN_VERIFICACION";
    private static final String ESTADO_EN_NOTIFICACION = "EN_NOTIFICACION";
    private static final String ESTADO_ARCHIVADO = "ARCHIVADO";
    private static final String ACCION_RECEPCION = "RECEPCION_ASIGNACION";
    private static final String ACCION_REGISTRO_ANALISIS = "REGISTRO_RESULTADO_ANALISIS";
    private static final String ACCION_ENVIO_VERIFICACION = "ENVIO_VERIFICACION";
    private static final String ACCION_REENVIO_VERIFICACION = "REENVIO_VERIFICACION";
    private static final String ACCION_DERIVACION_NOTIFICACION = "DERIVACION_A_NOTIFICACION";
    private static final String ACCION_ARCHIVO = "ARCHIVO";
    private static final String TIPO_DOCUMENTO_PROVEIDO = "PROVEIDO";
    private static final String TIPO_RELACION_DOCUMENTO_DUPLICADO = "DOCUMENTO_DUPLICADO_ASOCIADO";
    private static final String TIPO_RELACION_MISMA_ACTA_TITULAR = "MISMA_ACTA_TITULAR";

    private final CatalogoLookupDAO catalogoLookupDAO;
    private final DocumentoAnalisisDAO documentoAnalisisDAO;
    private final ObservacionExpedienteDAO observacionExpedienteDAO;
    private final CalendarioLaboralService calendarioLaboralService = new CalendarioLaboralService();

    public AnalisisExpedienteDAO() {
        this(new CatalogoLookupDAO(), new DocumentoAnalisisDAO(), new ObservacionExpedienteDAO());
    }

    public AnalisisExpedienteDAO(
            CatalogoLookupDAO catalogoLookupDAO,
            DocumentoAnalisisDAO documentoAnalisisDAO,
            ObservacionExpedienteDAO observacionExpedienteDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.documentoAnalisisDAO = documentoAnalisisDAO;
        this.observacionExpedienteDAO = observacionExpedienteDAO;
    }

    public List<AnalisisExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
        return buscarExpedientes(textoLibre, estadoCodigo, null, null, limite);
    }

    public AnalisisDetalleDTO obtenerAnalisisRegistrado(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return analisisVacio();
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            EvaluacionRegistrada evaluacion = obtenerEvaluacionRegistrada(conn, idExpediente);
            List<DocumentoAnalizadoDTO> documentos = documentoAnalisisDAO.listarPorExpediente(conn, idExpediente);
            ObservacionAnalisisDTO observacion = obtenerUltimaObservacionAnalisis(conn, idExpediente);
            String numeroProveido = obtenerUltimoNumeroProveido(conn, idExpediente);
            if (evaluacion == null) {
                return new AnalisisDetalleDTO(
                        false, "", "", null, null, false, false, false, "",
                        "", "", numeroProveido, null, observacion, documentos);
            }
            String resultadoCodigo = resolverResultadoRegistrado(evaluacion);
            String resultadoNombre = resolverNombreResultadoRegistrado(evaluacion, resultadoCodigo);
            return new AnalisisDetalleDTO(
                    true,
                    resultadoCodigo,
                    resultadoNombre,
                    evaluacion.corresponde,
                    evaluacion.incorporado,
                    evaluacion.requiereReconstitucion,
                    evaluacion.tieneLegitimidad,
                    evaluacion.cumpleMediosProbatorios,
                    evaluacion.fundamento,
                    evaluacion.motivoNoCorrespondeCodigo,
                    evaluacion.motivoNoCorrespondeNombre,
                    numeroProveido,
                    evaluacion.fechaEvaluacion,
                    observacion,
                    documentos);
        }
    }

    public List<AnalisisExpedienteDTO> buscarExpedientes(
            String textoLibre,
            String estadoCodigo,
            LocalDate fechaSolicitudDesde,
            LocalDate fechaSolicitudHasta,
            int limite) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("SELECT DISTINCT e.id_expediente, e.numero_expediente, e.numero_tramite_documentario, ");
        sql.append("esol.asunto AS procedimiento, p.tipo_documento, p.numero_documento AS numero_documento_titular, ");
        sql.append("ta.nombre AS tipo_acta, ea.numero_acta, ").append(nombrePersona("p")).append(" AS titular, ");
        sql.append("esol.fecha_recepcion, e.fecha_vencimiento, ");
        sql.append("e.fecha_registro, asig.fecha_asignacion, e.fecha_ultimo_movimiento, ");
        sql.append("ur.nombre_completo AS responsable, eq.nombre AS equipo, ");
        sql.append("et.codigo AS etapa_codigo, est.codigo AS estado_codigo, ");
        sql.append("UPPER(NVL(").append(nombrePersona("p")).append(", 'ZZZ')) AS orden_titular, ");
        sql.append("(SELECT COUNT(*) FROM expediente_observacion o WHERE o.id_expediente = e.id_expediente AND o.subsanada = 0 AND o.activo = 1) AS observaciones_pendientes, ");
        sql.append("(SELECT COUNT(*) FROM expediente_relacion r WHERE r.activo = 1 AND (r.id_expediente_principal = e.id_expediente OR r.id_expediente_relacionado = e.id_expediente)) AS relaciones_confirmadas, ");
        sql.append("(SELECT COUNT(*) FROM expediente_documento_analizado da WHERE da.id_expediente = e.id_expediente AND da.activo = 1) AS documentos_analizados, ");
        sql.append("(SELECT MAX(tre.codigo) KEEP (DENSE_RANK LAST ORDER BY ev.creado_en) ");
        sql.append("FROM expediente_evaluacion ev LEFT JOIN tipo_resultado_evaluacion tre ON tre.id_tipo_resultado_evaluacion = ev.id_tipo_resultado_evaluacion ");
        sql.append("WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS ultimo_resultado ");
        sql.append("FROM expediente e ");
        sql.append("JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual ");
        sql.append("JOIN estado_expediente est ON est.id_estado = e.id_estado_actual ");
        sql.append("LEFT JOIN expediente_asignacion asig ON asig.id_expediente = e.id_expediente AND asig.activa = 1 AND asig.activo = 1 ");
        sql.append("LEFT JOIN usuario ur ON ur.id_usuario = e.id_usuario_responsable_actual ");
        sql.append("LEFT JOIN equipo eq ON eq.id_equipo = e.id_equipo_responsable_actual ");
        sql.append("LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 ");
        sql.append("LEFT JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1 ");
        sql.append("LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta ");
        sql.append("LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' ");
        sql.append("LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 ");
        sql.append("WHERE e.activo = 1 ");
        sql.append("AND (");
        sql.append("(et.codigo = ? AND est.codigo = ?) ");
        params.add(ETAPA_ASIGNACION);
        params.add(ESTADO_ASIGNADO);
        sql.append("OR et.codigo = ?");
        params.add(ETAPA_ANALISIS);
        sql.append(") ");

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
                List<AnalisisExpedienteDTO> expedientes = new ArrayList<>();
                while (rs.next()) {
                    expedientes.add(map(conn, rs));
                }
                return expedientes;
            }
        }
    }

    public AnalisisResultadoDTO recibirExpediente(Long idExpediente, String comentario, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado principal = bloquearExpediente(conn, idExpediente);
                validarPendienteRecepcion(principal);
                Transicion transicion = requerirTransicionRecepcion(conn);
                Long idMovimiento = requerirId(
                        catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_RECEPCION),
                        "movimiento " + ACCION_RECEPCION);

                recibirExpedienteAsignado(
                        conn,
                        principal,
                        transicion,
                        idMovimiento,
                        comentarioMovimiento(ACCION_RECEPCION, comentario),
                        idUsuario);

                int asociadosRecibidos = 0;
                for (Long idAsociado : listarIdsAsociadosConfirmados(conn, idExpediente)) {
                    ExpedienteBloqueado asociado = bloquearExpediente(conn, idAsociado);
                    if (estaRecibidoEnAnalisis(asociado)) {
                        continue;
                    }
                    if (!estaPendienteRecepcion(asociado)) {
                        continue;
                    }
                    recibirExpedienteAsignado(
                            conn,
                            asociado,
                            transicion,
                            idMovimiento,
                            "Recepción automática junto con el expediente principal "
                                    + principal.numeroExpediente + ".",
                            idUsuario);
                    asociadosRecibidos++;
                }

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                String mensaje = "El expediente fue recibido para análisis.";
                if (asociadosRecibidos > 0) {
                    mensaje += " " + asociadosRecibidos
                            + " documento(s) asociado(s) también fueron recibidos.";
                }
                return new AnalisisResultadoDTO(
                        idExpediente,
                        principal.numeroExpediente,
                        ACCION_RECEPCION,
                        ETAPA_ANALISIS,
                        ESTADO_RECIBIDO,
                        mensaje);
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

    public AnalisisResultadoDTO recibirDocumentoAsociado(
            Long idExpedientePrincipal,
            Long idExpedienteAsociado,
            String comentario,
            Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado principal = bloquearExpediente(conn, idExpedientePrincipal);
                if (!ETAPA_ANALISIS.equalsIgnoreCase(principal.etapaCodigo)) {
                    throw new SQLException("El expediente principal debe encontrarse en la etapa Análisis.");
                }
                if (!existeRelacionConfirmada(conn, idExpedientePrincipal, idExpedienteAsociado)) {
                    throw new SQLException("El documento seleccionado ya no está asociado al expediente principal.");
                }

                ExpedienteBloqueado asociado = bloquearExpediente(conn, idExpedienteAsociado);
                if (estaRecibidoEnAnalisis(asociado)) {
                    throw new SQLException("El documento asociado ya fue recibido por el abogado.");
                }
                validarPendienteRecepcion(asociado);
                if (idUsuario == null
                        || asociado.idUsuarioResponsable == null
                        || !idUsuario.equals(asociado.idUsuarioResponsable)) {
                    throw new SQLException("Solo el abogado responsable puede registrar la recepción.");
                }

                Transicion transicion = requerirTransicionRecepcion(conn);
                Long idMovimiento = requerirId(
                        catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_RECEPCION),
                        "movimiento " + ACCION_RECEPCION);
                recibirExpedienteAsignado(
                        conn,
                        asociado,
                        transicion,
                        idMovimiento,
                        hasText(comentario)
                                ? comentario.trim()
                                : "Recepción de documento asociado al expediente principal "
                                        + principal.numeroExpediente + ".",
                        idUsuario);

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new AnalisisResultadoDTO(
                        idExpedienteAsociado,
                        asociado.numeroExpediente,
                        ACCION_RECEPCION,
                        ETAPA_ANALISIS,
                        ESTADO_RECIBIDO,
                        "El documento asociado fue recibido para análisis.");
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

    public AnalisisResultadoDTO registrarAnalisis(AnalisisRegistroDTO registro, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, registro.getIdExpediente());
                if (!ETAPA_ANALISIS.equalsIgnoreCase(expediente.etapaCodigo)
                        || !(ESTADO_RECIBIDO.equalsIgnoreCase(expediente.estadoCodigo)
                        || ESTADO_OBSERVADO.equalsIgnoreCase(expediente.estadoCodigo)
                        || ESTADO_SUBSANADO.equalsIgnoreCase(expediente.estadoCodigo))) {
                    throw new SQLException("El expediente debe estar recibido, observado o subsanado en la etapa Análisis.");
                }

                ResultadoDestino destino = resolverDestinoResultado(registro);
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_REGISTRO_ANALISIS,
                        expediente.etapaCodigo,
                        expediente.estadoCodigo,
                        ETAPA_ANALISIS,
                        destino.estadoDestinoCodigo);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_REGISTRO_ANALISIS), "movimiento REGISTRO_RESULTADO_ANALISIS");
                Long idResultado = destino.requiereResultadoCatalogo
                        ? requerirId(catalogoLookupDAO.obtenerTipoResultadoEvaluacionId(conn, registro.getResultadoCodigo()), "resultado " + registro.getResultadoCodigo())
                        : null;
                Long idMotivoNoCorresponde = hasText(registro.getMotivoNoCorrespondeCodigo())
                        ? catalogoLookupDAO.obtenerMotivoNoCorrespondeId(conn, registro.getMotivoNoCorrespondeCodigo())
                        : null;

                Long idEvaluacion = guardarEvaluacion(conn, registro, idResultado, idMotivoNoCorresponde, idUsuario);
                registrarProveidoSiInformado(conn, registro, idUsuario);
                for (DocumentoAnalizadoDTO documento : registro.getDocumentosAnalizados()) {
                    if (documento.getIdDocumentoAnalizado() == null) {
                        documentoAnalisisDAO.insertarDocumentoAnalizado(conn, registro.getIdExpediente(), documento, idUsuario);
                    } else {
                        documentoAnalisisDAO.actualizarDocumentoAnalizado(
                                conn,
                                registro.getIdExpediente(),
                                documento,
                                idUsuario);
                    }
                }
                observacionExpedienteDAO.insertarObservacion(conn, registro.getIdExpediente(), registro.getObservacion(), idUsuario);
                actualizarExpediente(conn, registro.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, null, null, idUsuario, false);
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
                        "EXPEDIENTE_EVALUACION",
                        idEvaluacion,
                        comentarioAnalisis(registro),
                        registro.getResultadoCodigo());
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new AnalisisResultadoDTO(
                        registro.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_REGISTRO_ANALISIS,
                        ETAPA_ANALISIS,
                        destino.estadoDestinoCodigo,
                        "El análisis fue registrado correctamente.");
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

    public AnalisisResultadoDTO enviarVerificacion(Long idExpediente, String comentario, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            ExpedienteBloqueado expediente = obtenerExpediente(conn, idExpediente);
            String accion = ESTADO_SUBSANADO.equalsIgnoreCase(expediente.estadoCodigo)
                    ? ACCION_REENVIO_VERIFICACION
                    : ACCION_ENVIO_VERIFICACION;
            if (!ETAPA_ANALISIS.equalsIgnoreCase(expediente.etapaCodigo)
                    || !(ESTADO_ATENDIDO.equalsIgnoreCase(expediente.estadoCodigo)
                    || ESTADO_SUBSANADO.equalsIgnoreCase(expediente.estadoCodigo))) {
                throw new SQLException("El expediente debe estar Atendido o Subsanado para enviarlo a verificación.");
            }
            if (!tieneEvaluacionActiva(conn, idExpediente)) {
                throw new SQLException("Registre el análisis antes de enviar a verificación.");
            }
            if (documentoAnalisisDAO.contarPorExpediente(conn, idExpediente) <= 0) {
                throw new SQLException("Registre al menos un documento analizado antes de enviar a verificación.");
            }
            return moverExpediente(
                    idExpediente,
                    accion,
                    ETAPA_ANALISIS,
                    expediente.estadoCodigo,
                    ETAPA_VERIFICACION,
                    ESTADO_EN_VERIFICACION,
                    comentario,
                    idUsuario,
                    false,
                    false);
        }
    }

    public AnalisisResultadoDTO derivarNotificacionEspecial(Long idExpediente, String comentario, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            ExpedienteBloqueado expediente = obtenerExpediente(conn, idExpediente);
            if (!ETAPA_ANALISIS.equalsIgnoreCase(expediente.etapaCodigo)
                    || !(ESTADO_EN_ABANDONO.equalsIgnoreCase(expediente.estadoCodigo)
                    || ESTADO_OBSERVACION_ADMINISTRATIVA.equalsIgnoreCase(expediente.estadoCodigo))) {
                throw new SQLException("Solo los expedientes en abandono u observación administrativa pueden derivarse a notificación desde Análisis.");
            }
            return moverExpediente(
                    idExpediente,
                    ACCION_DERIVACION_NOTIFICACION,
                    ETAPA_ANALISIS,
                    expediente.estadoCodigo,
                    ETAPA_NOTIFICACION,
                    ESTADO_EN_NOTIFICACION,
                    comentario,
                    idUsuario,
                    false,
                    false);
        }
    }

    public AnalisisResultadoDTO archivarNoCorresponde(Long idExpediente, String comentario, Long idUsuario) throws SQLException {
        return moverExpediente(
                idExpediente,
                ACCION_ARCHIVO,
                ETAPA_ANALISIS,
                ESTADO_NO_CORRESPONDE,
                ETAPA_CIERRE_ARCHIVO,
                ESTADO_ARCHIVADO,
                comentario,
                idUsuario,
                false,
                true);
    }

    private AnalisisResultadoDTO moverExpediente(
            Long idExpediente,
            String accionCodigo,
            String etapaOrigenCodigo,
            String estadoOrigenCodigo,
            String etapaDestinoCodigo,
            String estadoDestinoCodigo,
            String comentario,
            Long idUsuario,
            boolean registrarRecepcionAsignacion,
            boolean marcarArchivado) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, idExpediente);
                if (!etapaOrigenCodigo.equalsIgnoreCase(expediente.etapaCodigo)
                        || !estadoOrigenCodigo.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente ya no se encuentra en " + etapaOrigenCodigo + " / " + estadoOrigenCodigo + ".");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        accionCodigo,
                        etapaOrigenCodigo,
                        estadoOrigenCodigo,
                        etapaDestinoCodigo,
                        estadoDestinoCodigo);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, accionCodigo), "movimiento " + accionCodigo);
                if (registrarRecepcionAsignacion) {
                    actualizarFechaRecepcionAsignacion(conn, idExpediente, idUsuario);
                }
                actualizarExpediente(conn, idExpediente, transicion.idEtapaDestino, transicion.idEstadoDestino, null, null, idUsuario, marcarArchivado);
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
                conn.setAutoCommit(previousAutoCommit);
                return new AnalisisResultadoDTO(
                        idExpediente,
                        expediente.numeroExpediente,
                        accionCodigo,
                        etapaDestinoCodigo,
                        estadoDestinoCodigo,
                        mensajeMovimiento(accionCodigo));
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

    private void recibirExpedienteAsignado(
            Connection conn,
            ExpedienteBloqueado expediente,
            Transicion transicion,
            Long idMovimiento,
            String comentario,
            Long idUsuario) throws SQLException {
        actualizarFechaRecepcionAsignacion(conn, expediente.idExpediente, idUsuario);
        actualizarExpediente(
                conn,
                expediente.idExpediente,
                transicion.idEtapaDestino,
                transicion.idEstadoDestino,
                null,
                null,
                idUsuario,
                false);
        insertarHistorial(
                conn,
                expediente.idExpediente,
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
                comentario,
                ACCION_RECEPCION);
    }

    private Transicion requerirTransicionRecepcion(Connection conn) throws SQLException {
        return requerirTransicion(
                conn,
                ACCION_RECEPCION,
                ETAPA_ASIGNACION,
                ESTADO_ASIGNADO,
                ETAPA_ANALISIS,
                ESTADO_RECIBIDO);
    }

    private void validarPendienteRecepcion(ExpedienteBloqueado expediente) throws SQLException {
        if (!estaPendienteRecepcion(expediente)) {
            throw new SQLException("El expediente ya no se encuentra en "
                    + ETAPA_ASIGNACION + " / " + ESTADO_ASIGNADO + ".");
        }
    }

    private boolean estaPendienteRecepcion(ExpedienteBloqueado expediente) {
        return expediente != null
                && ETAPA_ASIGNACION.equalsIgnoreCase(expediente.etapaCodigo)
                && ESTADO_ASIGNADO.equalsIgnoreCase(expediente.estadoCodigo);
    }

    private boolean estaRecibidoEnAnalisis(ExpedienteBloqueado expediente) {
        return expediente != null
                && ETAPA_ANALISIS.equalsIgnoreCase(expediente.etapaCodigo)
                && ESTADO_RECIBIDO.equalsIgnoreCase(expediente.estadoCodigo);
    }

    private List<Long> listarIdsAsociadosConfirmados(Connection conn, Long idExpedientePrincipal) throws SQLException {
        List<Long> ids = new ArrayList<Long>();
        String sql = "SELECT DISTINCT id_expediente_relacionado "
                + "FROM expediente_relacion "
                + "WHERE id_expediente_principal = ? AND activo = 1 "
                + "AND UPPER(tipo_relacion) IN (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpedientePrincipal);
            ps.setString(2, TIPO_RELACION_DOCUMENTO_DUPLICADO);
            ps.setString(3, TIPO_RELACION_MISMA_ACTA_TITULAR);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long id = getLongOrNull(rs, "id_expediente_relacionado");
                    if (id != null) {
                        ids.add(id);
                    }
                }
            }
        }
        return ids;
    }

    private boolean existeRelacionConfirmada(
            Connection conn,
            Long idExpedientePrincipal,
            Long idExpedienteAsociado) throws SQLException {
        String sql = "SELECT 1 FROM expediente_relacion "
                + "WHERE id_expediente_principal = ? "
                + "AND id_expediente_relacionado = ? "
                + "AND activo = 1 "
                + "AND UPPER(tipo_relacion) IN (?, ?) "
                + "AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpedientePrincipal);
            ps.setLong(2, idExpedienteAsociado);
            ps.setString(3, TIPO_RELACION_DOCUMENTO_DUPLICADO);
            ps.setString(4, TIPO_RELACION_MISMA_ACTA_TITULAR);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private AnalisisExpedienteDTO map(Connection conn, ResultSet rs) throws SQLException {
        return new AnalisisExpedienteDTO(
                getLongOrNull(rs, "id_expediente"),
                rs.getString("numero_expediente"),
                rs.getString("numero_tramite_documentario"),
                rs.getString("procedimiento"),
                rs.getString("tipo_documento"),
                rs.getString("numero_documento_titular"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                calendarioLaboralService.calcularDiasHabilesRestantes(conn, rs.getDate("fecha_vencimiento")),
                toLocalDateTime(rs.getTimestamp("fecha_registro")),
                toLocalDateTime(rs.getTimestamp("fecha_asignacion")),
                toLocalDateTime(rs.getTimestamp("fecha_ultimo_movimiento")),
                rs.getString("responsable"),
                rs.getString("equipo"),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getInt("observaciones_pendientes") > 0,
                rs.getInt("relaciones_confirmadas"),
                rs.getInt("documentos_analizados"),
                rs.getString("ultimo_resultado"));
    }

    private ResultadoDestino resolverDestinoResultado(AnalisisRegistroDTO registro) {
        String resultado = registro.getResultadoCodigo();
        if (ESTADO_NO_CORRESPONDE.equalsIgnoreCase(resultado)) {
            return new ResultadoDestino(ESTADO_NO_CORRESPONDE, false);
        }
        if (ESTADO_EN_ABANDONO.equalsIgnoreCase(resultado)) {
            return new ResultadoDestino(ESTADO_EN_ABANDONO, true);
        }
        if (ESTADO_OBSERVACION_ADMINISTRATIVA.equalsIgnoreCase(resultado)) {
            return new ResultadoDestino(ESTADO_OBSERVACION_ADMINISTRATIVA, true);
        }
        if (ESTADO_OBSERVADO.equalsIgnoreCase(resultado)) {
            return new ResultadoDestino(ESTADO_OBSERVADO, false);
        }
        return new ResultadoDestino(ESTADO_ATENDIDO, true);
    }

    private EvaluacionRegistrada obtenerEvaluacionRegistrada(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT * FROM ("
                + "SELECT ev.corresponde, ev.incorporado, ev.requiere_reconstitucion, "
                + "ev.tiene_legitimidad, ev.cumple_medios_probatorios, ev.fundamento, "
                + "NVL(ev.fecha_evaluacion, ev.creado_en) AS fecha_evaluacion, "
                + "tre.codigo AS resultado_codigo, tre.nombre AS resultado_nombre, "
                + "mnc.codigo AS motivo_codigo, mnc.nombre AS motivo_nombre, est.codigo AS estado_codigo, "
                + "(SELECT MAX(h.motivo) KEEP (DENSE_RANK LAST ORDER BY h.fecha_movimiento, h.id_expediente_historial) "
                + "FROM expediente_historial h "
                + "WHERE h.id_expediente = ev.id_expediente AND h.activo = 1 "
                + "AND UPPER(h.tabla_relacionada) = 'EXPEDIENTE_EVALUACION' "
                + "AND h.id_registro_relacionado = ev.id_expediente_evaluacion) AS historial_resultado "
                + "FROM expediente_evaluacion ev "
                + "JOIN expediente e ON e.id_expediente = ev.id_expediente "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "LEFT JOIN tipo_resultado_evaluacion tre "
                + "ON tre.id_tipo_resultado_evaluacion = ev.id_tipo_resultado_evaluacion "
                + "LEFT JOIN motivo_no_corresponde mnc "
                + "ON mnc.id_motivo_no_corresponde = ev.id_motivo_no_corresponde "
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
                return new EvaluacionRegistrada(
                        getBooleanOrNull(rs, "corresponde"),
                        getBooleanOrNull(rs, "incorporado"),
                        rs.getInt("requiere_reconstitucion") == 1,
                        rs.getInt("tiene_legitimidad") == 1,
                        rs.getInt("cumple_medios_probatorios") == 1,
                        rs.getString("fundamento"),
                        rs.getString("resultado_codigo"),
                        rs.getString("resultado_nombre"),
                        rs.getString("motivo_codigo"),
                        rs.getString("motivo_nombre"),
                        rs.getString("estado_codigo"),
                        rs.getString("historial_resultado"),
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
                return new ObservacionAnalisisDTO(
                        rs.getString("tipo_codigo"),
                        rs.getString("tipo_nombre"),
                        rs.getString("descripcion"));
            }
        }
    }

    private String obtenerUltimoNumeroProveido(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT numero_documento FROM ("
                + "SELECT d.numero_documento "
                + "FROM expediente_documento d "
                + "JOIN tipo_documento_adjunto td "
                + "ON td.id_tipo_documento_adjunto = d.id_tipo_documento_adjunto "
                + "WHERE d.id_expediente = ? AND d.activo = 1 AND UPPER(td.codigo) = ? "
                + "ORDER BY NVL(d.modificado_en, d.creado_en) DESC, d.id_expediente_documento DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, TIPO_DOCUMENTO_PROVEIDO);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("numero_documento") : "";
            }
        }
    }

    private static String resolverResultadoRegistrado(EvaluacionRegistrada evaluacion) {
        if (hasText(evaluacion.resultadoCodigo)) {
            return evaluacion.resultadoCodigo;
        }
        if (hasText(evaluacion.resultadoHistorial)) {
            return evaluacion.resultadoHistorial;
        }
        if (Boolean.FALSE.equals(evaluacion.corresponde)
                || ESTADO_NO_CORRESPONDE.equalsIgnoreCase(evaluacion.estadoCodigo)) {
            return ESTADO_NO_CORRESPONDE;
        }
        if (ESTADO_OBSERVADO.equalsIgnoreCase(evaluacion.estadoCodigo)) {
            return ESTADO_OBSERVADO;
        }
        return evaluacion.estadoCodigo;
    }

    private static String resolverNombreResultadoRegistrado(
            EvaluacionRegistrada evaluacion,
            String resultadoCodigo) {
        if (hasText(evaluacion.resultadoNombre)) {
            return evaluacion.resultadoNombre;
        }
        if (ESTADO_NO_CORRESPONDE.equalsIgnoreCase(resultadoCodigo)) {
            return "No corresponde a SDRERC";
        }
        if (ESTADO_OBSERVADO.equalsIgnoreCase(resultadoCodigo)) {
            return "Observado / requiere subsanación";
        }
        return resultadoCodigo;
    }

    private static AnalisisDetalleDTO analisisVacio() {
        return new AnalisisDetalleDTO(
                false, "", "", null, null, false, false, false, "",
                "", "", "", null, null, new ArrayList<DocumentoAnalizadoDTO>());
    }

    private static Boolean getBooleanOrNull(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value == 1;
    }

    private Long guardarEvaluacion(
            Connection conn,
            AnalisisRegistroDTO registro,
            Long idResultado,
            Long idMotivoNoCorresponde,
            Long idUsuario) throws SQLException {
        Long idEvaluacion = obtenerEvaluacionActiva(conn, registro.getIdExpediente());
        if (idEvaluacion == null) {
            return insertarEvaluacion(conn, registro, idResultado, idMotivoNoCorresponde, idUsuario);
        }
        actualizarEvaluacion(conn, idEvaluacion, registro, idResultado, idMotivoNoCorresponde, idUsuario);
        return idEvaluacion;
    }

    private Long insertarEvaluacion(
            Connection conn,
            AnalisisRegistroDTO registro,
            Long idResultado,
            Long idMotivoNoCorresponde,
            Long idUsuario) throws SQLException {
        String sql = "INSERT INTO expediente_evaluacion ("
                + "id_expediente, id_tipo_resultado_evaluacion, corresponde, id_motivo_no_corresponde, "
                + "incorporado, requiere_reconstitucion, tiene_legitimidad, cumple_medios_probatorios, "
                + "fundamento, fecha_evaluacion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_EVALUACION"})) {
            ps.setLong(1, registro.getIdExpediente());
            setLongOrNull(ps, 2, idResultado);
            setBooleanNumberOrNull(ps, 3, registro.getCorresponde());
            setLongOrNull(ps, 4, idMotivoNoCorresponde);
            ps.setInt(5, Boolean.TRUE.equals(registro.getIncorporado()) ? 1 : 0);
            ps.setInt(6, registro.isRequiereReconstitucion() ? 1 : 0);
            ps.setInt(7, registro.isTieneLegitimidad() ? 1 : 0);
            ps.setInt(8, registro.isCumpleMediosProbatorios() ? 1 : 0);
            ps.setString(9, limitar(registro.getFundamento(), 2000));
            setLongOrNull(ps, 10, idUsuario);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "expediente_evaluacion");
        }
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
            setLongOrNull(ps, 1, idResultado);
            setBooleanNumberOrNull(ps, 2, registro.getCorresponde());
            setLongOrNull(ps, 3, idMotivoNoCorresponde);
            ps.setInt(4, Boolean.TRUE.equals(registro.getIncorporado()) ? 1 : 0);
            ps.setInt(5, registro.isRequiereReconstitucion() ? 1 : 0);
            ps.setInt(6, registro.isTieneLegitimidad() ? 1 : 0);
            ps.setInt(7, registro.isCumpleMediosProbatorios() ? 1 : 0);
            ps.setString(8, limitar(registro.getFundamento(), 2000));
            setLongOrNull(ps, 9, idUsuario);
            ps.setLong(10, idEvaluacion);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar la evaluación del expediente.");
            }
        }
    }

    private Long obtenerEvaluacionActiva(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT id_expediente_evaluacion FROM ("
                + "SELECT id_expediente_evaluacion FROM expediente_evaluacion "
                + "WHERE id_expediente = ? AND activo = 1 ORDER BY creado_en DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
    }

    private void registrarProveidoSiInformado(
            Connection conn,
            AnalisisRegistroDTO registro,
            Long idUsuario) throws SQLException {
        if (!hasText(registro.getNumeroDocumentoProveido())) {
            return;
        }
        Long idTipoDocumento = requerirId(
                catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, TIPO_DOCUMENTO_PROVEIDO),
                "tipo de documento PROVEIDO");
        if (existeProveido(conn, registro.getIdExpediente(), idTipoDocumento, registro.getNumeroDocumentoProveido())) {
            return;
        }
        String sql = "INSERT INTO expediente_documento ("
                + "id_expediente, id_tipo_documento_adjunto, nombre_documento, numero_documento, "
                + "fecha_documento, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, registro.getIdExpediente());
            ps.setLong(2, idTipoDocumento);
            ps.setString(3, "Proveído");
            ps.setString(4, limitar(registro.getNumeroDocumentoProveido(), 100));
            ps.setDate(5, Date.valueOf(LocalDate.now()));
            setLongOrNull(ps, 6, idUsuario);
            ps.executeUpdate();
        }
    }

    private boolean existeProveido(
            Connection conn,
            Long idExpediente,
            Long idTipoDocumento,
            String numeroDocumento) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expediente_documento "
                + "WHERE id_expediente = ? AND id_tipo_documento_adjunto = ? "
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

    private boolean tieneEvaluacionActiva(Connection conn, Long idExpediente) throws SQLException {
        return obtenerEvaluacionActiva(conn, idExpediente) != null;
    }

    private void actualizarFechaRecepcionAsignacion(Connection conn, Long idExpediente, Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_asignacion SET "
                + "fecha_recepcion = NVL(fecha_recepcion, SYSTIMESTAMP), "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activa = 1 AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setLongOrNull(ps, 1, idUsuario);
            ps.setLong(2, idExpediente);
            int updated = ps.executeUpdate();
            if (updated <= 0) {
                throw new SQLException("El expediente no tiene una asignación activa para recibir.");
            }
        }
    }

    private void actualizarExpediente(
            Connection conn,
            Long idExpediente,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idUsuarioResponsable,
            Long idEquipoResponsable,
            Long idUsuarioModificador,
            boolean marcarArchivado) throws SQLException {
        String sql = "UPDATE expediente SET "
                + "id_etapa_actual = ?, id_estado_actual = ?, "
                + "id_usuario_responsable_actual = NVL(?, id_usuario_responsable_actual), "
                + "id_equipo_responsable_actual = NVL(?, id_equipo_responsable_actual), "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, "
                + (marcarArchivado ? "archivado = 1, " : "")
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEtapaDestino);
            ps.setLong(2, idEstadoDestino);
            setLongOrNull(ps, 3, idUsuarioResponsable);
            setLongOrNull(ps, 4, idEquipoResponsable);
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
                return mapBloqueado(rs);
            }
        }
    }

    private ExpedienteBloqueado obtenerExpediente(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT e.id_expediente, e.numero_expediente, e.id_etapa_actual, e.id_estado_actual, "
                + "e.id_usuario_responsable_actual, e.id_equipo_responsable_actual, "
                + "et.codigo AS etapa_codigo, est.codigo AS estado_codigo "
                + "FROM expediente e "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "WHERE e.id_expediente = ? AND e.activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El expediente seleccionado no existe o no está activo.");
                }
                return mapBloqueado(rs);
            }
        }
    }

    private ExpedienteBloqueado mapBloqueado(ResultSet rs) throws SQLException {
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

    private static String comentarioMovimiento(String accionCodigo, String comentario) {
        if (hasText(comentario)) {
            return comentario.trim();
        }
        if (ACCION_RECEPCION.equals(accionCodigo)) {
            return "Recepción de expediente asignado para análisis.";
        }
        if (ACCION_ENVIO_VERIFICACION.equals(accionCodigo) || ACCION_REENVIO_VERIFICACION.equals(accionCodigo)) {
            return "Expediente enviado a verificación.";
        }
        if (ACCION_DERIVACION_NOTIFICACION.equals(accionCodigo)) {
            return "Derivación especial a notificación.";
        }
        if (ACCION_ARCHIVO.equals(accionCodigo)) {
            return "Archivo por no corresponder a SDRERC.";
        }
        return accionCodigo;
    }

    private static String comentarioAnalisis(AnalisisRegistroDTO registro) {
        String base = hasText(registro.getFundamento())
                ? registro.getFundamento()
                : "Registro de resultado de análisis.";
        String comentario = registro.getResultadoNombre().isEmpty()
                ? base
                : registro.getResultadoNombre() + ": " + base;
        if (hasText(registro.getNumeroDocumentoProveido())) {
            comentario += " Proveído: " + registro.getNumeroDocumentoProveido() + ".";
        }
        return comentario;
    }

    private static String mensajeMovimiento(String accionCodigo) {
        if (ACCION_RECEPCION.equals(accionCodigo)) {
            return "El expediente fue recibido para análisis.";
        }
        if (ACCION_ENVIO_VERIFICACION.equals(accionCodigo) || ACCION_REENVIO_VERIFICACION.equals(accionCodigo)) {
            return "El expediente fue enviado a verificación.";
        }
        if (ACCION_DERIVACION_NOTIFICACION.equals(accionCodigo)) {
            return "El expediente fue derivado a notificación.";
        }
        if (ACCION_ARCHIVO.equals(accionCodigo)) {
            return "El expediente fue archivado por no corresponder.";
        }
        return "Operación completada.";
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

    private static void setBooleanNumberOrNull(PreparedStatement ps, int index, Boolean value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setInt(index, Boolean.TRUE.equals(value) ? 1 : 0);
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

    private static class EvaluacionRegistrada {

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
        private final String estadoCodigo;
        private final String resultadoHistorial;
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
                String estadoCodigo,
                String resultadoHistorial,
                LocalDate fechaEvaluacion) {
            this.corresponde = corresponde;
            this.incorporado = incorporado;
            this.requiereReconstitucion = requiereReconstitucion;
            this.tieneLegitimidad = tieneLegitimidad;
            this.cumpleMediosProbatorios = cumpleMediosProbatorios;
            this.fundamento = fundamento == null ? "" : fundamento;
            this.resultadoCodigo = resultadoCodigo == null ? "" : resultadoCodigo;
            this.resultadoNombre = resultadoNombre == null ? "" : resultadoNombre;
            this.motivoNoCorrespondeCodigo = motivoNoCorrespondeCodigo == null ? "" : motivoNoCorrespondeCodigo;
            this.motivoNoCorrespondeNombre = motivoNoCorrespondeNombre == null ? "" : motivoNoCorrespondeNombre;
            this.estadoCodigo = estadoCodigo == null ? "" : estadoCodigo;
            this.resultadoHistorial = resultadoHistorial == null ? "" : resultadoHistorial;
            this.fechaEvaluacion = fechaEvaluacion;
        }
    }

    private static class ResultadoDestino {

        private final String estadoDestinoCodigo;
        private final boolean requiereResultadoCatalogo;

        private ResultadoDestino(String estadoDestinoCodigo, boolean requiereResultadoCatalogo) {
            this.estadoDestinoCodigo = estadoDestinoCodigo;
            this.requiereResultadoCatalogo = requiereResultadoCatalogo;
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
