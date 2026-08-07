package com.sdrerc.domain.dto.sdrercapp;

public class DashboardResumenDTO {

    private final int activos;
    private final int vencidos;
    private final int porVencer;
    private final int ingresadosPeriodo;
    private final int cerradosPeriodo;

    public DashboardResumenDTO(int activos, int vencidos, int porVencer, int ingresadosPeriodo, int cerradosPeriodo) {
        this.activos = activos;
        this.vencidos = vencidos;
        this.porVencer = porVencer;
        this.ingresadosPeriodo = ingresadosPeriodo;
        this.cerradosPeriodo = cerradosPeriodo;
    }

    public int getActivos() {
        return activos;
    }

    public int getVencidos() {
        return vencidos;
    }

    public int getPorVencer() {
        return porVencer;
    }

    public int getIngresadosPeriodo() {
        return ingresadosPeriodo;
    }

    public int getCerradosPeriodo() {
        return cerradosPeriodo;
    }
}
