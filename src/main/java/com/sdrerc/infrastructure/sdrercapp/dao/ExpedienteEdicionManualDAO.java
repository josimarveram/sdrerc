package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CalendarioLaboralService;
import com.sdrerc.domain.dto.sdrercapp.DatosActaDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosPersonaRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.DatosSolicitudDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteEdicionManualDTO;
import com.sdrerc.domain.dto.sdrercapp.RegistroManualResultadoDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Locale;

public class ExpedienteEdicionManualDAO {

    private static final String CODIGO_ETAPA_REGISTRO = "REGISTRO";
    private static final String CODIGO_ESTADO_REGISTRADO = "REGISTRADO";
    private static final String CODIGO_MOVIMIENTO_EDICION = "RECEPCION_DOCUMENTO";

    private final CatalogoLookupDAO catalogoLookupDAO;
    private final CalendarioLaboralService calendarioLaboralService;

    public ExpedienteEdicionManualDAO() {
        this(new CatalogoLookupDAO(), new CalendarioLaboralService());
    }

    public ExpedienteEdicionManualDAO(
            CatalogoLookupDAO catalogoLookupDAO,
            CalendarioLaboralService calendarioLaboralService) {
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.calendarioLaboralService = calendarioLaboralService;
    }

    public ExpedienteEdicionManualDTO obtenerParaEdicion(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para editar.");
        }
        String sql = ""
                + "WITH sol AS ("
                + "  SELECT * FROM (SELECT s.* FROM expediente_solicitud s "
                + "  WHERE s.id_expediente = ? AND s.activo = 1 ORDER BY s.creado_en DESC, s.id_expediente_solicitud DESC) WHERE ROWNUM = 1"
                + "), doc AS ("
                + "  SELECT * FROM (SELECT d.* FROM expediente_documento d "
                + "  WHERE d.id_expediente = ? AND d.activo = 1 ORDER BY d.creado_en DESC, d.id_expediente_documento DESC) WHERE ROWNUM = 1"
                + "), act AS ("
                + "  SELECT * FROM (SELECT a.* FROM expediente_acta a "
                + "  WHERE a.id_expediente = ? AND a.activo = 1 ORDER BY a.creado_en DESC, a.id_expediente_acta DESC) WHERE ROWNUM = 1"
                + "), titular AS ("
                + "  SELECT * FROM (SELECT ep.id_expediente_persona, p.* "
                + "  FROM expediente_persona ep JOIN persona p ON p.id_persona = ep.id_persona "
                + "  WHERE ep.id_expediente = ? AND ep.tipo_relacion_persona = 'TITULAR' AND ep.activo = 1 AND p.activo = 1 "
                + "  ORDER BY ep.creado_en DESC, ep.id_expediente_persona DESC) WHERE ROWNUM = 1"
                + "), remitente AS ("
                + "  SELECT * FROM (SELECT ep.id_expediente_persona, p.* "
                + "  FROM expediente_persona ep JOIN persona p ON p.id_persona = ep.id_persona "
                + "  WHERE ep.id_expediente = ? AND ep.tipo_relacion_persona = 'REMITENTE' AND ep.activo = 1 AND p.activo = 1 "
                + "  ORDER BY ep.creado_en DESC, ep.id_expediente_persona DESC) WHERE ROWNUM = 1"
                + ") "
                + "SELECT e.id_expediente, e.numero_expediente, e.numero_tramite_documentario, e.prioridad, "
                + "et.codigo AS etapa_codigo, est.codigo AS estado_codigo, "
                + "sol.numero_tramite_documentario AS solicitud_tramite, sol.numero_expediente_sgd, sol.fecha_recepcion, "
                + "sol.asunto, sol.observacion, cr.codigo AS canal_codigo, cr.nombre AS canal_nombre, "
                + "doc.nombre_documento, doc.numero_documento, "
                + "act.numero_acta, act.anio_acta, ta.codigo AS tipo_acta_codigo, ta.nombre AS tipo_acta_nombre, "
                + "titular.tipo_documento AS titular_tipo_documento, titular.numero_documento AS titular_numero_documento, "
                + nombrePersona("titular") + " AS titular_nombre, "
                + "NVL(remitente.tipo_documento, solicitante.tipo_documento) AS remitente_tipo_documento, "
                + "NVL(remitente.numero_documento, solicitante.numero_documento) AS remitente_numero_documento, "
                + "COALESCE(" + nombrePersona("remitente") + ", " + nombrePersona("solicitante") + ") AS remitente_nombre "
                + "FROM expediente e "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "LEFT JOIN sol ON sol.id_expediente = e.id_expediente "
                + "LEFT JOIN canal_recepcion cr ON cr.id_canal_recepcion = sol.id_canal_recepcion "
                + "LEFT JOIN persona solicitante ON solicitante.id_persona = sol.id_persona_solicitante "
                + "LEFT JOIN doc ON doc.id_expediente = e.id_expediente "
                + "LEFT JOIN act ON act.id_expediente = e.id_expediente "
                + "LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = act.id_tipo_acta "
                + "LEFT JOIN titular ON titular.id_persona IS NOT NULL "
                + "LEFT JOIN remitente ON remitente.id_persona IS NOT NULL "
                + "WHERE e.id_expediente = ? AND e.activo = 1";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setLong(i, idExpediente);
            }
            ps.setLong(6, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No se encontró el expediente seleccionado.");
                }
                ExpedienteEdicionManualDTO dto = map(rs);
                validarEditable(dto, tieneAsignacionActiva(conn, idExpediente));
                return dto;
            }
        }
    }

    public RegistroManualResultadoDTO guardar(ExpedienteEdicionManualDTO dto) throws SQLException {
        if (dto == null || dto.getIdExpediente() == null) {
            throw new IllegalArgumentException("Seleccione un expediente para editar.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                ExpedienteEstado estado = bloquearYObtenerEstado(conn, dto.getIdExpediente());
                validarEditable(estado, tieneAsignacionActiva(conn, dto.getIdExpediente()));

                Long idTitular = upsertPersonaRelacion(conn, dto.getIdExpediente(), "TITULAR", dto.getTitular());
                Long idRemitente = upsertPersonaRelacion(conn, dto.getIdExpediente(), "REMITENTE", dto.getRemitente());
                Long idSolicitante = idRemitente == null ? idTitular : idRemitente;

                actualizarExpediente(conn, dto);
                upsertSolicitud(conn, dto, idSolicitante);
                upsertActa(conn, dto);
                upsertDocumento(conn, dto);
                insertarHistorial(conn, dto, estado);

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                return new RegistroManualResultadoDTO(
                        dto.getIdExpediente(),
                        textoNumeroExpediente(dto.getNumeroExpediente()),
                        "Datos de registro actualizados correctamente. No se modificó el número de expediente.");
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

    private ExpedienteEdicionManualDTO map(ResultSet rs) throws SQLException {
        ExpedienteEdicionManualDTO dto = new ExpedienteEdicionManualDTO();
        dto.setIdExpediente(getLongOrNull(rs, "id_expediente"));
        dto.setNumeroExpediente(rs.getString("numero_expediente"));
        dto.setEtapaCodigo(rs.getString("etapa_codigo"));
        dto.setEstadoCodigo(rs.getString("estado_codigo"));
        dto.setNumeroExpedienteVistaPrevia(textoNumeroExpediente(dto.getNumeroExpediente()));

        String observacion = rs.getString("observacion");
        DatosSolicitudDTO solicitud = new DatosSolicitudDTO();
        solicitud.setNumeroTramite(firstText(rs.getString("solicitud_tramite"), rs.getString("numero_tramite_documentario")));
        solicitud.setNumeroDocumento(rs.getString("numero_documento"));
        solicitud.setNumeroExpedienteSgd(rs.getString("numero_expediente_sgd"));
        Date fechaRecepcion = rs.getDate("fecha_recepcion");
        solicitud.setFechaRecepcion(fechaRecepcion == null ? null : fechaRecepcion.toLocalDate());
        solicitud.setTipoSolicitudNombre(extraerObservacion(observacion, "Tipo de solicitud"));
        solicitud.setTipoSolicitudCodigo(codigoTipoSolicitud(solicitud.getTipoSolicitudNombre()));
        solicitud.setTipoProcedimientoNombre(rs.getString("asunto"));
        solicitud.setTipoProcedimientoCodigo(codigoCatalogoPorNombre("procedimiento_registral", rs.getString("asunto")));
        solicitud.setTipoDocumentoNombre(firstText(rs.getString("nombre_documento"), extraerObservacion(observacion, "Tipo de documento")));
        solicitud.setTipoDocumentoCodigo(codigoCatalogoPorNombre("tipo_documento", solicitud.getTipoDocumentoNombre()));
        solicitud.setCanalCodigo(rs.getString("canal_codigo"));
        solicitud.setCanalNombre(rs.getString("canal_nombre"));
        solicitud.setPrioridad(rs.getString("prioridad"));
        solicitud.setValidacionInicial(firstText(extraerObservacion(observacion, "Validación inicial"), "Sí corresponde a la SDRERC"));
        solicitud.setHojaEnvio(extraerObservacion(observacion, "Hoja de envío"));
        dto.setSolicitud(solicitud);

        DatosActaDTO acta = new DatosActaDTO();
        acta.setTipoActaCodigo(rs.getString("tipo_acta_codigo"));
        acta.setTipoActaNombre(rs.getString("tipo_acta_nombre"));
        acta.setNumeroActa(rs.getString("numero_acta"));
        int anioActa = rs.getInt("anio_acta");
        acta.setAnioActa(rs.wasNull() ? null : anioActa);
        dto.setActa(acta);

        DatosPersonaRegistroDTO titular = new DatosPersonaRegistroDTO();
        titular.setNombreCompleto(rs.getString("titular_nombre"));
        titular.setTipoDocumento(rs.getString("titular_tipo_documento"));
        titular.setNumeroDocumento(rs.getString("titular_numero_documento"));
        dto.setTitular(titular);

        DatosPersonaRegistroDTO remitente = new DatosPersonaRegistroDTO();
        remitente.setNombreCompleto(rs.getString("remitente_nombre"));
        remitente.setTipoDocumento(rs.getString("remitente_tipo_documento"));
        remitente.setNumeroDocumento(rs.getString("remitente_numero_documento"));
        dto.setRemitente(remitente);
        return dto;
    }

    private void actualizarExpediente(Connection conn, ExpedienteEdicionManualDTO dto) throws SQLException {
        DatosSolicitudDTO solicitud = dto.getSolicitud();
        String sql = "UPDATE expediente SET "
                + "numero_tramite_documentario = ?, prioridad = ?, fecha_vencimiento = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, solicitud.getNumeroTramite());
            ps.setString(2, hasText(solicitud.getPrioridad()) ? solicitud.getPrioridad() : "NORMAL");
            ps.setDate(3, calcularFechaVencimiento(conn, solicitud.getFechaRecepcion()));
            ps.setLong(4, dto.getIdExpediente());
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar el expediente seleccionado.");
            }
        }
    }

    private void upsertSolicitud(Connection conn, ExpedienteEdicionManualDTO dto, Long idSolicitante) throws SQLException {
        Long idSolicitud = obtenerUltimoId(conn, "expediente_solicitud", "id_expediente_solicitud", dto.getIdExpediente());
        DatosSolicitudDTO solicitud = dto.getSolicitud();
        Long idCanal = null;
        if (hasText(solicitud.getCanalCodigo())) {
            idCanal = catalogoLookupDAO.obtenerCanalRecepcionId(conn, solicitud.getCanalCodigo());
        }
        if (idSolicitud == null) {
            String insert = "INSERT INTO expediente_solicitud ("
                    + "id_expediente, id_canal_recepcion, id_persona_solicitante, numero_tramite_documentario, "
                    + "numero_expediente_sgd, fecha_recepcion, asunto, observacion, es_tramite_virtual, "
                    + "correo_electronico, potencial_duplicado, activo"
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, NULL, 0, 1)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setLong(1, dto.getIdExpediente());
                setLongOrNull(ps, 2, idCanal);
                setLongOrNull(ps, 3, idSolicitante);
                ps.setString(4, solicitud.getNumeroTramite());
                ps.setString(5, solicitud.getNumeroExpedienteSgd());
                ps.setDate(6, toSqlDate(solicitud.getFechaRecepcion()));
                ps.setString(7, solicitud.getTipoProcedimientoNombre());
                ps.setString(8, limitar(observacionSolicitud(dto), 1000));
                ps.executeUpdate();
            }
            return;
        }
        String update = "UPDATE expediente_solicitud SET "
                + "id_canal_recepcion = ?, id_persona_solicitante = ?, numero_tramite_documentario = ?, "
                + "numero_expediente_sgd = ?, fecha_recepcion = ?, asunto = ?, observacion = ?, "
                + "modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente_solicitud = ?";
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            setLongOrNull(ps, 1, idCanal);
            setLongOrNull(ps, 2, idSolicitante);
            ps.setString(3, solicitud.getNumeroTramite());
            ps.setString(4, solicitud.getNumeroExpedienteSgd());
            ps.setDate(5, toSqlDate(solicitud.getFechaRecepcion()));
            ps.setString(6, solicitud.getTipoProcedimientoNombre());
            ps.setString(7, limitar(observacionSolicitud(dto), 1000));
            ps.setLong(8, idSolicitud);
            ps.executeUpdate();
        }
    }

    private void upsertActa(Connection conn, ExpedienteEdicionManualDTO dto) throws SQLException {
        Long idActa = obtenerUltimoId(conn, "expediente_acta", "id_expediente_acta", dto.getIdExpediente());
        DatosActaDTO acta = dto.getActa();
        Long idTipoActa = null;
        if (hasText(acta.getTipoActaCodigo())) {
            idTipoActa = catalogoLookupDAO.obtenerTipoActaId(conn, acta.getTipoActaCodigo());
        }
        if (idActa == null) {
            String insert = "INSERT INTO expediente_acta (id_expediente, id_tipo_acta, numero_acta, anio_acta, activo) "
                    + "VALUES (?, ?, ?, ?, 1)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setLong(1, dto.getIdExpediente());
                setLongOrNull(ps, 2, idTipoActa);
                ps.setString(3, acta.getNumeroActa());
                setIntegerOrNull(ps, 4, acta.getAnioActa());
                ps.executeUpdate();
            }
            return;
        }
        String update = "UPDATE expediente_acta SET id_tipo_acta = ?, numero_acta = ?, anio_acta = ?, "
                + "modificado_en = SYSTIMESTAMP WHERE id_expediente_acta = ?";
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            setLongOrNull(ps, 1, idTipoActa);
            ps.setString(2, acta.getNumeroActa());
            setIntegerOrNull(ps, 3, acta.getAnioActa());
            ps.setLong(4, idActa);
            ps.executeUpdate();
        }
    }

    private void upsertDocumento(Connection conn, ExpedienteEdicionManualDTO dto) throws SQLException {
        Long idDocumento = obtenerUltimoId(conn, "expediente_documento", "id_expediente_documento", dto.getIdExpediente());
        DatosSolicitudDTO solicitud = dto.getSolicitud();
        if (idDocumento == null) {
            String insert = "INSERT INTO expediente_documento ("
                    + "id_expediente, nombre_documento, numero_documento, fecha_documento, activo"
                    + ") VALUES (?, ?, ?, ?, 1)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setLong(1, dto.getIdExpediente());
                ps.setString(2, solicitud.getTipoDocumentoNombre());
                ps.setString(3, solicitud.getNumeroDocumento());
                ps.setDate(4, toSqlDate(solicitud.getFechaRecepcion()));
                ps.executeUpdate();
            }
            return;
        }
        String update = "UPDATE expediente_documento SET nombre_documento = ?, numero_documento = ?, "
                + "fecha_documento = ?, modificado_en = SYSTIMESTAMP WHERE id_expediente_documento = ?";
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setString(1, solicitud.getTipoDocumentoNombre());
            ps.setString(2, solicitud.getNumeroDocumento());
            ps.setDate(3, toSqlDate(solicitud.getFechaRecepcion()));
            ps.setLong(4, idDocumento);
            ps.executeUpdate();
        }
    }

    private Long upsertPersonaRelacion(
            Connection conn,
            Long idExpediente,
            String tipoRelacion,
            DatosPersonaRegistroDTO persona) throws SQLException {
        if (persona == null || !hasText(persona.getNombreCompleto())) {
            return null;
        }
        Long idPersona = obtenerPersonaRelacion(conn, idExpediente, tipoRelacion);
        if (idPersona == null) {
            idPersona = insertarPersona(conn, persona);
            insertarExpedientePersona(conn, idExpediente, idPersona, tipoRelacion);
        } else {
            actualizarPersona(conn, idPersona, persona);
        }
        return idPersona;
    }

    private Long obtenerPersonaRelacion(Connection conn, Long idExpediente, String tipoRelacion) throws SQLException {
        String sql = "SELECT id_persona FROM (SELECT ep.id_persona FROM expediente_persona ep "
                + "WHERE ep.id_expediente = ? AND ep.tipo_relacion_persona = ? AND ep.activo = 1 "
                + "ORDER BY ep.creado_en DESC, ep.id_expediente_persona DESC) WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, tipoRelacion);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return getLongOrNull(rs, "id_persona");
            }
        }
    }

    private Long insertarPersona(Connection conn, DatosPersonaRegistroDTO persona) throws SQLException {
        String sql = "INSERT INTO persona (tipo_documento, numero_documento, razon_social, activo) "
                + "VALUES (?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_PERSONA"})) {
            ps.setString(1, persona.getTipoDocumento());
            ps.setString(2, persona.getNumeroDocumento());
            ps.setString(3, persona.getNombreCompleto());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo registrar la persona del expediente.");
    }

    private void actualizarPersona(Connection conn, Long idPersona, DatosPersonaRegistroDTO persona) throws SQLException {
        String sql = "UPDATE persona SET tipo_documento = ?, numero_documento = ?, razon_social = ?, "
                + "modificado_en = SYSTIMESTAMP WHERE id_persona = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, persona.getTipoDocumento());
            ps.setString(2, persona.getNumeroDocumento());
            ps.setString(3, persona.getNombreCompleto());
            ps.setLong(4, idPersona);
            ps.executeUpdate();
        }
    }

    private void insertarExpedientePersona(Connection conn, Long idExpediente, Long idPersona, String tipoRelacion) throws SQLException {
        String sql = "INSERT INTO expediente_persona (id_expediente, id_persona, tipo_relacion_persona, activo) "
                + "VALUES (?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idPersona);
            ps.setString(3, tipoRelacion);
            ps.executeUpdate();
        }
    }

    private void insertarHistorial(
            Connection conn,
            ExpedienteEdicionManualDTO dto,
            ExpedienteEstado estado) throws SQLException {
        Long idMovimiento = catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_EDICION);
        if (idMovimiento == null) {
            throw new SQLException("No se encontró el movimiento RECEPCION_DOCUMENTO para registrar la edición manual.");
        }
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_etapa_origen, id_estado_origen, "
                + "id_etapa_destino, id_estado_destino, tabla_relacionada, id_registro_relacionado, "
                + "comentario, motivo, activo"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, ?, ?, 'EXPEDIENTE', ?, ?, 'EDICION_MANUAL_RECEPCION', 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, dto.getIdExpediente());
            ps.setLong(2, idMovimiento);
            ps.setLong(3, estado.idEtapa);
            ps.setLong(4, estado.idEstado);
            ps.setLong(5, estado.idEtapa);
            ps.setLong(6, estado.idEstado);
            ps.setLong(7, dto.getIdExpediente());
            ps.setString(8, limitar("Edición manual de datos de recepción. Expediente: "
                    + textoNumeroExpediente(dto.getNumeroExpediente())
                    + ". No se modificó el número de expediente.", 2000));
            ps.executeUpdate();
        }
    }

    private ExpedienteEstado bloquearYObtenerEstado(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT e.id_etapa_actual, e.id_estado_actual, et.codigo AS etapa_codigo, est.codigo AS estado_codigo, "
                + "e.numero_expediente, e.id_usuario_responsable_actual "
                + "FROM expediente e "
                + "JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual "
                + "JOIN estado_expediente est ON est.id_estado = e.id_estado_actual "
                + "WHERE e.id_expediente = ? AND e.activo = 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No se encontró el expediente seleccionado.");
                }
                return new ExpedienteEstado(
                        rs.getLong("id_etapa_actual"),
                        rs.getLong("id_estado_actual"),
                        rs.getString("etapa_codigo"),
                        rs.getString("estado_codigo"),
                        rs.getString("numero_expediente"),
                        getLongOrNull(rs, "id_usuario_responsable_actual"));
            }
        }
    }

    private void validarEditable(ExpedienteEdicionManualDTO dto, boolean tieneAsignacionActiva) throws SQLException {
        validarEditable(new ExpedienteEstado(
                null,
                null,
                dto.getEtapaCodigo(),
                dto.getEstadoCodigo(),
                dto.getNumeroExpediente(),
                null),
                tieneAsignacionActiva);
    }

    private void validarEditable(ExpedienteEstado estado, boolean tieneAsignacionActiva) throws SQLException {
        if (!CODIGO_ETAPA_REGISTRO.equalsIgnoreCase(safe(estado.etapaCodigo))
                || !CODIGO_ESTADO_REGISTRADO.equalsIgnoreCase(safe(estado.estadoCodigo))) {
            throw new SQLException("Solo se permite editar expedientes en estado Registrado de Registro / Recepción.");
        }
        if (estado.idResponsableActual != null || tieneAsignacionActiva) {
            throw new SQLException("El expediente ya tiene asignación a abogado. No se permite edición manual desde Recepción.");
        }
    }

    private boolean tieneAsignacionActiva(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT COUNT(1) FROM expediente_asignacion "
                + "WHERE id_expediente = ? AND activa = 1 AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Long obtenerUltimoId(Connection conn, String tabla, String columnaId, Long idExpediente) throws SQLException {
        String sql = "SELECT " + columnaId + " FROM (SELECT " + columnaId + " FROM " + tabla
                + " WHERE id_expediente = ? AND activo = 1 ORDER BY creado_en DESC, " + columnaId + " DESC) WHERE ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return getLongOrNull(rs, columnaId);
            }
        }
    }

    private Date calcularFechaVencimiento(Connection conn, LocalDate fechaSolicitud) throws SQLException {
        if (fechaSolicitud == null) {
            return null;
        }
        return Date.valueOf(calendarioLaboralService.calcularFechaVencimientoSolicitud(conn, fechaSolicitud));
    }

    private String observacionSolicitud(ExpedienteEdicionManualDTO dto) {
        StringBuilder sb = new StringBuilder();
        append(sb, "Validación inicial", dto.getSolicitud().getValidacionInicial());
        append(sb, "Hoja de envío", dto.getSolicitud().getHojaEnvio());
        append(sb, "Tipo de solicitud", dto.getSolicitud().getTipoSolicitudNombre());
        append(sb, "Tipo de documento", dto.getSolicitud().getTipoDocumentoNombre());
        append(sb, "N° EXPEDIENTE SGD", dto.getSolicitud().getNumeroExpedienteSgd());
        append(sb, "Tipo de acta", dto.getActa().getTipoActaNombre());
        append(sb, "Observaciones de registro", dto.getObservacionesGenerales());
        append(sb, "Edición manual", "Datos de recepción actualizados sin modificar número de expediente");
        return sb.toString();
    }

    private void append(StringBuilder sb, String etiqueta, String valor) {
        if (!hasText(valor)) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(" | ");
        }
        sb.append(etiqueta).append(": ").append(valor.trim());
    }

    private String extraerObservacion(String observacion, String etiqueta) {
        if (!hasText(observacion) || !hasText(etiqueta)) {
            return null;
        }
        String prefix = etiqueta.trim().toUpperCase(Locale.ROOT) + ":";
        String[] partes = observacion.split("\\s*\\|\\s*");
        for (String parte : partes) {
            if (parte != null && parte.trim().toUpperCase(Locale.ROOT).startsWith(prefix)) {
                return parte.trim().substring(prefix.length()).trim();
            }
        }
        return null;
    }

    private String codigoTipoSolicitud(String nombre) {
        if (!hasText(nombre)) {
            return null;
        }
        if ("PARTE".equalsIgnoreCase(nombre) || "Parte".equalsIgnoreCase(nombre)) {
            return "PARTE";
        }
        if ("OFICIO".equalsIgnoreCase(nombre) || "Oficio".equalsIgnoreCase(nombre)) {
            return "OFICIO";
        }
        return nombre.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private String codigoCatalogoPorNombre(String tabla, String nombre) throws SQLException {
        if (!hasText(nombre)) {
            return null;
        }
        String sql = "SELECT codigo FROM " + tabla + " "
                + "WHERE activo = 1 AND (UPPER(nombre) = ? OR UPPER(codigo) = ?) AND ROWNUM = 1";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String normalized = nombre.trim().toUpperCase(Locale.ROOT);
            ps.setString(1, normalized);
            ps.setString(2, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return rs.getString("codigo");
            }
        }
    }

    private static String nombrePersona(String alias) {
        return "NULLIF(TRIM(NVL(" + alias + ".razon_social, TRIM(NVL(" + alias + ".nombres, '') || ' ' || NVL(" + alias + ".apellidos, '')))), '')";
    }

    private static String textoNumeroExpediente(String numeroExpediente) {
        return hasText(numeroExpediente) ? numeroExpediente.trim() : "Sin número por duplicado";
    }

    private static String firstText(String first, String fallback) {
        return hasText(first) ? first : fallback;
    }

    private static Date toSqlDate(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
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

    private static void setIntegerOrNull(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
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

    private static class ExpedienteEstado {

        private final Long idEtapa;
        private final Long idEstado;
        private final String etapaCodigo;
        private final String estadoCodigo;
        private final String numeroExpediente;
        private final Long idResponsableActual;

        private ExpedienteEstado(
                Long idEtapa,
                Long idEstado,
                String etapaCodigo,
                String estadoCodigo,
                String numeroExpediente,
                Long idResponsableActual) {
            this.idEtapa = idEtapa;
            this.idEstado = idEstado;
            this.etapaCodigo = etapaCodigo;
            this.estadoCodigo = estadoCodigo;
            this.numeroExpediente = numeroExpediente;
            this.idResponsableActual = idResponsableActual;
        }
    }
}
