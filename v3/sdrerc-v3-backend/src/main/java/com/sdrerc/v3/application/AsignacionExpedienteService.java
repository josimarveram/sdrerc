package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.AsignacionExpedienteDTO;
import com.sdrerc.v3.domain.AsignacionHistorialDTO;
import com.sdrerc.v3.domain.AsignacionResultadoDTO;
import com.sdrerc.v3.domain.EquipoAsignacionDTO;
import com.sdrerc.v3.domain.UsuarioAsignableDTO;
import com.sdrerc.v3.infrastructure.dao.AsignacionExpedienteDAO;
import com.sdrerc.v3.infrastructure.dao.EquipoAsignacionDAO;
import com.sdrerc.v3.infrastructure.dao.UsuarioAsignacionDAO;
import com.sdrerc.v3.security.jwt.CurrentUser;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Port acotado de com.sdrerc.application.sdrercapp.AsignacionExpedienteService (V2) +
 * com.sdrerc.application.sdrercapp.UsuarioAsignacionService (solo los métodos de catálogo que
 * necesita este incremento). Única diferencia real: {@code asignar()} resuelve el usuario/rol
 * actual desde {@link CurrentUser} (JWT del request) en vez de {@code SessionContext} estático
 * — mismo patrón ya aplicado en Fases 0-2.
 */
@Service
public class AsignacionExpedienteService {

    private final AsignacionExpedienteDAO asignacionExpedienteDAO;
    private final EquipoAsignacionDAO equipoAsignacionDAO;
    private final UsuarioAsignacionDAO usuarioAsignacionDAO;
    private final AsignacionValidacionService asignacionValidacionService;

    public AsignacionExpedienteService(
            AsignacionExpedienteDAO asignacionExpedienteDAO,
            EquipoAsignacionDAO equipoAsignacionDAO,
            UsuarioAsignacionDAO usuarioAsignacionDAO,
            AsignacionValidacionService asignacionValidacionService) {
        this.asignacionExpedienteDAO = asignacionExpedienteDAO;
        this.equipoAsignacionDAO = equipoAsignacionDAO;
        this.usuarioAsignacionDAO = usuarioAsignacionDAO;
        this.asignacionValidacionService = asignacionValidacionService;
    }

    public List<AsignacionExpedienteDTO> buscarExpedientes(
            String textoLibre,
            String estadoCodigo,
            LocalDate fechaSolicitudDesde,
            LocalDate fechaSolicitudHasta,
            int limite) throws SQLException {
        return asignacionExpedienteDAO.buscarExpedientes(textoLibre, estadoCodigo, fechaSolicitudDesde, fechaSolicitudHasta, limite);
    }

    public List<EquipoAsignacionDTO> listarEquiposActivos() throws SQLException {
        return equipoAsignacionDAO.listarEquiposActivos();
    }

    public List<UsuarioAsignableDTO> listarAbogadosAsignables(Long idEquipo) throws SQLException {
        return usuarioAsignacionDAO.listarAbogadosAsignables(idEquipo);
    }

    public AsignacionResultadoDTO asignar(
            List<Long> idsExpediente,
            EquipoAsignacionDTO equipo,
            UsuarioAsignableDTO abogado,
            String comentario,
            Map<Long, String> hojasEnvioPorExpediente) throws SQLException {
        List<String> errores = asignacionValidacionService.validarAsignacion(idsExpediente, equipo, abogado);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errores));
        }
        return asignacionExpedienteDAO.asignarExpedientes(
                idsExpediente,
                equipo.getIdEquipo(),
                abogado.getIdUsuario(),
                comentario,
                CurrentUser.get().idUsuario(),
                CurrentUser.get().hasRole("ADMIN_SISTEMA"),
                hojasEnvioPorExpediente);
    }

    public AsignacionResultadoDTO reasignar(
            Long idExpediente,
            EquipoAsignacionDTO equipo,
            UsuarioAsignableDTO abogado,
            String numeroHojaEnvio,
            String comentario) throws SQLException {
        List<String> errores = asignacionValidacionService.validarAsignacion(
                idExpediente == null ? List.of() : List.of(idExpediente), equipo, abogado);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errores));
        }
        return asignacionExpedienteDAO.reasignarExpediente(
                idExpediente,
                equipo.getIdEquipo(),
                abogado.getIdUsuario(),
                numeroHojaEnvio,
                comentario,
                CurrentUser.get().idUsuario(),
                CurrentUser.get().hasRole("ADMIN_SISTEMA"));
    }

    public List<AsignacionHistorialDTO> listarHistorialAsignaciones(Long idExpediente) throws SQLException {
        return asignacionExpedienteDAO.listarHistorialAsignaciones(idExpediente);
    }

    /** "Decisión de número" del panel "Asociar" (V2): genera número independiente para una solicitud sin número. */
    public String generarNumeroExpediente(Long idExpediente) throws SQLException {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente para generar número.");
        }
        return asignacionExpedienteDAO.generarNumeroExpediente(idExpediente, CurrentUser.get().idUsuario());
    }
}
