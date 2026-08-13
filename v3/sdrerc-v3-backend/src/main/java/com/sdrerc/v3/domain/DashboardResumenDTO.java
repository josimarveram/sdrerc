package com.sdrerc.v3.domain;

/** Port literal de com.sdrerc.domain.dto.sdrercapp.DashboardResumenDTO (V2). */
public record DashboardResumenDTO(
        int activos,
        int vencidos,
        int porVencer,
        int ingresadosPeriodo,
        int cerradosPeriodo) {
}
