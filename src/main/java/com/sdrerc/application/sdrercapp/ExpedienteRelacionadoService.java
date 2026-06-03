package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionadoDTO;
import com.sdrerc.domain.dto.sdrercapp.ExpedienteRelacionResultadoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.ExpedienteRelacionadoDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.List;

public class ExpedienteRelacionadoService {

    private final ExpedienteRelacionadoDAO expedienteRelacionadoDAO;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public ExpedienteRelacionadoService() {
        this(new ExpedienteRelacionadoDAO(), new UsuarioAsignacionService());
    }

    public ExpedienteRelacionadoService(
            ExpedienteRelacionadoDAO expedienteRelacionadoDAO,
            UsuarioAsignacionService usuarioAsignacionService) {
        this.expedienteRelacionadoDAO = expedienteRelacionadoDAO;
        this.usuarioAsignacionService = usuarioAsignacionService;
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
                resolverUsuarioActualSdrercApp(),
                descripcion);
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
