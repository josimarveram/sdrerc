package com.sdrerc.application.sdrercapp;

import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteRegistroDAO;
import java.sql.SQLException;
import java.util.List;

public class GrupoFamiliarRegistroService {

    private final ExpedienteRegistroDAO expedienteRegistroDAO;

    public GrupoFamiliarRegistroService() {
        this(new ExpedienteRegistroDAO());
    }

    public GrupoFamiliarRegistroService(ExpedienteRegistroDAO expedienteRegistroDAO) {
        this.expedienteRegistroDAO = expedienteRegistroDAO;
    }

    public int registrarGrupoFamiliar(List<Long> idsExpediente) throws SQLException {
        return expedienteRegistroDAO.registrarGrupoFamiliar(idsExpediente);
    }
}
