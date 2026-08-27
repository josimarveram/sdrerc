package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.AnalisisDetalleDTO;
import com.sdrerc.v3.domain.AnalisisExpedienteDTO;
import com.sdrerc.v3.domain.AnalisisRegistroDTO;
import com.sdrerc.v3.domain.AnalisisResultadoDTO;
import com.sdrerc.v3.domain.DocumentoAnalizadoDTO;
import com.sdrerc.v3.domain.ExpedienteEdicionManualDTO;
import com.sdrerc.v3.infrastructure.dao.AnalisisExpedienteDAO;
import com.sdrerc.v3.infrastructure.dao.CatalogoLookupDAO;
import com.sdrerc.v3.infrastructure.dao.DocumentoAnalisisDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import com.sdrerc.v3.web.dto.DocumentoWordDTO;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Port acotado de com.sdrerc.application.sdrercapp.AnalisisExpedienteService (V2) — mismo alcance
 * que {@link AnalisisExpedienteDAO} (20/08/2026, primer incremento: bandeja + "Recibir
 * expediente"). Resuelve {@code esAdmin}/{@code idsEquipoActual} desde {@link CurrentUser} antes de
 * llamar al DAO — primer Service de V3 que necesita visibilidad por asignación (ver Javadoc del
 * DAO), generalizando el mismo patrón ya usado para "autor de historial sustituido si actúa
 * ADMIN_SISTEMA" en los demás `*ExpedienteService`.
 */
@Service
public class AnalisisExpedienteService {

    private final AnalisisExpedienteDAO analisisExpedienteDAO;
    private final CatalogoLookupDAO catalogoLookupDAO;
    private final DocumentoAnalisisDAO documentoAnalisisDAO;
    private final AnalisisValidacionService analisisValidacionService;
    private final ExpedienteEdicionManualService expedienteEdicionManualService;
    private final AnalisisPlantillaDocumentoService analisisPlantillaDocumentoService;

    public AnalisisExpedienteService(
            AnalisisExpedienteDAO analisisExpedienteDAO,
            CatalogoLookupDAO catalogoLookupDAO,
            DocumentoAnalisisDAO documentoAnalisisDAO,
            AnalisisValidacionService analisisValidacionService,
            ExpedienteEdicionManualService expedienteEdicionManualService,
            AnalisisPlantillaDocumentoService analisisPlantillaDocumentoService) {
        this.analisisExpedienteDAO = analisisExpedienteDAO;
        this.catalogoLookupDAO = catalogoLookupDAO;
        this.documentoAnalisisDAO = documentoAnalisisDAO;
        this.analisisValidacionService = analisisValidacionService;
        this.expedienteEdicionManualService = expedienteEdicionManualService;
        this.analisisPlantillaDocumentoService = analisisPlantillaDocumentoService;
    }

    public List<AnalisisExpedienteDTO> buscarExpedientes(
            String textoLibre,
            String estadoCodigo,
            LocalDate fechaSolicitudDesde,
            LocalDate fechaSolicitudHasta,
            Integer limite) throws SQLException {
        var usuario = CurrentUser.get();
        boolean esAdmin = usuario.hasRole("ADMIN_SISTEMA");
        List<Long> idsEquipoActual = esAdmin ? List.of() : catalogoLookupDAO.listarIdsEquipoDeUsuario(usuario.idUsuario());
        return analisisExpedienteDAO.buscarExpedientes(
                textoLibre, estadoCodigo, fechaSolicitudDesde, fechaSolicitudHasta,
                esAdmin, usuario.idUsuario(), idsEquipoActual, limite);
    }

    public AnalisisResultadoDTO recibirExpediente(Long idExpediente, String comentario) throws SQLException {
        return analisisExpedienteDAO.recibirExpediente(idExpediente, comentario, CurrentUser.get().idUsuario());
    }

    public AnalisisResultadoDTO recibirDocumentoAsociado(
            Long idExpedientePrincipal, Long idExpedienteAsociado, String comentario) throws SQLException {
        return analisisExpedienteDAO.recibirDocumentoAsociado(
                idExpedientePrincipal, idExpedienteAsociado, comentario, CurrentUser.get().idUsuario());
    }

    public AnalisisDetalleDTO obtenerAnalisisRegistrado(Long idExpediente) throws SQLException {
        return analisisExpedienteDAO.obtenerAnalisisRegistrado(idExpediente);
    }

    public List<String> validarRegistroAnalisis(AnalisisRegistroDTO registro) {
        return analisisValidacionService.validarRegistroAnalisis(registro);
    }

    public AnalisisResultadoDTO registrarAnalisis(AnalisisRegistroDTO registro) throws SQLException {
        return analisisExpedienteDAO.registrarAnalisis(registro, CurrentUser.get().idUsuario());
    }

    public AnalisisResultadoDTO enviarVerificacion(Long idExpediente, String comentario) throws SQLException {
        return analisisExpedienteDAO.enviarVerificacion(idExpediente, comentario, CurrentUser.get().idUsuario());
    }

    public List<DocumentoAnalizadoDTO> listarDocumentosAnalizados(Long idExpediente) throws SQLException {
        return documentoAnalisisDAO.listarPorExpediente(idExpediente);
    }

    public Long guardarDocumentoAnalizado(Long idExpediente, DocumentoAnalizadoDTO documento) throws SQLException {
        return documentoAnalisisDAO.guardarDocumentoAnalizado(idExpediente, documento, CurrentUser.get().idUsuario());
    }

    public void darBajaDocumentoAnalizado(Long idExpediente, Long idDocumentoAnalizado) throws SQLException {
        documentoAnalisisDAO.darBajaDocumentoAnalizado(idExpediente, idDocumentoAnalizado, CurrentUser.get().idUsuario());
    }

    /**
     * Genera el .docx de un documento analizado — alcance simplificado, ver Javadoc de
     * {@link AnalisisPlantillaDocumentoService}. Reutiliza el mismo DTO que ya arma el "Panel de
     * datos" ({@code ExpedienteEdicionManualService#obtenerDatos}) para el mapa de variables, sin
     * SELECT propio.
     */
    public DocumentoWordDTO generarDocumentoWord(Long idExpediente, Long idDocumentoAnalizado) throws SQLException, IOException {
        DocumentoAnalizadoDTO documento = documentoAnalisisDAO.listarPorExpediente(idExpediente).stream()
                .filter(d -> idDocumentoAnalizado.equals(d.getIdDocumentoAnalizado()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El documento analizado seleccionado ya no existe."));
        ExpedienteEdicionManualDTO expediente = expedienteEdicionManualService.obtenerDatos(idExpediente);
        String resultadoNombre = analisisExpedienteDAO.obtenerAnalisisRegistrado(idExpediente).getResultadoNombre();
        Map<String, String> valores = analisisPlantillaDocumentoService.valores(expediente, documento, resultadoNombre);
        byte[] contenido = analisisPlantillaDocumentoService.generarDocumento(
                documento.getTipoDocumentoCodigo(), documento.getTipoDocumentoNombre(), valores);
        String nombreArchivo = analisisPlantillaDocumentoService.nombreSugerido(
                expediente.getNumeroExpediente(), documento.getTipoDocumentoNombre());
        return new DocumentoWordDTO(contenido, nombreArchivo);
    }
}
