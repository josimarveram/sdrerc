package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CargaDiariaPreviewDTO;
import com.sdrerc.domain.dto.sdrercapp.CargaDiariaResultadoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteRegistroDAO;
import java.sql.SQLException;
import java.util.List;

public class CargaDiariaRegistroService {

    private final ExpedienteRegistroDAO expedienteRegistroDAO;

    public CargaDiariaRegistroService() {
        this(new ExpedienteRegistroDAO());
    }

    public CargaDiariaRegistroService(ExpedienteRegistroDAO expedienteRegistroDAO) {
        this.expedienteRegistroDAO = expedienteRegistroDAO;
    }

    public CargaDiariaResultadoDTO confirmarCarga(List<CargaDiariaPreviewDTO> registros) throws SQLException {
        return expedienteRegistroDAO.registrarCarga(registros);
    }
}
