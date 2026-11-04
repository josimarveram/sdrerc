package com.sdrerc.v3.web.dto;

/** "Registrar Validación" (Bandeja Validación de Notificación) — resultadoCodigo: APROBADO/OBSERVADO. */
public record NotificacionValidacionRequest(Long idDocumentoAnalizado, String resultadoCodigo, String comentario) {
}
