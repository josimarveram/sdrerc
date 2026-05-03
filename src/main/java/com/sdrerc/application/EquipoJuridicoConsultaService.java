package com.sdrerc.application;

import com.sdrerc.domain.model.EquipoJuridicoConsultaItem;
import com.sdrerc.domain.model.EquipoJuridicoDetalle;
import com.sdrerc.domain.model.EquipoJuridicoResumen;
import com.sdrerc.domain.model.EquipoJuridicoUpdateRequest;
import com.sdrerc.domain.model.PaginatedResult;
import com.sdrerc.domain.model.SupervisorComboItem;
import com.sdrerc.infrastructure.repository.EquipoJuridicoRepository;
import java.sql.SQLException;
import java.util.List;

public class EquipoJuridicoConsultaService {

    private final EquipoJuridicoRepository repository = new EquipoJuridicoRepository();

    public List<SupervisorComboItem> listarSupervisoresActivos() throws SQLException {
        return repository.listarSupervisoresActivos();
    }

    public PaginatedResult<EquipoJuridicoConsultaItem> buscarAbogados(
            Long supervisorId,
            boolean soloSinSupervisor,
            String filtro,
            String estado,
            int page,
            int pageSize) throws SQLException {
        return repository.buscarAbogados(supervisorId, soloSinSupervisor, filtro, estado, page, pageSize);
    }

    public EquipoJuridicoResumen obtenerResumen() throws SQLException {
        return repository.obtenerResumen();
    }

    public EquipoJuridicoDetalle obtenerDetalleAbogado(Long abogadoId) throws SQLException {
        return repository.obtenerDetalleAbogado(abogadoId);
    }

    public void actualizarEquipoJuridico(EquipoJuridicoUpdateRequest request) throws SQLException {
        repository.actualizarEquipoJuridico(request);
    }
}
