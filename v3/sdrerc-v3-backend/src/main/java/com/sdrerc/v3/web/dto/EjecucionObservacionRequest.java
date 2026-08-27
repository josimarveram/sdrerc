package com.sdrerc.v3.web.dto;

/**
 * Cuerpo compartido por "Registrar observación" (OBSERVACION_EJECUCION) y "Registrar error
 * material" (REVERSION_ESTADO_DOCUMENTO_EJECUCION) — mismo shape que usa V2 para ambas acciones.
 */
public record EjecucionObservacionRequest(
        Long idExpediente, String tipoObservacionCodigo, String motivoCorreccionCodigo, String descripcion) {
}
