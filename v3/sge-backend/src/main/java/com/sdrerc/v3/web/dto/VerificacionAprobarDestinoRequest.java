package com.sdrerc.v3.web.dto;

public record VerificacionAprobarDestinoRequest(Long idExpediente, String comentario, Long idEquipoDestino, Long idUsuarioDestino) {
}
