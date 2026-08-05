package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO;
import com.sdrerc.domain.dto.sdrercapp.CargaLaboralAbogadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CargaLaboralDocumentoDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.EquipoAsignacionDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.UsuarioAsignacionDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class UsuarioAsignacionService {

    private final EquipoAsignacionDAO equipoAsignacionDAO;
    private final UsuarioAsignacionDAO usuarioAsignacionDAO;

    public UsuarioAsignacionService() {
        this(new EquipoAsignacionDAO(), new UsuarioAsignacionDAO());
    }

    public UsuarioAsignacionService(EquipoAsignacionDAO equipoAsignacionDAO, UsuarioAsignacionDAO usuarioAsignacionDAO) {
        this.equipoAsignacionDAO = equipoAsignacionDAO;
        this.usuarioAsignacionDAO = usuarioAsignacionDAO;
    }

    public List<EquipoAsignacionDTO> listarEquiposActivos() throws SQLException {
        return equipoAsignacionDAO.listarEquiposActivos();
    }

    public List<UsuarioAsignableDTO> listarAbogadosAsignables(Long idEquipo) throws SQLException {
        return usuarioAsignacionDAO.listarAbogadosAsignables(idEquipo);
    }

    public List<UsuarioAsignableDTO> listarUsuariosAsignablesPorEquipo(Long idEquipo) throws SQLException {
        return usuarioAsignacionDAO.listarUsuariosAsignablesPorEquipo(idEquipo);
    }

    public List<CargaLaboralAbogadoDTO> listarCargaLaboralAbogados(Long idEquipo) throws SQLException {
        return usuarioAsignacionDAO.listarCargaLaboralAbogados(idEquipo);
    }

    public List<CargaLaboralDocumentoDTO> listarDocumentosPorAbogado(Long idUsuario) throws SQLException {
        return usuarioAsignacionDAO.listarDocumentosPorAbogado(idUsuario);
    }

    public Set<Long> listarIdsUsuarioConVencimientoEnRango(LocalDate desde, LocalDate hasta) throws SQLException {
        return usuarioAsignacionDAO.listarIdsUsuarioConVencimientoEnRango(desde, hasta);
    }

    public Long obtenerIdUsuarioActivoPorUsername(String username) throws SQLException {
        return usuarioAsignacionDAO.obtenerIdUsuarioActivoPorUsername(username);
    }

    /**
     * Resuelve el ID de USUARIO (SDRERC_APP) del usuario de sesion actual, buscando por
     * username; retorna null si no hay sesion o no se pudo resolver, para no romper
     * flujos existentes que ya toleran un actor nulo.
     */
    public Long resolverUsuarioActualSdrercApp() {
        try {
            return obtenerIdUsuarioActivoPorUsername(SessionContext.getUsername());
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Indica si la sesion actual tiene el rol ADMIN_SISTEMA. Se usa para el bypass de
     * visibilidad de bandejas (el admin ve todo) y para sustituir el autor real por el
     * usuario asignado/responsable en EXPEDIENTE_HISTORIAL cuando quien ejecuta la accion
     * es un administrador.
     */
    public boolean esAdminSistemaActual() {
        try {
            return SessionContext.hasRole("ADMIN_SISTEMA");
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * IDs de equipo activos del usuario indicado (EQUIPO_USUARIO), usados para el filtro
     * de visibilidad "veo lo asignado a mi equipo" en las bandejas operativas.
     */
    public List<Long> listarIdsEquipoDeUsuario(Long idUsuario) throws SQLException {
        if (idUsuario == null) {
            return Collections.emptyList();
        }
        return usuarioAsignacionDAO.listarIdsEquipoDeUsuario(idUsuario);
    }
}
