package com.sdrerc.v3.web;

import com.sdrerc.v3.application.NotificacionNotificacionService;
import com.sdrerc.v3.domain.NotificacionAsignacionDocumentoDTO;
import com.sdrerc.v3.domain.NotificacionIntentoDTO;
import com.sdrerc.v3.web.dto.NotificacionIntentoActualizarRequest;
import com.sdrerc.v3.web.dto.NotificacionIntentoConfirmarRequest;
import com.sdrerc.v3.web.dto.NotificacionIntentoNuevoRequest;
import java.sql.SQLException;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente web de la Bandeja Notificación de Notificación (3ra de las 4 pestañas de
 * {@code JPanelNotificacionV2}, 22/08/2026) — ver Javadoc de {@code DocumentoAnalisisDAO}/
 * {@code NotificacionNotificacionService} para el alcance exacto.
 */
@RestController
@RequestMapping("/api/notificacion/notificacion")
public class NotificacionNotificacionController {

    private final NotificacionNotificacionService notificacionNotificacionService;

    public NotificacionNotificacionController(NotificacionNotificacionService notificacionNotificacionService) {
        this.notificacionNotificacionService = notificacionNotificacionService;
    }

    @GetMapping("/bandeja")
    public List<NotificacionAsignacionDocumentoDTO> listarBandeja() throws SQLException {
        return notificacionNotificacionService.listarBandeja();
    }

    /** Intentos directos (1/2) de un documento — expansión "+" de una fila padre. */
    @GetMapping("/{idDocumentoAnalizado}/intentos")
    public List<NotificacionIntentoDTO> listarIntentos(@PathVariable Long idDocumentoAnalizado) throws SQLException {
        return notificacionNotificacionService.listarIntentos(idDocumentoAnalizado);
    }

    /** "+ Agregar intento", fila borrador guardada. */
    @PostMapping("/intentos")
    public void agregarIntento(@RequestBody NotificacionIntentoNuevoRequest request) throws SQLException {
        notificacionNotificacionService.agregarIntento(
                request.idExpediente(), request.idDocumentoAnalizado(), request.tipoNotificacionCodigo(),
                request.codigoNotificacion(), request.fechaEnvio());
    }

    /** Guardar una fila de intento existente sin marcarla "Ubicado". */
    @PutMapping("/intentos")
    public void actualizarIntento(@RequestBody NotificacionIntentoActualizarRequest request) throws SQLException {
        notificacionNotificacionService.actualizarIntento(
                request.idExpedienteNotificacion(), request.tipoNotificacionCodigo(), request.estadoNotificacionCodigo(),
                request.codigoNotificacion(), request.fechaEnvio());
    }

    /** Guardar una fila de intento marcándola "Ubicado" — confirma recepción (Fecha Acuse). */
    @PostMapping("/intentos/confirmar-recepcion")
    public void confirmarRecepcion(@RequestBody NotificacionIntentoConfirmarRequest request) throws SQLException {
        notificacionNotificacionService.confirmarRecepcion(
                request.idExpediente(), request.idExpedienteNotificacion(), request.idDocumentoAnalizado(),
                request.codigoORecibidoPor(), request.fechaEnvio(), request.fechaRecepcion());
    }

    /** Baja lógica de un intento ya registrado. */
    @DeleteMapping("/intentos/{idExpedienteNotificacion}")
    public void eliminarIntento(@PathVariable Long idExpedienteNotificacion) throws SQLException {
        notificacionNotificacionService.eliminarIntento(idExpedienteNotificacion);
    }
}
