package com.sdrerc.v3.domain;

import java.time.LocalDate;

/** Port literal de com.sdrerc.domain.dto.sdrercapp.DashboardTendenciaMensualDTO (V2). */
public record DashboardTendenciaMensualDTO(LocalDate mes, int ingresados, int cerrados) {
}
