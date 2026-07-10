package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.AsignacionCartaRespuestaDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO;
import com.sdrerc.domain.dto.sdrercapp.NotificacionIntentoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.DocumentoAnalisisDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.List;

public class DocumentoAnalisisService {

    private final DocumentoAnalisisDAO documentoAnalisisDAO;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public DocumentoAnalisisService() {
        this(new DocumentoAnalisisDAO(), new UsuarioAsignacionService());
    }

    public DocumentoAnalisisService(DocumentoAnalisisDAO documentoAnalisisDAO) {
        this(documentoAnalisisDAO, new UsuarioAsignacionService());
    }

    public DocumentoAnalisisService(
            DocumentoAnalisisDAO documentoAnalisisDAO,
            UsuarioAsignacionService usuarioAsignacionService) {
        this.documentoAnalisisDAO = documentoAnalisisDAO;
        this.usuarioAsignacionService = usuarioAsignacionService;
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

    public List<AsignacionCartaRespuestaDTO> listarCartasRespuestaPendientes() throws SQLException {
        return documentoAnalisisDAO.listarCartasRespuestaPendientes();
    }

    public void guardarRespuestaDocumentoAnalizado(
            Long idExpediente,
            DocumentoAnalizadoDTO documento) throws SQLException {
        documentoAnalisisDAO.actualizarRespuestaDocumentoAnalizado(
                idExpediente,
                documento,
                resolverUsuarioActualSdrercApp());
    }

    public void guardarCartaRespuesta(Long idExpediente, DocumentoAnalizadoDTO carta) throws SQLException {
        documentoAnalisisDAO.guardarCartaRespuesta(idExpediente, carta, resolverUsuarioActualSdrercApp());
    }

    public List<NotificacionAsignacionDocumentoDTO> listarDocumentosAsignacionNotificacion() throws SQLException {
        return documentoAnalisisDAO.listarDocumentosAsignacionNotificacion();
    }

    public List<NotificacionAsignacionDocumentoDTO> listarDocumentosValidacion() throws SQLException {
        return documentoAnalisisDAO.listarDocumentosValidacion();
    }

    public void registrarValidacion(Long idDocumentoAnalizado) throws SQLException {
        documentoAnalisisDAO.registrarValidacion(idDocumentoAnalizado, resolverUsuarioActualSdrercApp());
    }

    public List<NotificacionAsignacionDocumentoDTO> listarDocumentosNotificacion() throws SQLException {
        return documentoAnalisisDAO.listarDocumentosNotificacion();
    }

    public List<NotificacionAsignacionDocumentoDTO> listarDocumentosPublicacion() throws SQLException {
        return documentoAnalisisDAO.listarDocumentosPublicacion();
    }

    public List<NotificacionIntentoDTO> listarIntentosNotificacion(Long idDocumentoAnalizado) throws SQLException {
        return documentoAnalisisDAO.listarIntentosNotificacion(idDocumentoAnalizado);
    }

    public void registrarIntentoNotificacion(
            Long idExpediente,
            Long idDocumentoAnalizado,
            String tipoNotificacionCodigo,
            String codigoNotificacion) throws SQLException {
        documentoAnalisisDAO.registrarIntentoNotificacion(
                idExpediente,
                idDocumentoAnalizado,
                tipoNotificacionCodigo,
                codigoNotificacion,
                resolverUsuarioActualSdrercApp());
    }

    public void asignarNotificacion(
            List<Long> idsDocumentoAnalizado,
            Long idEquipoDestino,
            Long idUsuarioDestino,
            String numeroHojaEnvio) throws SQLException {
        documentoAnalisisDAO.asignarNotificacion(
                idsDocumentoAnalizado, idEquipoDestino, idUsuarioDestino, numeroHojaEnvio, resolverUsuarioActualSdrercApp());
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
