package com.sdrerc.v3.web;

import com.sdrerc.v3.application.NotificacionAsignacionService;
import com.sdrerc.v3.domain.AsignacionHistorialDTO;
import com.sdrerc.v3.domain.CatalogoItemDTO;
import com.sdrerc.v3.domain.EquipoAsignacionDTO;
import com.sdrerc.v3.domain.NotificacionAsignacionDocumentoDTO;
import com.sdrerc.v3.domain.UsuarioAsignableDTO;
import com.sdrerc.v3.web.dto.NotificacionAsignacionGenerarRequest;
import com.sdrerc.v3.web.dto.NotificacionFirmaRequest;
import com.sdrerc.v3.web.dto.NotificacionSupervisionObservadaRequest;
import com.sdrerc.v3.web.dto.NotificacionSupervisionRequest;
import java.sql.SQLException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web de la Bandeja Asignación de Notificación (1ra de las 4 pestañas de
 * {@code JPanelNotificacionV2}, 21/08/2026, primera fase del módulo) — ver Javadoc de
 * {@code DocumentoAnalisisDAO}/{@code NotificacionAsignacionService} para el alcance exacto.
 */
@RestController
@RequestMapping("/api/notificacion/asignacion")
public class NotificacionAsignacionController {

    private final NotificacionAsignacionService notificacionAsignacionService;

    public NotificacionAsignacionController(NotificacionAsignacionService notificacionAsignacionService) {
        this.notificacionAsignacionService = notificacionAsignacionService;
    }

    @GetMapping("/bandeja")
    public List<NotificacionAsignacionDocumentoDTO> listarBandeja() throws SQLException {
        return notificacionAsignacionService.listarBandeja();
    }

    @GetMapping("/{idExpediente}/documentos")
    public List<NotificacionAsignacionDocumentoDTO> listarDocumentosPorExpediente(@PathVariable Long idExpediente) throws SQLException {
        return notificacionAsignacionService.listarDocumentosPorExpediente(idExpediente);
    }

    /** Ícono "Guardar" de "Documentos a firmar" — mini-panel "① Emisión". */
    @PostMapping("/firma")
    public void guardarFirma(@RequestBody NotificacionFirmaRequest request) throws SQLException {
        notificacionAsignacionService.guardarFirma(
                request.idDocumentoAnalizado(), request.numeroDocumento(), request.fechaEmision(), request.estadoDocumentoCodigo());
    }

    /** "Destino operativo" — mini-panel "② Asignación". */
    @PostMapping("/generar")
    public void generarAsignacion(@RequestBody NotificacionAsignacionGenerarRequest request) throws SQLException {
        notificacionAsignacionService.generarAsignacion(
                request.idsDocumentoAnalizado(), request.idEquipoDestino(), request.idUsuarioDestino(),
                request.hojasEnvioPorDocumento(), request.comentario(), request.reasignacion());
    }

    /** "Registrar Supervisión" (resultado Aprobado). */
    @PostMapping("/supervision")
    public void registrarSupervision(@RequestBody NotificacionSupervisionRequest request) throws SQLException {
        notificacionAsignacionService.registrarSupervisionAprobada(request.idExpediente(), request.idDocumentoAnalizado());
    }

    /** "Registrar Supervisión" (resultado Observado) — deriva a Equipo Análisis o Equipo Ejecución. */
    @PostMapping("/supervision-observada")
    public void registrarSupervisionObservada(@RequestBody NotificacionSupervisionObservadaRequest request) throws SQLException {
        notificacionAsignacionService.registrarSupervisionObservada(
                request.idExpediente(), request.idDocumentoAnalizado(), request.idEquipoDestino(), request.idUsuarioDestino(),
                request.motivoCorreccionNombre(), request.comentario());
    }

    /** Combo "Equipo destino" exclusivo del resultado Observado (Análisis/Ejecución). */
    @GetMapping("/equipos-destino-observado")
    public List<EquipoAsignacionDTO> listarEquiposDestinoObservado() throws SQLException {
        return notificacionAsignacionService.listarEquiposDestinoObservado();
    }

    /** Combo "Motivo de corrección" del resultado Observado. */
    @GetMapping("/catalogos/motivos-correccion")
    public List<CatalogoItemDTO> listarMotivosCorreccion() throws SQLException {
        return notificacionAsignacionService.listarMotivosCorreccion();
    }

    @GetMapping("/{idDocumentoAnalizado}/supervision")
    public boolean existeSupervisionAprobada(@PathVariable Long idDocumentoAnalizado) throws SQLException {
        return notificacionAsignacionService.existeSupervisionAprobada(idDocumentoAnalizado);
    }

    @GetMapping("/{idDocumentoAnalizado}/historial")
    public List<AsignacionHistorialDTO> listarHistorial(@PathVariable Long idDocumentoAnalizado) throws SQLException {
        return notificacionAsignacionService.listarHistorial(idDocumentoAnalizado);
    }

    @GetMapping("/equipos")
    public List<EquipoAsignacionDTO> listarEquiposDestino() throws SQLException {
        return notificacionAsignacionService.listarEquiposDestino();
    }

    @GetMapping("/usuarios")
    public List<UsuarioAsignableDTO> listarUsuariosDestino(@RequestParam Long idEquipo) throws SQLException {
        return notificacionAsignacionService.listarUsuariosDestino(idEquipo);
    }
}
