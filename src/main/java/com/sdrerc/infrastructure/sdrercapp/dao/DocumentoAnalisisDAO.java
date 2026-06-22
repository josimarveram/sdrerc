package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocumentoAnalisisDAO {

    private final CatalogoLookupDAO catalogoLookupDAO;

    public DocumentoAnalisisDAO() {
        this(new CatalogoLookupDAO());
    }

    public DocumentoAnalisisDAO(CatalogoLookupDAO catalogoLookupDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    public List<CatalogoItemDTO> listarTiposDocumentoAnalizado() throws SQLException {
        return catalogoLookupDAO.listarTiposDocumentoAdjuntoAnalisis();
    }

    public List<CatalogoItemDTO> listarEstadosDocumento() throws SQLException {
        return catalogoLookupDAO.listarEstadosDocumento();
    }

    public List<DocumentoAnalizadoDTO> listarPorExpediente(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            return new ArrayList<DocumentoAnalizadoDTO>();
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            return listarPorExpediente(conn, idExpediente);
        }
    }

    public List<DocumentoAnalizadoDTO> listarPorExpediente(Connection conn, Long idExpediente) throws SQLException {
        List<DocumentoAnalizadoDTO> documentos = new ArrayList<>();
        if (conn == null || idExpediente == null) {
            return documentos;
        }
        boolean soportaRespuesta = soportaRespuestaDocumentoAnalizado(conn);
        String sql = "SELECT da.id_documento_analizado, da.id_expediente, "
                + "td.codigo AS tipo_documento_codigo, td.nombre AS tipo_documento_nombre, "
                + "ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre, "
                + "da.fecha_documento, da.descripcion, "
                + (soportaRespuesta
                        ? "NVL(da.notificado, 0) AS notificado, da.fecha_acuse, "
                        + "NVL(da.requiere_respuesta, 0) AS requiere_respuesta, "
                        + "da.confirmacion_respuesta, da.fecha_respuesta, da.numero_hoja_envio_respuesta "
                        : "0 AS notificado, CAST(NULL AS DATE) AS fecha_acuse, "
                        + "0 AS requiere_respuesta, CAST(NULL AS VARCHAR2(20)) AS confirmacion_respuesta, "
                        + "CAST(NULL AS DATE) AS fecha_respuesta, "
                        + "CAST(NULL AS VARCHAR2(120)) AS numero_hoja_envio_respuesta ")
                + "FROM expediente_documento_analizado da "
                + "LEFT JOIN tipo_documento_adjunto td ON td.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                + "WHERE da.id_expediente = ? AND da.activo = 1 "
                + "ORDER BY da.fecha_documento DESC NULLS LAST, da.id_documento_analizado DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    documentos.add(new DocumentoAnalizadoDTO(
                            getLongOrNull(rs, "id_documento_analizado"),
                            getLongOrNull(rs, "id_expediente"),
                            rs.getString("tipo_documento_codigo"),
                            rs.getString("tipo_documento_nombre"),
                            rs.getString("estado_documento_codigo"),
                            rs.getString("estado_documento_nombre"),
                            toLocalDate(rs.getDate("fecha_documento")),
                            rs.getString("descripcion"),
                            rs.getInt("notificado") == 1,
                            toLocalDate(rs.getDate("fecha_acuse")),
                            rs.getInt("requiere_respuesta") == 1,
                            rs.getString("confirmacion_respuesta"),
                            toLocalDate(rs.getDate("fecha_respuesta")),
                            rs.getString("numero_hoja_envio_respuesta")));
                }
            }
        }
        return documentos;
    }

    public int contarPorExpediente(Connection conn, Long idExpediente) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expediente_documento_analizado WHERE id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void actualizarRespuestaDocumentoAnalizado(
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        if (idExpediente == null || documento == null || documento.getIdDocumentoAnalizado() == null) {
            throw new IllegalArgumentException("Seleccione un documento analizado para actualizar la respuesta.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                actualizarRespuestaDocumentoAnalizado(conn, idExpediente, documento, idUsuarioModificador);
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
    }

    public void actualizarRespuestaDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        if (!soportaRespuestaDocumentoAnalizado(conn)) {
            throw new SQLException("Faltan columnas de respuesta en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 29_patch_documento_analizado_respuesta.sql.");
        }
        String confirmacion = normalizarConfirmacionRespuesta(documento.getConfirmacionRespuesta());
        LocalDate fechaRespuesta = documento.getFechaRespuesta();
        String hojaRespuesta = limitar(emptyToNull(documento.getNumeroHojaEnvioRespuesta()), 120);
        if (!documento.isRequiereRespuesta()) {
            confirmacion = null;
            fechaRespuesta = null;
            hojaRespuesta = null;
        } else if (!documento.isNotificado() || documento.getFechaAcuse() == null) {
            confirmacion = "PENDIENTE";
            fechaRespuesta = null;
            hojaRespuesta = null;
        }
        String sql = "UPDATE expediente_documento_analizado SET "
                + "notificado = ?, fecha_acuse = ?, confirmacion_respuesta = ?, fecha_respuesta = ?, "
                + "numero_hoja_envio_respuesta = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, documento.isNotificado() ? 1 : 0);
            setDateOrNull(ps, 2, documento.getFechaAcuse());
            if (confirmacion == null) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, confirmacion);
            }
            setDateOrNull(ps, 4, fechaRespuesta);
            if (hojaRespuesta == null) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, hojaRespuesta);
            }
            if (idUsuarioModificador == null) {
                ps.setNull(6, Types.NUMERIC);
            } else {
                ps.setLong(6, idUsuarioModificador);
            }
            ps.setLong(7, documento.getIdDocumentoAnalizado());
            ps.setLong(8, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar la respuesta del documento analizado.");
            }
        }
    }

    public void insertarDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioCreador) throws SQLException {
        Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, documento.getTipoDocumentoCodigo());
        Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo());
        if (idTipoDocumento == null) {
            throw new SQLException("No se encontró el tipo de documento analizado: " + documento.getTipoDocumentoCodigo() + ".");
        }
        if (idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        boolean soportaRespuesta = soportaRespuestaDocumentoAnalizado(conn);
        if (!soportaRespuesta && tieneDatosRespuesta(documento)) {
            throw new SQLException("Faltan columnas de respuesta en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 29_patch_documento_analizado_respuesta.sql.");
        }
        String sql = soportaRespuesta
                ? "INSERT INTO expediente_documento_analizado ("
                + "id_expediente, id_tipo_documento_adjunto, id_estado_documento, fecha_documento, "
                + "descripcion, notificado, fecha_acuse, requiere_respuesta, confirmacion_respuesta, "
                + "fecha_respuesta, numero_hoja_envio_respuesta, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)"
                : "INSERT INTO expediente_documento_analizado ("
                + "id_expediente, id_tipo_documento_adjunto, id_estado_documento, fecha_documento, "
                + "descripcion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idTipoDocumento);
            ps.setLong(3, idEstadoDocumento);
            setDateOrNull(ps, 4, documento.getFechaDocumento());
            ps.setString(5, limitar(documento.getDescripcion(), 1000));
            int usuarioIndex;
            if (soportaRespuesta) {
                RespuestaPersistencia respuesta = respuestaPersistencia(documento, true);
                ps.setInt(6, documento.isNotificado() ? 1 : 0);
                setDateOrNull(ps, 7, documento.getFechaAcuse());
                ps.setInt(8, documento.isRequiereRespuesta() ? 1 : 0);
                setStringOrNull(ps, 9, respuesta.confirmacion);
                setDateOrNull(ps, 10, respuesta.fechaRespuesta);
                setStringOrNull(ps, 11, respuesta.hojaRespuesta);
                usuarioIndex = 12;
            } else {
                usuarioIndex = 6;
            }
            if (idUsuarioCreador == null) {
                ps.setNull(usuarioIndex, java.sql.Types.NUMERIC);
            } else {
                ps.setLong(usuarioIndex, idUsuarioCreador);
            }
            ps.executeUpdate();
        }
    }

    public void actualizarDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        Long idTipoDocumento = catalogoLookupDAO.obtenerTipoDocumentoAdjuntoId(conn, documento.getTipoDocumentoCodigo());
        Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo());
        if (idTipoDocumento == null) {
            throw new SQLException("No se encontró el tipo de documento analizado: " + documento.getTipoDocumentoCodigo() + ".");
        }
        if (idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        boolean soportaRespuesta = soportaRespuestaDocumentoAnalizado(conn);
        if (!soportaRespuesta && tieneDatosRespuesta(documento)) {
            throw new SQLException("Faltan columnas de respuesta en EXPEDIENTE_DOCUMENTO_ANALIZADO. Ejecute el script 29_patch_documento_analizado_respuesta.sql.");
        }
        String sql = soportaRespuesta
                ? "UPDATE expediente_documento_analizado SET "
                + "id_tipo_documento_adjunto = ?, id_estado_documento = ?, fecha_documento = ?, descripcion = ?, "
                + "notificado = ?, fecha_acuse = ?, requiere_respuesta = ?, confirmacion_respuesta = ?, "
                + "fecha_respuesta = ?, numero_hoja_envio_respuesta = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1"
                : "UPDATE expediente_documento_analizado SET "
                + "id_tipo_documento_adjunto = ?, id_estado_documento = ?, fecha_documento = ?, descripcion = ?, "
                + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTipoDocumento);
            ps.setLong(2, idEstadoDocumento);
            setDateOrNull(ps, 3, documento.getFechaDocumento());
            ps.setString(4, limitar(documento.getDescripcion(), 1000));
            int userIndex;
            int idIndex;
            if (soportaRespuesta) {
                RespuestaPersistencia respuesta = respuestaPersistencia(documento, true);
                ps.setInt(5, documento.isNotificado() ? 1 : 0);
                setDateOrNull(ps, 6, documento.getFechaAcuse());
                ps.setInt(7, documento.isRequiereRespuesta() ? 1 : 0);
                setStringOrNull(ps, 8, respuesta.confirmacion);
                setDateOrNull(ps, 9, respuesta.fechaRespuesta);
                setStringOrNull(ps, 10, respuesta.hojaRespuesta);
                userIndex = 11;
                idIndex = 12;
            } else {
                userIndex = 5;
                idIndex = 6;
            }
            if (idUsuarioModificador == null) {
                ps.setNull(userIndex, Types.NUMERIC);
            } else {
                ps.setLong(userIndex, idUsuarioModificador);
            }
            ps.setLong(idIndex, documento.getIdDocumentoAnalizado());
            ps.setLong(idIndex + 1, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el documento analizado.");
            }
        }
    }

    public void actualizarEstadoDocumentoAnalizado(
            Connection conn,
            Long idExpediente,
            DocumentoAnalizadoDTO documento,
            Long idUsuarioModificador) throws SQLException {
        Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, documento.getEstadoDocumentoCodigo());
        if (idEstadoDocumento == null) {
            throw new SQLException("No se encontró el estado de documento: " + documento.getEstadoDocumentoCodigo() + ".");
        }
        String sql = "UPDATE expediente_documento_analizado SET "
                + "id_estado_documento = ?, modificado_por = ?, modificado_en = SYSTIMESTAMP "
                + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEstadoDocumento);
            if (idUsuarioModificador == null) {
                ps.setNull(2, java.sql.Types.NUMERIC);
            } else {
                ps.setLong(2, idUsuarioModificador);
            }
            ps.setLong(3, documento.getIdDocumentoAnalizado());
            ps.setLong(4, idExpediente);
            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new SQLException("No se pudo actualizar el estado del documento analizado.");
            }
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static void setDateOrNull(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }

    private static void setStringOrNull(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    private static boolean soportaRespuestaDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT column_name) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name IN ("
                + "'NOTIFICADO', 'FECHA_ACUSE', 'REQUIERE_RESPUESTA', "
                + "'CONFIRMACION_RESPUESTA', 'FECHA_RESPUESTA', 'NUMERO_HOJA_ENVIO_RESPUESTA'"
                + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 6;
        }
    }

    private static String normalizarConfirmacionRespuesta(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        normalized = normalized.replace('Í', 'I');
        if ("SI".equals(normalized) || "NO".equals(normalized) || "PENDIENTE".equals(normalized)) {
            return normalized;
        }
        return "PENDIENTE";
    }

    private static RespuestaPersistencia respuestaPersistencia(
            DocumentoAnalizadoDTO documento,
            boolean incluirRequiereRespuesta) {
        boolean requiereRespuesta = incluirRequiereRespuesta && documento.isRequiereRespuesta();
        String confirmacion = requiereRespuesta
                ? normalizarConfirmacionRespuesta(documento.getConfirmacionRespuesta())
                : null;
        LocalDate fechaRespuesta = requiereRespuesta ? documento.getFechaRespuesta() : null;
        String hojaRespuesta = requiereRespuesta ? limitar(emptyToNull(documento.getNumeroHojaEnvioRespuesta()), 120) : null;
        return new RespuestaPersistencia(confirmacion, fechaRespuesta, hojaRespuesta);
    }

    private static boolean tieneDatosRespuesta(DocumentoAnalizadoDTO documento) {
        return documento.isNotificado()
                || documento.getFechaAcuse() != null
                || documento.isRequiereRespuesta()
                || !emptyString(documento.getConfirmacionRespuesta()).isEmpty()
                || documento.getFechaRespuesta() != null
                || !emptyString(documento.getNumeroHojaEnvioRespuesta()).isEmpty();
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static String emptyString(String value) {
        return value == null ? "" : value.trim();
    }

    private static void rollbackSilencioso(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // rollback de contingencia
        }
    }

    private static String limitar(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static final class RespuestaPersistencia {
        private final String confirmacion;
        private final LocalDate fechaRespuesta;
        private final String hojaRespuesta;

        private RespuestaPersistencia(String confirmacion, LocalDate fechaRespuesta, String hojaRespuesta) {
            this.confirmacion = confirmacion;
            this.fechaRespuesta = fechaRespuesta;
            this.hojaRespuesta = hojaRespuesta;
        }
    }
}
