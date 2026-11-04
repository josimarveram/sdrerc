package com.sdrerc.v3.web.dto;

import java.time.LocalDate;

/** Guardar una fila de intento YA persistida, sin marcarla "Ubicado" (Bandeja Notificación, 3ra pestaña). */
public record NotificacionIntentoActualizarRequest(
        Long idExpedienteNotificacion,
        String tipoNotificacionCodigo,
        String estadoNotificacionCodigo,
        String codigoNotificacion,
        LocalDate fechaEnvio) {
}
