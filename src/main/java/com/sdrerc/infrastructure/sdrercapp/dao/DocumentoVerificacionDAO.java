package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoVerificacionDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocumentoVerificacionDAO {

    private final CatalogoLookupDAO catalogoLookupDAO = new CatalogoLookupDAO();

    public List<CatalogoItemDTO> listarEstadosDocumento() throws SQLException {
        return catalogoLookupDAO.listarEstadosDocumento();
    }

    public List<DocumentoVerificacionDTO> listarDocumentosAnalizados(Long idExpediente) throws SQLException {
        List<DocumentoVerificacionDTO> documentos = new ArrayList<DocumentoVerificacionDTO>();
        if (idExpediente == null) {
            return documentos;
        }
        String sql = "SELECT da.id_documento_analizado, da.id_expediente, "
                + "td.nombre AS tipo_documento, ed.codigo AS estado_documento_codigo, ed.nombre AS estado_documento, "
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
                    documentos.add(new DocumentoVerificacionDTO(
                            getLongOrNull(rs, "id_documento_analizado"),
                            getLongOrNull(rs, "id_expediente"),
                            rs.getString("tipo_documento"),
                            rs.getString("estado_documento_codigo"),
                            rs.getString("estado_documento"),
                            toLocalDate(rs.getDate("fecha_documento")),
                            rs.getString("descripcion")));
                }
            }
        }
        return documentos;
    }

    public void actualizarEstadoDocumentoAnalizado(
            Long idExpediente,
            Long idDocumentoAnalizado,
            String estadoCodigo,
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
                    ps.setLong(3, idDocumentoAnalizado);
                    ps.setLong(4, idExpediente);
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

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
