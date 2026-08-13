package com.sdrerc.v3.web;

import com.sdrerc.v3.application.AsignacionExpedienteService;
import com.sdrerc.v3.application.CargaLaboralAbogadoService;
import com.sdrerc.v3.application.CartaRespuestaService;
import com.sdrerc.v3.domain.AsignacionCartaRespuestaDTO;
import com.sdrerc.v3.domain.AsignacionExpedienteDTO;
import com.sdrerc.v3.domain.AsignacionHistorialDTO;
import com.sdrerc.v3.domain.AsignacionResultadoDTO;
import com.sdrerc.v3.domain.CargaLaboralAbogadoDTO;
import com.sdrerc.v3.domain.CargaLaboralDocumentoDTO;
import com.sdrerc.v3.domain.EquipoAsignacionDTO;
import com.sdrerc.v3.domain.UsuarioAsignableDTO;
import com.sdrerc.v3.web.dto.AsignarExpedientesRequest;
import com.sdrerc.v3.web.dto.ReasignarExpedienteRequest;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web de la pestaña "Bandeja Asignación" (JPanelAsignacionV2, lengueta principal) más
 * el bloque "Asignación de abogado" del panel derecho, y sus sub-pestañas "Cartas de respuesta"
 * (solo lectura) y "Carga Abogados" (solo lectura). Alcance hasta ahora (Fase 3, completo): listar
 * la bandeja, asignar/reasignar expedientes a un equipo/abogado, historial de
 * asignaciones/reasignaciones, Cartas de respuesta y Carga Abogados.
 *
 * <p>Esta bandeja está deliberadamente fuera del filtro de visibilidad por asignación
 * (VisibilidadBandejaSql en V2): es una pantalla de coordinación que debe ver toda la cola
 * entrante, igual que en V2 (ver CLAUDE.md, sección Permisos).</p>
 */
@RestController
@RequestMapping("/api/asignacion")
public class AsignacionController {

    private final AsignacionExpedienteService asignacionExpedienteService;
    private final CartaRespuestaService cartaRespuestaService;
    private final CargaLaboralAbogadoService cargaLaboralAbogadoService;

    public AsignacionController(
            AsignacionExpedienteService asignacionExpedienteService,
            CartaRespuestaService cartaRespuestaService,
            CargaLaboralAbogadoService cargaLaboralAbogadoService) {
        this.asignacionExpedienteService = asignacionExpedienteService;
        this.cartaRespuestaService = cartaRespuestaService;
        this.cargaLaboralAbogadoService = cargaLaboralAbogadoService;
    }

    @GetMapping("/bandeja")
    public List<AsignacionExpedienteDTO> buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false, defaultValue = "200") int limite) throws SQLException {
        return asignacionExpedienteService.buscarExpedientes(texto, estado, desde, hasta, limite);
    }

    @GetMapping("/equipos")
    public List<EquipoAsignacionDTO> listarEquipos() throws SQLException {
        return asignacionExpedienteService.listarEquiposActivos();
    }

    @GetMapping("/abogados")
    public List<UsuarioAsignableDTO> listarAbogados(@RequestParam Long idEquipo) throws SQLException {
        return asignacionExpedienteService.listarAbogadosAsignables(idEquipo);
    }

    @PostMapping("/asignar")
    public AsignacionResultadoDTO asignar(@RequestBody AsignarExpedientesRequest request) throws SQLException {
        EquipoAsignacionDTO equipo = asignacionExpedienteService.listarEquiposActivos().stream()
                .filter(e -> e.getIdEquipo().equals(request.idEquipo()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Seleccione el equipo destino."));
        UsuarioAsignableDTO abogado = asignacionExpedienteService.listarAbogadosAsignables(request.idEquipo()).stream()
                .filter(a -> a.getIdUsuario().equals(request.idAbogado()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Seleccione el abogado responsable."));
        return asignacionExpedienteService.asignar(
                request.idsExpediente(),
                equipo,
                abogado,
                request.comentario(),
                request.hojasEnvioPorExpediente());
    }

    @PostMapping("/reasignar")
    public AsignacionResultadoDTO reasignar(@RequestBody ReasignarExpedienteRequest request) throws SQLException {
        EquipoAsignacionDTO equipo = asignacionExpedienteService.listarEquiposActivos().stream()
                .filter(e -> e.getIdEquipo().equals(request.idEquipo()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Seleccione el equipo destino."));
        UsuarioAsignableDTO abogado = asignacionExpedienteService.listarAbogadosAsignables(request.idEquipo()).stream()
                .filter(a -> a.getIdUsuario().equals(request.idAbogado()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Seleccione el abogado responsable."));
        return asignacionExpedienteService.reasignar(
                request.idExpediente(),
                equipo,
                abogado,
                request.numeroHojaEnvio(),
                request.comentario());
    }

    @GetMapping("/{idExpediente}/historial")
    public List<AsignacionHistorialDTO> listarHistorial(@PathVariable Long idExpediente) throws SQLException {
        return asignacionExpedienteService.listarHistorialAsignaciones(idExpediente);
    }

    /** "Decisión de número" del panel "Asociar" (V2): genera número independiente para una solicitud sin número. */
    @PostMapping("/{idExpediente}/generar-numero")
    public GenerarNumeroResponse generarNumero(@PathVariable Long idExpediente) throws SQLException {
        return new GenerarNumeroResponse(asignacionExpedienteService.generarNumeroExpediente(idExpediente));
    }

    public record GenerarNumeroResponse(String numeroExpediente) {
    }

    @GetMapping("/cartas-respuesta")
    public List<AsignacionCartaRespuestaDTO> listarCartasRespuesta() throws SQLException {
        return cartaRespuestaService.listarCartasRespuestaPendientes();
    }

    @GetMapping("/carga-abogados")
    public List<CargaLaboralAbogadoDTO> listarCargaAbogados(@RequestParam(required = false) Long idEquipo) throws SQLException {
        return cargaLaboralAbogadoService.listarCargaLaboralAbogados(idEquipo);
    }

    @GetMapping("/carga-abogados/{idUsuario}/documentos")
    public List<CargaLaboralDocumentoDTO> listarDocumentosPorAbogado(@PathVariable Long idUsuario) throws SQLException {
        return cargaLaboralAbogadoService.listarDocumentosPorAbogado(idUsuario);
    }

    @GetMapping("/carga-abogados/vencimiento")
    public Set<Long> listarIdsUsuarioConVencimientoEnRango(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) throws SQLException {
        return cargaLaboralAbogadoService.listarIdsUsuarioConVencimientoEnRango(desde, hasta);
    }
}
