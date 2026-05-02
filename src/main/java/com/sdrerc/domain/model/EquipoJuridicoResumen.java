package com.sdrerc.domain.model;

public class EquipoJuridicoResumen {

    private int totalAbogados;
    private int totalSupervisores;
    private int abogadosSinSupervisor;
    private int activos;
    private int inactivos;

    public int getTotalAbogados() {
        return totalAbogados;
    }

    public void setTotalAbogados(int totalAbogados) {
        this.totalAbogados = totalAbogados;
    }

    public int getTotalSupervisores() {
        return totalSupervisores;
    }

    public void setTotalSupervisores(int totalSupervisores) {
        this.totalSupervisores = totalSupervisores;
    }

    public int getAbogadosSinSupervisor() {
        return abogadosSinSupervisor;
    }

    public void setAbogadosSinSupervisor(int abogadosSinSupervisor) {
        this.abogadosSinSupervisor = abogadosSinSupervisor;
    }

    public int getActivos() {
        return activos;
    }

    public void setActivos(int activos) {
        this.activos = activos;
    }

    public int getInactivos() {
        return inactivos;
    }

    public void setInactivos(int inactivos) {
        this.inactivos = inactivos;
    }
}
