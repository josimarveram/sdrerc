package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.ExpedienteTimelineDTO;
import com.sdrerc.v3.infrastructure.dao.ExpedienteTrazabilidadDAO;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

/** Línea de tiempo completa de un expediente — ver Javadoc de {@link ExpedienteTrazabilidadDAO}. */
@Service
public class ExpedienteTrazabilidadService {

    private final ExpedienteTrazabilidadDAO expedienteTrazabilidadDAO;

    public ExpedienteTrazabilidadService(ExpedienteTrazabilidadDAO expedienteTrazabilidadDAO) {
        this.expedienteTrazabilidadDAO = expedienteTrazabilidadDAO;
    }

    public List<ExpedienteTimelineDTO> listarTrazabilidad(Long idExpediente) throws SQLException {
        return expedienteTrazabilidadDAO.listarPorExpediente(idExpediente);
    }
}
