package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoVerificacionDTO;
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

public class DocumentoVerificacionDAO {

    private final CatalogoLookupDAO catalogoLookupDAO = new CatalogoLookupDAO();

    public List<CatalogoItemDTO> listarEstadosDocumento() throws SQLException {
        return filtrarEstadosDocumentoAnalizado(catalogoLookupDAO.listarEstadosDocumento());
    }

    public List<DocumentoVerificacionDTO> listarDocumentosAnalizados(Long idExpediente) throws SQLException {
        List<DocumentoVerificacionDTO> documentos = new ArrayList<DocumentoVerificacionDTO>();
        if (idExpediente == null) {
            return documentos;
        }
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlListarDocumentos(conn))) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    documentos.add(new DocumentoVerificacionDTO(
                            getLongOrNull(rs, "id_documento_analizado"),
                            getLongOrNull(rs, "id_expediente"),
                            rs.getString("tipo_documento"),
                            rs.getString("estado_documento_codigo"),
                            rs.getString("estado_documento"),
                            toLocalDate(rs.getDate("fecha_documento")),
                            rs.getString("numero_documento"),
                            rs.getString("descripcion"),
                            rs.getInt("requiere_respuesta") == 1,
                            toLocalDate(rs.getDate("fecha_acuse")),
                            rs.getString("confirmacion_respuesta"),
                            toLocalDate(rs.getDate("fecha_respuesta")),
                            rs.getString("numero_hoja_envio_respuesta"),
                            rs.getInt("notificado") == 1,
                            rs.getString("detalle_observacion"),
                            getLongOrNull(rs, "id_documento_padre"),
                            rs.getInt("nivel"),
                            rs.getInt("orden"),
                            toLocalDate(rs.getDate("fecha_publicacion")),
                            getBooleanOrNull(rs, "existe_oposicion")));
                }
            }
        }
        return documentos;
    }

    public void actualizarEstadoDocumentoAnalizado(
            Long idExpediente,
            Long idDocumentoAnalizado,
            String estadoCodigo,
            String comentario,
            LocalDate fechaEmision,
            String numeroDocumento,
            Long idUsuarioModificador) throws SQLException {
        if (idExpediente == null || idDocumentoAnalizado == null) {
            throw new SQLException("Documento analizado no identificado.");
        }
        try (Connection conn = SdrercAppConnection.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long idEstadoDocumento = catalogoLookupDAO.obtenerEstadoDocumentoId(conn, estadoCodigo);
                if (idEstadoDocumento == null) {
                    throw new SQLException("No se encontró el estado de documento: " + estadoCodigo + ".");
                }
                boolean soportaNumero = soportaNumeroDocumentoAnalizado(conn);
                boolean soportaFecha = soportaFechaDocumentoAnalizado(conn);
                if (!soportaNumero && hasText(numeroDocumento)) {
                    throw new SQLException("Falta la columna NUMERO_DOCUMENTO en EXPEDIENTE_DOCUMENTO_ANALIZADO.");
                }
                if (!soportaFecha && fechaEmision != null) {
                    throw new SQLException("Falta la columna FECHA_DOCUMENTO en EXPEDIENTE_DOCUMENTO_ANALIZADO.");
                }
                String sql = "UPDATE expediente_documento_analizado SET "
                        + "id_estado_documento = ?, "
                        + (soportaFecha ? "fecha_documento = ?, " : "")
                        + (soportaNumero ? "numero_documento = ?, " : "")
                        + "descripcion = ?, "
                        + "modificado_por = ?, modificado_en = SYSTIMESTAMP "
                        + "WHERE id_documento_analizado = ? AND id_expediente = ? AND activo = 1";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, idEstadoDocumento);
                    int index = 2;
                    if (soportaFecha) {
                        setDateOrNull(ps, index++, fechaEmision);
                    }
                    if (soportaNumero) {
                        setStringOrNull(ps, index++, limitar(numeroDocumento, 120));
                    }
                    setStringOrNull(ps, index++, limitar(comentario, 1000));
                    if (idUsuarioModificador == null) {
                        ps.setNull(index++, java.sql.Types.NUMERIC);
                    } else {
                        ps.setLong(index++, idUsuarioModificador);
                    }
                    ps.setLong(index++, idDocumentoAnalizado);
                    ps.setLong(index, idExpediente);
                    int updated = ps.executeUpdate();
                    if (updated != 1) {
                        throw new SQLException("No se pudo actualizar el estado del documento analizado.");
                    }
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static Boolean getBooleanOrNull(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value == 1;
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static String sqlListarDocumentos(Connection conn) throws SQLException {
        boolean soportaRespuesta = soportaRespuestaDocumentoAnalizado(conn);
        boolean soportaNumero = soportaNumeroDocumentoAnalizado(conn);
        boolean soportaDetalle = soportaDetalleObservacionDocumentoAnalizado(conn);
        boolean soportaJerarquia = soportaJerarquiaDocumentoAnalizado(conn);
        boolean soportaPublicacion = soportaPublicacionPreparada(conn);
        boolean soportaOposicion = soportaExisteOposicion(conn);
        return "SELECT da.id_documento_analizado, da.id_expediente, "
                + "td.nombre AS tipo_documento, ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento, "
                + "da.fecha_documento, "
                + (soportaNumero ? "da.numero_documento, " : "CAST(NULL AS VARCHAR2(120)) AS numero_documento, ")
                + "da.descripcion, "
                + (soportaRespuesta
                        ? "NVL(da.requiere_respuesta, 0) AS requiere_respuesta, da.fecha_acuse, "
                        + "da.confirmacion_respuesta, da.fecha_respuesta, da.numero_hoja_envio_respuesta, "
                        + "NVL(da.notificado, 0) AS notificado, "
                        : "0 AS requiere_respuesta, CAST(NULL AS DATE) AS fecha_acuse, "
                        + "CAST(NULL AS VARCHAR2(20)) AS confirmacion_respuesta, CAST(NULL AS DATE) AS fecha_respuesta, "
                        + "CAST(NULL AS VARCHAR2(120)) AS numero_hoja_envio_respuesta, 0 AS notificado, ")
                + (soportaDetalle ? "da.detalle_observacion, " : "CAST(NULL AS VARCHAR2(1000)) AS detalle_observacion, ")
                + (soportaJerarquia
                        ? "da.id_documento_padre, NVL(da.nivel, 0) AS nivel, NVL(da.orden, 0) AS orden, "
                        : "CAST(NULL AS NUMBER) AS id_documento_padre, 0 AS nivel, 0 AS orden, ")
                + (soportaPublicacion
                        ? "(SELECT fecha_publicacion FROM ("
                        + " SELECT p.fecha_publicacion FROM expediente_publicacion p "
                        + " WHERE p.id_expediente = da.id_expediente AND p.activo = 1 "
                        + " ORDER BY p.creado_en DESC, p.id_expediente_publicacion DESC"
                        + ") WHERE ROWNUM = 1) AS fecha_publicacion, "
                        : "CAST(NULL AS DATE) AS fecha_publicacion, ")
                + (soportaOposicion ? "da.existe_oposicion " : "CAST(NULL AS NUMBER(1)) AS existe_oposicion ")
                + "FROM expediente_documento_analizado da "
                + "LEFT JOIN tipo_documento_adjunto td ON td.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                + "WHERE da.id_expediente = ? AND da.activo = 1 "
                + (soportaJerarquia
                        ? "ORDER BY NVL(da.id_documento_padre, da.id_documento_analizado), NVL(da.nivel, 0), "
                        + "NVL(da.orden, da.id_documento_analizado), da.id_documento_analizado"
                        : "ORDER BY da.fecha_documento DESC NULLS LAST, da.id_documento_analizado DESC");
    }

    private static boolean soportaExisteOposicion(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name = 'EXISTE_OPOSICION'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static boolean soportaJerarquiaDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT column_name) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name IN ('ID_DOCUMENTO_PADRE', 'NIVEL', 'ORDEN')";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 3;
        }
    }

    private static boolean soportaPublicacionPreparada(Connection conn) throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(1) FROM user_tables WHERE table_name = 'EXPEDIENTE_PUBLICACION') + "
                + "(SELECT COUNT(1) FROM user_tab_columns WHERE table_name = 'EXPEDIENTE_PUBLICACION' AND column_name = 'FECHA_PUBLICACION') "
                + "AS total FROM dual";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt("total") == 2;
        }
    }

    private static boolean soportaRespuestaDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT column_name) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' "
                + "AND column_name IN ('NOTIFICADO', 'FECHA_ACUSE', 'REQUIERE_RESPUESTA', "
                + "'CONFIRMACION_RESPUESTA', 'FECHA_RESPUESTA', 'NUMERO_HOJA_ENVIO_RESPUESTA')";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 6;
        }
    }

    private static boolean soportaNumeroDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' AND column_name = 'NUMERO_DOCUMENTO'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static boolean soportaFechaDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' AND column_name = 'FECHA_DOCUMENTO'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static boolean soportaDetalleObservacionDocumentoAnalizado(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(1) FROM user_tab_columns "
                + "WHERE table_name = 'EXPEDIENTE_DOCUMENTO_ANALIZADO' AND column_name = 'DETALLE_OBSERVACION'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static void setStringOrNull(PreparedStatement ps, int index, String value) throws SQLException {
        if (!hasText(value)) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    private static void setDateOrNull(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(value));
        }
    }

    private static List<CatalogoItemDTO> filtrarEstadosDocumentoAnalizado(List<CatalogoItemDTO> estados) {
        List<CatalogoItemDTO> filtrados = new ArrayList<CatalogoItemDTO>();
        if (estados == null) {
            return filtrados;
        }
        for (CatalogoItemDTO estado : estados) {
            if (estado != null && esEstadoDocumentoAnalizadoPermitido(estado.getCodigo())) {
                filtrados.add(estado);
            }
        }
        return filtrados;
    }

    private static boolean esEstadoDocumentoAnalizadoPermitido(String codigo) {
        String value = codigo == null ? "" : codigo.trim().toUpperCase();
        return "EN_PROYECTO".equals(value)
                || "EN_DESPACHO".equals(value)
                || "EMITIDO".equals(value)
                || "OBSERVADO".equals(value);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String limitar(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
