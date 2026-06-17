package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.CierrePublicacionDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionResultadoDTO;
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

public class PublicacionExpedienteDAO {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String ETAPA_PUBLICACION = "PUBLICACION_CONDICIONAL";
    private static final String ETAPA_CIERRE = "CIERRE_ARCHIVO";
    private static final String ESTADO_PENDIENTE_PUBLICACION = "PENDIENTE_PUBLICACION";
    private static final String ESTADO_PUBLICACION_REGISTRADA = "PUBLICACION_REGISTRADA";
    private static final String ESTADO_CERRADO = "CERRADO";
    private static final String ACCION_REGISTRO_PUBLICACION = "REGISTRO_PUBLICACION";
    private static final String ACCION_CIERRE = "CIERRE";

    private final CatalogoLookupDAO catalogoLookupDAO;

    public PublicacionExpedienteDAO() {
        this(new CatalogoLookupDAO());
    }

    public PublicacionExpedienteDAO(CatalogoLookupDAO catalogoLookupDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    public List<PublicacionExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("SELECT DISTINCT e.id_expediente, e.numero_expediente, e.numero_tramite_documentario, ");
        sql.append("esol.asunto AS procedimiento, p.tipo_documento, ");
        sql.append("ta.nombre AS tipo_acta, ea.numero_acta, ").append(nombrePersona("p")).append(" AS titular, ");
        sql.append("esol.fecha_recepcion, CASE WHEN e.fecha_vencimiento IS NULL THEN NULL ");
        sql.append("ELSE TRUNC(e.fecha_vencimiento) - TRUNC(SYSDATE) END AS dias_restantes, ");
        sql.append("e.fecha_ultimo_movimiento, ");
        sql.append("(SELECT MAX(h.fecha_movimiento) FROM expediente_historial h ");
        sql.append("JOIN etapa_expediente edh ON edh.id_etapa = h.id_etapa_destino ");
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 AND edh.codigo = 'PUBLICACION_CONDICIONAL') AS fecha_ingreso_publicacion, ");
        sql.append("ur.nombre_completo AS responsable, eq.nombre AS equipo, et.codigo AS etapa_codigo, est.codigo AS estado_codigo, ");
        sql.append("(SELECT MAX(NVL(tre.nombre, tre.codigo)) KEEP (DENSE_RANK LAST ORDER BY ev.fecha_evaluacion NULLS FIRST, ev.creado_en) ");
        sql.append("FROM expediente_evaluacion ev LEFT JOIN tipo_resultado_evaluacion tre ON tre.id_tipo_resultado_evaluacion = ev.id_tipo_resultado_evaluacion ");
        sql.append("WHERE ev.id_expediente = e.id_expediente AND ev.activo = 1) AS resultado_analisis, ");
        sql.append("(SELECT CASE WHEN COUNT(*) > 0 THEN 'Verificación aprobada' ELSE '' END ");
        sql.append("FROM expediente_historial h JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = h.id_tipo_movimiento ");
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 AND tm.codigo IN ('APROBACION_VERIFICACION','ENVIO_FIRMA')) AS resultado_verificacion, ");
        sql.append("(SELECT CASE WHEN COUNT(*) > 0 THEN 'Ejecución registrada' ELSE '' END ");
        sql.append("FROM expediente_historial h JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = h.id_tipo_movimiento ");
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 AND tm.codigo IN ('INICIO_EJECUCION','DERIVACION_A_NOTIFICACION')) AS resultado_ejecucion, ");
        sql.append("(SELECT MAX(o.descripcion) KEEP (DENSE_RANK LAST ORDER BY o.fecha_observacion) ");
        sql.append("FROM expediente_observacion o WHERE o.id_expediente = e.id_expediente AND o.activo = 1) AS ultima_observacion, ");
        sql.append("(SELECT COUNT(*) FROM expediente_documento d WHERE d.id_expediente = e.id_expediente AND d.activo = 1) AS documentos, ");
        sql.append("(SELECT COUNT(*) FROM expediente_relacion r WHERE r.activo = 1 AND (r.id_expediente_principal = e.id_expediente OR r.id_expediente_relacionado = e.id_expediente)) AS relaciones_confirmadas, ");
        sql.append("res.id_expediente_resolucion, tr.nombre AS tipo_resolucion, res.numero_resolucion, res.fecha_resolucion, ");
        sql.append("n.id_expediente_notificacion, tn.nombre AS tipo_notificacion, en.nombre AS estado_notificacion, ");
        sql.append("n.resultado AS resultado_notificacion, n.requiere_publicacion, ");
        sql.append("ca.id_expediente_cargo_acuse, eca.nombre AS estado_cargo, ca.fecha_recepcion AS fecha_cargo, ");
        sql.append("pub.id_expediente_publicacion, pub.tipo_publicacion, pub.estado_publicacion, pub.fecha_generacion, ");
        sql.append("pub.fecha_publicacion, pub.medio_publicacion, pub.numero_publicacion, pub.observacion AS observacion_publicacion, ");
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
        sql.append("LEFT JOIN (SELECT id_expediente, MAX(id_expediente_publicacion) KEEP (DENSE_RANK LAST ORDER BY creado_en, id_expediente_publicacion) AS id_expediente_publicacion ");
        sql.append("FROM expediente_publicacion WHERE activo = 1 GROUP BY id_expediente) pub_pick ON pub_pick.id_expediente = e.id_expediente ");
        sql.append("LEFT JOIN expediente_publicacion pub ON pub.id_expediente_publicacion = pub_pick.id_expediente_publicacion ");
        sql.append("WHERE e.activo = 1 AND et.codigo = ? ");
        params.add(ETAPA_PUBLICACION);

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
            sql.append("OR UPPER(NVL(pub.numero_publicacion, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(").append(nombrePersona("p")).append(", '')) LIKE ? ");
            sql.append("OR UPPER(NVL(esol.numero_expediente_sgd, '')) LIKE ?) ");
            for (int i = 0; i < 8; i++) {
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
                List<PublicacionExpedienteDTO> expedientes = new ArrayList<PublicacionExpedienteDTO>();
                while (rs.next()) {
                    expedientes.add(map(rs));
                }
                return expedientes;
            }
        }
    }

    public PublicacionResultadoDTO registrarPublicacion(PublicacionRegistroDTO registro, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, registro.getIdExpediente());
                if (!ETAPA_PUBLICACION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !ESTADO_PENDIENTE_PUBLICACION.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente debe estar pendiente de publicación para registrar la publicación.");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_REGISTRO_PUBLICACION,
                        ETAPA_PUBLICACION,
                        ESTADO_PENDIENTE_PUBLICACION,
                        ETAPA_PUBLICACION,
                        ESTADO_PUBLICACION_REGISTRADA);
                validarRequisitosTransicion(conn, transicion, registro.getComentario(), registro.getIdExpediente(), false);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_REGISTRO_PUBLICACION), "movimiento " + ACCION_REGISTRO_PUBLICACION);
                Long idPublicacion = guardarPublicacion(conn, registro, idUsuario);
                actualizarExpediente(conn, registro.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario, false);
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
                        "EXPEDIENTE_PUBLICACION",
                        idPublicacion,
                        comentarioMovimiento(registro.getComentario(), "Registro de publicación efectuada."),
                        motivoPublicacion(registro));
                conn.commit();
                return new PublicacionResultadoDTO(
                        registro.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_REGISTRO_PUBLICACION,
                        ETAPA_PUBLICACION,
                        ESTADO_PUBLICACION_REGISTRADA,
                        "La publicación fue registrada correctamente.");
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

    public PublicacionResultadoDTO cerrarExpediente(CierrePublicacionDTO cierre, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, cierre.getIdExpediente());
                if (!ETAPA_PUBLICACION.equalsIgnoreCase(expediente.etapaCodigo)
                        || !ESTADO_PUBLICACION_REGISTRADA.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente debe tener publicación registrada para cerrarlo.");
                }
                Long idPublicacion = requerirPublicacionActiva(conn, cierre.getIdExpediente());
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_CIERRE,
                        ETAPA_PUBLICACION,
                        ESTADO_PUBLICACION_REGISTRADA,
                        ETAPA_CIERRE,
                        ESTADO_CERRADO);
                validarRequisitosTransicion(conn, transicion, cierre.getComentario(), cierre.getIdExpediente(), false);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_CIERRE), "movimiento " + ACCION_CIERRE);
                actualizarExpediente(conn, cierre.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario, true);
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
                        "EXPEDIENTE_PUBLICACION",
                        idPublicacion,
                        comentarioMovimiento(cierre.getComentario(), "Cierre de expediente publicado."),
                        "Publicación registrada");
                conn.commit();
                return new PublicacionResultadoDTO(
                        cierre.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_CIERRE,
                        ETAPA_CIERRE,
                        ESTADO_CERRADO,
                        "El expediente publicado fue cerrado correctamente.");
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

    private Long guardarPublicacion(Connection conn, PublicacionRegistroDTO registro, Long idUsuario) throws SQLException {
        Long existing = obtenerPublicacionActiva(conn, registro.getIdExpediente());
        if (existing != null) {
            actualizarPublicacion(conn, existing, registro, idUsuario);
            return existing;
        }
        return insertarPublicacion(conn, registro, obtenerNotificacionActiva(conn, registro.getIdExpediente()), idUsuario);
    }

    private Long insertarPublicacion(
            Connection conn,
            PublicacionRegistroDTO registro,
            Long idNotificacion,
            Long idUsuario) throws SQLException {
        String sql = "INSERT INTO expediente_publicacion ("
                + "id_expediente, id_expediente_notificacion, tipo_publicacion, estado_publicacion, "
                + "fecha_generacion, fecha_publicacion, medio_publicacion, numero_publicacion, observacion, "
                + "activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, 'PUBLICACION_REGISTRADA', SYSDATE, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_PUBLICACION"})) {
            ps.setLong(1, registro.getIdExpediente());
            setLongOrNull(ps, 2, idNotificacion);
            ps.setString(3, limitar(registro.getTipoPublicacion(), 60));
            if (registro.getFechaPublicacion() == null) {
                ps.setDate(4, new Date(System.currentTimeMillis()));
            } else {
                ps.setDate(4, Date.valueOf(registro.getFechaPublicacion()));
            }
            ps.setString(5, limitar(registro.getMedioPublicacion(), 250));
            ps.setString(6, limitar(registro.getNumeroPublicacion(), 100));
            ps.setString(7, limitar(observacionPublicacion(registro), 1500));
            setLongOrNull(ps, 8, idUsuario);
            ps.executeUpdate();
            return generatedId(ps, "No se pudo obtener el identificador de la publicación.");
        }
    }

    private void actualizarPublicacion(
            Connection conn,
            Long idPublicacion,
            PublicacionRegistroDTO registro,
            Long idUsuario) throws SQLException {
        String sql = "UPDATE expediente_publicacion SET "
                + "tipo_publicacion = ?, estado_publicacion = 'PUBLICACION_REGISTRADA', fecha_publicacion = ?, "
                + "medio_publicacion = ?, numero_publicacion = ?, observacion = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente_publicacion = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, limitar(registro.getTipoPublicacion(), 60));
            if (registro.getFechaPublicacion() == null) {
                ps.setDate(2, new Date(System.currentTimeMillis()));
            } else {
                ps.setDate(2, Date.valueOf(registro.getFechaPublicacion()));
            }
            ps.setString(3, limitar(registro.getMedioPublicacion(), 250));
            ps.setString(4, limitar(registro.getNumeroPublicacion(), 100));
            ps.setString(5, limitar(observacionPublicacion(registro), 1500));
            setLongOrNull(ps, 6, idUsuario);
            ps.setLong(7, idPublicacion);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar la publicación del expediente.");
            }
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

    private Long requerirPublicacionActiva(Connection conn, Long idExpediente) throws SQLException {
        Long idPublicacion = obtenerPublicacionActiva(conn, idExpediente);
        if (idPublicacion == null) {
            throw new SQLException("El expediente no tiene publicación activa registrada.");
        }
        return idPublicacion;
    }

    private Long obtenerNotificacionActiva(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT id_expediente_notificacion FROM ("
                + "SELECT id_expediente_notificacion FROM expediente_notificacion "
                + "WHERE id_expediente = ? AND activo = 1 "
                + "ORDER BY creado_en DESC, id_expediente_notificacion DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return getLongOrNull(rs, "id_expediente_notificacion");
            }
        }
    }

    private void actualizarExpediente(
            Connection conn,
            Long idExpediente,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idUsuarioModificador,
            boolean cerrarExpediente) throws SQLException {
        String sql = "UPDATE expediente SET "
                + "id_etapa_actual = ?, id_estado_actual = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, "
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
            throw new SQLException("No existe documento, resolución, notificación o publicación registrada para sustentar la acción.");
        }
    }

    private boolean tieneDocumentoSoporte(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT ("
                + "(SELECT COUNT(*) FROM expediente_documento d WHERE d.id_expediente = ? AND d.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_documento_analizado da WHERE da.id_expediente = ? AND da.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_resolucion r WHERE r.id_expediente = ? AND r.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_notificacion n WHERE n.id_expediente = ? AND n.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_cargo_acuse c WHERE c.id_expediente = ? AND c.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_publicacion p WHERE p.id_expediente = ? AND p.activo = 1)"
                + ") AS total FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idExpediente);
            ps.setLong(3, idExpediente);
            ps.setLong(4, idExpediente);
            ps.setLong(5, idExpediente);
            ps.setLong(6, idExpediente);
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

    private PublicacionExpedienteDTO map(ResultSet rs) throws SQLException {
        Boolean requierePublicacion = null;
        int requiereValue = rs.getInt("requiere_publicacion");
        if (!rs.wasNull()) {
            requierePublicacion = requiereValue == 1;
        }
        return new PublicacionExpedienteDTO(
                getLongOrNull(rs, "id_expediente"),
                rs.getString("numero_expediente"),
                rs.getString("numero_tramite_documentario"),
                rs.getString("procedimiento"),
                rs.getString("tipo_documento"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                getLongOrNull(rs, "dias_restantes"),
                toLocalDateTime(rs.getTimestamp("fecha_ingreso_publicacion")),
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
                rs.getString("resultado_notificacion"),
                requierePublicacion,
                getLongOrNull(rs, "id_expediente_cargo_acuse"),
                rs.getString("estado_cargo"),
                toLocalDateTime(rs.getTimestamp("fecha_cargo")),
                getLongOrNull(rs, "id_expediente_publicacion"),
                rs.getString("tipo_publicacion"),
                rs.getString("estado_publicacion"),
                toLocalDate(rs.getDate("fecha_generacion")),
                toLocalDate(rs.getDate("fecha_publicacion")),
                rs.getString("medio_publicacion"),
                rs.getString("numero_publicacion"),
                rs.getString("observacion_publicacion"),
                rs.getString("acciones_permitidas"));
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

    private static String observacionPublicacion(PublicacionRegistroDTO registro) {
        List<String> partes = new ArrayList<String>();
        if (hasText(registro.getResultadoPublicacion())) {
            partes.add("Resultado: " + registro.getResultadoPublicacion().trim());
        }
        if (hasText(registro.getComentario())) {
            partes.add(registro.getComentario().trim());
        }
        return partes.isEmpty() ? "Publicación registrada." : String.join(". ", partes);
    }

    private static String motivoPublicacion(PublicacionRegistroDTO registro) {
        List<String> partes = new ArrayList<String>();
        if (hasText(registro.getMedioPublicacion())) {
            partes.add("Medio: " + registro.getMedioPublicacion().trim());
        }
        if (hasText(registro.getNumeroPublicacion())) {
            partes.add("Referencia: " + registro.getNumeroPublicacion().trim());
        }
        return partes.isEmpty() ? "Publicación registrada" : String.join(" · ", partes);
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
}
