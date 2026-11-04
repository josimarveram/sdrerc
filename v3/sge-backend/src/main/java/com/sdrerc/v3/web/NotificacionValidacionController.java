package com.sdrerc.v3.web;

import com.sdrerc.v3.application.NotificacionValidacionService;
import com.sdrerc.v3.domain.EquipoAsignacionDTO;
import com.sdrerc.v3.domain.NotificacionAsignacionDocumentoDTO;
import com.sdrerc.v3.domain.UsuarioAsignableDTO;
import com.sdrerc.v3.web.dto.NotificacionEnviarSupervisionRequest;
import com.sdrerc.v3.web.dto.NotificacionValidacionRequest;
import java.sql.SQLException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web de la Bandeja Validación de Notificación (2da de las 4 pestañas de
 * {@code JPanelNotificacionV2}, 21/08/2026) — ver Javadoc de {@code DocumentoAnalisisDAO}/
 * {@code NotificacionValidacionService} para el alcance exacto.
 */
@RestController
@RequestMapping("/api/notificacion/validacion")
public class NotificacionValidacionController {

    private final NotificacionValidacionService notificacionValidacionService;

    public NotificacionValidacionController(NotificacionValidacionService notificacionValidacionService) {
        this.notificacionValidacionService = notificacionValidacionService;
    }

    @GetMapping("/bandeja")
    public List<NotificacionAsignacionDocumentoDTO> listarBandeja() throws SQLException {
        return notificacionValidacionService.listarBandeja();
    }

    @GetMapping("/{idExpediente}/documentos")
    public List<NotificacionAsignacionDocumentoDTO> listarDocumentosPorExpediente(@PathVariable Long idExpediente) throws SQLException {
        return notificacionValidacionService.listarDocumentosPorExpediente(idExpediente);
    }

    /** "Registrar Validación" (Aprobado/Observado). */
    @PostMapping("/resultado")
    public void registrarResultadoValidacion(@RequestBody NotificacionValidacionRequest request) throws SQLException {
        notificacionValidacionService.registrarResultadoValidacion(
                request.idDocumentoAnalizado(), request.resultadoCodigo(), request.comentario());
    }

    /** Combo "Equipo destino" del bloque "Enviar a supervisión" — fijo a EQ_SUPERVISION. */
    @GetMapping("/equipo-supervision")
    public List<EquipoAsignacionDTO> listarEquipoSupervision() throws SQLException {
        return notificacionValidacionService.listarEquipoSupervision();
    }

    /** Combo "Usuario destino" del bloque "Enviar a supervisión". */
    @GetMapping("/usuarios-supervision")
    public List<UsuarioAsignableDTO> listarUsuariosSupervision(@RequestParam Long idEquipo) throws SQLException {
        return notificacionValidacionService.listarUsuariosSupervision(idEquipo);
    }

    /** "Enviar a supervisión" — bloque "Destino operativo" (22/08/2026, pedido explícito del usuario). */
    @PostMapping("/enviar-supervision")
    public void enviarASupervision(@RequestBody NotificacionEnviarSupervisionRequest request) throws SQLException {
        notificacionValidacionService.enviarASupervision(
                request.idDocumentoAnalizado(), request.idEquipoDestino(), request.idUsuarioDestino());
    }
}
