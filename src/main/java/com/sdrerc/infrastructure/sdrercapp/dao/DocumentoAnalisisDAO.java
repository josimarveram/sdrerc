package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        return catalogoLookupDAO.listarTiposDocumentoAdjunto();
    }

    public List<CatalogoItemDTO> listarEstadosDocumento() throws SQLException {
        return catalogoLookupDAO.listarEstadosDocumento();
    }

    public List<DocumentoAnalizadoDTO> listarPorExpediente(Long idExpediente) throws SQLException {
        List<DocumentoAnalizadoDTO> documentos = new ArrayList<>();
        if (idExpediente == null) {
            return documentos;
        }
        String sql = "SELECT da.id_documento_analizado, da.id_expediente, "
                + "td.codigo AS tipo_documento_codigo, td.nombre AS tipo_documento_nombre, "
                + "ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento_nombre, "
                + "da.fecha_documento, da.descripcion "
                + "FROM expediente_documento_analizado da "
                + "LEFT JOIN tipo_documento_adjunto td ON td.id_tipo_documento_adjunto = da.id_tipo_documento_adjunto "
                + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = da.id_estado_documento "
                + "WHERE da.id_expediente = ? AND da.activo = 1 "
                + "ORDER BY da.fecha_documento DESC NULLS LAST, da.id_documento_analizado DESC";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
                            rs.getString("descripcion")));
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
        String sql = "INSERT INTO expediente_documento_analizado ("
                + "id_expediente, id_tipo_documento_adjunto, id_estado_documento, fecha_documento, "
                + "descripcion, activo, creado_por, creado_en"
                + ") VALUES (?, ?, ?, ?, ?, 1, ?, SYSTIMESTAMP)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            ps.setLong(2, idTipoDocumento);
            ps.setLong(3, idEstadoDocumento);
            if (documento.getFechaDocumento() == null) {
                ps.setDate(4, null);
            } else {
                ps.setDate(4, Date.valueOf(documento.getFechaDocumento()));
            }
            ps.setString(5, limitar(documento.getDescripcion(), 1000));
            if (idUsuarioCreador == null) {
                ps.setNull(6, java.sql.Types.NUMERIC);
            } else {
                ps.setLong(6, idUsuarioCreador);
            }
            ps.executeUpdate();
        }
    }

    private static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
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
}
