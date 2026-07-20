package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CalendarioLaboralService;
import com.sdrerc.domain.dto.sdrercapp.AnalisisDetalleDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.AnalisisItemDTO;
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
        return buscarExpedientes(textoLibre, estadoCodigo, null, null, limite, true, null, null);
    }

    public AnalisisDetalleDTO obtenerAnalisisRegistrado(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return analisisVacio();
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return obtenerAnalisisRegistrado(conn, idExpediente, null);
        }
    }

    private AnalisisDetalleDTO obtenerAnalisisRegistrado(
            Connection conn,
            Long idExpediente,
            Long idExpedienteAnalisis) throws SQLException {
        EvaluacionRegistrada evaluacion = obtenerEvaluacionRegistrada(conn, idExpediente, idExpedienteAnalisis);
        List<DocumentoAnalizadoDTO> documentos = documentoAnalisisDAO.listarPorExpediente(conn, idExpediente, idExpedienteAnalisis);
        ObservacionAnalisisDTO observacion = obtenerUltimaObservacionAnalisis(conn, idExpediente);
        DocumentoNoCorrespondeInfo documentoNoCorresponde = obtenerUltimoDocumentoNoCorresponde(conn, idExpediente);
        if (evaluacion == null) {
            return new AnalisisDetalleDTO(
                    idExpedienteAnalisis,
                    false, "", "", null, null, false, false, false, "",
                    "", "",
                    documentoNoCorresponde.codigo,
                    documentoNoCorresponde.nombre,
                    documentoNoCorresponde.numero,
                    null,
                    observacion,
                    documentos);
        }
        String resultadoCodigo = resolverResultadoRegistrado(evaluacion);
        String resultadoNombre = resolverNombreResultadoRegistrado(evaluacion, resultadoCodigo);
        return new AnalisisDetalleDTO(
                idExpedienteAnalisis,
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
                documentoNoCorresponde.codigo,
                documentoNoCorresponde.nombre,
                documentoNoCorresponde.numero,
                evaluacion.fechaEvaluacion,
                observacion,
                documentos);
    }

    public List<AnalisisItemDTO> listarAnalisisPorExpediente(Long idExpediente) throws SQLException {
        List<AnalisisItemDTO> items = new ArrayList<AnalisisItemDTO>();
        if (idExpediente == null) {
            return items;
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            items.add(new AnalisisItemDTO(null, 1, "Análisis 1", "UNICO", obtenerAnalisisRegistrado(conn, idExpediente, null)));
            return items;
        }
    }

    public AnalisisItemDTO crearBloqueAnalisis(Long idExpediente, Long idUsuario) throws SQLException {
        if (idExpediente == null) {
            throw new SQLException("Seleccione un expediente para crear el análisis.");
        }
        throw new SQLException("El módulo de Análisis quedó configurado con un único análisis por expediente.");
    }

    public List<AnalisisExpedienteDTO> buscarExpedientes(
            String textoLibre,
            String estadoCodigo,
            LocalDate fechaSolicitudDesde,
            LocalDate fechaSolicitudHasta,
            int limite,
            boolean esAdmin,
            Long idUsuarioActual,
            List<Long> idsEquipoActual) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        boolean soportaNumeroHojaEnvio;
        boolean soportaGrupoFamiliar;
        try (Connection connSoporte = SdrercAppConnection.getConnection()) {
            soportaNumeroHojaEnvio = soportaNumeroHojaEnvio(connSoporte);
            soportaGrupoFamiliar = soportaGrupoFamiliar(connSoporte);
        }
        sql.append("SELECT * FROM (");
        sql.append("SELECT DISTINCT e.id_expediente, e.numero_expediente, esol.numero_expediente_sgd, e.numero_tramite_documentario, ");
        if (soportaNumeroHojaEnvio) {
            sql.append("(SELECT MAX(axa.numero_hoja_envio) KEEP (DENSE_RANK LAST ORDER BY axa.fecha_asignacion, axa.id_expediente_asignacion) ");
            sql.append(" FROM expediente_asignacion axa ");
            sql.append(" WHERE axa.id_expediente = e.id_expediente AND axa.activa = 1 AND axa.activo = 1) AS numero_hoja_envio_asignacion, ");
        } else {
            sql.append("CAST(NULL AS VARCHAR2(120)) AS numero_hoja_envio_asignacion, ");
        }
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
        sql.append("cr.nombre AS canal_ingreso, e.prioridad, esol.observacion AS observacion_solicitud, ");
        if (soportaGrupoFamiliar) {
            sql.append("NVL(esol.grupo_familiar, 0) AS grupo_familiar, ");
            sql.append("esol.criterio_grupo_familiar, esol.observacion_grupo_familiar, ");
        } else {
            sql.append("0 AS grupo_familiar, CAST(NULL AS VARCHAR2(80)) AS criterio_grupo_familiar, ");
            sql.append("CAST(NULL AS VARCHAR2(500)) AS observacion_grupo_familiar, ");
        }
        sql.append("esol.fecha_recepcion, e.fecha_vencimiento, ");
        sql.append("e.fecha_registro, asig.fecha_asignacion, e.fecha_ultimo_movimiento, ");
        sql.append("ur.nombre_completo AS responsable, eq.nombre AS equipo, ");
        sql.append("et.codigo AS etapa_codigo, est.codigo AS estado_codigo, ");
        sql.append("UPPER(NVL(").append(nombrePersona("p")).append(", 'ZZZ')) AS orden_titular, ");
        sql.append("(SELECT COUNT(*) FROM expediente_observacion o WHERE o.id_expediente = e.id_expediente AND o.subsanada = 0 AND o.activo = 1) AS observaciones_pendientes, ");
        sql.append("(SELECT COUNT(DISTINCT canon.id_asociado) FROM (");
        sql.append("SELECT ");
        sql.append("CASE ");
        sql.append("WHEN NVL(TRIM(op.numero_expediente), '') <> '' AND NVL(TRIM(orrel.numero_expediente), '') = '' THEN op.id_expediente ");
        sql.append("WHEN NVL(TRIM(op.numero_expediente), '') = '' AND NVL(TRIM(orrel.numero_expediente), '') <> '' THEN orrel.id_expediente ");
        sql.append("WHEN NVL(TRIM(op.numero_expediente), '') <> '' AND NVL(TRIM(orrel.numero_expediente), '') <> '' THEN ");
        sql.append("CASE WHEN NVL(op.fecha_registro, DATE '1900-01-01') <= NVL(orrel.fecha_registro, DATE '1900-01-01') THEN op.id_expediente ELSE orrel.id_expediente END ");
        sql.append("ELSE CASE WHEN NVL(op.fecha_registro, DATE '1900-01-01') <= NVL(orrel.fecha_registro, DATE '1900-01-01') THEN op.id_expediente ELSE orrel.id_expediente END ");
        sql.append("END AS id_canonico, ");
        sql.append("CASE WHEN r_canon.id_expediente_principal = e.id_expediente THEN r_canon.id_expediente_relacionado ELSE r_canon.id_expediente_principal END AS id_asociado ");
        sql.append("FROM expediente_relacion r_canon ");
        sql.append("JOIN expediente op ON op.id_expediente = r_canon.id_expediente_principal AND op.activo = 1 ");
        sql.append("JOIN expediente orrel ON orrel.id_expediente = r_canon.id_expediente_relacionado AND orrel.activo = 1 ");
        sql.append("WHERE r_canon.activo = 1 ");
        sql.append("AND UPPER(r_canon.tipo_relacion) IN (?, ?) ");
        sql.append("AND (r_canon.id_expediente_principal = e.id_expediente OR r_canon.id_expediente_relacionado = e.id_expediente)) canon ");
        params.add(TIPO_RELACION_DOCUMENTO_DUPLICADO);
        params.add(TIPO_RELACION_MISMA_ACTA_TITULAR);
        sql.append("WHERE canon.id_canonico = e.id_expediente) AS relaciones_confirmadas, ");
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
        sql.append("LEFT JOIN canal_recepcion cr ON cr.id_canal_recepcion = esol.id_canal_recepcion ");
        sql.append("LEFT JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1 ");
        sql.append("LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta ");
        sql.append("LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' ");
        sql.append("LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 ");
        sql.append("LEFT JOIN persona ps ON ps.id_persona = esol.id_persona_solicitante AND ps.activo = 1 ");
        sql.append("WHERE e.activo = 1 ");
        appendFiltroPrincipalCanonico(sql, params, "e");
        sql.append("AND (");
        sql.append("(et.codigo = ? AND est.codigo = ?) ");
        params.add(ETAPA_ASIGNACION);
        params.add(ESTADO_ASIGNADO);
        sql.append("OR et.codigo = ?");
        params.add(ETAPA_ANALISIS);
        sql.append(") ");
        sql.append(VisibilidadBandejaSql.construirCondicion(
                params, esAdmin, idUsuarioActual, idsEquipoActual,
                "e.id_usuario_responsable_actual", "e.id_equipo_responsable_actual"));

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
            sql.append("OR UPPER(NVL(ps.numero_documento, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(").append(nombrePersona("ps")).append(", '')) LIKE ? ");
            sql.append("OR UPPER(NVL(esol.numero_expediente_sgd, '')) LIKE ? ");
            sql.append(") ");
            for (int i = 0; i < 9; i++) {
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

                // El cambio de etapa/estado ya se propagó a los asociados dentro de
                // actualizarExpediente (ver ExpedienteEstadoPropagacionDAO); solo falta contar
                // cuántos hay para el mensaje de confirmación.
                int asociadosRecibidos = listarIdsAsociadosConfirmados(conn, idExpediente).size();

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

                Long idAnalisis = resolverIdAnalisisParaEscritura(conn, registro.getIdExpediente(), registro.getIdExpedienteAnalisis(), idUsuario);
                Long idEvaluacion = guardarEvaluacion(conn, registro, idAnalisis, idResultado, idMotivoNoCorresponde, idUsuario);
                registrarDocumentoNoCorrespondeSiInformado(conn, registro, idUsuario);
                for (DocumentoAnalizadoDTO documento : registro.getDocumentosAnalizados()) {
                    DocumentoAnalizadoDTO documentoPersistencia = documentoConAnalisis(documento, idAnalisis);
                    if (documento.getIdDocumentoAnalizado() == null) {
                        documentoAnalisisDAO.insertarDocumentoAnalizado(conn, registro.getIdExpediente(), documentoPersistencia, idUsuario);
                    } else {
                        documentoAnalisisDAO.actualizarDocumentoAnalizado(
                                conn,
                                registro.getIdExpediente(),
                                documentoPersistencia,
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

    public AnalisisResultadoDTO guardarDocumentosAnalisis(
            Long idExpediente,
            List<DocumentoAnalizadoDTO> documentos,
            Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, idExpediente);
                if (!ETAPA_ANALISIS.equalsIgnoreCase(expediente.etapaCodigo)
                        || !(ESTADO_RECIBIDO.equalsIgnoreCase(expediente.estadoCodigo)
                        || ESTADO_OBSERVADO.equalsIgnoreCase(expediente.estadoCodigo)
                        || ESTADO_SUBSANADO.equalsIgnoreCase(expediente.estadoCodigo)
                        || ESTADO_ATENDIDO.equalsIgnoreCase(expediente.estadoCodigo))) {
                    throw new SQLException("El expediente debe estar en Análisis para guardar documentos de análisis.");
                }
                for (DocumentoAnalizadoDTO documento : documentos) {
                    if (documento.getIdDocumentoAnalizado() == null) {
                        documentoAnalisisDAO.insertarDocumentoAnalizado(conn, idExpediente, documento, idUsuario);
                    } else {
                        documentoAnalisisDAO.actualizarDocumentoAnalizado(conn, idExpediente, documento, idUsuario);
                    }
                }
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new AnalisisResultadoDTO(
                        idExpediente,
                        expediente.numeroExpediente,
                        "GUARDAR_DOCUMENTOS_ANALISIS",
                        ETAPA_ANALISIS,
                        expediente.estadoCodigo,
                        "Los documentos de análisis fueron guardados correctamente.");
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

    public AnalisisResultadoDTO guardarDocumentosAnalisisJerarquicos(
            Long idExpediente,
            List<DocumentoAnalizadoDTO> documentos,
            Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, idExpediente);
                if (!ETAPA_ANALISIS.equalsIgnoreCase(expediente.etapaCodigo)
                        || !(ESTADO_RECIBIDO.equalsIgnoreCase(expediente.estadoCodigo)
                        || ESTADO_OBSERVADO.equalsIgnoreCase(expediente.estadoCodigo)
                        || ESTADO_SUBSANADO.equalsIgnoreCase(expediente.estadoCodigo)
                        || ESTADO_ATENDIDO.equalsIgnoreCase(expediente.estadoCodigo))) {
                    throw new SQLException("El expediente debe estar en Análisis para guardar documentos de análisis.");
                }
                documentoAnalisisDAO.guardarDocumentosJerarquicos(conn, idExpediente, documentos, idUsuario);
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new AnalisisResultadoDTO(
                        idExpediente,
                        expediente.numeroExpediente,
                        "GUARDAR_DOCUMENTOS_ANALISIS",
                        ETAPA_ANALISIS,
                        expediente.estadoCodigo,
                        "Los documentos de análisis fueron guardados correctamente.");
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

    public AnalisisResultadoDTO darBajaDocumentosAnalisis(
            Long idExpediente,
            List<Long> idsDocumentoAnalizado,
            Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, idExpediente);
                if (!ETAPA_ANALISIS.equalsIgnoreCase(expediente.etapaCodigo)
                        || !(ESTADO_RECIBIDO.equalsIgnoreCase(expediente.estadoCodigo)
                        || ESTADO_OBSERVADO.equalsIgnoreCase(expediente.estadoCodigo)
                        || ESTADO_SUBSANADO.equalsIgnoreCase(expediente.estadoCodigo)
                        || ESTADO_ATENDIDO.equalsIgnoreCase(expediente.estadoCodigo))) {
                    throw new SQLException("El expediente debe estar en Análisis para dar de baja documentos de análisis.");
                }
                for (Long idDocumentoAnalizado : idsDocumentoAnalizado) {
                    documentoAnalisisDAO.darBajaDocumentoAnalizado(conn, idExpediente, idDocumentoAnalizado, idUsuario);
                }
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new AnalisisResultadoDTO(
                        idExpediente,
                        expediente.numeroExpediente,
                        "DAR_BAJA_DOCUMENTOS_ANALISIS",
                        ETAPA_ANALISIS,
                        expediente.estadoCodigo,
                        "El documento fue dado de baja correctamente.");
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
                    || !(ESTADO_RECIBIDO.equalsIgnoreCase(expediente.estadoCodigo)
                    || ESTADO_OBSERVADO.equalsIgnoreCase(expediente.estadoCodigo)
                    || ESTADO_ATENDIDO.equalsIgnoreCase(expediente.estadoCodigo)
                    || ESTADO_SUBSANADO.equalsIgnoreCase(expediente.estadoCodigo))) {
                throw new SQLException("El expediente debe estar Recibido, Observado, Atendido o Subsanado para enviarlo a verificación.");
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

    private void appendFiltroPrincipalCanonico(StringBuilder sql, List<Object> params, String aliasExpediente) {
        sql.append("AND (");
        sql.append("NOT EXISTS (SELECT 1 FROM expediente_relacion r_excl ");
        sql.append("WHERE r_excl.activo = 1 ");
        sql.append("AND UPPER(r_excl.tipo_relacion) IN (?, ?) ");
        sql.append("AND r_excl.id_expediente_relacionado = ").append(aliasExpediente).append(".id_expediente) ");
        params.add(TIPO_RELACION_DOCUMENTO_DUPLICADO);
        params.add(TIPO_RELACION_MISMA_ACTA_TITULAR);
        sql.append("OR EXISTS (SELECT 1 FROM (");
        sql.append("SELECT DISTINCT ");
        sql.append("CASE ");
        sql.append("WHEN NVL(TRIM(op.numero_expediente), '') <> '' AND NVL(TRIM(orrel.numero_expediente), '') = '' THEN op.id_expediente ");
        sql.append("WHEN NVL(TRIM(op.numero_expediente), '') = '' AND NVL(TRIM(orrel.numero_expediente), '') <> '' THEN orrel.id_expediente ");
        sql.append("WHEN NVL(TRIM(op.numero_expediente), '') <> '' AND NVL(TRIM(orrel.numero_expediente), '') <> '' THEN ");
        sql.append("CASE WHEN NVL(op.fecha_registro, DATE '1900-01-01') <= NVL(orrel.fecha_registro, DATE '1900-01-01') THEN op.id_expediente ELSE orrel.id_expediente END ");
        sql.append("ELSE CASE WHEN NVL(op.fecha_registro, DATE '1900-01-01') <= NVL(orrel.fecha_registro, DATE '1900-01-01') THEN op.id_expediente ELSE orrel.id_expediente END ");
        sql.append("END AS id_canonico ");
        sql.append("FROM expediente_relacion r_canon ");
        sql.append("JOIN expediente op ON op.id_expediente = r_canon.id_expediente_principal AND op.activo = 1 ");
        sql.append("JOIN expediente orrel ON orrel.id_expediente = r_canon.id_expediente_relacionado AND orrel.activo = 1 ");
        sql.append("WHERE r_canon.activo = 1 ");
        sql.append("AND UPPER(r_canon.tipo_relacion) IN (?, ?) ");
        sql.append("AND (r_canon.id_expediente_principal = ").append(aliasExpediente).append(".id_expediente ");
        sql.append("OR r_canon.id_expediente_relacionado = ").append(aliasExpediente).append(".id_expediente)) canon ");
        params.add(TIPO_RELACION_DOCUMENTO_DUPLICADO);
        params.add(TIPO_RELACION_MISMA_ACTA_TITULAR);
        sql.append("WHERE canon.id_canonico = ").append(aliasExpediente).append(".id_expediente)");
        sql.append(") ");
    }

    private AnalisisExpedienteDTO map(Connection conn, ResultSet rs) throws SQLException {
        return new AnalisisExpedienteDTO(
                getLongOrNull(rs, "id_expediente"),
                rs.getString("numero_expediente"),
                rs.getString("numero_expediente_sgd"),
                rs.getString("numero_tramite_documentario"),
                rs.getString("numero_hoja_envio_asignacion"),
                rs.getString("procedimiento"),
                rs.getString("tipo_documento"),
                rs.getString("numero_documento"),
                rs.getString("tipo_documento_titular"),
                rs.getString("numero_documento_titular"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                rs.getString("solicitante"),
                rs.getString("solicitante_tipo_documento"),
                rs.getString("numero_documento_solicitante"),
                rs.getString("solicitante_correo"),
                rs.getString("solicitante_telefono"),
                rs.getString("solicitante_direccion"),
                rs.getString("solicitante_departamento"),
                rs.getString("solicitante_provincia"),
                rs.getString("solicitante_distrito"),
                rs.getString("canal_ingreso"),
                rs.getString("prioridad"),
                rs.getString("observacion_solicitud"),
                getBooleanFromNumber(rs, "grupo_familiar"),
                rs.getString("criterio_grupo_familiar"),
                rs.getString("observacion_grupo_familiar"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                calendarioLaboralService.calcularDiasHabilesRestantes(conn, rs.getDate("fecha_vencimiento")),
                toLocalDate(rs.getDate("fecha_vencimiento")),
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
        if (ESTADO_OBSERVACION_ADMINISTRATIVA.equalsIgnoreCase(resultado)) {
            return new ResultadoDestino(ESTADO_OBSERVACION_ADMINISTRATIVA, true);
        }
        if (ESTADO_OBSERVADO.equalsIgnoreCase(resultado)) {
            return new ResultadoDestino(ESTADO_OBSERVADO, false);
        }
        return new ResultadoDestino(ESTADO_ATENDIDO, true);
    }

    private EvaluacionRegistrada obtenerEvaluacionRegistrada(Connection conn, Long idExpediente) throws SQLException {
        return obtenerEvaluacionRegistrada(conn, idExpediente, null);
    }

    private EvaluacionRegistrada obtenerEvaluacionRegistrada(
            Connection conn,
            Long idExpediente,
            Long idExpedienteAnalisis) throws SQLException {
        boolean soportaMultiple = soportaAnalisisMultiple(conn);
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
                + (soportaMultiple && idExpedienteAnalisis != null
                        ? "AND ev.id_expediente_analisis = ? "
                        : "")
                + "ORDER BY NVL(ev.modificado_en, ev.creado_en) DESC, ev.id_expediente_evaluacion DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            if (soportaMultiple && idExpedienteAnalisis != null) {
                ps.setLong(2, idExpedienteAnalisis);
            }
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

    private DocumentoNoCorrespondeInfo obtenerUltimoDocumentoNoCorresponde(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT codigo, nombre, numero_documento FROM ("
                + "SELECT td.codigo, td.nombre, d.numero_documento "
                + "FROM expediente_documento d "
                + "JOIN tipo_documento_adjunto td "
                + "ON td.id_tipo_documento_adjunto = d.id_tipo_documento_adjunto "
                + "WHERE d.id_expediente = ? AND d.activo = 1 "
                + "AND UPPER(td.codigo) IN ('PROVEIDO', 'HOJA_ENVIO') "
                + "ORDER BY NVL(d.modificado_en, d.creado_en) DESC, d.id_expediente_documento DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return DocumentoNoCorrespondeInfo.proveidoVacio();
                }
                return new DocumentoNoCorrespondeInfo(
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getString("numero_documento"));
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
            Long idExpedienteAnalisis,
            Long idResultado,
            Long idMotivoNoCorresponde,
            Long idUsuario) throws SQLException {
        Long idEvaluacion = obtenerEvaluacionActiva(conn, registro.getIdExpediente(), idExpedienteAnalisis);
        if (idEvaluacion == null) {
            return insertarEvaluacion(conn, registro, idExpedienteAnalisis, idResultado, idMotivoNoCorresponde, idUsuario);
        }
        actualizarEvaluacion(conn, idEvaluacion, registro, idExpedienteAnalisis, idResultado, idMotivoNoCorresponde, idUsuario);
        return idEvaluacion;
    }

    private Long insertarEvaluacion(
            Connection conn,
            AnalisisRegistroDTO registro,
            Long idExpedienteAnalisis,
            Long idResultado,
            Long idMotivoNoCorresponde,
            Long idUsuario) throws SQLException {
        boolean soportaMultiple = soportaAnalisisMultiple(conn);
        String sql = "INSERT INTO expediente_evaluacion ("
                + "id_expediente, "
                + (soportaMultiple ? "id_expediente_analisis, " : "")
                + "id_tipo_resultado_evaluacion, corresponde, id_motivo_no_corresponde, "
                + "incorporado, requiere_reconstitucion, tiene_legitimidad, cumple_medios_probatorios, "
                + "fundamento, fecha_evaluacion, activo, creado_por, creado_en"
                + ") VALUES (?, " + (soportaMultiple ? "?, " : "") + "?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_EVALUACION"})) {
            int index = 1;
            ps.setLong(index++, registro.getIdExpediente());
            if (soportaMultiple) {
                setLongOrNull(ps, index++, idExpedienteAnalisis);
            }
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
            return obtenerGeneratedKey(ps, "expediente_evaluacion");
        }
    }

    private void actualizarEvaluacion(
            Connection conn,
            Long idEvaluacion,
            AnalisisRegistroDTO registro,
            Long idExpedienteAnalisis,
            Long idResultado,
            Long idMotivoNoCorresponde,
            Long idUsuario) throws SQLException {
        boolean soportaMultiple = soportaAnalisisMultiple(conn);
        String sql = "UPDATE expediente_evaluacion SET "
                + (soportaMultiple ? "id_expediente_analisis = ?, " : "")
                + "id_tipo_resultado_evaluacion = ?, corresponde = ?, id_motivo_no_corresponde = ?, "
                + "incorporado = ?, requiere_reconstitucion = ?, tiene_legitimidad = ?, cumple_medios_probatorios = ?, "
                + "fundamento = ?, fecha_evaluacion = SYSTIMESTAMP, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente_evaluacion = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            if (soportaMultiple) {
                setLongOrNull(ps, index++, idExpedienteAnalisis);
            }
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
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar la evaluación del expediente.");
            }
        }
    }

    private Long obtenerEvaluacionActiva(Connection conn, Long idExpediente) throws SQLException {
        return obtenerEvaluacionActiva(conn, idExpediente, null);
    }

    private Long obtenerEvaluacionActiva(Connection conn, Long idExpediente, Long idExpedienteAnalisis) throws SQLException {
        boolean soportaMultiple = soportaAnalisisMultiple(conn);
        String sql = "SELECT id_expediente_evaluacion FROM ("
                + "SELECT id_expediente_evaluacion FROM expediente_evaluacion "
                + "WHERE id_expediente = ? AND activo = 1 "
                + (soportaMultiple && idExpedienteAnalisis != null
                        ? "AND id_expediente_analisis = ? "
                        : "")
                + "ORDER BY creado_en DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            if (soportaMultiple && idExpedienteAnalisis != null) {
                ps.setLong(2, idExpedienteAnalisis);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long value = rs.getLong(1);
                return rs.wasNull() ? null : value;
            }
        }
    }

    private void registrarDocumentoNoCorrespondeSiInformado(
            Connection conn,
            AnalisisRegistroDTO registro,
            Long idUsuario) throws SQLException {
        if (!hasText(registro.getNumeroDocumentoProveido())) {
            return;
        }
        String codigoTipo = normalizarTipoDocumentoNoCorresponde(registro.getTipoDocumentoNoCorrespondeCodigo());
        String nombreTipo = hasText(registro.getTipoDocumentoNoCorrespondeNombre())
                ? registro.getTipoDocumentoNoCorrespondeNombre()
                : nombreTipoDocumentoNoCorresponde(codigoTipo);
        Long idTipoDocumento = requerirId(
                catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, codigoTipo),
                "tipo de documento " + codigoTipo);
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

    private static String normalizarTipoDocumentoNoCorresponde(String codigo) {
        if ("HOJA_ENVIO".equalsIgnoreCase(codigo)) {
            return "HOJA_ENVIO";
        }
        return TIPO_DOCUMENTO_PROVEIDO;
    }

    private static String nombreTipoDocumentoNoCorresponde(String codigo) {
        if ("HOJA_ENVIO".equalsIgnoreCase(codigo)) {
            return "Hoja de Envío";
        }
        return "Proveido";
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
        ExpedienteEstadoPropagacionDAO.propagarEstadoAAsociados(conn, idExpediente, idEtapaDestino, idEstadoDestino, idUsuarioModificador);
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
        Long idAutorHistorial = resolverAutorHistorial(conn, idUsuarioOrigen, idUsuarioDestino);
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
            setLongOrNull(ps, 7, idAutorHistorial);
            setLongOrNull(ps, 8, idUsuarioDestino);
            setLongOrNull(ps, 9, idEquipoDestino);
            ps.setString(10, tablaRelacionada);
            setLongOrNull(ps, 11, idRegistroRelacionado);
            ps.setString(12, limitar(comentario, 2000));
            ps.setString(13, limitar(motivo, 1000));
            setLongOrNull(ps, 14, idAutorHistorial);
            ps.executeUpdate();
        }
    }

    /**
     * Si quien ejecuta la accion es ADMIN_SISTEMA, el historial no debe quedar a su nombre:
     * se sustituye por el usuario asignado/responsable/destino de esa misma accion. Si no hay
     * un destino resuelto, se conserva el autor real.
     */
    private Long resolverAutorHistorial(Connection conn, Long idUsuarioActor, Long idUsuarioDestino) throws SQLException {
        if (idUsuarioDestino == null || !catalogoLookupDAO.tieneRolAdminSistema(conn, idUsuarioActor)) {
            return idUsuarioActor;
        }
        return idUsuarioDestino;
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
            String tipo = hasText(registro.getTipoDocumentoNoCorrespondeNombre())
                    ? registro.getTipoDocumentoNoCorrespondeNombre()
                    : nombreTipoDocumentoNoCorresponde(registro.getTipoDocumentoNoCorrespondeCodigo());
            comentario += " " + tipo + ": " + registro.getNumeroDocumentoProveido() + ".";
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

    private static DocumentoAnalizadoDTO documentoConAnalisis(DocumentoAnalizadoDTO documento, Long idExpedienteAnalisis) {
        if (documento == null || idExpedienteAnalisis == null) {
            return documento;
        }
        return new DocumentoAnalizadoDTO(
                documento.getIdDocumentoAnalizado(),
                documento.getIdExpediente(),
                idExpedienteAnalisis,
                documento.getTipoDocumentoCodigo(),
                documento.getTipoDocumentoNombre(),
                documento.getEstadoDocumentoCodigo(),
                documento.getEstadoDocumentoNombre(),
                documento.getFechaDocumento(),
                documento.getNumeroDocumento(),
                documento.getDescripcion(),
                documento.isNotificado(),
                documento.getFechaAcuse(),
                documento.isRequiereRespuesta(),
                documento.getConfirmacionRespuesta(),
                documento.getFechaRespuesta(),
                documento.getNumeroHojaEnvioRespuesta(),
                documento.isRequierePublicacion(),
                documento.getFechaPublicacion(),
                documento.getDetalleObservacion());
    }

    private AnalisisItemDTO crearBloqueAnalisis(Connection conn, Long idExpediente, Long idUsuario) throws SQLException {
        int numero = siguienteNumeroAnalisis(conn, idExpediente);
        String titulo = "Análisis " + numero;
        String sql = "INSERT INTO expediente_analisis ("
                + "id_expediente, numero_analisis, titulo, estado_analisis, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, 'EN_PROCESO', 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_ANALISIS"})) {
            ps.setLong(1, idExpediente);
            ps.setInt(2, numero);
            ps.setString(3, titulo);
            setLongOrNull(ps, 4, idUsuario);
            ps.executeUpdate();
            Long id = obtenerGeneratedKey(ps, "expediente_analisis");
            return new AnalisisItemDTO(id, numero, titulo, "EN_PROCESO", analisisVacioConId(id));
        }
    }

    private Long resolverIdAnalisisParaEscritura(
            Connection conn,
            Long idExpediente,
            Long idExpedienteAnalisis,
            Long idUsuario) throws SQLException {
        if (!soportaAnalisisMultiple(conn)) {
            return null;
        }
        if (idExpedienteAnalisis != null) {
            return idExpedienteAnalisis;
        }
        return crearBloqueAnalisis(conn, idExpediente, idUsuario).getIdExpedienteAnalisis();
    }

    private int siguienteNumeroAnalisis(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT NVL(MAX(numero_analisis), 0) + 1 FROM expediente_analisis "
                + "WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 1;
            }
        }
    }

    private static AnalisisDetalleDTO analisisVacioConId(Long idExpedienteAnalisis) {
        return new AnalisisDetalleDTO(
                idExpedienteAnalisis,
                false, "", "", null, null, false, false, false, "",
                "", "", "", "", "", null, null, new ArrayList<DocumentoAnalizadoDTO>());
    }

    private static boolean soportaAnalisisMultiple(Connection conn) throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(1) FROM user_tables WHERE table_name = 'EXPEDIENTE_ANALISIS') + "
                + "(SELECT COUNT(1) FROM user_tab_columns WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' AND column_name = 'ID_EXPEDIENTE_ANALISIS') + "
                + "(SELECT COUNT(1) FROM user_tab_columns WHERE table_name = 'EXPEDIENTE_EVALUACION' AND column_name = 'ID_EXPEDIENTE_ANALISIS') "
                + "AS total FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt("total") == 3;
        }
    }

    private static boolean soportaNumeroHojaEnvio(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_ASIGNACION' AND column_name = 'NUMERO_HOJA_ENVIO'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static boolean soportaGrupoFamiliar(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_SOLICITUD' "
                + "AND column_name IN ('GRUPO_FAMILIAR', 'CRITERIO_GRUPO_FAMILIAR', 'OBSERVACION_GRUPO_FAMILIAR')";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 3;
        }
    }

    private static boolean getBooleanFromNumber(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return !rs.wasNull() && value == 1;
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

    private static class DocumentoNoCorrespondeInfo {

        private final String codigo;
        private final String nombre;
        private final String numero;

        private DocumentoNoCorrespondeInfo(String codigo, String nombre, String numero) {
            this.codigo = hasText(codigo) ? codigo : TIPO_DOCUMENTO_PROVEIDO;
            this.nombre = hasText(nombre) ? nombre : nombreTipoDocumentoNoCorresponde(this.codigo);
            this.numero = numero == null ? "" : numero;
        }

        private static DocumentoNoCorrespondeInfo proveidoVacio() {
            return new DocumentoNoCorrespondeInfo(TIPO_DOCUMENTO_PROVEIDO, "Proveido", "");
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
