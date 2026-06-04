package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.DocumentoFirmaDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.DocumentoFirmaDAO;
import java.sql.SQLException;
import java.util.List;

public class DocumentoFirmaService {

    private final DocumentoFirmaDAO documentoFirmaDAO;

    public DocumentoFirmaService() {
        this(new DocumentoFirmaDAO());
    }

    public DocumentoFirmaService(DocumentoFirmaDAO documentoFirmaDAO) {
        this.documentoFirmaDAO = documentoFirmaDAO;
    }

    public List<DocumentoFirmaDTO> listarPorExpediente(Long idExpediente) throws SQLException {
        return documentoFirmaDAO.listarPorExpediente(idExpediente);
    }
}
