package com.sdrerc.v3.web.dto;

/** "Devolver a Análisis" (DEVOLUCION_A_ANALISIS) — exige que el expediente esté en estado corregible. */
public record EjecucionReversionRequest(
        Long idExpediente, String tipoObservacionCodigo, String motivoCorreccionCodigo, String motivoReversion, String comentario) {
}
