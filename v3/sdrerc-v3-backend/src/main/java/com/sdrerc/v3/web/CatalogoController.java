package com.sdrerc.v3.web;

import com.sdrerc.v3.application.CatalogoLookupService;
import com.sdrerc.v3.domain.CatalogoItemDTO;
import com.sdrerc.v3.domain.CentroAtencionDTO;
import com.sdrerc.v3.domain.MunicipalidadDTO;
import com.sdrerc.v3.domain.UnidadOrganicaDTO;
import java.sql.SQLException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Catálogos para combos del formulario de Registro manual (canal, procedimiento, tipo de documento, tipo de acta). */
@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    private final CatalogoLookupService catalogoLookupService;

    public CatalogoController(CatalogoLookupService catalogoLookupService) {
        this.catalogoLookupService = catalogoLookupService;
    }

    @GetMapping("/canales-recepcion")
    public List<CatalogoItemDTO> canalesRecepcion() throws SQLException {
        return catalogoLookupService.listarCanalesRecepcion();
    }

    @GetMapping("/procedimientos-registrales")
    public List<CatalogoItemDTO> procedimientosRegistrales() throws SQLException {
        return catalogoLookupService.listarProcedimientosRegistrales();
    }

    @GetMapping("/tipos-documento")
    public List<CatalogoItemDTO> tiposDocumento() throws SQLException {
        return catalogoLookupService.listarTiposDocumento();
    }

    @GetMapping("/tipos-acta")
    public List<CatalogoItemDTO> tiposActa() throws SQLException {
        return catalogoLookupService.listarTiposActa();
    }

    /** Catálogo de Solicitante (tipo Centro de Atención): 240 filas, se cargan completas. */
    @GetMapping("/centros-atencion")
    public List<CentroAtencionDTO> centrosAtencion() throws SQLException {
        return catalogoLookupService.listarCentrosAtencion();
    }

    /** Catálogo de Solicitante (tipo Unidad Orgánica): 7 valores fijos. */
    @GetMapping("/unidades-organicas")
    public List<UnidadOrganicaDTO> unidadesOrganicas() throws SQLException {
        return catalogoLookupService.listarUnidadesOrganicas();
    }

    /** Catálogo de Solicitante (tipo Municipalidad): 1652 filas, se cargan completas. */
    @GetMapping("/municipalidades")
    public List<MunicipalidadDTO> municipalidades() throws SQLException {
        return catalogoLookupService.listarMunicipalidades();
    }

    /** Catálogos de la subpestaña "Análisis" (Bandeja Análisis). */
    @GetMapping("/tipos-documento-adjunto-analisis")
    public List<CatalogoItemDTO> tiposDocumentoAdjuntoAnalisis() throws SQLException {
        return catalogoLookupService.listarTiposDocumentoAdjuntoAnalisis();
    }

    @GetMapping("/estados-documento-analisis")
    public List<CatalogoItemDTO> estadosDocumentoAnalisis() throws SQLException {
        return catalogoLookupService.listarEstadosDocumentoAnalisis();
    }

    /** Catálogo de "Estado documento" de las grillas de Notificación (incluye VALIDADO). */
    @GetMapping("/estados-documento-notificacion")
    public List<CatalogoItemDTO> estadosDocumentoNotificacion() throws SQLException {
        return catalogoLookupService.listarEstadosDocumentoNotificacion();
    }

    @GetMapping("/resultados-analisis")
    public List<CatalogoItemDTO> resultadosAnalisis() throws SQLException {
        return catalogoLookupService.listarResultadosAnalisis();
    }

    @GetMapping("/motivos-no-corresponde")
    public List<CatalogoItemDTO> motivosNoCorresponde() throws SQLException {
        return catalogoLookupService.listarMotivosNoCorresponde();
    }

    @GetMapping("/tipos-observacion")
    public List<CatalogoItemDTO> tiposObservacion() throws SQLException {
        return catalogoLookupService.listarTiposObservacion();
    }
}
