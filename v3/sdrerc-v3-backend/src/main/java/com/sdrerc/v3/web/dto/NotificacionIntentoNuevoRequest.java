package com.sdrerc.v3.web.dto;

import java.time.LocalDate;

/** "+ Agregar intento", fila borrador guardada (Bandeja Notificación, 3ra pestaña). */
public record NotificacionIntentoNuevoRequest(
        Long idExpediente,
        Long idDocumentoAnalizado,
        String tipoNotificacionCodigo,
        String codigoNotificacion,
        LocalDate fechaEnvio) {
}
