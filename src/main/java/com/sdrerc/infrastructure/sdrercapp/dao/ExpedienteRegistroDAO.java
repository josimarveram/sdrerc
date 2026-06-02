package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.application.sdrercapp.CorrelativoExpedienteService;
import com.sdrerc.domain.dto.sdrercapp.CargaDiariaPreviewDTO;
import com.sdrerc.domain.dto.sdrercapp.CargaDiariaResultadoDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpedienteRegistroDAO {

    private static final String CODIGO_ETAPA_REGISTRO = "REGISTRO";
    private static final String CODIGO_ESTADO_REGISTRADO = "REGISTRADO";
    private static final String CODIGO_MOVIMIENTO_CARGA_DIARIA = "IMPORTACION_CARGA_DIARIA";

    private final CatalogoLookupDAO catalogoLookupDAO;

    public ExpedienteRegistroDAO() {
        this(new CatalogoLookupDAO());
    }

    public ExpedienteRegistroDAO(CatalogoLookupDAO catalogoLookupDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    public Map<Integer, String> detectarDuplicadosContraBase(List<CargaDiariaPreviewDTO> registros) throws SQLException {
        Map<Integer, String> duplicados = new LinkedHashMap<>();
        if (registros == null || registros.isEmpty()) {
            return duplicados;
        }

        try (Connection conn = SdrercAppConnection.getConnection()) {
            for (CargaDiariaPreviewDTO item : registros) {
                List<String> motivos = new ArrayList<>();
                String porTramite = buscarPorTramite(conn, item.getNumeroTramite());
                if (porTramite != null) {
                    motivos.add("Trámite ya existe en " + porTramite);
                }
                String porActa = buscarPorActa(conn, item.getActa());
                if (porActa != null) {
                    motivos.add("Acta ya existe en " + porActa);
                }
                if (!motivos.isEmpty()) {
                    duplicados.put(item.getFila(), String.join("; ", motivos));
                }
            }
        }
        return duplicados;
    }

    public CargaDiariaResultadoDTO registrarCarga(
            List<CargaDiariaPreviewDTO> registros,
            CorrelativoExpedienteService correlativoService) throws SQLException {
        if (registros == null || registros.isEmpty()) {
            return new CargaDiariaResultadoDTO(0, 0, "No hay registros para confirmar.", registros);
        }

        List<CargaDiariaPreviewDTO> candidatos = new ArrayList<>();
        int omitidos = 0;
        for (CargaDiariaPreviewDTO item : registros) {
            if (item.isListoParaRegistrar() && !item.isRegistrado()) {
                candidatos.add(item);
            } else {
                omitidos++;
            }
        }
        if (candidatos.isEmpty()) {
            return new CargaDiariaResultadoDTO(0, omitidos, "No hay filas listas para registrar.", registros);
        }

        List<RegistroConfirmado> confirmados = new ArrayList<>();
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idEtapaRegistro = requerirId(catalogoLookupDAO.obtenerEtapaId(conn, CODIGO_ETAPA_REGISTRO), "etapa REGISTRO");
                Long idEstadoRegistrado = requerirId(catalogoLookupDAO.obtenerEstadoId(conn, CODIGO_ESTADO_REGISTRADO), "estado REGISTRADO");
                Long idTipoMovimiento = requerirId(catalogoLookupDAO.obtenerTipoMovimientoId(conn, CODIGO_MOVIMIENTO_CARGA_DIARIA), "movimiento IMPORTACION_CARGA_DIARIA");

                for (CargaDiariaPreviewDTO item : candidatos) {
                    Long idTitular = insertarPersona(conn, item.getTitular());
                    Long idRemitente = hasText(item.getRemitente()) && !equalsIgnoreCase(item.getRemitente(), item.getTitular())
                            ? insertarPersona(conn, item.getRemitente())
                            : null;
                    Long idSolicitante = idRemitente == null ? idTitular : idRemitente;

                    Long idExpediente = insertarExpediente(conn, item, idEtapaRegistro, idEstadoRegistrado);
                    String numeroExpediente = correlativoService.generarDesdeId(idExpediente);
                    actualizarNumeroExpediente(conn, idExpediente, numeroExpediente);

                    insertarSolicitud(conn, item, idExpediente, idSolicitante);
                    insertarExpedientePersona(conn, idExpediente, idTitular, "TITULAR");
                    if (idRemitente != null) {
                        insertarExpedientePersona(conn, idExpediente, idRemitente, "REMITENTE");
                    }
                    insertarActa(conn, item, idExpediente);
                    insertarDocumento(conn, item, idExpediente);
                    insertarHistorial(conn, item, idExpediente, idTipoMovimiento, idEtapaRegistro, idEstadoRegistrado);

                    confirmados.add(new RegistroConfirmado(item, idExpediente, numeroExpediente));
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

        for (RegistroConfirmado confirmado : confirmados) {
            confirmado.item.setRegistrado(true);
            confirmado.item.setListoParaRegistrar(false);
            confirmado.item.setEstadoValidacion("Registrado");
            confirmado.item.setMensajeValidacion("Registrado en SDRERC_APP.");
            confirmado.item.setIdExpedienteRegistrado(confirmado.idExpediente);
            confirmado.item.setNumeroExpedienteGenerado(confirmado.numeroExpediente);
        }

        return new CargaDiariaResultadoDTO(
                confirmados.size(),
                omitidos,
                confirmados.size() + " expediente(s) registrado(s) en SDRERC_APP.",
                registros);
    }

    private String buscarPorTramite(Connection conn, String numeroTramite) throws SQLException {
        if (!hasText(numeroTramite)) {
            return null;
        }
        String sql = "SELECT numero_expediente FROM expediente "
                + "WHERE activo = 1 AND UPPER(numero_tramite_documentario) = ? AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroTramite.trim().toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("numero_expediente") : null;
            }
        }
    }

    private String buscarPorActa(Connection conn, String acta) throws SQLException {
        if (!hasText(acta)) {
            return null;
        }
        String sql = "SELECT e.numero_expediente FROM expediente e "
                + "JOIN expediente_acta a ON a.id_expediente = e.id_expediente "
                + "WHERE e.activo = 1 AND a.activo = 1 AND UPPER(a.numero_acta) = ? AND ROWNUM = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, acta.trim().toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("numero_expediente") : null;
            }
        }
    }

    private Long insertarPersona(Connection conn, String nombre) throws SQLException {
        String sql = "INSERT INTO persona (razon_social, activo) VALUES (?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_PERSONA"})) {
            ps.setString(1, nombre);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "persona");
        }
    }

    private Long insertarExpediente(Connection conn, CargaDiariaPreviewDTO item, Long idEtapa, Long idEstado) throws SQLException {
        String sql = "INSERT INTO expediente ("
                + "numero_expediente, numero_tramite_documentario, id_etapa_actual, id_estado_actual, "
                + "fecha_registro, fecha_ultimo_movimiento, prioridad, requiere_publicacion, "
                + "expediente_digital_completo, archivado, cerrado, activo"
                + ") VALUES (NULL, ?, ?, ?, SYSTIMESTAMP, SYSTIMESTAMP, 'NORMAL', 0, 0, 0, 0, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_EXPEDIENTE"})) {
            ps.setString(1, item.getNumeroTramite());
            ps.setLong(2, idEtapa);
            ps.setLong(3, idEstado);
            ps.executeUpdate();
            return obtenerGeneratedKey(ps, "expediente");
        }
    }

    private void actualizarNumeroExpediente(Connection conn, Long idExpediente, String numeroExpediente) throws SQLException {
        String sql = "UPDATE expediente SET numero_expediente = ?, modificado_en = SYSTIMESTAMP WHERE id_expediente = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroExpediente);
            ps.setLong(2, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo registrar el número de expediente generado.");
            }
        }
    }

    private void insertarSolicitud(Connection conn, CargaDiariaPreviewDTO item, Long idExpediente, Long idPersonaSolicitante) throws SQLException {
        String sql = "INSERT INTO expediente_solicitud ("
                + "id_expediente, id_persona_solicitante, numero_tramite_documentario, fecha_recepcion, "
                + "asunto, observacion, es_tramite_virtual, potencial_duplicado, activo"
                + ") VALUES (?, ?, ?, ?, ?, ?, 0, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            setLongOrNull(ps, 2, idPersonaSolicitante);
            ps.setString(3, item.getNumeroTramite());
            ps.setDate(4, item.getFechaRecepcion() == null ? null : Date.valueOf(item.getFechaRecepcion()));
            ps.setString(5, item.getTipoProcedimiento());
            ps.setString(6, item.getObservacionInicial());
            ps.setInt(7, item.isPosibleDuplicado() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    private void insertarExpedientePersona(Connection conn, Long idExpediente, Long idPersona, String tipoRelacion) throws SQLException {
        if (idPersona == null) {
            return;
        }
        String sql = "INSERT INTO expediente_persona (id_expediente, id_persona, tipo_relacion_persona, activo) "
                + "VALUES (?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idPersona);
            ps.setString(3, tipoRelacion);
            ps.executeUpdate();
        }
    }

    private void insertarActa(Connection conn, CargaDiariaPreviewDTO item, Long idExpediente) throws SQLException {
        if (!hasText(item.getActa())) {
            return;
        }
        String sql = "INSERT INTO expediente_acta (id_expediente, numero_acta, anio_acta, activo) "
                + "VALUES (?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, item.getActa());
            if (item.getFechaRecepcion() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, item.getFechaRecepcion().getYear());
            }
            ps.executeUpdate();
        }
    }

    private void insertarDocumento(Connection conn, CargaDiariaPreviewDTO item, Long idExpediente) throws SQLException {
        if (!hasText(item.getTipoDocumento())) {
            return;
        }
        String sql = "INSERT INTO expediente_documento ("
                + "id_expediente, nombre_documento, numero_documento, fecha_documento, activo"
                + ") VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setString(2, item.getTipoDocumento());
            ps.setString(3, item.getNumeroTramite());
            ps.setDate(4, item.getFechaRecepcion() == null ? null : Date.valueOf(item.getFechaRecepcion()));
            ps.executeUpdate();
        }
    }

    private void insertarHistorial(
            Connection conn,
            CargaDiariaPreviewDTO item,
            Long idExpediente,
            Long idTipoMovimiento,
            Long idEtapaDestino,
            Long idEstadoDestino) throws SQLException {
        String sql = "INSERT INTO expediente_historial ("
                + "id_expediente, id_tipo_movimiento, fecha_movimiento, id_etapa_destino, id_estado_destino, "
                + "tabla_relacionada, id_registro_relacionado, comentario, motivo, activo"
                + ") VALUES (?, ?, SYSTIMESTAMP, ?, ?, 'EXPEDIENTE', ?, ?, 'CARGA_DIARIA', 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idTipoMovimiento);
            ps.setLong(3, idEtapaDestino);
            ps.setLong(4, idEstadoDestino);
            ps.setLong(5, idExpediente);
            ps.setString(6, "Registro inicial por carga diaria. Trámite: " + item.getNumeroTramite());
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

    private void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NUMERIC);
        } else {
            ps.setLong(index, value);
        }
    }

    private void rollbackSilencioso(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // El error original se reporta al usuario; el rollback fallido no debe ocultarlo.
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }

    private static class RegistroConfirmado {

        private final CargaDiariaPreviewDTO item;
        private final Long idExpediente;
        private final String numeroExpediente;

        private RegistroConfirmado(CargaDiariaPreviewDTO item, Long idExpediente, String numeroExpediente) {
            this.item = item;
            this.idExpediente = idExpediente;
            this.numeroExpediente = numeroExpediente;
        }
    }
}
