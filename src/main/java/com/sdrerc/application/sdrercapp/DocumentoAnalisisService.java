package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.DocumentoAnalisisDAO;
import java.sql.SQLException;
import java.util.List;

public class DocumentoAnalisisService {

    private final DocumentoAnalisisDAO documentoAnalisisDAO;

    public DocumentoAnalisisService() {
        this(new DocumentoAnalisisDAO());
    }

    public DocumentoAnalisisService(DocumentoAnalisisDAO documentoAnalisisDAO) {
        this.documentoAnalisisDAO = documentoAnalisisDAO;
    }

    public List<CatalogoItemDTO> listarTiposDocumentoAnalizado() throws SQLException {
        return documentoAnalisisDAO.listarTiposDocumentoAnalizado();
    }

    public List<CatalogoItemDTO> listarEstadosDocumento() throws SQLException {
        return documentoAnalisisDAO.listarEstadosDocumento();
    }

    public List<DocumentoAnalizadoDTO> listarDocumentosAnalizados(Long idExpediente) throws SQLException {
        return documentoAnalisisDAO.listarPorExpediente(idExpediente);
    }
}
