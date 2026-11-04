package com.sdrerc.v3.web.dto;

import java.time.LocalDate;

/** Guardar una fila de intento marcándola "Ubicado" — confirma recepción (Bandeja Notificación, 3ra pestaña). */
public record NotificacionIntentoConfirmarRequest(
        Long idExpediente,
        Long idExpedienteNotificacion,
        Long idDocumentoAnalizado,
        String codigoORecibidoPor,
        LocalDate fechaEnvio,
        LocalDate fechaRecepcion) {
}
