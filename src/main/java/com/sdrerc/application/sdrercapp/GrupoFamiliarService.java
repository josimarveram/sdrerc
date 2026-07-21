package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.GrupoFamiliarCandidatoDTO;
import com.sdrerc.domain.dto.sdrercapp.GrupoFamiliarEstadoAlertaDTO;
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

    public List<GrupoFamiliarCandidatoDTO> buscarPosiblesIntegrantesManual(Long idExpediente, String texto) throws SQLException {
        return grupoFamiliarDAO.buscarPosiblesIntegrantesManual(idExpediente, texto);
    }

    public List<GrupoFamiliarIntegranteDTO> listarIntegrantesGrupoFamiliar(Long idExpediente) throws SQLException {
        return grupoFamiliarDAO.listarIntegrantesGrupoFamiliar(idExpediente);
    }

    public GrupoFamiliarResultadoDTO asociarGrupoFamiliar(Long idExpedientePrincipal, List<Long> idsExpedientesCandidatos) throws SQLException {
        return grupoFamiliarDAO.asociarGrupoFamiliar(idExpedientePrincipal, idsExpedientesCandidatos, resolverUsuarioActualSdrercApp());
    }

    public GrupoFamiliarResultadoDTO eliminarDeGrupoFamiliar(Long idExpediente) throws SQLException {
        return grupoFamiliarDAO.eliminarDeGrupoFamiliar(idExpediente, resolverUsuarioActualSdrercApp());
    }

    public GrupoFamiliarEstadoAlertaDTO obtenerEstadoAlerta(Long idExpediente) throws SQLException {
        return grupoFamiliarDAO.obtenerEstadoAlerta(idExpediente);
    }

    public void eliminarAlertaPosibleGrupoFamiliar(Long idExpediente) throws SQLException {
        grupoFamiliarDAO.eliminarAlertaPosibleGrupoFamiliar(idExpediente, resolverUsuarioActualSdrercApp());
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
