package com.sdrerc.v3.web.dto;

public record RecibirAsociadoRequest(Long idExpedientePrincipal, Long idExpedienteAsociado, String comentario) {
}
