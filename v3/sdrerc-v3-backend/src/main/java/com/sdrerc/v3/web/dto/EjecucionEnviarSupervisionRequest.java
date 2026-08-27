package com.sdrerc.v3.web.dto;

/**
 * "Enviar a Supervisión" — ejecuta únicamente DERIVACION_A_NOTIFICACION. Sin idEquipoDestino: V2
 * hardcodea el combo a un único equipo real ({@code EQ_SUPERVISION}, ver
 * {@code esEquipoDestinoEjecucionValido}) — V3 lo resuelve internamente en el Service.
 */
public record EjecucionEnviarSupervisionRequest(Long idExpediente, String comentario, Long idUsuarioDestino) {
}
