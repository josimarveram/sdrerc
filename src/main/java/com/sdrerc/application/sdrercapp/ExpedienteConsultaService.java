package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.ExpedienteBandejaDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteBandejaDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ExpedienteConsultaService {

    private final ExpedienteBandejaDAO expedienteBandejaDAO;

    public ExpedienteConsultaService() {
        this(new ExpedienteBandejaDAO());
    }

    public ExpedienteConsultaService(ExpedienteBandejaDAO expedienteBandejaDAO) {
        this.expedienteBandejaDAO = expedienteBandejaDAO;
    }

    public List<ExpedienteBandejaDTO> listarBandeja() throws SQLException {
        return expedienteBandejaDAO.listarTodos();
    }

    public List<ExpedienteBandejaDTO> buscarBandeja(String textoLibre, String etapaCodigo, String estadoCodigo, int limite) throws SQLException {
        return expedienteBandejaDAO.buscar(textoLibre, etapaCodigo, estadoCodigo, limite);
    }

    public List<ExpedienteBandejaDTO> buscarBandeja(
            String textoLibre,
            String etapaCodigo,
            String estadoCodigo,
            LocalDate fechaSolicitudDesde,
            LocalDate fechaSolicitudHasta,
            int limite) throws SQLException {
        return expedienteBandejaDAO.buscar(
                textoLibre,
                etapaCodigo,
                estadoCodigo,
                fechaSolicitudDesde,
                fechaSolicitudHasta,
                limite);
    }

    public List<ExpedienteBandejaDTO> buscarBandeja(String textoLibre) throws SQLException {
        return expedienteBandejaDAO.buscarPorTexto(textoLibre);
    }
}
