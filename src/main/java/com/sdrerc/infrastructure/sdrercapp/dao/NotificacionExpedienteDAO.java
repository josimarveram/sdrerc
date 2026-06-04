package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.CargoAcuseDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreNotificacionDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionRequeridaDTO;
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

public class NotificacionExpedienteDAO {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String ETAPA_NOTIFICACION = "NOTIFICACION";
    private static final String ETAPA_PUBLICACION = "PUBLICACION_CONDICIONAL";
    private static final String ETAPA_CIERRE = "CIERRE_ARCHIVO";
    private static final String ESTADO_EN_NOTIFICACION = "EN_NOTIFICACION";
    private static final String ESTADO_CARGO_PENDIENTE = "CARGO_PENDIENTE";
    private static final String ESTADO_CARGO_RECIBIDO = "CARGO_RECIBIDO";
    private static final String ESTADO_NOTIFICADO = "NOTIFICADO";
    private static final String ESTADO_REQUIERE_PUBLICACION = "REQUIERE_PUBLICACION";
    private static final String ESTADO_PENDIENTE_PUBLICACION = "PENDIENTE_PUBLICACION";
    private static final String ESTADO_CERRADO = "CERRADO";
    private static final String ESTADO_NOTIFICACION_ENVIADA = "ENVIADA";
    private static final String ESTADO_NOTIFICACION_EXITOSA = "EXITOSA";
    private static final String ESTADO_NOTIFICACION_FALLIDA = "FALLIDA";
    private static final String ESTADO_CARGO_ACUSE_RECIBIDO = "CARGO_RECIBIDO";
    private static final String ACCION_NOTIFICACION_VIRTUAL = "NOTIFICACION_VIRTUAL";
    private static final String ACCION_NOTIFICACION_PRESENCIAL_1 = "NOTIFICACION_PRESENCIAL_1";
    private static final String ACCION_NOTIFICACION_PRESENCIAL_2 = "NOTIFICACION_PRESENCIAL_2";
    private static final String ACCION_RECEPCION_CARGO = "RECEPCION_CARGO_ACUSE";
    private static final String ACCION_CONFIRMACION = "CONFIRMACION_NOTIFICACION";
    private static final String ACCION_NOTIFICACION_FALLIDA = "REGISTRO_NOTIFICACION_FALLIDA";
    private static final String ACCION_GENERACION_PUBLICACION = "GENERACION_PUBLICACION";
    private static final String ACCION_CIERRE = "CIERRE";

    private final CatalogoLookupDAO catalogoLookupDAO;
    private final ResolucionDocumentoDAO resolucionDocumentoDAO;

    public NotificacionExpedienteDAO() {
        this(new CatalogoLookupDAO(), new ResolucionDocumentoDAO());
    }

    public NotificacionExpedienteDAO(
            CatalogoLookupDAO catalogoLookupDAO,
            ResolucionDocumentoDAO resolucionDocumentoDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.resolucionDocumentoDAO = resolucionDocumentoDAO;
    }

    public List<NotificacionExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("SELECT DISTINCT e.id_expediente, e.numero_expediente, e.numero_tramite_documentario, ");
        sql.append("esol.asunto AS procedimiento, p.tipo_documento, ");
        sql.append("ta.nombre AS tipo_acta, ea.numero_acta, ").append(nombrePersona("p")).append(" AS titular, ");
        sql.append("esol.fecha_recepcion, e.fecha_ultimo_movimiento, ");
        sql.append("(SELECT MAX(h.fecha_movimiento) FROM expediente_historial h ");
        sql.append("JOIN etapa_expediente edh ON edh.id_etapa = h.id_etapa_destino ");
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 AND edh.codigo = 'NOTIFICACION') AS fecha_ingreso_notificacion, ");
        sql.append("ur.nombre_completo AS responsable, eq.nombre AS equipo, et.codigo AS etapa_codigo, est.codigo AS estado_codigo, ");
        sql.append("(SELECT MAX(NVL(tre.nombre, tre.codigo)) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) ");
        sql.append("FROM expediente_evaluacion ev LEFT JOIN tipo_resultado_evaluacion tre ON tre.id_tipo_resultado_evaluacion = ev.id_tipo_resultado_evaluacion ");
        sql.append("WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS resultado_analisis, ");
        sql.append("(SELECT CASE WHEN COUNT(*) > 0 THEN 'Verificación aprobada' ELSE '' END ");
        sql.append("FROM expediente_historial h JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = h.id_tipo_movimiento ");
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 AND tm.codigo = 'APROBACION_VERIFICACION') AS resultado_verificacion, ");
        sql.append("(SELECT CASE WHEN COUNT(*) > 0 THEN 'Ejecución registrada' ELSE '' END ");
        sql.append("FROM expediente_historial h JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = h.id_tipo_movimiento ");
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 AND tm.codigo IN ('INICIO_EJECUCION','DERIVACION_A_NOTIFICACION')) AS resultado_ejecucion, ");
        sql.append("(SELECT MAX(o.descripcion) KEEP (DENSE_RANK LAST ORDER BY o.fecha_observacion) ");
        sql.append("FROM expediente_observacion o WHERE o.id_expediente = e.id_expediente AND o.activo = 1) AS ultima_observacion, ");
        sql.append("(SELECT COUNT(*) FROM expediente_documento d WHERE d.id_expediente = e.id_expediente AND d.activo = 1) AS documentos, ");
        sql.append("(SELECT COUNT(*) FROM expediente_relacion r WHERE r.activo = 1 AND (r.id_expediente_principal = e.id_expediente OR r.id_expediente_relacionado = e.id_expediente)) AS relaciones_confirmadas, ");
        sql.append("res.id_expediente_resolucion, tr.nombre AS tipo_resolucion, res.numero_resolucion, res.fecha_resolucion, ");
        sql.append("n.id_expediente_notificacion, tn.nombre AS tipo_notificacion, en.nombre AS estado_notificacion, ");
        sql.append("n.numero_intento, n.fecha_envio, n.resultado AS resultado_notificacion, n.requiere_publicacion, ");
        sql.append("ca.id_expediente_cargo_acuse, eca.nombre AS estado_cargo, ca.fecha_recepcion AS fecha_cargo, ca.recibido_por, ");
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
        sql.append("LEFT JOIN (SELECT id_expediente, MAX(id_expediente_resolucion) KEEP (DENSE_RANK LAST ORDER BY creado_en, id_expediente_resolucion) AS id_expediente_resolucion ");
        sql.append("FROM expediente_resolucion WHERE activo = 1 GROUP BY id_expediente) res_pick ON res_pick.id_expediente = e.id_expediente ");
        sql.append("LEFT JOIN expediente_resolucion res ON res.id_expediente_resolucion = res_pick.id_expediente_resolucion ");
        sql.append("LEFT JOIN tipo_resolucion tr ON tr.id_tipo_resolucion = res.id_tipo_resolucion ");
        sql.append("LEFT JOIN (SELECT id_expediente, MAX(id_expediente_notificacion) KEEP (DENSE_RANK LAST ORDER BY creado_en, id_expediente_notificacion) AS id_expediente_notificacion ");
        sql.append("FROM expediente_notificacion WHERE activo = 1 GROUP BY id_expediente) n_pick ON n_pick.id_expediente = e.id_expediente ");
        sql.append("LEFT JOIN expediente_notificacion n ON n.id_expediente_notificacion = n_pick.id_expediente_notificacion ");
        sql.append("LEFT JOIN tipo_notificacion tn ON tn.id_tipo_notificacion = n.id_tipo_notificacion ");
        sql.append("LEFT JOIN estado_notificacion en ON en.id_estado_notificacion = n.id_estado_notificacion ");
        sql.append("LEFT JOIN (SELECT id_expediente, MAX(id_expediente_cargo_acuse) KEEP (DENSE_RANK LAST ORDER BY creado_en, id_expediente_cargo_acuse) AS id_expediente_cargo_acuse ");
        sql.append("FROM expediente_cargo_acuse WHERE activo = 1 GROUP BY id_expediente) ca_pick ON ca_pick.id_expediente = e.id_expediente ");
        sql.append("LEFT JOIN expediente_cargo_acuse ca ON ca.id_expediente_cargo_acuse = ca_pick.id_expediente_cargo_acuse ");
        sql.append("LEFT JOIN estado_cargo_acuse eca ON eca.id_estado_cargo_acuse = ca.id_estado_cargo_acuse ");
        sql.append("WHERE e.activo = 1 AND et.codigo = ? ");
        params.add(ETAPA_NOTIFICACION);
        sql.append("AND est.codigo IN (?, ?, ?, ?, ?) ");
        params.add(ESTADO_EN_NOTIFICACION);
        params.add(ESTADO_CARGO_PENDIENTE);
        params.add(ESTADO_CARGO_RECIBIDO);
        params.add(ESTADO_NOTIFICADO);
        params.add(ESTADO_REQUIERE_PUBLICACION);

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
            sql.append("OR UPPER(NVL(").append(nombrePersona("p")).append(", '')) LIKE ?) ");
            for (int i = 0; i < 6; i++) {
                params.add(pattern);
            }
        }

        sql.append("ORDER BY e.fecha_ultimo_movimiento ASC NULLS LAST, e.id_expediente ASC");
        sql.append(") WHERE ROWNUM <= ?");
        params.add(normalizarLimite(limite));

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<NotificacionExpedienteDTO> expedientes = new ArrayList<NotificacionExpedienteDTO>();
                while (rs.next()) {
                    expedientes.add(map(rs));
                }
                return expedientes;
            }
        }
    }

    public NotificacionResultadoDTO registrarNotificacion(NotificacionRegistroDTO registro, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, registro.getIdExpediente());
                String estadoOrigen = estadoOrigenNotificacion(registro.getAccionCodigo());
                if (!ETAPA_NOTIFICACION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !estadoOrigen.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente ya no se encuentra en el estado requerido para registrar la notificación.");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        registro.getAccionCodigo(),
                        ETAPA_NOTIFICACION,
                        estadoOrigen,
                        ETAPA_NOTIFICACION,
                        ESTADO_CARGO_PENDIENTE);
                validarRequisitosTransicion(conn, transicion, registro.getComentario(), registro.getIdExpediente(), false);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, registro.getAccionCodigo()), "movimiento " + registro.getAccionCodigo());
                Long idNotificacion = insertarNotificacion(conn, registro, idUsuario);
                actualizarExpediente(conn, registro.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario, false, false);
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
                        "EXPEDIENTE_NOTIFICACION",
                        idNotificacion,
                        comentarioMovimiento(registro.getComentario(), "Registro de notificación."),
                        registro.getTipoNotificacionCodigo());
                conn.commit();
                return new NotificacionResultadoDTO(
                        registro.getIdExpediente(),
                        expediente.numeroExpediente,
                        registro.getAccionCodigo(),
                        ETAPA_NOTIFICACION,
                        ESTADO_CARGO_PENDIENTE,
                        "La notificación fue registrada correctamente.");
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

    public NotificacionResultadoDTO registrarCargo(CargoAcuseDTO cargo, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, cargo.getIdExpediente());
                if (!ETAPA_NOTIFICACION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !ESTADO_CARGO_PENDIENTE.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente debe tener cargo pendiente para registrar el acuse.");
                }
                NotificacionActiva notificacion = requerirNotificacionActiva(conn, cargo.getIdExpediente());
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_RECEPCION_CARGO,
                        ETAPA_NOTIFICACION,
                        ESTADO_CARGO_PENDIENTE,
                        ETAPA_NOTIFICACION,
                        ESTADO_CARGO_RECIBIDO);
                validarRequisitosTransicion(conn, transicion, cargo.getComentario(), cargo.getIdExpediente(), false);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_RECEPCION_CARGO), "movimiento " + ACCION_RECEPCION_CARGO);
                Long idCargo = insertarCargoAcuse(conn, cargo, notificacion.idNotificacion, idUsuario);
                actualizarExpediente(conn, cargo.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario, false, false);
                insertarHistorial(
                        conn,
                        cargo.getIdExpediente(),
                        idMovimiento,
                        expediente.idEtapa,
                        expediente.idEstado,
                        transicion.idEtapaDestino,
                        transicion.idEstadoDestino,
                        idUsuario,
                        expediente.idUsuarioResponsable,
                        expediente.idEquipoResponsable,
                        "EXPEDIENTE_CARGO_ACUSE",
                        idCargo,
                        comentarioMovimiento(cargo.getComentario(), "Recepción de cargo de acuse."),
                        cargo.getRecibidoPor());
                conn.commit();
                return new NotificacionResultadoDTO(
                        cargo.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_RECEPCION_CARGO,
                        ETAPA_NOTIFICACION,
                        ESTADO_CARGO_RECIBIDO,
                        "El cargo de acuse fue registrado correctamente.");
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

    public NotificacionResultadoDTO marcarNotificado(NotificacionRegistroDTO registro, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, registro.getIdExpediente());
                if (!ETAPA_NOTIFICACION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !ESTADO_CARGO_RECIBIDO.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente debe tener cargo recibido para confirmar la notificación.");
                }
                NotificacionActiva notificacion = requerirNotificacionActiva(conn, registro.getIdExpediente());
                requerirCargoActivo(conn, registro.getIdExpediente());
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_CONFIRMACION,
                        ETAPA_NOTIFICACION,
                        ESTADO_CARGO_RECIBIDO,
                        ETAPA_NOTIFICACION,
                        ESTADO_NOTIFICADO);
                validarRequisitosTransicion(conn, transicion, registro.getComentario(), registro.getIdExpediente(), false);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_CONFIRMACION), "movimiento " + ACCION_CONFIRMACION);
                actualizarEstadoNotificacion(conn, notificacion.idNotificacion, ESTADO_NOTIFICACION_EXITOSA, registro.getResultado(), idUsuario);
                actualizarExpediente(conn, registro.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario, false, false);
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
                        "EXPEDIENTE_NOTIFICACION",
                        notificacion.idNotificacion,
                        comentarioMovimiento(registro.getComentario(), "Confirmación de notificación."),
                        registro.getResultado());
                conn.commit();
                return new NotificacionResultadoDTO(
                        registro.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_CONFIRMACION,
                        ETAPA_NOTIFICACION,
                        ESTADO_NOTIFICADO,
                        "El expediente fue marcado como notificado.");
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

    public NotificacionResultadoDTO registrarPublicacion(PublicacionRequeridaDTO publicacion, Long idUsuario) throws SQLException {
        if (ACCION_GENERACION_PUBLICACION.equalsIgnoreCase(publicacion.getAccionCodigo())) {
            return generarPublicacion(publicacion, idUsuario);
        }
        return registrarNotificacionFallida(publicacion, idUsuario);
    }

    public NotificacionResultadoDTO cerrarExpediente(CierreNotificacionDTO cierre, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, cierre.getIdExpediente());
                if (!ETAPA_NOTIFICACION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !ESTADO_NOTIFICADO.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente debe estar notificado para cerrarlo.");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_CIERRE,
                        ETAPA_NOTIFICACION,
                        ESTADO_NOTIFICADO,
                        ETAPA_CIERRE,
                        ESTADO_CERRADO);
                validarRequisitosTransicion(conn, transicion, cierre.getComentario(), cierre.getIdExpediente(), false);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_CIERRE), "movimiento " + ACCION_CIERRE);
                actualizarExpediente(conn, cierre.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario, false, true);
                insertarHistorial(
                        conn,
                        cierre.getIdExpediente(),
                        idMovimiento,
                        expediente.idEtapa,
                        expediente.idEstado,
                        transicion.idEtapaDestino,
                        transicion.idEstadoDestino,
                        idUsuario,
                        expediente.idUsuarioResponsable,
                        expediente.idEquipoResponsable,
                        "EXPEDIENTE",
                        cierre.getIdExpediente(),
                        comentarioMovimiento(cierre.getComentario(), "Cierre de expediente notificado."),
                        "Notificación confirmada");
                conn.commit();
                return new NotificacionResultadoDTO(
                        cierre.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_CIERRE,
                        ETAPA_CIERRE,
                        ESTADO_CERRADO,
                        "El expediente fue cerrado correctamente.");
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

    private NotificacionResultadoDTO registrarNotificacionFallida(PublicacionRequeridaDTO publicacion, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, publicacion.getIdExpediente());
                if (!ETAPA_NOTIFICACION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !ESTADO_CARGO_PENDIENTE.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente debe tener cargo pendiente para registrar notificación fallida.");
                }
                NotificacionActiva notificacion = requerirNotificacionActiva(conn, publicacion.getIdExpediente());
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_NOTIFICACION_FALLIDA,
                        ETAPA_NOTIFICACION,
                        ESTADO_CARGO_PENDIENTE,
                        ETAPA_NOTIFICACION,
                        ESTADO_REQUIERE_PUBLICACION);
                validarRequisitosTransicion(conn, transicion, publicacion.getComentario(), publicacion.getIdExpediente(), true);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_NOTIFICACION_FALLIDA), "movimiento " + ACCION_NOTIFICACION_FALLIDA);
                actualizarEstadoNotificacion(conn, notificacion.idNotificacion, ESTADO_NOTIFICACION_FALLIDA, publicacion.getMotivo(), idUsuario);
                marcarNotificacionRequierePublicacion(conn, notificacion.idNotificacion, idUsuario);
                actualizarExpediente(conn, publicacion.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario, true, false);
                insertarHistorial(
                        conn,
                        publicacion.getIdExpediente(),
                        idMovimiento,
                        expediente.idEtapa,
                        expediente.idEstado,
                        transicion.idEtapaDestino,
                        transicion.idEstadoDestino,
                        idUsuario,
                        expediente.idUsuarioResponsable,
                        expediente.idEquipoResponsable,
                        "EXPEDIENTE_NOTIFICACION",
                        notificacion.idNotificacion,
                        comentarioMovimiento(publicacion.getComentario(), "Notificación fallida. Requiere publicación."),
                        publicacion.getMotivo());
                conn.commit();
                return new NotificacionResultadoDTO(
                        publicacion.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_NOTIFICACION_FALLIDA,
                        ETAPA_NOTIFICACION,
                        ESTADO_REQUIERE_PUBLICACION,
                        "La notificación fallida fue registrada y requiere publicación.");
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

    private NotificacionResultadoDTO generarPublicacion(PublicacionRequeridaDTO publicacion, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, publicacion.getIdExpediente());
                if (!ETAPA_NOTIFICACION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !ESTADO_REQUIERE_PUBLICACION.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente debe estar en Requiere publicación para derivarlo.");
                }
                NotificacionActiva notificacion = requerirNotificacionActiva(conn, publicacion.getIdExpediente());
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_GENERACION_PUBLICACION,
                        ETAPA_NOTIFICACION,
                        ESTADO_REQUIERE_PUBLICACION,
                        ETAPA_PUBLICACION,
                        ESTADO_PENDIENTE_PUBLICACION);
                validarRequisitosTransicion(conn, transicion, publicacion.getComentario(), publicacion.getIdExpediente(), true);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_GENERACION_PUBLICACION), "movimiento " + ACCION_GENERACION_PUBLICACION);
                Long idPublicacion = insertarPublicacionSiNoExiste(conn, publicacion.getIdExpediente(), notificacion.idNotificacion, publicacion, idUsuario);
                actualizarExpediente(conn, publicacion.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario, true, false);
                insertarHistorial(
                        conn,
                        publicacion.getIdExpediente(),
                        idMovimiento,
                        expediente.idEtapa,
                        expediente.idEstado,
                        transicion.idEtapaDestino,
                        transicion.idEstadoDestino,
                        idUsuario,
                        expediente.idUsuarioResponsable,
                        expediente.idEquipoResponsable,
                        "EXPEDIENTE_PUBLICACION",
                        idPublicacion,
                        comentarioMovimiento(publicacion.getComentario(), "Derivación a publicación condicional."),
                        publicacion.getMotivo());
                conn.commit();
                return new NotificacionResultadoDTO(
                        publicacion.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_GENERACION_PUBLICACION,
                        ETAPA_PUBLICACION,
                        ESTADO_PENDIENTE_PUBLICACION,
                        "El expediente fue derivado a Publicación condicional.");
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

    private Long insertarNotificacion(Connection conn, NotificacionRegistroDTO registro, Long idUsuario) throws SQLException {
        Long idTipoNotificacion = requerirId(catalogoLookupDAO.obtenerTipoNotificacionId(conn, registro.getTipoNotificacionCodigo()), "tipo de notificación " + registro.getTipoNotificacionCodigo());
        Long idEstadoNotificacion = requerirId(catalogoLookupDAO.obtenerEstadoNotificacionId(conn, ESTADO_NOTIFICACION_ENVIADA), "estado de notificación " + ESTADO_NOTIFICACION_ENVIADA);
        Long idResolucion = resolucionDocumentoDAO.obtenerIdResolucionActiva(conn, registro.getIdExpediente());
        int intento = siguienteIntento(conn, registro.getIdExpediente());
        String sql = "INSERT INTO expediente_notificacion ("
                + "id_expediente, id_expediente_resolucion, id_tipo_notificacion, id_estado_notificacion, "
                + "numero_intento, fecha_programada, fecha_envio, resultado, requiere_publicacion, observacion, "
                + "activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, ?, SYSTIMESTAMP, ?, 0, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_NOTIFICACION"})) {
            ps.setLong(1, registro.getIdExpediente());
            setLongOrNull(ps, 2, idResolucion);
            ps.setLong(3, idTipoNotificacion);
            ps.setLong(4, idEstadoNotificacion);
            ps.setInt(5, intento);
            if (registro.getFechaNotificacion() == null) {
                ps.setNull(6, Types.DATE);
            } else {
                ps.setDate(6, Date.valueOf(registro.getFechaNotificacion()));
            }
            ps.setString(7, limitar(hasText(registro.getResultado()) ? registro.getResultado() : "Notificación registrada", 150));
            ps.setString(8, limitar(observacionConDestinatario(registro.getDestinatario(), registro.getComentario()), 1000));
            setLongOrNull(ps, 9, idUsuario);
            ps.executeUpdate();
            return generatedId(ps, "No se pudo obtener el identificador de la notificación.");
        }
    }

    private Long insertarCargoAcuse(Connection conn, CargoAcuseDTO cargo, Long idNotificacion, Long idUsuario) throws SQLException {
        Long idEstadoCargo = requerirId(catalogoLookupDAO.obtenerEstadoCargoAcuseId(conn, cargo.getEstadoCargoCodigo()), "estado de cargo " + cargo.getEstadoCargoCodigo());
        String sql = "INSERT INTO expediente_cargo_acuse ("
                + "id_expediente, id_expediente_notificacion, id_estado_cargo_acuse, fecha_recepcion, "
                + "recibido_por, observacion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_CARGO_ACUSE"})) {
            ps.setLong(1, cargo.getIdExpediente());
            ps.setLong(2, idNotificacion);
            ps.setLong(3, idEstadoCargo);
            if (cargo.getFechaCargo() == null) {
                ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            } else {
                ps.setTimestamp(4, Timestamp.valueOf(cargo.getFechaCargo().atStartOfDay()));
            }
            ps.setString(5, limitar(cargo.getRecibidoPor(), 250));
            ps.setString(6, limitar(cargo.getComentario(), 1000));
            setLongOrNull(ps, 7, idUsuario);
            ps.executeUpdate();
            return generatedId(ps, "No se pudo obtener el identificador del cargo.");
        }
    }

    private Long insertarPublicacionSiNoExiste(
            Connection conn,
            Long idExpediente,
            Long idNotificacion,
            PublicacionRequeridaDTO publicacion,
            Long idUsuario) throws SQLException {
        Long existing = obtenerPublicacionActiva(conn, idExpediente);
        if (existing != null) {
            return existing;
        }
        String sql = "INSERT INTO expediente_publicacion ("
                + "id_expediente, id_expediente_notificacion, tipo_publicacion, estado_publicacion, "
                + "fecha_generacion, observacion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, 'NOTIFICACION_FALLIDA', 'PENDIENTE_PUBLICACION', SYSDATE, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_PUBLICACION"})) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idNotificacion);
            ps.setString(3, limitar(comentarioMovimiento(publicacion.getComentario(), publicacion.getMotivo()), 1500));
            setLongOrNull(ps, 4, idUsuario);
            ps.executeUpdate();
            return generatedId(ps, "No se pudo obtener el identificador de la publicación.");
        }
    }

    private Long obtenerPublicacionActiva(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT id_expediente_publicacion FROM ("
                + "SELECT id_expediente_publicacion FROM expediente_publicacion "
                + "WHERE id_expediente = ? AND activo = 1 "
                + "ORDER BY creado_en DESC, id_expediente_publicacion DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return getLongOrNull(rs, "id_expediente_publicacion");
            }
        }
    }

    private void actualizarEstadoNotificacion(
            Connection conn,
            Long idNotificacion,
            String estadoCodigo,
            String resultado,
            Long idUsuario) throws SQLException {
        Long idEstado = requerirId(catalogoLookupDAO.obtenerEstadoNotificacionId(conn, estadoCodigo), "estado de notificación " + estadoCodigo);
        String sql = "UPDATE expediente_notificacion SET "
                + "id_estado_notificacion = ?, resultado = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente_notificacion = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEstado);
            ps.setString(2, limitar(hasText(resultado) ? resultado : estadoCodigo, 150));
            setLongOrNull(ps, 3, idUsuario);
            ps.setLong(4, idNotificacion);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar la notificación.");
            }
        }
    }

    private void marcarNotificacionRequierePublicacion(Connection conn, Long idNotificacion, Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_notificacion SET requiere_publicacion = 1, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente_notificacion = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setLongOrNull(ps, 1, idUsuario);
            ps.setLong(2, idNotificacion);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo marcar la notificación para publicación.");
            }
        }
    }

    private void actualizarExpediente(
            Connection conn,
            Long idExpediente,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idUsuarioModificador,
            boolean requierePublicacion,
            boolean cerrarExpediente) throws SQLException {
        String sql = "UPDATE expediente SET "
                + "id_etapa_actual = ?, id_estado_actual = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, "
                + (requierePublicacion ? "requiere_publicacion = 1, " : "")
                + (cerrarExpediente ? "cerrado = 1, " : "")
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEtapaDestino);
            ps.setLong(2, idEstadoDestino);
            setLongOrNull(ps, 3, idUsuarioModificador);
            ps.setLong(4, idExpediente);
            if (ps.executeUpdate() != 1) {
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
            throw new SQLException("No existe documento, resolución o cargo registrado para sustentar la acción.");
        }
    }

    private boolean tieneDocumentoSoporte(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT ("
                + "(SELECT COUNT(*) FROM expediente_documento d WHERE d.id_expediente = ? AND d.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_documento_analizado da WHERE da.id_expediente = ? AND da.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_resolucion r WHERE r.id_expediente = ? AND r.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_cargo_acuse c WHERE c.id_expediente = ? AND c.activo = 1)"
                + ") AS total FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idExpediente);
            ps.setLong(3, idExpediente);
            ps.setLong(4, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
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

    private NotificacionActiva requerirNotificacionActiva(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT id_expediente_notificacion, numero_intento FROM ("
                + "SELECT id_expediente_notificacion, numero_intento FROM expediente_notificacion "
                + "WHERE id_expediente = ? AND activo = 1 "
                + "ORDER BY creado_en DESC, id_expediente_notificacion DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El expediente no tiene notificación activa registrada.");
                }
                return new NotificacionActiva(
                        getLongOrNull(rs, "id_expediente_notificacion"),
                        rs.getInt("numero_intento"));
            }
        }
    }

    private Long requerirCargoActivo(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT id_expediente_cargo_acuse FROM ("
                + "SELECT id_expediente_cargo_acuse FROM expediente_cargo_acuse "
                + "WHERE id_expediente = ? AND activo = 1 "
                + "ORDER BY creado_en DESC, id_expediente_cargo_acuse DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El expediente no tiene cargo de acuse registrado.");
                }
                return getLongOrNull(rs, "id_expediente_cargo_acuse");
            }
        }
    }

    private int siguienteIntento(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT NVL(MAX(numero_intento), 0) + 1 AS intento FROM expediente_notificacion WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return 1;
                }
                int intento = rs.getInt("intento");
                if (intento > 3) {
                    throw new SQLException("El expediente ya alcanzó el máximo de tres intentos de notificación.");
                }
                return intento;
            }
        }
    }

    private NotificacionExpedienteDTO map(ResultSet rs) throws SQLException {
        Integer numeroIntento = null;
        int intentoValue = rs.getInt("numero_intento");
        if (!rs.wasNull()) {
            numeroIntento = intentoValue;
        }
        return new NotificacionExpedienteDTO(
                getLongOrNull(rs, "id_expediente"),
                rs.getString("numero_expediente"),
                rs.getString("numero_tramite_documentario"),
                rs.getString("procedimiento"),
                rs.getString("tipo_documento"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                toLocalDateTime(rs.getTimestamp("fecha_ingreso_notificacion")),
                toLocalDateTime(rs.getTimestamp("fecha_ultimo_movimiento")),
                rs.getString("responsable"),
                rs.getString("equipo"),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getString("resultado_analisis"),
                rs.getString("resultado_verificacion"),
                rs.getString("resultado_ejecucion"),
                rs.getString("ultima_observacion"),
                rs.getInt("documentos"),
                rs.getInt("relaciones_confirmadas"),
                getLongOrNull(rs, "id_expediente_resolucion"),
                rs.getString("tipo_resolucion"),
                rs.getString("numero_resolucion"),
                toLocalDate(rs.getDate("fecha_resolucion")),
                getLongOrNull(rs, "id_expediente_notificacion"),
                rs.getString("tipo_notificacion"),
                rs.getString("estado_notificacion"),
                numeroIntento,
                toLocalDateTime(rs.getTimestamp("fecha_envio")),
                rs.getString("resultado_notificacion"),
                rs.getInt("requiere_publicacion") == 1,
                getLongOrNull(rs, "id_expediente_cargo_acuse"),
                rs.getString("estado_cargo"),
                toLocalDateTime(rs.getTimestamp("fecha_cargo")),
                rs.getString("recibido_por"),
                rs.getString("acciones_permitidas"));
    }

    private static String estadoOrigenNotificacion(String accionCodigo) throws SQLException {
        if (ACCION_NOTIFICACION_VIRTUAL.equalsIgnoreCase(accionCodigo)
                || ACCION_NOTIFICACION_PRESENCIAL_1.equalsIgnoreCase(accionCodigo)) {
            return ESTADO_EN_NOTIFICACION;
        }
        if (ACCION_NOTIFICACION_PRESENCIAL_2.equalsIgnoreCase(accionCodigo)) {
            return ESTADO_CARGO_PENDIENTE;
        }
        throw new SQLException("Acción de notificación no soportada: " + accionCodigo + ".");
    }

    private Long requerirId(Long value, String descripcion) throws SQLException {
        if (value == null) {
            throw new SQLException("No se encontró el catálogo requerido: " + descripcion + ".");
        }
        return value;
    }

    private static Long generatedId(PreparedStatement ps, String mensajeError) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException(mensajeError);
            }
            return keys.getLong(1);
        }
    }

    private static String observacionConDestinatario(String destinatario, String comentario) {
        if (hasText(destinatario) && hasText(comentario)) {
            return "Destinatario: " + destinatario.trim() + ". " + comentario.trim();
        }
        if (hasText(destinatario)) {
            return "Destinatario: " + destinatario.trim();
        }
        return comentario;
    }

    private static String comentarioMovimiento(String comentario, String defecto) {
        if (hasText(comentario)) {
            return comentario.trim();
        }
        return hasText(defecto) ? defecto : "";
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
            // Se conserva el error original.
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

    private static class NotificacionActiva {

        private final Long idNotificacion;
        private final int numeroIntento;

        private NotificacionActiva(Long idNotificacion, int numeroIntento) {
            this.idNotificacion = idNotificacion;
            this.numeroIntento = numeroIntento;
        }
    }
}
