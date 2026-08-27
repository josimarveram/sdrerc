package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.CatalogoItemDTO;
import com.sdrerc.v3.domain.CentroAtencionDTO;
import com.sdrerc.v3.domain.MunicipalidadDTO;
import com.sdrerc.v3.domain.UnidadOrganicaDTO;
import com.sdrerc.v3.infrastructure.dao.CatalogoLookupDAO;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Subconjunto de com.sdrerc.application.sdrercapp.CatalogoLookupService (V2): solo los 4
 * catálogos que necesita el formulario de Registro manual.
 */
@Service
public class CatalogoLookupService {

    private final CatalogoLookupDAO catalogoLookupDAO;

    public CatalogoLookupService(CatalogoLookupDAO catalogoLookupDAO) {
        this.catalogoLookupDAO = catalogoLookupDAO;
    }

    public List<CatalogoItemDTO> listarCanalesRecepcion() throws SQLException {
        return catalogoLookupDAO.listarCanalesRecepcion();
    }

    public List<CatalogoItemDTO> listarProcedimientosRegistrales() throws SQLException {
        return catalogoLookupDAO.listarProcedimientosRegistrales();
    }

    public List<CatalogoItemDTO> listarTiposDocumento() throws SQLException {
        return catalogoLookupDAO.listarTiposDocumento();
    }

    public List<CatalogoItemDTO> listarTiposActa() throws SQLException {
        return catalogoLookupDAO.listarTiposActa();
    }

    public List<CentroAtencionDTO> listarCentrosAtencion() throws SQLException {
        return catalogoLookupDAO.listarCentrosAtencion();
    }

    public List<UnidadOrganicaDTO> listarUnidadesOrganicas() throws SQLException {
        return catalogoLookupDAO.listarUnidadesOrganicas();
    }

    public List<MunicipalidadDTO> listarMunicipalidades() throws SQLException {
        return catalogoLookupDAO.listarMunicipalidades();
    }

    public List<CatalogoItemDTO> listarTiposDocumentoAdjuntoAnalisis() throws SQLException {
        return catalogoLookupDAO.listarTiposDocumentoAdjuntoAnalisis();
    }

    public List<CatalogoItemDTO> listarEstadosDocumentoAnalisis() throws SQLException {
        return catalogoLookupDAO.listarEstadosDocumentoAnalisis();
    }

    public List<CatalogoItemDTO> listarEstadosDocumentoNotificacion() throws SQLException {
        return catalogoLookupDAO.listarEstadosDocumentoNotificacion();
    }

    public List<CatalogoItemDTO> listarResultadosAnalisis() throws SQLException {
        return catalogoLookupDAO.listarResultadosAnalisis();
    }

    public List<CatalogoItemDTO> listarMotivosNoCorresponde() throws SQLException {
        return catalogoLookupDAO.listarMotivosNoCorresponde();
    }

    public List<CatalogoItemDTO> listarTiposObservacion() throws SQLException {
        return catalogoLookupDAO.listarTiposObservacion();
    }
}
