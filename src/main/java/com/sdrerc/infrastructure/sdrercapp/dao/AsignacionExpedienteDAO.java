package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.AsignacionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AsignacionExpedienteDAO {

    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final String CODIGO_FLUJO = "SDRERC_TO_BE";
    private static final String CODIGO_ETAPA_ORIGEN = "REGISTRO";
    private static final String CODIGO_ESTADO_ORIGEN = "REGISTRADO";
    private static final String CODIGO_ETAPA_DESTINO = "ASIGNACION";
    private static final String CODIGO_ESTADO_DESTINO = "ASIGNADO";
    private static final String CODIGO_MOVIMIENTO = "ASIGNACION_ABOGADO";

    private final CatalogoLookupDAO catalogoLookupDAO;
    private final ExpedienteRelacionadoDAO expedienteRelacionadoDAO;

    public AsignacionExpedienteDAO() {
        this(new CatalogoLookupDAO(), new ExpedienteRelacionadoDAO());
    }

    public AsignacionExpedienteDAO(CatalogoLookupDAO catalogoLookupDAO, ExpedienteRelacionadoDAO expedienteRelacionadoDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.expedienteRelacionadoDAO = expedienteRelacionadoDAO;
    }

    public List<AsignacionExpedienteDTO> buscarPendientes(String textoLibre, int limite) throws SQLException {
        return buscarExpedientes(textoLibre, CODIGO_ESTADO_ORIGEN, null, null, limite);
    }

    public List<AsignacionExpedienteDTO> buscarExpedientes(
            String textoLibre,
            String estadoCodigo,
            LocalDate fechaSolicitudDesde,
            LocalDate fechaSolicitudHasta,
            int limite) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (");
        sql.append("SELECT DISTINCT e.id_expediente, e.numero_expediente, e.numero_tramite_documentario, ");
        sql.append("esol.asunto AS procedimiento, ta.nombre AS tipo_acta, ea.numero_acta, ");
        sql.append(nombrePersona("p")).append(" AS titular, ");
        sql.append(nombrePersona("ps")).append(" AS solicitante, p.numero_documento AS numero_documento_titular, ");
        sql.append("(SELECT MAX(ua.nombre_completo) FROM expediente_asignacion axa ");
        sql.append(" JOIN usuario ua ON ua.id_usuario = axa.id_usuario_asignado ");
        sql.append(" WHERE axa.id_expediente = e.id_expediente AND axa.activa = 1 AND axa.activo = 1) AS abogado_asignado, ");
        sql.append("esol.fecha_recepcion, esol.potencial_duplicado, esol.observacion AS observacion_solicitud, ");
        sql.append("e.fecha_registro, et.codigo AS etapa_codigo, est.codigo AS estado_codigo, ");
        sql.append("(SELECT COUNT(*) FROM expediente_asignacion ax ");
        sql.append(" WHERE ax.id_expediente = e.id_expediente AND ax.activa = 1 AND ax.activo = 1) AS asignacion_activa ");
        sql.append("FROM expediente e ");
        sql.append("JOIN etapa_expediente et ON et.id_etapa = e.id_etapa_actual ");
        sql.append("JOIN estado_expediente est ON est.id_estado = e.id_estado_actual ");
        sql.append("LEFT JOIN expediente_solicitud esol ON esol.id_expediente = e.id_expediente AND esol.activo = 1 ");
        sql.append("LEFT JOIN expediente_acta ea ON ea.id_expediente = e.id_expediente AND ea.activo = 1 ");
        sql.append("LEFT JOIN tipo_acta ta ON ta.id_tipo_acta = ea.id_tipo_acta ");
        sql.append("LEFT JOIN expediente_persona ep ON ep.id_expediente = e.id_expediente AND ep.activo = 1 AND UPPER(ep.tipo_relacion_persona) = 'TITULAR' ");
        sql.append("LEFT JOIN persona p ON p.id_persona = ep.id_persona AND p.activo = 1 ");
        sql.append("LEFT JOIN persona ps ON ps.id_persona = esol.id_persona_solicitante AND ps.activo = 1 ");
        sql.append("WHERE e.activo = 1 ");

        if (hasText(estadoCodigo) && !"TODOS".equalsIgnoreCase(estadoCodigo)) {
            sql.append("AND est.codigo = ? ");
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
            sql.append("AND (");
            sql.append("UPPER(NVL(e.numero_expediente, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(e.numero_tramite_documentario, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(esol.asunto, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(ea.numero_acta, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(").append(nombrePersona("p")).append(", '')) LIKE ? ");
            sql.append("OR UPPER(NVL(").append(nombrePersona("ps")).append(", '')) LIKE ? ");
            sql.append("OR UPPER(NVL(p.numero_documento, '')) LIKE ? ");
            sql.append("OR UPPER(NVL(esol.observacion, '')) LIKE ? ");
            sql.append(") ");
            String pattern = "%" + textoLibre.trim().toUpperCase(Locale.ROOT) + "%";
            for (int i = 0; i < 8; i++) {
                params.add(pattern);
            }
        }

        sql.append("ORDER BY e.fecha_registro ASC NULLS LAST, e.id_expediente ASC");
        sql.append(") WHERE ROWNUM <= ?");
        params.add(normalizarLimite(limite));

        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<AsignacionExpedienteDTO> expedientes = new ArrayList<>();
                while (rs.next()) {
                    expedientes.add(mapPendiente(conn, rs));
                }
                return expedientes;
            }
        }
    }

    public List<CatalogoItemDTO> listarEstadosExpediente() throws SQLException {
        return catalogoLookupDAO.listarEstadosExpediente();
    }

    public AsignacionResultadoDTO asignarExpedientes(
            List<Long> idsExpediente,
            Long idEquipoDestino,
            Long idAbogadoResponsable,
            String comentario,
            Long idUsuarioAsignador) throws SQLException {
        Set<Long> idsUnicos = normalizarIds(idsExpediente);
        if (idsUnicos.isEmpty()) {
            throw new IllegalArgumentException("Seleccione al menos un expediente para asignar.");
        }
        if (idEquipoDestino == null) {
            throw new IllegalArgumentException("Seleccione el equipo destino.");
        }
        if (idAbogadoResponsable == null) {
            throw new IllegalArgumentException("Seleccione el abogado responsable.");
        }

        List<String> detalles = new ArrayList<>();
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idEtapaOrigen = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, CODIGO_ETAPA_ORIGEN), "etapa REGISTRO");
                Long idEstadoOrigen = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, CODIGO_ESTADO_ORIGEN), "estado REGISTRADO");
                Long idEtapaDestino = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, CODIGO_ETAPA_DESTINO), "etapa ASIGNACION");
                Long idEstadoDestino = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, CODIGO_ESTADO_DESTINO), "estado ASIGNADO");
                Long idMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO), "movimiento ASIGNACION_ABOGADO");
                validarTransicion(conn, idEtapaOrigen, idEstadoOrigen, idEtapaDestino, idEstadoDestino);
                validarEquipoActivo(conn, idEquipoDestino);
                validarAbogadoAsignable(conn, idAbogadoResponsable, idEquipoDestino);

                for (Long idExpediente : idsUnicos) {
                    ExpedienteBloqueado expediente = bloquearExpediente(conn, idExpediente);
                    if (!CODIGO_ETAPA_ORIGEN.equalsIgnoreCase(expediente.etapaCodigo)
                            || !CODIGO_ESTADO_ORIGEN.equalsIgnoreCase(expediente.estadoCodigo)) {
                        throw new SQLException("El expediente ya se encuentra asignado.");
                    }
                    if (tieneAsignacionActiva(conn, idExpediente)) {
                        throw new SQLException("El expediente ya se encuentra asignado.");
                    }

                    Long idAsignacion = insertarAsignacion(
                            conn,
                            idExpediente,
                            idAbogadoResponsable,
                            idEquipoDestino,
                            idEtapaDestino,
                            comentario,
                            idUsuarioAsignador);
                    actualizarExpediente(
                            conn,
                            idExpediente,
                            idEtapaDestino,
                            idEstadoDestino,
                            idAbogadoResponsable,
                            idEquipoDestino,
                            idUsuarioAsignador);
                    insertarHistorial(
                            conn,
                            idExpediente,
                            idMovimiento,
                            idEtapaOrigen,
                            idEstadoOrigen,
                            idEtapaDestino,
                            idEstadoDestino,
                            idUsuarioAsignador,
                            idAbogadoResponsable,
                            idEquipoDestino,
                            idAsignacion,
                            comentario);
                    detalles.add(expediente.numeroExpediente + " asignado.");
                }

                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
            } catch (Exception ex) {
                rollbackSilencioso(conn);
                conn.setAutoCommit(previousAutoCommit);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException(ex.getMessage(), ex);
            }
        }

        return AsignacionResultadoDTO.exito(
                idsUnicos.size(),
                idsUnicos.size() + " expediente(s) asignado(s) correctamente.",
                detalles);
    }

    private AsignacionExpedienteDTO mapPendiente(Connection conn, ResultSet rs) throws SQLException {
        Long idExpediente = getLongOrNull(rs, "id_expediente");
        return new AsignacionExpedienteDTO(
                idExpediente,
                rs.getString("numero_expediente"),
                rs.getString("numero_tramite_documentario"),
                rs.getString("procedimiento"),
                rs.getString("tipo_acta"),
                rs.getString("numero_acta"),
                rs.getString("titular"),
                rs.getString("solicitante"),
                rs.getString("abogado_asignado"),
                rs.getString("numero_documento_titular"),
                toLocalDate(rs.getDate("fecha_recepcion")),
                toLocalDateTime(rs.getTimestamp("fecha_registro")),
                rs.getString("etapa_codigo"),
                rs.getString("estado_codigo"),
                rs.getInt("asignacion_activa") > 0,
                expedienteRelacionadoDAO.contarPosiblesRelacionados(conn, idExpediente),
                rs.getInt("potencial_duplicado") > 0,
                rs.getString("observacion_solicitud"));
    }

    private void validarTransicion(
            Connection conn,
            Long idEtapaOrigen,
            Long idEstadoOrigen,
            Long idEtapaDestino,
            Long idEstadoDestino) throws SQLException {
        String sql = "SELECT 1 FROM flujo f "
                + "JOIN flujo_transicion ft ON ft.id_flujo = f.id_flujo "
                + "WHERE f.codigo = ? AND f.activo = 1 "
                + "AND ft.codigo_accion = ? AND ft.activo = 1 "
                + "AND ft.id_etapa_origen = ? AND ft.id_estado_origen = ? "
                + "AND ft.id_etapa_destino = ? AND ft.id_estado_destino = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, CODIGO_FLUJO);
            ps.setString(2, CODIGO_MOVIMIENTO);
            ps.setLong(3, idEtapaOrigen);
            ps.setLong(4, idEstadoOrigen);
            ps.setLong(5, idEtapaDestino);
            ps.setLong(6, idEstadoDestino);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No existe transición activa REGISTRO/REGISTRADO -> ASIGNACION/ASIGNADO para ASIGNACION_ABOGADO.");
                }
            }
        }
    }

    private void validarEquipoActivo(Connection conn, Long idEquipo) throws SQLException {
        String sql = "SELECT 1 FROM equipo WHERE id_equipo = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEquipo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El equipo destino no está activo o no existe.");
                }
            }
        }
    }

    private void validarAbogadoAsignable(Connection conn, Long idUsuario, Long idEquipo) throws SQLException {
        String sql = "SELECT 1 FROM usuario u "
                + "JOIN usuario_rol ur ON ur.id_usuario = u.id_usuario AND ur.activo = 1 "
                + "JOIN rol r ON r.id_rol = ur.id_rol AND r.activo = 1 "
                + "JOIN equipo_usuario eu ON eu.id_usuario = u.id_usuario AND eu.activo = 1 "
                + "WHERE u.id_usuario = ? "
                + "AND eu.id_equipo = ? "
                + "AND u.activo = 1 "
                + "AND UPPER(u.estado) = 'ACTIVO' "
                + "AND UPPER(r.codigo) IN ('ABOGADO', 'ANALISTA')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idUsuario);
            ps.setLong(2, idEquipo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El abogado seleccionado no está activo o no pertenece al equipo destino.");
                }
            }
        }
    }

    private ExpedienteBloqueado bloquearExpediente(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT e.id_expediente, e.numero_expediente, et.codigo AS etapa_codigo, est.codigo AS estado_codigo "
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
                        rs.getString("numero_expediente"),
                        rs.getString("etapa_codigo"),
                        rs.getString("estado_codigo"));
            }
        }
    }

    private boolean tieneAsignacionActiva(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT 1 FROM expediente_asignacion WHERE id_expediente = ? AND activa = 1 AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Long insertarAsignacion(
            Connection conn,
            Long idExpediente,
            Long idAbogado,
            Long idEquipo,
            Long idEtapa,
            String comentario,
            Long idUsuarioAsignador) throws SQLException {
        String sql = "INSERT INTO expediente_asignacion ("
                + "id_expediente, id_usuario_asignado, id_equipo_asignado, id_etapa, fecha_asignacion, "
                + "activa, es_abogado_principal, es_reasignacion_excepcional, motivo, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, SYSTIMESTAMP, 1, 1, 0, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE_ASIGNACION"})) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idAbogado);
            ps.setLong(3, idEquipo);
            ps.setLong(4, idEtapa);
            ps.setString(5, limitar(comentario, 1000));
            setLongOrNull(ps, 6, idUsuarioAsignador);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "expediente_asignacion");
        }
    }

    private void actualizarExpediente(
            Connection conn,
            Long idExpediente,
            Long idEtapaDestino,
            Long idEstadoDestino,
            Long idAbogadoResponsable,
            Long idEquipoDestino,
            Long idUsuarioModificador) throws SQLException {
        String sql = "UPDATE expediente SET "
                + "id_etapa_actual = ?, "
                + "id_estado_actual = ?, "
                + "id_usuario_responsable_actual = ?, "
                + "id_usuario_abogado_inicial = NVL(id_usuario_abogado_inicial, ?), "
                + "id_equipo_responsable_actual = ?, "
                + "fecha_ultimo_movimiento = SYSTIMESTAMP, "
                + "modificado_por = ?, "
                + "modificado_en = SYSTIMESTAMP "
                + "WHERE id_expediente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEtapaDestino);
            ps.setLong(2, idEstadoDestino);
            ps.setLong(3, idAbogadoResponsable);
            ps.setLong(4, idAbogadoResponsable);
            ps.setLong(5, idEquipoDestino);
            setLongOrNull(ps, 6, idUsuarioModificador);
            ps.setLong(7, idExpediente);
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
            Long idUsuarioAsignador,
            Long idAbogadoResponsable,
            Long idEquipoDestino,
            Long idAsignacion,
            String comentario) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, "
                + "id_etapa_origen, id_estado_origen, id_etapa_destino, id_estado_destino, "
                + "id_usuario_origen, id_usuario_destino, id_equipo_destino, "
                + "tabla_relacionada, id_registro_relacionado, comentario, motivo, activo, creado_por, creado_en"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, ?, ?, ?, ?, ?, 'EXPEDIENTE_ASIGNACION', ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idMovimiento);
            ps.setLong(3, idEtapaOrigen);
            ps.setLong(4, idEstadoOrigen);
            ps.setLong(5, idEtapaDestino);
            ps.setLong(6, idEstadoDestino);
            setLongOrNull(ps, 7, idUsuarioAsignador);
            ps.setLong(8, idAbogadoResponsable);
            ps.setLong(9, idEquipoDestino);
            ps.setLong(10, idAsignacion);
            ps.setString(11, comentario == null || comentario.trim().isEmpty()
                    ? "Asignación de expediente a abogado responsable."
                    : limitar(comentario.trim(), 2000));
            ps.setString(12, CODIGO_MOVIMIENTO);
            setLongOrNull(ps, 13, idUsuarioAsignador);
            ps.executeUpdate();
        }
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

    private static Set<Long> normalizarIds(List<Long> ids) {
        Set<Long> result = new LinkedHashSet<>();
        if (ids == null) {
            return result;
        }
        for (Long id : ids) {
            if (id != null) {
                result.add(id);
            }
        }
        return result;
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

    private static class ExpedienteBloqueado {

        private final String numeroExpediente;
        private final String etapaCodigo;
        private final String estadoCodigo;

        private ExpedienteBloqueado(String numeroExpediente, String etapaCodigo, String estadoCodigo) {
            this.numeroExpediente = numeroExpediente == null ? "" : numeroExpediente;
            this.etapaCodigo = etapaCodigo == null ? "" : etapaCodigo;
            this.estadoCodigo = estadoCodigo == null ? "" : estadoCodigo;
        }
    }
}
