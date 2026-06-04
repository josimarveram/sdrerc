package com.sdrerc.infrastructure.sdrercapp.dao;

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

    public List<DocumentoVerificacionDTO> listarDocumentosAnalizados(Long idExpediente) throws SQLException {
        List<DocumentoVerificacionDTO> documentos = new ArrayList<DocumentoVerificacionDTO>();
        if (idExpediente == null) {
            return documentos;
        }
        String sql = "SELECT td.nombre AS tipo_documento, ed.nombre AS estado_documento, "
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
                            rs.getString("tipo_documento"),
                            rs.getString("estado_documento"),
                            toLocalDate(rs.getDate("fecha_documento")),
                            rs.getString("descripcion")));
                }
            }
        }
        return documentos;
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
