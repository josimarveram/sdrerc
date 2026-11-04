package com.sdrerc.v3.web.dto;

public record ReasignarExpedienteRequest(
        Long idExpediente,
        Long idEquipo,
        Long idAbogado,
        String numeroHojaEnvio,
        String comentario) {
}
