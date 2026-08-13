package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.GrupoFamiliarCandidatoDTO;
import com.sdrerc.v3.domain.GrupoFamiliarIntegranteDTO;
import com.sdrerc.v3.domain.GrupoFamiliarResultadoDTO;
import com.sdrerc.v3.infrastructure.dao.GrupoFamiliarDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Port de com.sdrerc.application.sdrercapp.GrupoFamiliarService (V2). Compartido por Registro y
 * Asignación (ver {@code GrupoFamiliarController}), mismo patrón que
 * {@code ExpedienteRelacionadoService}/"Asociar": un solo backend, ambas páginas del frontend
 * consumen el mismo endpoint. Única diferencia real: resuelve el usuario actual desde
 * {@link CurrentUser} en vez de {@code SessionContext}.
 */
@Service
public class GrupoFamiliarService {

    private final GrupoFamiliarDAO grupoFamiliarDAO;

    public GrupoFamiliarService(GrupoFamiliarDAO grupoFamiliarDAO) {
        this.grupoFamiliarDAO = grupoFamiliarDAO;
    }

    public List<GrupoFamiliarCandidatoDTO> listarPosiblesIntegrantes(Long idExpediente) throws SQLException {
        return grupoFamiliarDAO.listarPosiblesIntegrantes(idExpediente);
    }

    public List<GrupoFamiliarCandidatoDTO> buscarPosiblesIntegrantesManual(Long idExpediente, String texto) throws SQLException {
        return grupoFamiliarDAO.buscarPosiblesIntegrantesManual(idExpediente, texto);
    }

    public List<GrupoFamiliarIntegranteDTO> listarIntegrantesGrupoFamiliar(Long idExpediente) throws SQLException {
        return grupoFamiliarDAO.listarIntegrantesGrupoFamiliar(idExpediente);
    }

    public GrupoFamiliarResultadoDTO asociarGrupoFamiliar(Long idExpedientePrincipal, List<Long> idsExpedientesCandidatos) throws SQLException {
        return grupoFamiliarDAO.asociarGrupoFamiliar(idExpedientePrincipal, idsExpedientesCandidatos, CurrentUser.get().idUsuario());
    }

    public GrupoFamiliarResultadoDTO eliminarDeGrupoFamiliar(Long idExpediente) throws SQLException {
        return grupoFamiliarDAO.eliminarDeGrupoFamiliar(idExpediente, CurrentUser.get().idUsuario());
    }
}
