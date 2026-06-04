package com.sdrerc.infrastructure.sdrercapp.dao;

import com.sdrerc.domain.dto.sdrercapp.DocumentoFirmaDTO;
import com.sdrerc.infrastructure.database.SdrercAppConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocumentoFirmaDAO {

    public List<DocumentoFirmaDTO> listarPorExpediente(Long idExpediente) throws SQLException {
        List<DocumentoFirmaDTO> documentos = new ArrayList<DocumentoFirmaDTO>();
        if (idExpediente == null) {
            return documentos;
        }
        String sql = "SELECT td.nombre AS tipo_documento, ed.nombre AS estado_documento, "
                + "d.numero_documento, d.nombre_documento, d.fecha_documento "
                + "FROM expediente_documento d "
                + "LEFT JOIN tipo_documento_adjunto td ON td.id_tipo_documento_adjunto = d.id_tipo_documento_adjunto "
                + "LEFT JOIN estado_documento ed ON ed.id_estado_documento = d.id_estado_documento "
                + "WHERE d.id_expediente = ? AND d.activo = 1 "
                + "ORDER BY d.fecha_documento DESC NULLS LAST, d.id_expediente_documento DESC";
        try (Connection conn = SdrercAppConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    documentos.add(new DocumentoFirmaDTO(
                            rs.getString("tipo_documento"),
                            rs.getString("estado_documento"),
                            rs.getString("numero_documento"),
                            rs.getString("nombre_documento"),
                            toLocalDate(rs.getDate("fecha_documento"))));
                }
            }
        }
        return documentos;
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
