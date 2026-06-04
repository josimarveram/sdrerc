package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.DocumentoEjecucionDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.DocumentoEjecucionDAO;
import java.sql.SQLException;
import java.util.List;

public class DocumentoEjecucionService {

    private final DocumentoEjecucionDAO documentoEjecucionDAO;

    public DocumentoEjecucionService() {
        this(new DocumentoEjecucionDAO());
    }

    public DocumentoEjecucionService(DocumentoEjecucionDAO documentoEjecucionDAO) {
        this.documentoEjecucionDAO = documentoEjecucionDAO;
    }

    public List<DocumentoEjecucionDTO> listarPorExpediente(Long idExpediente) throws SQLException {
        return documentoEjecucionDAO.listarPorExpediente(idExpediente);
    }
}
