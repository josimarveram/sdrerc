package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.CatalogoLookupDAO;
import java.sql.SQLException;
import java.util.List;

public class CatalogoLookupService {

    private final CatalogoLookupDAO catalogoLookupDAO;

    public CatalogoLookupService() {
        this(new CatalogoLookupDAO());
    }

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
}
