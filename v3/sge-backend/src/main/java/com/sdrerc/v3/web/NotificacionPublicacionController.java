package com.sdrerc.v3.web;

import com.sdrerc.v3.application.NotificacionPublicacionService;
import com.sdrerc.v3.domain.NotificacionAsignacionDocumentoDTO;
import com.sdrerc.v3.domain.NotificacionIntentoDTO;
import com.sdrerc.v3.web.dto.NotificacionPublicacionActualizarRequest;
import com.sdrerc.v3.web.dto.NotificacionPublicacionNuevaRequest;
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
 * Equivalente web de la Bandeja Publicación de Notificación (4ta de las 4 pestañas de
 * {@code JPanelNotificacionV2}, 24/08/2026) — ver Javadoc de {@code DocumentoAnalisisDAO}/
 * {@code NotificacionPublicacionService} para el alcance exacto.
 */
@RestController
@RequestMapping("/api/notificacion/publicacion")
public class NotificacionPublicacionController {

    private final NotificacionPublicacionService notificacionPublicacionService;

    public NotificacionPublicacionController(NotificacionPublicacionService notificacionPublicacionService) {
        this.notificacionPublicacionService = notificacionPublicacionService;
    }

    @GetMapping("/bandeja")
    public List<NotificacionAsignacionDocumentoDTO> listarBandeja() throws SQLException {
        return notificacionPublicacionService.listarBandeja();
    }

    /** Intentos 1/2 (solo lectura) + Publicación (editable) de un documento. */
    @GetMapping("/{idDocumentoAnalizado}/intentos")
    public List<NotificacionIntentoDTO> listarIntentos(@PathVariable Long idDocumentoAnalizado) throws SQLException {
        return notificacionPublicacionService.listarIntentos(idDocumentoAnalizado);
    }

    /** "+ Agregar publicación", fila borrador guardada. */
    @PostMapping("/publicacion")
    public void agregarPublicacion(@RequestBody NotificacionPublicacionNuevaRequest request) throws SQLException {
        notificacionPublicacionService.agregarPublicacion(request.idExpediente(), request.idDocumentoAnalizado(), request.fechaPublicacion());
    }

    /** Guardar la fila de Publicación ya persistida. */
    @PutMapping("/publicacion")
    public void actualizarPublicacion(@RequestBody NotificacionPublicacionActualizarRequest request) throws SQLException {
        notificacionPublicacionService.actualizarPublicacion(
                request.idExpedienteNotificacion(), request.estadoNotificacionCodigo(), request.fechaPublicacion());
    }

    /** Baja lógica de la fila de Publicación. */
    @DeleteMapping("/publicacion/{idExpedienteNotificacion}")
    public void eliminarPublicacion(@PathVariable Long idExpedienteNotificacion) throws SQLException {
        notificacionPublicacionService.eliminarPublicacion(idExpedienteNotificacion);
    }
}
