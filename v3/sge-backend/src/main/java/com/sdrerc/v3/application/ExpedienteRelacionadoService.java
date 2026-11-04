package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.ExpedienteRelacionResultadoDTO;
import com.sdrerc.v3.domain.ExpedienteRelacionadoDTO;
import com.sdrerc.v3.infrastructure.dao.ExpedienteRelacionadoDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Port de com.sdrerc.application.sdrercapp.ExpedienteRelacionadoService (V2). Compartido por
 * Registro y Asignación (ver {@code ExpedienteRelacionadoController}): ambos módulos usan
 * exactamente el mismo servicio de asociación en V2 (ver CLAUDE.md raíz, sección Registro), así
 * que en V3 se porta una sola vez y ambas páginas del frontend consumen el mismo endpoint —en vez
 * de duplicar el backend por módulo. Única diferencia real: resuelve el usuario actual desde
 * {@link CurrentUser} en vez de {@code SessionContext}, mismo patrón ya aplicado en el resto del
 * backend.
 */
@Service
public class ExpedienteRelacionadoService {

    private final ExpedienteRelacionadoDAO expedienteRelacionadoDAO;

    public ExpedienteRelacionadoService(ExpedienteRelacionadoDAO expedienteRelacionadoDAO) {
        this.expedienteRelacionadoDAO = expedienteRelacionadoDAO;
    }

    public List<ExpedienteRelacionadoDTO> listarSolicitudesDelGrupo(Long idExpediente) throws SQLException {
        return expedienteRelacionadoDAO.listarSolicitudesDelGrupo(idExpediente);
    }

    public List<ExpedienteRelacionadoDTO> listarAsociadosConfirmados(Long idExpediente) throws SQLException {
        return expedienteRelacionadoDAO.listarAsociadosConfirmados(idExpediente);
    }

    public ExpedienteRelacionResultadoDTO asociarRelacionados(
            Long idExpedientePrincipal,
            List<Long> idsRelacionados,
            String descripcion) throws SQLException {
        return expedienteRelacionadoDAO.asociarRelacionados(
                idExpedientePrincipal,
                idsRelacionados,
                CurrentUser.get().idUsuario(),
                descripcion);
    }
}
