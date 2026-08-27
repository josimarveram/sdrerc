package com.sdrerc.v3.web.dto;

/** "Enviar a supervisión" (Bandeja Validación de Notificación, bloque "Destino operativo"). */
public record NotificacionEnviarSupervisionRequest(Long idDocumentoAnalizado, Long idEquipoDestino, Long idUsuarioDestino) {
}
