package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.ExpedienteBandejaDTO;
import com.sdrerc.v3.infrastructure.dao.ExpedienteBandejaDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/** Port literal de com.sdrerc.application.sdrercapp.ExpedienteConsultaService (V2). */
@Service
public class ExpedienteConsultaService {

    private final ExpedienteBandejaDAO expedienteBandejaDAO;

    public ExpedienteConsultaService(ExpedienteBandejaDAO expedienteBandejaDAO) {
        this.expedienteBandejaDAO = expedienteBandejaDAO;
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
}
