package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.DocumentoVerificacionDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.DocumentoVerificacionDAO;
import java.sql.SQLException;
import java.util.List;

public class DocumentoVerificacionService {

    private final DocumentoVerificacionDAO documentoVerificacionDAO;

    public DocumentoVerificacionService() {
        this(new DocumentoVerificacionDAO());
    }

    public DocumentoVerificacionService(DocumentoVerificacionDAO documentoVerificacionDAO) {
        this.documentoVerificacionDAO = documentoVerificacionDAO;
    }

    public List<DocumentoVerificacionDTO> listarDocumentosAnalizados(Long idExpediente) throws SQLException {
        return documentoVerificacionDAO.listarDocumentosAnalizados(idExpediente);
    }
}
