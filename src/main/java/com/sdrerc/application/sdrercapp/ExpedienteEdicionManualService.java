package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.ExpedienteEdicionManualDTO;
import com.sdrerc.domain.dto.sdrercapp.RegistroManualResultadoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteEdicionManualDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExpedienteEdicionManualService {

    private final RegistroManualValidacionService validacionService;
    private final ExpedienteEdicionManualDAO edicionManualDAO;

    public ExpedienteEdicionManualService() {
        this(new RegistroManualValidacionService(), new ExpedienteEdicionManualDAO());
    }

    public ExpedienteEdicionManualService(
            RegistroManualValidacionService validacionService,
            ExpedienteEdicionManualDAO edicionManualDAO) {
        this.validacionService = validacionService;
        this.edicionManualDAO = edicionManualDAO;
    }

    public ExpedienteEdicionManualDTO obtenerParaEdicion(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para editar.");
        }
        return edicionManualDAO.obtenerParaEdicion(idExpediente);
    }

    public ExpedienteEdicionManualDTO obtenerParaEdicionDesdeAnalisis(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para editar.");
        }
        return edicionManualDAO.obtenerParaEdicionDesdeAnalisis(idExpediente);
    }

    public List<String> validar(ExpedienteEdicionManualDTO dto) {
        List<String> errores = new ArrayList<String>();
        if (dto == null || dto.getIdExpediente() == null) {
            errores.add("Seleccione un expediente para editar.");
            return errores;
        }
        errores.addAll(validacionService.validar(dto));
        return errores;
    }

    public RegistroManualResultadoDTO guardar(ExpedienteEdicionManualDTO dto) throws SQLException {
        List<String> errores = validar(dto);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(" | ", errores));
        }
        return edicionManualDAO.guardar(dto);
    }

    public RegistroManualResultadoDTO guardarDesdeAnalisis(ExpedienteEdicionManualDTO dto) throws SQLException {
        List<String> errores = validar(dto);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(" | ", errores));
        }
        return edicionManualDAO.guardarDesdeAnalisis(dto);
    }
}
