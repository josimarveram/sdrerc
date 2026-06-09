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
        if (registro == null) {
            throw new IllegalArgumentException("Complete los datos del formulario antes de registrar.");
        }
        List<String> errores = validar(registro);
        if (!errores.isEmpty()) {
            registro.setObservacionesGenerales(unirObservaciones(
                    registro.getObservacionesGenerales(),
                    "Advertencias de validación: " + String.join(" | ", errores)));
        }
        return expedienteRegistroDAO.registrarManual(registro, correlativoExpedienteService);
    }

    private String unirObservaciones(String actual, String nueva) {
        if (actual == null || actual.trim().isEmpty()) {
            return nueva;
        }
        if (nueva == null || nueva.trim().isEmpty()) {
            return actual;
        }
        if (actual.contains(nueva)) {
            return actual;
        }
        return actual.trim() + " | " + nueva.trim();
    }
}
