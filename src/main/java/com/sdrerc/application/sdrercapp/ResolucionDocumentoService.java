package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.ResolucionDocumentoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.ResolucionDocumentoDAO;
import java.sql.SQLException;

public class ResolucionDocumentoService {

    private final ResolucionDocumentoDAO resolucionDocumentoDAO;

    public ResolucionDocumentoService() {
        this(new ResolucionDocumentoDAO());
    }

    public ResolucionDocumentoService(ResolucionDocumentoDAO resolucionDocumentoDAO) {
        this.resolucionDocumentoDAO = resolucionDocumentoDAO;
    }

    public ResolucionDocumentoDTO obtenerActiva(Long idExpediente) throws SQLException {
        return resolucionDocumentoDAO.obtenerActiva(idExpediente);
    }
}
