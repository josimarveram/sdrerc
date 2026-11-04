package com.sdrerc.v3.domain;

/**
 * Port literal de com.sdrerc.domain.dto.sdrercapp.DashboardConteoDTO (V2). Par etiqueta/total
 * generico, reutilizado por los graficos de distribucion del Dashboard (expedientes por etapa,
 * resultado de analisis, estado final de notificacion).
 */
public record DashboardConteoDTO(String etiqueta, int total) {
}
