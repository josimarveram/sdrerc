package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.AreaDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoJuridicoDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoJuridicoFiltroDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoJuridicoResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoMiembroDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableEquipoDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.AreaDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.EquipoJuridicoDAO;
import com.sdrerc.infrastructure.sdrercapp.dao.EquipoMiembroDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.util.List;

public class EquipoJuridicoService {

    private final EquipoJuridicoDAO equipoJuridicoDAO;
    private final EquipoMiembroDAO equipoMiembroDAO;
    private final AreaDAO areaDAO;
    private final UsuarioAsignacionService usuarioAsignacionService;
    private final EquipoJuridicoValidacionService validacionService;

    public EquipoJuridicoService() {
        this(
                new EquipoJuridicoDAO(),
                new EquipoMiembroDAO(),
                new AreaDAO(),
                new UsuarioAsignacionService(),
                new EquipoJuridicoValidacionService());
    }

    public EquipoJuridicoService(
            EquipoJuridicoDAO equipoJuridicoDAO,
            EquipoMiembroDAO equipoMiembroDAO,
            AreaDAO areaDAO,
            UsuarioAsignacionService usuarioAsignacionService,
            EquipoJuridicoValidacionService validacionService) {
        this.equipoJuridicoDAO = equipoJuridicoDAO;
        this.equipoMiembroDAO = equipoMiembroDAO;
        this.areaDAO = areaDAO;
        this.usuarioAsignacionService = usuarioAsignacionService;
        this.validacionService = validacionService;
    }

    public List<EquipoJuridicoDTO> buscar(EquipoJuridicoFiltroDTO filtro) throws SQLException {
        return equipoJuridicoDAO.buscar(filtro);
    }

    public EquipoJuridicoDTO obtenerPorId(Long idEquipo) throws SQLException {
        return equipoJuridicoDAO.obtenerPorId(idEquipo);
    }

    public List<AreaDTO> listarAreasActivas() throws SQLException {
        return areaDAO.listar(Boolean.TRUE);
    }

    public List<UsuarioAsignableEquipoDTO> listarUsuariosAsignables() throws SQLException {
        return equipoMiembroDAO.listarUsuariosAsignables(null, false);
    }

    public List<UsuarioAsignableEquipoDTO> listarSupervisoresAsignables() throws SQLException {
        return equipoMiembroDAO.listarUsuariosAsignables(null, true);
    }

    public List<EquipoMiembroDTO> listarMiembros(Long idEquipo) throws SQLException {
        return equipoMiembroDAO.listarMiembros(idEquipo);
    }

    public EquipoJuridicoResultadoDTO guardar(EquipoJuridicoDTO equipo, Long idResponsable) throws SQLException {
        validacionService.prepararEquipo(equipo);
        List<String> errores = validacionService.validarGuardar(equipo);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
        if (equipoJuridicoDAO.existeCodigo(equipo.getCodigo(), equipo.getIdEquipo())) {
            throw new IllegalArgumentException("Ya existe un equipo con el código indicado.");
        }
        boolean esNuevo = equipo.getIdEquipo() == null;
        if (!esNuevo) {
            EquipoJuridicoDTO actual = equipoJuridicoDAO.obtenerPorId(equipo.getIdEquipo());
            if (actual == null) {
                throw new IllegalArgumentException("El equipo seleccionado no existe.");
            }
            if (!equipo.isActivo()) {
                validarEquipoSinExpedientesActivos(equipo.getIdEquipo());
            }
        }
        if (idResponsable != null && !equipoMiembroDAO.usuarioSupervisorCompatible(idResponsable)) {
            throw new IllegalArgumentException("Seleccione un usuario con rol de supervisión para responsable del equipo.");
        }

        EquipoJuridicoDTO guardado = equipoJuridicoDAO.guardarConResponsable(
                equipo,
                idResponsable,
                resolverUsuarioActualSdrercApp());
        String mensaje = esNuevo
                ? "El equipo fue registrado correctamente."
                : "El equipo fue actualizado correctamente.";
        return EquipoJuridicoResultadoDTO.exito(mensaje, guardado);
    }

    public EquipoJuridicoResultadoDTO cambiarActivo(Long idEquipo, boolean activo) throws SQLException {
        if (idEquipo == null) {
            throw new IllegalArgumentException("Seleccione un equipo.");
        }
        EquipoJuridicoDTO actual = equipoJuridicoDAO.obtenerPorId(idEquipo);
        if (actual == null) {
            throw new IllegalArgumentException("El equipo seleccionado no existe.");
        }
        if (!activo) {
            validarEquipoSinExpedientesActivos(idEquipo);
        }
        EquipoJuridicoDTO actualizado = equipoJuridicoDAO.cambiarActivo(idEquipo, activo, resolverUsuarioActualSdrercApp());
        String mensaje = activo
                ? "El equipo fue activado correctamente."
                : "El equipo fue inactivado correctamente.";
        return EquipoJuridicoResultadoDTO.exito(mensaje, actualizado);
    }

    public void agregarMiembro(Long idEquipo, Long idUsuario) throws SQLException {
        validarEquipoExistente(idEquipo);
        if (idUsuario == null) {
            throw new IllegalArgumentException("Seleccione un usuario para agregar.");
        }
        equipoMiembroDAO.agregarMiembro(idEquipo, idUsuario, resolverUsuarioActualSdrercApp());
    }

    public void quitarMiembro(Long idEquipo, Long idUsuario) throws SQLException {
        validarEquipoExistente(idEquipo);
        if (idUsuario == null) {
            throw new IllegalArgumentException("Seleccione un miembro del equipo.");
        }
        int expedientesActivos = equipoMiembroDAO.contarExpedientesActivosMiembro(idEquipo, idUsuario);
        if (expedientesActivos > 0) {
            throw new IllegalArgumentException("No se puede retirar el miembro porque tiene expedientes activos.");
        }
        equipoMiembroDAO.quitarMiembro(idEquipo, idUsuario, resolverUsuarioActualSdrercApp());
    }

    public void marcarResponsable(Long idEquipo, Long idUsuario) throws SQLException {
        validarEquipoExistente(idEquipo);
        if (idUsuario == null) {
            throw new IllegalArgumentException("Seleccione un miembro del equipo.");
        }
        if (!equipoMiembroDAO.usuarioSupervisorCompatible(idUsuario)) {
            throw new IllegalArgumentException("Seleccione un usuario con rol de supervisión para responsable del equipo.");
        }
        equipoMiembroDAO.marcarResponsable(idEquipo, idUsuario, resolverUsuarioActualSdrercApp());
    }

    private void validarEquipoExistente(Long idEquipo) throws SQLException {
        if (idEquipo == null) {
            throw new IllegalArgumentException("Seleccione un equipo.");
        }
        if (equipoJuridicoDAO.obtenerPorId(idEquipo) == null) {
            throw new IllegalArgumentException("El equipo seleccionado no existe.");
        }
    }

    private void validarEquipoSinExpedientesActivos(Long idEquipo) throws SQLException {
        int expedientesActivos = equipoJuridicoDAO.contarExpedientesActivosEquipo(idEquipo);
        if (expedientesActivos > 0) {
            throw new IllegalArgumentException("No se puede inactivar el equipo porque tiene expedientes activos.");
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
