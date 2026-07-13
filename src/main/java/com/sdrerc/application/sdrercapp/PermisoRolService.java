package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.PermisoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.PermisoDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.RolPermisoDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class PermisoRolService {

    private final PermisoDAO permisoDAO;
    private final RolPermisoDAO rolPermisoDAO;
    private final UsuarioAsignacionService usuarioAsignacionService;

    public PermisoRolService() {
        this(new PermisoDAO(), new RolPermisoDAO(), new UsuarioAsignacionService());
    }

    public PermisoRolService(
            PermisoDAO permisoDAO,
            RolPermisoDAO rolPermisoDAO,
            UsuarioAsignacionService usuarioAsignacionService) {
        this.permisoDAO = permisoDAO;
        this.rolPermisoDAO = rolPermisoDAO;
        this.usuarioAsignacionService = usuarioAsignacionService;
    }

    public List<PermisoDTO> listarPermisosPorRol(Long idRol) throws SQLException {
        return permisoDAO.listarPermisosPorRol(idRol);
    }

    public void guardarPermisos(Long idRol, List<Long> idsPermisoSeleccionados) throws SQLException {
        rolPermisoDAO.sincronizarPermisos(idRol, idsPermisoSeleccionados, resolverUsuarioActualSdrercApp());
    }

    /**
     * Códigos de permiso del usuario de sesión actual. Nunca lanza excepción: si no hay sesión,
     * el usuario no existe en SDRERC_APP, o falla la consulta, retorna un conjunto vacío
     * (interpretado como "sin catálogo resuelto" por SessionContext.tienePermiso).
     */
    public Set<String> listarCodigosPermisoUsuarioActual() {
        try {
            Long idUsuario = resolverUsuarioActualSdrercApp();
            if (idUsuario == null) {
                return Collections.emptySet();
            }
            return permisoDAO.listarCodigosPermisoPorUsuario(idUsuario);
        } catch (Exception ex) {
            return Collections.emptySet();
        }
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
