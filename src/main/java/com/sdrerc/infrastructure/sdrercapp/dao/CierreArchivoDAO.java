package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CalendarioLaboralService;
import com.sdrerc.domain.dto.sdrercapp.ArchivoExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreArchivoExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreArchivoResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteTimelineDTO;
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

public class CierreArchivoDAO {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String ETAPA_CIERRE_ARCHIVO = "CIERRE_ARCHIVO";
    private static final String ESTADO_CERRADO = "CERRADO";
    private static final String ESTADO_ARCHIVADO = "ARCHIVADO";
    private static final String ACCION_CIERRE = "CIERRE";
    private static final String ACCION_ARCHIVO = "ARCHIVO";

    private final CatalogoLookupDAO catalogoLookupDAO;
    private final ExpedienteTimelineDAO timelineDAO;
    private final CalendarioLaboralService calendarioLaboralService = new CalendarioLaboralService();

    public CierreArchivoDAO() {
        this(new CatalogoLookupDAO(), new ExpedienteTimelineDAO());
    }

    public CierreArchivoDAO(CatalogoLookupDAO catalogoLookupDAO, ExpedienteTimelineDAO timelineDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.timelineDAO = timelineDAO;
    }

    public List<CierreArchivoExpedienteDTO> buscarExpedientes(String textoLibre, String estadoCodigo, int limite) throws SQLException {
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
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 AND tm.codigo = 'CIERRE') AS fecha_cierre, ");
        sql.append("(SELECT MAX(h.fecha_movimiento) FROM expediente_historial h ");
        sql.append("JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = h.id_tipo_movimiento ");
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 AND tm.codigo = 'ARCHIVO') AS fecha_archivo, ");
        sql.append("ur.nombre_completo AS responsable, eq.nombre AS equipo, et.codigo AS etapa_codigo, est.codigo AS estado_codigo, ");
        sql.append("e.cerrado, e.archivado, e.expediente_digital_completo, ");
        sql.append("(SELECT MAX(o.descripcion) KEEP (DENSE_RANK LAST ORDER BY o.fecha_observacion) ");
        sql.append("FROM expediente_observacion o WHERE o.id_expediente = e.id_expediente AND o.activo = 1) AS ultima_observacion, ");
        sql.append("(SELECT MAX(NVL(h.motivo, h.comentario)) KEEP (DENSE_RANK LAST ORDER BY h.fecha_movimiento) ");
        sql.append("FROM expediente_historial h JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = h.id_tipo_movimiento ");
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 AND tm.codigo IN ('CIERRE','ARCHIVO','DERIVACION_EXTERNA')) AS motivo_final, ");
        sql.append("(SELECT COUNT(*) FROM expediente_documento d WHERE d.id_expediente = e.id_expediente AND d.activo = 1) AS documentos, ");
        sql.append("(SELECT COUNT(*) FROM expediente_relacion r WHERE r.activo = 1 AND (r.id_expediente_principal = e.id_expediente OR r.id_expediente_relacionado = e.id_expediente)) AS relaciones_confirmadas, ");
        sql.append("res.id_expediente_resolucion, res.numero_resolucion, res.fecha_resolucion, ");
        sql.append("n.id_expediente_notificacion, n.resultado AS resultado_notificacion, ");
        sql.append("pub.id_expediente_publicacion, pub.estado_publicacion, ");
        sql.append("dig.id_expediente_digital, dig.ruta_carpeta, dig.enlace_carpeta, ");
        sql.append("der.id_derivacion_externa, ent.nombre AS entidad_destino, der.tipo_derivacion, der.numero_oficio, ");
        sql.append("der.fecha_envio AS fecha_derivacion, der.estado_respuesta AS estado_derivacion, der.comentario AS comentario_derivacion, ");
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
        sql.append("LEFT JOIN (SELECT id_expediente, MAX(id_expediente_notificacion) KEEP (DENSE_RANK LAST ORDER BY creado_en, id_expediente_notificacion) AS id_expediente_notificacion ");
        sql.append("FROM expediente_notificacion WHERE activo = 1 GROUP BY id_expediente) n_pick ON n_pick.id_expediente = e.id_expediente ");
        sql.append("LEFT JOIN expediente_notificacion n ON n.id_expediente_notificacion = n_pick.id_expediente_notificacion ");
        sql.append("LEFT JOIN (SELECT id_expediente, MAX(id_expediente_publicacion) KEEP (DENSE_RANK LAST ORDER BY creado_en, id_expediente_publicacion) AS id_expediente_publicacion ");
        sql.append("FROM expediente_publicacion WHERE activo = 1 GROUP BY id_expediente) pub_pick ON pub_pick.id_expediente = e.id_expediente ");
        sql.append("LEFT JOIN expediente_publicacion pub ON pub.id_expediente_publicacion = pub_pick.id_expediente_publicacion ");
        sql.append("LEFT JOIN (SELECT id_expediente, MAX(id_expediente_digital) KEEP (DENSE_RANK LAST ORDER BY creado_en, id_expediente_digital) AS id_expediente_digital ");
        sql.append("FROM expediente_digital WHERE activo = 1 GROUP BY id_expediente) dig_pick ON dig_pick.id_expediente = e.id_expediente ");
        sql.append("LEFT JOIN expediente_digital dig ON dig.id_expediente_digital = dig_pick.id_expediente_digital ");
        sql.append("LEFT JOIN (SELECT id_expediente, MAX(id_derivacion_externa) KEEP (DENSE_RANK LAST ORDER BY creado_en, id_derivacion_externa) AS id_derivacion_externa ");
        sql.append("FROM expediente_derivacion_externa WHERE activo = 1 GROUP BY id_expediente) der_pick ON der_pick.id_expediente = e.id_expediente ");
        sql.append("LEFT JOIN expediente_derivacion_externa der ON der.id_derivacion_externa = der_pick.id_derivacion_externa ");
        sql.append("LEFT JOIN entidad_externa ent ON ent.id_entidad_externa = der.id_entidad_destino ");
        sql.append("WHERE e.activo = 1 AND (et.codigo = ? ");
        params.add(ETAPA_CIERRE_ARCHIVO);
        sql.append("OR EXISTS (SELECT 1 FROM vw_expediente_acciones_permitidas ap ");
        sql.append("WHERE ap.id_expediente = e.id_expediente AND ap.codigo_accion IN (?, ?))) ");
        params.add(ACCION_CIERRE);
        params.add(ACCION_ARCHIVO);

        if (hasText(estadoCodigo) && !"TODOS".equalsIgnoreCase(estadoCodigo)) {
            String filtro = estadoCodigo.trim().toUpperCase(Locale.ROOT);
            if ("CANDIDATOS_CIERRE".equals(filtro)) {
                sql.append("AND EXISTS (SELECT 1 FROM vw_expediente_acciones_permitidas ap WHERE ap.id_expediente = e.id_expediente AND ap.codigo_accion = ?) ");
                params.add(ACCION_CIERRE);
            } else if ("CANDIDATOS_ARCHIVO".equals(filtro)) {
                sql.append("AND EXISTS (SELECT 1 FROM vw_expediente_acciones_permitidas ap WHERE ap.id_expediente = e.id_expediente AND ap.codigo_accion = ?) ");
                params.add(ACCION_ARCHIVO);
            } else {
                sql.append("AND UPPER(est.codigo) = ? ");
                params.add(filtro);
            }
        }

        if (hasText(textoLibre)) {
            String pattern = "%" + textoLibre.trim().toUpperCase(Locale.ROOT) + "%";
            sql.append("AND (UPPER(NVL(e.numero_expediente, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(e.numero_tramite_documentario, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(esol.asunto, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(ea.numero_acta, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(res.numero_resolucion, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(ent.nombre, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(der.numero_oficio, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(").append(nombrePersona("p")).append(", '')) LIKE ? ");
            sql.append("OR UPPER(NVL(esol.numero_expediente_sgd, '')) LIKE ?) ");
            for (int i = 0; i < 9; i++) {
                params.add(pattern);
            }
        }

        sql.append("ORDER BY CASE WHEN et.codigo = 'CIERRE_ARCHIVO' THEN 1 ELSE 0 END, ");
        sql.append("e.fecha_ultimo_movimiento DESC NULLS LAST, e.id_expediente DESC");
        sql.append(") WHERE ROWNUM <= ?");
        params.add(normalizarLimite(limite));

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<CierreArchivoExpedienteDTO> expedientes = new ArrayList<CierreArchivoExpedienteDTO>();
                while (rs.next()) {
                    expedientes.add(map(conn, rs));
                }
                return expedientes;
            }
        }
    }

    public CierreArchivoResultadoDTO registrarCierre(CierreExpedienteDTO cierre, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, cierre.getIdExpediente());
                if (ETAPA_CIERRE_ARCHIVO.equalsIgnoreCase(expediente.etapaCodigo)
                        && ESTADO_CERRADO.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente ya se encuentra cerrado.");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_CIERRE,
                        expediente.etapaCodigo,
                        expediente.estadoCodigo,
                        ETAPA_CIERRE_ARCHIVO,
                        ESTADO_CERRADO);
                validarRequisitosTransicion(conn, transicion, cierre.getComentario(), cierre.getIdExpediente(), false);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_CIERRE), "movimiento " + ACCION_CIERRE);
                actualizarExpediente(conn, cierre.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario, true, false);
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
                        comentarioMovimiento(cierre.getComentario(), "Cierre de expediente."),
                        motivoMovimiento(cierre.getMotivo(), "Cierre"));
                conn.commit();
                return new CierreArchivoResultadoDTO(
                        cierre.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_CIERRE,
                        ETAPA_CIERRE_ARCHIVO,
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

    public CierreArchivoResultadoDTO registrarArchivo(ArchivoExpedienteDTO archivo, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, archivo.getIdExpediente());
                if (ETAPA_CIERRE_ARCHIVO.equalsIgnoreCase(expediente.etapaCodigo)
                        && ESTADO_ARCHIVADO.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente ya se encuentra archivado.");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_ARCHIVO,
                        expediente.etapaCodigo,
                        expediente.estadoCodigo,
                        ETAPA_CIERRE_ARCHIVO,
                        ESTADO_ARCHIVADO);
                validarRequisitosTransicion(conn, transicion, archivo.getComentario(), archivo.getIdExpediente(), true);
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_ARCHIVO), "movimiento " + ACCION_ARCHIVO);
                actualizarExpediente(conn, archivo.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario, false, true);
                insertarHistorial(
                        conn,
                        archivo.getIdExpediente(),
                        idMovimiento,
                        expediente.idEtapa,
                        expediente.idEstado,
                        transicion.idEtapaDestino,
                        transicion.idEstadoDestino,
                        idUsuario,
                        expediente.idUsuarioResponsable,
                        expediente.idEquipoResponsable,
                        "EXPEDIENTE",
                        archivo.getIdExpediente(),
                        comentarioMovimiento(archivo.getComentario(), "Archivo de expediente."),
                        motivoMovimiento(archivo.getMotivo(), "Archivo"));
                conn.commit();
                return new CierreArchivoResultadoDTO(
                        archivo.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_ARCHIVO,
                        ETAPA_CIERRE_ARCHIVO,
                        ESTADO_ARCHIVADO,
                        "El expediente fue archivado correctamente.");
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

    public List<ExpedienteTimelineDTO> listarHistorial(Long idExpediente) throws SQLException {
        return timelineDAO.listarPorExpediente(idExpediente);
    }

    private void actualizarExpediente(
            Connection conn,
            Long idExpediente,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idUsuarioModificador,
            boolean cerrarExpediente,
            boolean archivarExpediente) throws SQLException {
        String sql = "UPDATE expediente SET "
                + "id_etapa_actual = ?, id_estado_actual = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, "
                + "cerrado = CASE WHEN ? = 1 THEN 1 ELSE cerrado END, "
                + "archivado = CASE WHEN ? = 1 THEN 1 ELSE archivado END, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEtapaDestino);
            ps.setLong(2, idEstadoDestino);
            ps.setInt(3, cerrarExpediente ? 1 : 0);
            ps.setInt(4, archivarExpediente ? 1 : 0);
            setLongOrNull(ps, 5, idUsuarioModificador);
            ps.setLong(6, idExpediente);
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
            throw new SQLException("No existe documento, resolución, notificación, publicación o expediente digital para sustentar la acción.");
        }
    }

    private boolean tieneDocumentoSoporte(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT ("
                + "(SELECT COUNT(*) FROM expediente_documento d WHERE d.id_expediente = ? AND d.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_documento_analizado da WHERE da.id_expediente = ? AND da.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_resolucion r WHERE r.id_expediente = ? AND r.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_notificacion n WHERE n.id_expediente = ? AND n.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_cargo_acuse c WHERE c.id_expediente = ? AND c.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_publicacion p WHERE p.id_expediente = ? AND p.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_digital ed WHERE ed.id_expediente = ? AND ed.activo = 1) + "
                + "(SELECT COUNT(*) FROM expediente_derivacion_externa de WHERE de.id_expediente = ? AND de.activo = 1)"
                + ") AS total FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 8; i++) {
                ps.setLong(i, idExpediente);
            }
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

    private CierreArchivoExpedienteDTO map(Connection conn, ResultSet rs) throws SQLException {
        return new CierreArchivoExpedienteDTO(
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
                toLocalDateTime(rs.getTimestamp("fecha_ultimo_movimiento")),
                toLocalDateTime(rs.getTimestamp("fecha_cierre")),
                toLocalDateTime(rs.getTimestamp("fecha_archivo")),
                rs.getString("responsable"),
                rs.getString("equipo"),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getInt("cerrado") == 1,
                rs.getInt("archivado") == 1,
                rs.getInt("expediente_digital_completo") == 1,
                rs.getString("ultima_observacion"),
                rs.getString("motivo_final"),
                rs.getInt("documentos"),
                rs.getInt("relaciones_confirmadas"),
                getLongOrNull(rs, "id_expediente_resolucion"),
                rs.getString("numero_resolucion"),
                toLocalDate(rs.getDate("fecha_resolucion")),
                getLongOrNull(rs, "id_expediente_notificacion"),
                rs.getString("resultado_notificacion"),
                getLongOrNull(rs, "id_expediente_publicacion"),
                rs.getString("estado_publicacion"),
                getLongOrNull(rs, "id_expediente_digital"),
                rs.getString("ruta_carpeta"),
                rs.getString("enlace_carpeta"),
                getLongOrNull(rs, "id_derivacion_externa"),
                rs.getString("entidad_destino"),
                rs.getString("tipo_derivacion"),
                rs.getString("numero_oficio"),
                toLocalDate(rs.getDate("fecha_derivacion")),
                rs.getString("estado_derivacion"),
                rs.getString("comentario_derivacion"),
                rs.getString("acciones_permitidas"));
    }

    private Long requerirId(Long value, String descripcion) throws SQLException {
        if (value == null) {
            throw new SQLException("No se encontró el catálogo requerido: " + descripcion + ".");
        }
        return value;
    }

    private static String comentarioMovimiento(String comentario, String defecto) {
        if (hasText(comentario)) {
            return comentario.trim();
        }
        return hasText(defecto) ? defecto : "";
    }

    private static String motivoMovimiento(String motivo, String defecto) {
        if (hasText(motivo)) {
            return motivo.trim();
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
