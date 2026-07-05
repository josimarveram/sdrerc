package com.sdrerc.application.sdrercapp;

public class CargaDiariaValidacionService extends CargaDiariaReglasService {

    public CargaDiariaValidacionService() {
        super();
    }

    public CargaDiariaValidacionService(
            com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteRegistroDAO expedienteRegistroDAO,
            CorrelativoExpedienteService correlativoExpedienteService,
            GrupoFamiliarHeuristicaService grupoFamiliarHeuristicaService) {
        super(expedienteRegistroDAO, correlativoExpedienteService, grupoFamiliarHeuristicaService);
    }
}
