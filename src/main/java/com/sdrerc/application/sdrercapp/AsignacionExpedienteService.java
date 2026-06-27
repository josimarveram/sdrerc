package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.AsignacionExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.AsignacionResultadoDTO;
import com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO;
import com.sdrerc.domain.dto.sdrercapp.EquipoAsignacionDTO;
import com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableDTO;
import com.sdrerc.infrastructure.sdrercapp.dao.AsignacionExpedienteDAO;
import com.sdrerc.shared.session.SessionContext;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    public List<AsignacionExpedienteDTO> buscarExpedientes(
            String textoLibre,
            String estadoCodigo,
            LocalDate fechaSolicitudDesde,
            LocalDate fechaSolicitudHasta,
            int limite) throws SQLException {
        return buscarExpedientes(
                textoLibre,
                estadoCodigo,
                fechaSolicitudDesde,
                fechaSolicitudHasta,
                limite,
                false);
    }

    public List<AsignacionExpedienteDTO> buscarExpedientes(
            String textoLibre,
            String estadoCodigo,
            LocalDate fechaSolicitudDesde,
            LocalDate fechaSolicitudHasta,
            int limite,
            boolean soloGrupoFamiliar) throws SQLException {
        return asignacionExpedienteDAO.buscarExpedientes(
                textoLibre,
                estadoCodigo,
                fechaSolicitudDesde,
                fechaSolicitudHasta,
                limite,
                soloGrupoFamiliar);
    }

    public List<CatalogoItemDTO> listarEstadosExpediente() throws SQLException {
        return asignacionExpedienteDAO.listarEstadosExpediente();
    }

    public AsignacionExpedienteDTO obtenerExpedientePorId(Long idExpediente) throws SQLException {
        return asignacionExpedienteDAO.obtenerExpedientePorId(idExpediente);
    }

    public AsignacionResultadoDTO asignar(
            List<Long> idsExpediente,
            EquipoAsignacionDTO equipo,
            UsuarioAsignableDTO abogado,
            String comentario) throws SQLException {
        return asignar(idsExpediente, equipo, abogado, comentario, Collections.<Long, String>emptyMap());
    }

    public AsignacionResultadoDTO asignar(
            List<Long> idsExpediente,
            EquipoAsignacionDTO equipo,
            UsuarioAsignableDTO abogado,
            String comentario,
            Map<Long, String> hojasEnvioPorExpediente) throws SQLException {
        List<String> errores = validacionService.validarAsignacion(idsExpediente, equipo, abogado);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errores));
        }
        return asignacionExpedienteDAO.asignarExpedientes(
                idsExpediente,
                equipo.getIdEquipo(),
                abogado.getIdUsuario(),
                comentario,
                resolverUsuarioActualSdrercApp(),
                hojasEnvioPorExpediente);
    }

    public String generarNumeroExpediente(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para generar número.");
        }
        return asignacionExpedienteDAO.generarNumeroExpediente(idExpediente, resolverUsuarioActualSdrercApp());
    }

    public void actualizarProcedimientoRegistral(
            Long idExpediente,
            String procedimientoDestino) throws SQLException {
        asignacionExpedienteDAO.actualizarProcedimientoRegistral(
                idExpediente,
                procedimientoDestino,
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
