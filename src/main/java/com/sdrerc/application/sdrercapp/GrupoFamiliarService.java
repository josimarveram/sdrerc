package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.GrupoFamiliarCandidatoDTO;
import com.sdrerc.domain.dto.sdrercapp.GrupoFamiliarIntegranteDTO;
import com.sdrerc.domain.dto.sdrercapp.GrupoFamiliarResultadoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.GrupoFamiliarDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.List;

public class GrupoFamiliarService {

    private final GrupoFamiliarDAO grupoFamiliarDAO;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public GrupoFamiliarService() {
        this(new GrupoFamiliarDAO(), new UsuarioAsignacionService());
    }

    public GrupoFamiliarService(GrupoFamiliarDAO grupoFamiliarDAO, UsuarioAsignacionService usuarioAsignacionService) {
        this.grupoFamiliarDAO = grupoFamiliarDAO;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public List<GrupoFamiliarCandidatoDTO> listarPosiblesIntegrantes(Long idExpediente) throws SQLException {
        return grupoFamiliarDAO.listarPosiblesIntegrantes(idExpediente);
    }

    public List<GrupoFamiliarIntegranteDTO> listarIntegrantesGrupoFamiliar(Long idExpediente) throws SQLException {
        return grupoFamiliarDAO.listarIntegrantesGrupoFamiliar(idExpediente);
    }

    public GrupoFamiliarResultadoDTO asociarGrupoFamiliar(Long idExpedientePrincipal, List<Long> idsExpedientesCandidatos) throws SQLException {
        return grupoFamiliarDAO.asociarGrupoFamiliar(idExpedientePrincipal, idsExpedientesCandidatos, resolverUsuarioActualSdrercApp());
    }

    private Long resolverUsuarioActualSdrercApp() {
        try {
            String username = SessionContext.getUsername();
            return usuarioAsignacionService.obtenerIdUsuarioActivoPorUsername(username);
        } catch (Exception ex) {
            return null;
        }
    }
}
