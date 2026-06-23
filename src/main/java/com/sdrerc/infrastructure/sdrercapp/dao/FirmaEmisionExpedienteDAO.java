package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CalendarioLaboralService;
import com.sdrerc.domain.dto.sdrercapp.FirmaEmisionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.FirmaEmisionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.FirmaEmisionResultadoDTO;
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

public class FirmaEmisionExpedienteDAO {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String ETAPA_FIRMA = "FIRMA_EMISION";
    private static final String ETAPA_EJECUCION = "EJECUCION";
    private static final String ETAPA_NOTIFICACION = "NOTIFICACION";
    private static final String ESTADO_PARA_FIRMA = "PARA_FIRMA";
    private static final String ESTADO_FIRMADO = "FIRMADO";
    private static final String ESTADO_EMITIDO = "EMITIDO";
    private static final String ESTADO_RESOLUCION_NUMERADA = "RESOLUCION_NUMERADA";
    private static final String ESTADO_EN_EJECUCION = "EN_EJECUCION";
    private static final String ESTADO_EN_NOTIFICACION = "EN_NOTIFICACION";
    private static final String ACCION_FIRMA_DOCUMENTO = "FIRMA_DOCUMENTO";
    private static final String ACCION_REGISTRO_NUMERO = "REGISTRO_NUMERO_RESOLUCION";
    private static final String ACCION_DERIVACION_NOTIFICACION = "DERIVACION_A_NOTIFICACION";

    private final CatalogoLookupDAO catalogoLookupDAO;
    private final CalendarioLaboralService calendarioLaboralService = new CalendarioLaboralService();

    public FirmaEmisionExpedienteDAO() {
        this(new CatalogoLookupDAO());
    }

    public FirmaEmisionExpedienteDAO(CatalogoLookupDAO catalogoLookupDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    public List<FirmaEmisionExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
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
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 AND tm.codigo = 'ENVIO_FIRMA') AS fecha_envio_firma, ");
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
        sql.append("res.id_expediente_resolucion, tr.nombre AS tipo_resolucion, res.numero_resolucion, res.fecha_resolucion, res.fecha_firma ");
        sql.append("FROM expediente e ");
        sql.append("JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual ");
        sql.append("JOIN estado_expediente est ON est.id_estado = e.id_estado_actual ");
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
        params.add(ETAPA_FIRMA);

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
                List<FirmaEmisionExpedienteDTO> expedientes = new ArrayList<FirmaEmisionExpedienteDTO>();
                while (rs.next()) {
                    expedientes.add(map(conn, rs));
                }
                return expedientes;
            }
        }
    }

    public FirmaEmisionResultadoDTO registrarFirma(FirmaEmisionRegistroDTO registro, Long idUsuario) throws SQLException {
        return ejecutarConResolucion(
                registro,
                ACCION_FIRMA_DOCUMENTO,
                ETAPA_FIRMA,
                ESTADO_PARA_FIRMA,
                ETAPA_FIRMA,
                ESTADO_FIRMADO,
                idUsuario,
                OperacionResolucion.FIRMA,
                "El documento fue firmado correctamente.");
    }

    public FirmaEmisionResultadoDTO registrarEmision(FirmaEmisionRegistroDTO registro, Long idUsuario) throws SQLException {
        return ejecutarConResolucion(
                registro,
                ACCION_FIRMA_DOCUMENTO,
                ETAPA_FIRMA,
                ESTADO_FIRMADO,
                ETAPA_FIRMA,
                ESTADO_EMITIDO,
                idUsuario,
                OperacionResolucion.EMISION,
                "La emisión del documento fue registrada correctamente.");
    }

    public FirmaEmisionResultadoDTO registrarNumeroResolucion(FirmaEmisionRegistroDTO registro, Long idUsuario) throws SQLException {
        if (!hasText(registro.getNumeroResolucion())) {
            throw new SQLException("Ingrese el número de resolución o documento.");
        }
        return ejecutarConResolucion(
                registro,
                ACCION_REGISTRO_NUMERO,
                ETAPA_FIRMA,
                ESTADO_EMITIDO,
                ETAPA_FIRMA,
                ESTADO_RESOLUCION_NUMERADA,
                idUsuario,
                OperacionResolucion.NUMERACION,
                "El número de resolución fue registrado correctamente.");
    }

    public FirmaEmisionResultadoDTO enviarEjecucion(FirmaEmisionRegistroDTO registro, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, registro.getIdExpediente());
                if (!ETAPA_FIRMA.equalsIgnoreCase(expediente.etapaCodigo)
                        || !ESTADO_RESOLUCION_NUMERADA.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente debe estar con resolución numerada para enviarlo a Ejecución.");
                }
                Long idResolucion = obtenerIdResolucionActiva(conn, registro.getIdExpediente());
                if (idResolucion == null || !tieneNumeroResolucion(conn, idResolucion)) {
                    throw new SQLException("Registre el número de resolución antes de enviar a Ejecución.");
                }
                validarDestinoPorResultado(conn, registro.getIdExpediente(), true);
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_REGISTRO_NUMERO,
                        ETAPA_FIRMA,
                        ESTADO_RESOLUCION_NUMERADA,
                        ETAPA_EJECUCION,
                        ESTADO_EN_EJECUCION);
                validarRequisitosTransicion(conn, transicion, registro.getComentario(), registro.getIdExpediente());
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_REGISTRO_NUMERO), "movimiento " + ACCION_REGISTRO_NUMERO);
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
                        "EXPEDIENTE_RESOLUCION",
                        idResolucion,
                        comentarioMovimiento(ACCION_REGISTRO_NUMERO, registro.getComentario(), "Expediente enviado a Ejecución."),
                        "ENVIO_EJECUCION");
                conn.commit();
                return new FirmaEmisionResultadoDTO(
                        registro.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_REGISTRO_NUMERO,
                        ETAPA_EJECUCION,
                        ESTADO_EN_EJECUCION,
                        "El expediente fue enviado a Ejecución.");
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

    public FirmaEmisionResultadoDTO enviarNotificacion(FirmaEmisionRegistroDTO registro, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, registro.getIdExpediente());
                if (!ETAPA_FIRMA.equalsIgnoreCase(expediente.etapaCodigo)) {
                    throw new SQLException("El documento debe encontrarse dentro de Verificación para derivarlo a Notificación.");
                }
                validarDestinoPorResultado(conn, registro.getIdExpediente(), false);
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_DERIVACION_NOTIFICACION,
                        ETAPA_FIRMA,
                        expediente.estadoCodigo,
                        ETAPA_NOTIFICACION,
                        ESTADO_EN_NOTIFICACION);
                validarRequisitosTransicion(conn, transicion, registro.getComentario(), registro.getIdExpediente());
                Long idMovimiento = requerirId(
                        catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_DERIVACION_NOTIFICACION),
                        "movimiento " + ACCION_DERIVACION_NOTIFICACION);
                Long idResolucion = obtenerIdResolucionActiva(conn, registro.getIdExpediente());
                actualizarExpediente(
                        conn,
                        registro.getIdExpediente(),
                        transicion.idEtapaDestino,
                        transicion.idEstadoDestino,
                        idUsuario);
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
                        idResolucion == null ? null : "EXPEDIENTE_RESOLUCION",
                        idResolucion,
                        comentarioMovimiento(
                                ACCION_DERIVACION_NOTIFICACION,
                                registro.getComentario(),
                                "Documento emitido derivado a Notificación."),
                        "ENVIO_NOTIFICACION");
                conn.commit();
                return new FirmaEmisionResultadoDTO(
                        registro.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_DERIVACION_NOTIFICACION,
                        ETAPA_NOTIFICACION,
                        ESTADO_EN_NOTIFICACION,
                        "El documento emitido fue derivado a Notificación.");
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

    private FirmaEmisionResultadoDTO ejecutarConResolucion(
            FirmaEmisionRegistroDTO registro,
            String accionCodigo,
            String etapaOrigenCodigo,
            String estadoOrigenCodigo,
            String etapaDestinoCodigo,
            String estadoDestinoCodigo,
            Long idUsuario,
            OperacionResolucion operacion,
            String mensaje) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, registro.getIdExpediente());
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
                Long idResolucion = guardarResolucion(conn, registro, operacion, idUsuario);
                validarRequisitosTransicion(conn, transicion, registro.getComentario(), registro.getIdExpediente());
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
                        "EXPEDIENTE_RESOLUCION",
                        idResolucion,
                        comentarioMovimiento(accionCodigo, registro.getComentario(), mensaje),
                        accionCodigo);
                conn.commit();
                return new FirmaEmisionResultadoDTO(
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

    private Long guardarResolucion(
            Connection conn,
            FirmaEmisionRegistroDTO registro,
            OperacionResolucion operacion,
            Long idUsuario) throws SQLException {
        Long idTipoResolucion = null;
        if (hasText(registro.getTipoResolucionCodigo())) {
            idTipoResolucion = catalogoLookupDAO.obtenerTipoResolucionId(conn, registro.getTipoResolucionCodigo());
            if (idTipoResolucion == null) {
                throw new SQLException("No se encontró el tipo de resolución: " + registro.getTipoResolucionCodigo() + ".");
            }
        }
        Long idResolucion = obtenerIdResolucionActiva(conn, registro.getIdExpediente());
        if (OperacionResolucion.NUMERACION.equals(operacion)) {
            validarNumeroUnico(conn, registro.getIdExpediente(), registro.getNumeroResolucion());
        }
        if (idResolucion == null) {
            return insertarResolucion(conn, registro, idTipoResolucion, operacion, idUsuario);
        }
        actualizarResolucion(conn, idResolucion, registro, idTipoResolucion, operacion, idUsuario);
        return idResolucion;
    }

    private Long insertarResolucion(
            Connection conn,
            FirmaEmisionRegistroDTO registro,
            Long idTipoResolucion,
            OperacionResolucion operacion,
            Long idUsuario) throws SQLException {
        String sql = "INSERT INTO expediente_resolucion ("
                + "id_expediente, id_tipo_resolucion, numero_resolucion, fecha_resolucion, fecha_firma, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_RESOLUCION"})) {
            ps.setLong(1, registro.getIdExpediente());
            setLongOrNull(ps, 2, idTipoResolucion);
            ps.setString(3, OperacionResolucion.NUMERACION.equals(operacion) ? limitar(registro.getNumeroResolucion(), 100) : null);
            setDateOrNull(ps, 4, fechaResolucionPorOperacion(registro, operacion));
            setTimestampOrNull(ps, 5, OperacionResolucion.FIRMA.equals(operacion) ? registro.getFechaFirma() : null);
            setLongOrNull(ps, 6, idUsuario);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "expediente_resolucion");
        }
    }

    private void actualizarResolucion(
            Connection conn,
            Long idResolucion,
            FirmaEmisionRegistroDTO registro,
            Long idTipoResolucion,
            OperacionResolucion operacion,
            Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_resolucion SET "
                + "id_tipo_resolucion = NVL(?, id_tipo_resolucion), "
                + "numero_resolucion = NVL(?, numero_resolucion), "
                + "fecha_resolucion = NVL(?, fecha_resolucion), "
                + "fecha_firma = NVL(?, fecha_firma), "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente_resolucion = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setLongOrNull(ps, 1, idTipoResolucion);
            ps.setString(2, OperacionResolucion.NUMERACION.equals(operacion) ? limitar(registro.getNumeroResolucion(), 100) : null);
            setDateOrNull(ps, 3, fechaResolucionPorOperacion(registro, operacion));
            setTimestampOrNull(ps, 4, OperacionResolucion.FIRMA.equals(operacion) ? registro.getFechaFirma() : null);
            setLongOrNull(ps, 5, idUsuario);
            ps.setLong(6, idResolucion);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar la resolución del expediente.");
            }
        }
    }

    private LocalDate fechaResolucionPorOperacion(FirmaEmisionRegistroDTO registro, OperacionResolucion operacion) {
        if (OperacionResolucion.EMISION.equals(operacion)) {
            return registro.getFechaEmision();
        }
        if (OperacionResolucion.NUMERACION.equals(operacion)) {
            return registro.getFechaResolucion();
        }
        return null;
    }

    private void validarNumeroUnico(Connection conn, Long idExpediente, String numeroResolucion) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expediente_resolucion "
                + "WHERE activo = 1 AND UPPER(numero_resolucion) = ? AND id_expediente <> ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroResolucion == null ? "" : numeroResolucion.trim().toUpperCase(Locale.ROOT));
            ps.setLong(2, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Ya existe una resolución activa con el número indicado.");
                }
            }
        }
    }

    private boolean tieneNumeroResolucion(Connection conn, Long idResolucion) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expediente_resolucion "
                + "WHERE id_expediente_resolucion = ? AND activo = 1 AND numero_resolucion IS NOT NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idResolucion);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void validarDestinoPorResultado(Connection conn, Long idExpediente, boolean requiereResolutivo) throws SQLException {
        String sql = "SELECT UPPER(NVL(tre.codigo, '')) AS resultado_codigo "
                + "FROM expediente_evaluacion ev "
                + "LEFT JOIN tipo_resultado_evaluacion tre "
                + "ON tre.id_tipo_resultado_evaluacion = ev.id_tipo_resultado_evaluacion "
                + "WHERE ev.id_expediente = ? AND ev.activo = 1 "
                + "ORDER BY ev.fecha_evaluacion DESC NULLS LAST, ev.creado_en DESC, ev.id_expediente_evaluacion DESC";
        String resultadoCodigo = "";
        try (PreparedStatement ps = conn.prepareStatement("SELECT resultado_codigo FROM (" + sql + ") WHERE ROWNUM = 1")) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultadoCodigo = rs.getString("resultado_codigo");
                }
            }
        }
        boolean resolutivo = "PROCEDENTE".equalsIgnoreCase(resultadoCodigo)
                || "PROCEDENTE_EN_PARTE".equalsIgnoreCase(resultadoCodigo)
                || "IMPROCEDENTE".equalsIgnoreCase(resultadoCodigo);
        if (requiereResolutivo && !resolutivo) {
            throw new SQLException("Solo las resoluciones procedentes, procedentes en parte o improcedentes pueden enviarse a Ejecución.");
        }
        if (!requiereResolutivo && resolutivo) {
            throw new SQLException("Las resoluciones deben continuar a Ejecución; no corresponde derivarlas directamente a Notificación.");
        }
    }

    private void validarRequisitosTransicion(Connection conn, Transicion transicion, String comentario, Long idExpediente) throws SQLException {
        if (transicion.requiereComentario && !hasText(comentario)) {
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

    private Long obtenerIdResolucionActiva(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT id_expediente_resolucion FROM ("
                + "SELECT id_expediente_resolucion FROM expediente_resolucion "
                + "WHERE id_expediente = ? AND activo = 1 "
                + "ORDER BY creado_en DESC, id_expediente_resolucion DESC"
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

    private FirmaEmisionExpedienteDTO map(Connection conn, ResultSet rs) throws SQLException {
        return new FirmaEmisionExpedienteDTO(
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
                toLocalDateTime(rs.getTimestamp("fecha_envio_firma")),
                toLocalDateTime(rs.getTimestamp("fecha_ultimo_movimiento")),
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
                toLocalDateTime(rs.getTimestamp("fecha_firma")));
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

    private static void setDateOrNull(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(value));
        }
    }

    private static void setTimestampOrNull(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value.atStartOfDay()));
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

    private enum OperacionResolucion {
        FIRMA,
        EMISION,
        NUMERACION
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
