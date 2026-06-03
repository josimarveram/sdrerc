package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.AsignacionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.AsignacionExpedienteDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.List;

public class AsignacionExpedienteService {

    private final AsignacionExpedienteDAO asignacionExpedienteDAO;
    private final AsignacionValidacionService validacionService;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public AsignacionExpedienteService() {
        this(new AsignacionExpedienteDAO(), new AsignacionValidacionService(), new UsuarioAsignacionService());
    }

    public AsignacionExpedienteService(
            AsignacionExpedienteDAO asignacionExpedienteDAO,
            AsignacionValidacionService validacionService,
            UsuarioAsignacionService usuarioAsignacionService) {
        this.asignacionExpedienteDAO = asignacionExpedienteDAO;
        this.validacionService = validacionService;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public List<AsignacionExpedienteDTO> buscarPendientes(String textoLibre, int limite) throws SQLException {
        return asignacionExpedienteDAO.buscarPendientes(textoLibre, limite);
    }

    public AsignacionResultadoDTO asignar(
            List<Long> idsExpediente,
            EquipoAsignacionDTO equipo,
            UsuarioAsignableDTO abogado,
            String comentario) throws SQLException {
        List<String> errores = validacionService.validarAsignacion(idsExpediente, equipo, abogado);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
        return asignacionExpedienteDAO.asignarExpedientes(
                idsExpediente,
                equipo.getIdEquipo(),
                abogado.getIdUsuario(),
                comentario,
                resolverUsuarioActualSdrercApp());
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
