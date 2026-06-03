package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteRelacionadoDAO;
import java.sql.SQLException;
import java.util.List;

public class ExpedienteRelacionadoDeteccionService {

    private final ExpedienteRelacionadoDAO expedienteRelacionadoDAO;

    public ExpedienteRelacionadoDeteccionService() {
        this(new ExpedienteRelacionadoDAO());
    }

    public ExpedienteRelacionadoDeteccionService(ExpedienteRelacionadoDAO expedienteRelacionadoDAO) {
        this.expedienteRelacionadoDAO = expedienteRelacionadoDAO;
    }

    public List<ExpedienteRelacionadoDTO> listarPosiblesRelacionados(Long idExpediente) throws SQLException {
        return expedienteRelacionadoDAO.listarPosiblesRelacionados(idExpediente);
    }
}
