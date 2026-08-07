package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class DashboardTendenciaMensualDTO {

    private final LocalDate mes;
    private final int ingresados;
    private final int cerrados;

    public DashboardTendenciaMensualDTO(LocalDate mes, int ingresados, int cerrados) {
        this.mes = mes;
        this.ingresados = ingresados;
        this.cerrados = cerrados;
    }

    public LocalDate getMes() {
        return mes;
    }

    public int getIngresados() {
        return ingresados;
    }

    public int getCerrados() {
        return cerrados;
    }
}
