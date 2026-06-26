package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoVerificacionDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.DocumentoVerificacionDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.List;

public class DocumentoVerificacionService {

    private final DocumentoVerificacionDAO documentoVerificacionDAO;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public DocumentoVerificacionService() {
        this(new DocumentoVerificacionDAO(), new UsuarioAsignacionService());
    }

    public DocumentoVerificacionService(
            DocumentoVerificacionDAO documentoVerificacionDAO,
            UsuarioAsignacionService usuarioAsignacionService) {
        this.documentoVerificacionDAO = documentoVerificacionDAO;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public List<CatalogoItemDTO> listarEstadosDocumento() throws SQLException {
        return documentoVerificacionDAO.listarEstadosDocumento();
    }

    public List<DocumentoVerificacionDTO> listarDocumentosAnalizados(Long idExpediente) throws SQLException {
        return documentoVerificacionDAO.listarDocumentosAnalizados(idExpediente);
    }

    public void actualizarEstadoDocumentoAnalizado(
            Long idExpediente,
            Long idDocumentoAnalizado,
            String estadoCodigo,
            String detalleObservacion) throws SQLException {
        documentoVerificacionDAO.actualizarEstadoDocumentoAnalizado(
                idExpediente,
                idDocumentoAnalizado,
                estadoCodigo,
                detalleObservacion,
                resolverUsuarioActualSdrercApp());
    }

    private Long resolverUsuarioActualSdrercApp() {
        try {
            String username = SessionContext.getUsername();
            return usuarioAsignacionService.obtenerIdUsuarioActivoPorUsername(username);
        } catch (Exception ex) {
            return null;
        }
    }
}
