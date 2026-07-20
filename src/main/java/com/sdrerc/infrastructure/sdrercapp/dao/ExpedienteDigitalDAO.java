package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CalendarioLaboralService;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteDigitalDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteDigitalFiltroDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteDigitalRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteDigitalResultadoDTO;
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

public class ExpedienteDigitalDAO {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String ETAPA_DIGITAL = "EXPEDIENTE_DIGITAL";
    private static final String ESTADO_CARPETA_CREADA = "CARPETA_CREADA";
    private static final String ESTADO_LINK_REGISTRADO = "LINK_REGISTRADO";
    private static final String ESTADO_COMPLETO = "EXPEDIENTE_DIGITAL_COMPLETO";
    private static final String ACCION_CREACION_CARPETA = "CREACION_CARPETA_EXPEDIENTE_DIGITAL";
    private static final String ACCION_CARGA_DOCUMENTOS = "CARGA_DOCUMENTOS_EXPEDIENTE_DIGITAL";

    private final CatalogoLookupDAO catalogoLookupDAO;
    private final CalendarioLaboralService calendarioLaboralService = new CalendarioLaboralService();

    public ExpedienteDigitalDAO() {
        this(new CatalogoLookupDAO());
    }

    public ExpedienteDigitalDAO(CatalogoLookupDAO catalogoLookupDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    public List<ExpedienteDigitalDTO> buscarExpedientes(ExpedienteDigitalFiltroDTO filtro) throws SQLException {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("SELECT DISTINCT e.id_expediente, e.numero_expediente, e.numero_tramite_documentario, ");
        sql.append("esol.asunto AS procedimiento, p.tipo_documento, ");
        sql.append("ta.nombre AS tipo_acta, ea.numero_acta, ").append(nombrePersona("p")).append(" AS titular, ");
        sql.append("esol.fecha_recepcion, e.fecha_vencimiento, ");
        sql.append("e.fecha_ultimo_movimiento, ");
        sql.append("(SELECT MAX(h.fecha_movimiento) FROM expediente_historial h ");
        sql.append("JOIN etapa_expediente edh ON edh.id_etapa = h.id_etapa_destino ");
        sql.append("WHERE h.id_expediente = e.id_expediente AND h.activo = 1 AND edh.codigo = 'EXPEDIENTE_DIGITAL') AS fecha_ingreso_digital, ");
        sql.append("ur.nombre_completo AS responsable, eq.nombre AS equipo, et.codigo AS etapa_codigo, est.codigo AS estado_codigo, ");
        sql.append("UPPER(NVL(").append(nombrePersona("p")).append(", 'ZZZ')) AS orden_titular, ");
        sql.append("(SELECT MAX(o.descripcion) KEEP (DENSE_RANK LAST ORDER BY o.fecha_observacion) ");
        sql.append("FROM expediente_observacion o WHERE o.id_expediente = e.id_expediente AND o.activo = 1) AS ultima_observacion, ");
        sql.append("(SELECT COUNT(*) FROM expediente_documento d WHERE d.id_expediente = e.id_expediente AND d.activo = 1) AS documentos, ");
        sql.append("(SELECT COUNT(*) FROM expediente_documento_analizado da WHERE da.id_expediente = e.id_expediente AND da.activo = 1) AS documentos_analizados, ");
        sql.append("(SELECT COUNT(*) FROM expediente_relacion r WHERE r.activo = 1 AND (r.id_expediente_principal = e.id_expediente OR r.id_expediente_relacionado = e.id_expediente)) AS relaciones_confirmadas, ");
        sql.append("res.id_expediente_resolucion, tr.nombre AS tipo_resolucion, res.numero_resolucion, res.fecha_resolucion, ");
        sql.append("n.id_expediente_notificacion, n.resultado AS resultado_notificacion, ");
        sql.append("pub.id_expediente_publicacion, pub.estado_publicacion, ");
        sql.append("dig.id_expediente_digital, dig.codigo_expediente_digital, dig.ruta_carpeta, dig.enlace_carpeta, ");
        sql.append("dig.documentos_cargados, dig.completo, dig.fecha_creacion_carpeta, dig.fecha_actualizacion, ");
        sql.append("udr.nombre_completo AS responsable_digital, udc.nombre_completo AS custodio_digital, ");
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
        sql.append("LEFT JOIN (SELECT id_expediente, MAX(id_expediente_publicacion) KEEP (DENSE_RANK LAST ORDER BY creado_en, id_expediente_publicacion) AS id_expediente_publicacion ");
        sql.append("FROM expediente_publicacion WHERE activo = 1 GROUP BY id_expediente) pub_pick ON pub_pick.id_expediente = e.id_expediente ");
        sql.append("LEFT JOIN expediente_publicacion pub ON pub.id_expediente_publicacion = pub_pick.id_expediente_publicacion ");
        sql.append("LEFT JOIN (SELECT id_expediente, MAX(id_expediente_digital) KEEP (DENSE_RANK LAST ORDER BY NVL(fecha_actualizacion, creado_en), id_expediente_digital) AS id_expediente_digital ");
        sql.append("FROM expediente_digital WHERE activo = 1 GROUP BY id_expediente) dig_pick ON dig_pick.id_expediente = e.id_expediente ");
        sql.append("LEFT JOIN expediente_digital dig ON dig.id_expediente_digital = dig_pick.id_expediente_digital ");
        sql.append("LEFT JOIN usuario udr ON udr.id_usuario = dig.id_usuario_responsable ");
        sql.append("LEFT JOIN usuario udc ON udc.id_usuario = dig.id_usuario_custodio ");
        sql.append("WHERE e.activo = 1 AND et.codigo = ? ");
        params.add(ETAPA_DIGITAL);

        if (filtro != null && hasText(filtro.getEstadoCodigo()) && !"TODOS".equalsIgnoreCase(filtro.getEstadoCodigo())) {
            sql.append("AND UPPER(est.codigo) = ? ");
            params.add(filtro.getEstadoCodigo().trim().toUpperCase(Locale.ROOT));
        }

        if (filtro != null && hasText(filtro.getTextoLibre())) {
            String pattern = "%" + filtro.getTextoLibre().trim().toUpperCase(Locale.ROOT) + "%";
            sql.append("AND (UPPER(NVL(e.numero_expediente, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(e.numero_tramite_documentario, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(esol.asunto, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(ea.numero_acta, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(res.numero_resolucion, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(dig.codigo_expediente_digital, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(dig.ruta_carpeta, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(dig.enlace_carpeta, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(p.numero_documento, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(").append(nombrePersona("p")).append(", '')) LIKE ? ");
            sql.append("OR UPPER(NVL(esol.numero_expediente_sgd, '')) LIKE ?) ");
            for (int i = 0; i < 11; i++) {
                params.add(pattern);
            }
        }

        sql.append("ORDER BY fecha_vencimiento ASC NULLS LAST, orden_titular ASC, id_expediente ASC");
        sql.append(") WHERE ROWNUM <= ?");
        params.add(normalizarLimite(filtro == null ? 0 : filtro.getLimite()));

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<ExpedienteDigitalDTO> expedientes = new ArrayList<ExpedienteDigitalDTO>();
                while (rs.next()) {
                    expedientes.add(map(conn, rs));
                }
                return expedientes;
            }
        }
    }

    public ExpedienteDigitalResultadoDTO registrarCarpeta(ExpedienteDigitalRegistroDTO registro, Long idUsuario) throws SQLException {
        return registrarMetadata(registro, idUsuario, "La carpeta digital fue registrada correctamente.");
    }

    public ExpedienteDigitalResultadoDTO registrarEnlace(ExpedienteDigitalRegistroDTO registro, Long idUsuario) throws SQLException {
        return registrarMetadata(registro, idUsuario, "El enlace del expediente digital fue registrado correctamente.");
    }

    public ExpedienteDigitalResultadoDTO marcarCompleto(ExpedienteDigitalRegistroDTO registro, Long idUsuario) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, registro.getIdExpediente());
                if (!ETAPA_DIGITAL.equalsIgnoreCase(expediente.etapaCodigo)
                        || !ESTADO_LINK_REGISTRADO.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente debe tener enlace registrado para marcarlo como completo.");
                }
                Long idDigital = requerirExpedienteDigitalActivo(conn, registro.getIdExpediente());
                requerirRutaOEnlace(conn, idDigital);
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_CARGA_DOCUMENTOS,
                        ETAPA_DIGITAL,
                        ESTADO_LINK_REGISTRADO,
                        ETAPA_DIGITAL,
                        ESTADO_COMPLETO);
                validarRequisitosTransicion(conn, transicion, registro.getComentario(), registro.getIdExpediente());
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_CARGA_DOCUMENTOS), "movimiento " + ACCION_CARGA_DOCUMENTOS);
                actualizarDigitalCompleto(conn, idDigital, registro, idUsuario);
                actualizarExpediente(conn, registro.getIdExpediente(), transicion.idEtapaDestino, transicion.idEstadoDestino, idUsuario, true);
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
                        "EXPEDIENTE_DIGITAL",
                        idDigital,
                        comentarioMovimiento(registro.getComentario(), "Expediente digital marcado como completo."),
                        "Expediente digital completo");
                conn.commit();
                return new ExpedienteDigitalResultadoDTO(
                        registro.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_CARGA_DOCUMENTOS,
                        ETAPA_DIGITAL,
                        ESTADO_COMPLETO,
                        "El expediente digital fue marcado como completo.");
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

    private ExpedienteDigitalResultadoDTO registrarMetadata(
            ExpedienteDigitalRegistroDTO registro,
            Long idUsuario,
            String mensajeExito) throws SQLException {
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteBloqueado expediente = bloquearExpediente(conn, registro.getIdExpediente());
                if (!ETAPA_DIGITAL.equalsIgnoreCase(expediente.etapaCodigo)
                        || !ESTADO_CARPETA_CREADA.equalsIgnoreCase(expediente.estadoCodigo)) {
                    throw new SQLException("El expediente debe estar con carpeta creada para registrar la metadata digital.");
                }
                Transicion transicion = requerirTransicion(
                        conn,
                        ACCION_CREACION_CARPETA,
                        ETAPA_DIGITAL,
                        ESTADO_CARPETA_CREADA,
                        ETAPA_DIGITAL,
                        ESTADO_LINK_REGISTRADO);
                validarRequisitosTransicion(conn, transicion, registro.getComentario(), registro.getIdExpediente());
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, ACCION_CREACION_CARPETA), "movimiento " + ACCION_CREACION_CARPETA);
                Long idDigital = guardarMetadataDigital(conn, registro, idUsuario, false);
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
                        "EXPEDIENTE_DIGITAL",
                        idDigital,
                        comentarioMovimiento(registro.getComentario(), "Registro de metadata de expediente digital."),
                        motivoMetadata(registro));
                conn.commit();
                return new ExpedienteDigitalResultadoDTO(
                        registro.getIdExpediente(),
                        expediente.numeroExpediente,
                        ACCION_CREACION_CARPETA,
                        ETAPA_DIGITAL,
                        ESTADO_LINK_REGISTRADO,
                        mensajeExito);
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

    private Long guardarMetadataDigital(
            Connection conn,
            ExpedienteDigitalRegistroDTO registro,
            Long idUsuario,
            boolean completo) throws SQLException {
        Long existing = obtenerExpedienteDigitalActivo(conn, registro.getIdExpediente());
        if (existing != null) {
            actualizarMetadataDigital(conn, existing, registro, idUsuario, completo);
            return existing;
        }
        return insertarMetadataDigital(conn, registro, idUsuario, completo);
    }

    private Long insertarMetadataDigital(
            Connection conn,
            ExpedienteDigitalRegistroDTO registro,
            Long idUsuario,
            boolean completo) throws SQLException {
        String sql = "INSERT INTO expediente_digital ("
                + "id_expediente, codigo_expediente_digital, ruta_carpeta, enlace_carpeta, "
                + "documentos_cargados, completo, id_usuario_responsable, id_usuario_custodio, "
                + "fecha_creacion_carpeta, fecha_actualizacion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_DIGITAL"})) {
            ps.setLong(1, registro.getIdExpediente());
            ps.setString(2, limitar(nullIfBlank(registro.getCodigoExpedienteDigital()), 100));
            ps.setString(3, limitar(nullIfBlank(registro.getRutaCarpeta()), 1000));
            ps.setString(4, limitar(nullIfBlank(registro.getEnlaceCarpeta()), 1000));
            ps.setInt(5, completo ? 1 : 0);
            ps.setInt(6, completo ? 1 : 0);
            setLongOrNull(ps, 7, idUsuario);
            setLongOrNull(ps, 8, idUsuario);
            setLongOrNull(ps, 9, idUsuario);
            ps.executeUpdate();
            return generatedId(ps, "No se pudo obtener el identificador del expediente digital.");
        }
    }

    private void actualizarMetadataDigital(
            Connection conn,
            Long idDigital,
            ExpedienteDigitalRegistroDTO registro,
            Long idUsuario,
            boolean completo) throws SQLException {
        String sql = "UPDATE expediente_digital SET "
                + "codigo_expediente_digital = NVL(?, codigo_expediente_digital), "
                + "ruta_carpeta = NVL(?, ruta_carpeta), "
                + "enlace_carpeta = NVL(?, enlace_carpeta), "
                + "documentos_cargados = CASE WHEN ? = 1 THEN 1 ELSE documentos_cargados END, "
                + "completo = CASE WHEN ? = 1 THEN 1 ELSE completo END, "
                + "id_usuario_responsable = NVL(id_usuario_responsable, ?), "
                + "id_usuario_custodio = NVL(id_usuario_custodio, ?), "
                + "fecha_creacion_carpeta = NVL(fecha_creacion_carpeta, SYSTIMESTAMP), "
                + "fecha_actualizacion = SYSTIMESTAMP, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente_digital = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, limitar(nullIfBlank(registro.getCodigoExpedienteDigital()), 100));
            ps.setString(2, limitar(nullIfBlank(registro.getRutaCarpeta()), 1000));
            ps.setString(3, limitar(nullIfBlank(registro.getEnlaceCarpeta()), 1000));
            ps.setInt(4, completo ? 1 : 0);
            ps.setInt(5, completo ? 1 : 0);
            setLongOrNull(ps, 6, idUsuario);
            setLongOrNull(ps, 7, idUsuario);
            setLongOrNull(ps, 8, idUsuario);
            ps.setLong(9, idDigital);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar la metadata del expediente digital.");
            }
        }
    }

    private void actualizarDigitalCompleto(
            Connection conn,
            Long idDigital,
            ExpedienteDigitalRegistroDTO registro,
            Long idUsuario) throws SQLException {
        actualizarMetadataDigital(conn, idDigital, registro, idUsuario, true);
    }

    private Long obtenerExpedienteDigitalActivo(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT id_expediente_digital FROM ("
                + "SELECT id_expediente_digital FROM expediente_digital "
                + "WHERE id_expediente = ? AND activo = 1 "
                + "ORDER BY NVL(fecha_actualizacion, creado_en) DESC, id_expediente_digital DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return getLongOrNull(rs, "id_expediente_digital");
            }
        }
    }

    private Long requerirExpedienteDigitalActivo(Connection conn, Long idExpediente) throws SQLException {
        Long idDigital = obtenerExpedienteDigitalActivo(conn, idExpediente);
        if (idDigital == null) {
            throw new SQLException("El expediente no tiene metadata digital activa registrada.");
        }
        return idDigital;
    }

    private void requerirRutaOEnlace(Connection conn, Long idDigital) throws SQLException {
        String sql = "SELECT ruta_carpeta, enlace_carpeta FROM expediente_digital WHERE id_expediente_digital = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idDigital);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || (!hasText(rs.getString("ruta_carpeta")) && !hasText(rs.getString("enlace_carpeta")))) {
                    throw new SQLException("Registre una ruta o enlace digital antes de marcar el expediente como completo.");
                }
            }
        }
    }

    private void actualizarExpediente(
            Connection conn,
            Long idExpediente,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idUsuarioModificador,
            boolean expedienteDigitalCompleto) throws SQLException {
        String sql = "UPDATE expediente SET "
                + "id_etapa_actual = ?, id_estado_actual = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, "
                + (expedienteDigitalCompleto ? "expediente_digital_completo = 1, " : "")
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
            Long idExpediente) throws SQLException {
        if (transicion.requiereComentario && !hasText(comentario)) {
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
                + "(SELECT COUNT(*) FROM expediente_publicacion p WHERE p.id_expediente = ? AND p.activo = 1)"
                + ") AS total FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idExpediente);
            ps.setLong(3, idExpediente);
            ps.setLong(4, idExpediente);
            ps.setLong(5, idExpediente);
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

    private ExpedienteDigitalDTO map(Connection conn, ResultSet rs) throws SQLException {
        return new ExpedienteDigitalDTO(
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
                toLocalDateTime(rs.getTimestamp("fecha_ingreso_digital")),
                toLocalDateTime(rs.getTimestamp("fecha_ultimo_movimiento")),
                rs.getString("responsable"),
                rs.getString("equipo"),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getString("ultima_observacion"),
                rs.getInt("documentos"),
                rs.getInt("documentos_analizados"),
                rs.getInt("relaciones_confirmadas"),
                getLongOrNull(rs, "id_expediente_resolucion"),
                rs.getString("tipo_resolucion"),
                rs.getString("numero_resolucion"),
                toLocalDate(rs.getDate("fecha_resolucion")),
                getLongOrNull(rs, "id_expediente_notificacion"),
                rs.getString("resultado_notificacion"),
                getLongOrNull(rs, "id_expediente_publicacion"),
                rs.getString("estado_publicacion"),
                getLongOrNull(rs, "id_expediente_digital"),
                rs.getString("codigo_expediente_digital"),
                rs.getString("ruta_carpeta"),
                rs.getString("enlace_carpeta"),
                rs.getInt("documentos_cargados") == 1,
                rs.getInt("completo") == 1,
                rs.getString("responsable_digital"),
                rs.getString("custodio_digital"),
                toLocalDateTime(rs.getTimestamp("fecha_creacion_carpeta")),
                toLocalDateTime(rs.getTimestamp("fecha_actualizacion")),
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

    private static String motivoMetadata(ExpedienteDigitalRegistroDTO registro) {
        List<String> partes = new ArrayList<String>();
        if (hasText(registro.getRutaCarpeta())) {
            partes.add("Ruta registrada");
        }
        if (hasText(registro.getEnlaceCarpeta())) {
            partes.add("Enlace registrado");
        }
        return partes.isEmpty() ? "Metadata digital registrada" : String.join(" · ", partes);
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

    private static String nullIfBlank(String value) {
        return hasText(value) ? value.trim() : null;
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
