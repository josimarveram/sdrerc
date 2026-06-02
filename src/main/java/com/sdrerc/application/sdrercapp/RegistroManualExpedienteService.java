package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.RegistroManualExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.RegistroManualResultadoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteRegistroDAO;
import java.sql.SQLException;
import java.util.List;

public class RegistroManualExpedienteService {

    private final RegistroManualValidacionService validacionService;
    private final ExpedienteRegistroDAO expedienteRegistroDAO;
    private final CorrelativoExpedienteService correlativoExpedienteService;

    public RegistroManualExpedienteService() {
        this(new RegistroManualValidacionService(), new ExpedienteRegistroDAO(), new CorrelativoExpedienteService());
    }

    public RegistroManualExpedienteService(
            RegistroManualValidacionService validacionService,
            ExpedienteRegistroDAO expedienteRegistroDAO,
            CorrelativoExpedienteService correlativoExpedienteService) {
        this.validacionService = validacionService;
        this.expedienteRegistroDAO = expedienteRegistroDAO;
        this.correlativoExpedienteService = correlativoExpedienteService;
    }

    public List<String> validar(RegistroManualExpedienteDTO registro) {
        return validacionService.validar(registro);
    }

    public RegistroManualResultadoDTO registrar(RegistroManualExpedienteDTO registro) throws SQLException {
        List<String> errores = validar(registro);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
        return expedienteRegistroDAO.registrarManual(registro, correlativoExpedienteService);
    }
}
