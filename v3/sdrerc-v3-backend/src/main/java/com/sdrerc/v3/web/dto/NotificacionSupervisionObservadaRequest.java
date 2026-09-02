package com.sdrerc.v3.web.dto;

/** "Registrar Supervisión" (resultado Observado) — deriva a Equipo Análisis o Equipo Ejecución. */
public record NotificacionSupervisionObservadaRequest(
        Long idExpediente, Long idDocumentoAnalizado, Long idEquipoDestino, Long idUsuarioDestino,
        String motivoCorreccionNombre, String comentario) {
}
