package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.AccionPermitidaDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteConsolaDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteTimelineDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.AccionesPermitidasDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteConsolaDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteTimelineDAO;
import java.sql.SQLException;
import java.util.List;

public class ExpedienteDetalleService {

    private final ExpedienteConsolaDAO expedienteConsolaDAO;
    private final ExpedienteTimelineDAO expedienteTimelineDAO;
    private final AccionesPermitidasDAO accionesPermitidasDAO;

    public ExpedienteDetalleService() {
        this(new ExpedienteConsolaDAO(), new ExpedienteTimelineDAO(), new AccionesPermitidasDAO());
    }

    public ExpedienteDetalleService(
            ExpedienteConsolaDAO expedienteConsolaDAO,
            ExpedienteTimelineDAO expedienteTimelineDAO,
            AccionesPermitidasDAO accionesPermitidasDAO) {
        this.expedienteConsolaDAO = expedienteConsolaDAO;
        this.expedienteTimelineDAO = expedienteTimelineDAO;
        this.accionesPermitidasDAO = accionesPermitidasDAO;
    }

    public ExpedienteConsolaDTO obtenerConsolaPorExpediente(Long idExpediente) throws SQLException {
        return expedienteConsolaDAO.obtenerPorExpediente(idExpediente);
    }

    public List<ExpedienteTimelineDTO> listarTimeline(Long idExpediente) throws SQLException {
        return expedienteTimelineDAO.listarPorExpediente(idExpediente);
    }

    public List<AccionPermitidaDTO> listarAccionesPermitidas(Long idExpediente) throws SQLException {
        return accionesPermitidasDAO.listarPorExpediente(idExpediente);
    }
}
