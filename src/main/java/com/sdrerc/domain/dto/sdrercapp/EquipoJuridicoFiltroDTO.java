package com.sdrerc.domain.dto.sdrercapp;

public class EquipoJuridicoFiltroDTO {

    private String texto;
    private Boolean activo;
    private Long idArea;
    private Long idSupervisor;
    private int limite;

    public EquipoJuridicoFiltroDTO() {
        this.limite = 300;
    }

    public EquipoJuridicoFiltroDTO(String texto, Boolean activo, Long idArea, Long idSupervisor, int limite) {
        this.texto = texto;
        this.activo = activo;
        this.idArea = idArea;
        this.idSupervisor = idSupervisor;
        this.limite = limite;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Long getIdArea() {
        return idArea;
    }

    public void setIdArea(Long idArea) {
        this.idArea = idArea;
    }

    public Long getIdSupervisor() {
        return idSupervisor;
    }

    public void setIdSupervisor(Long idSupervisor) {
        this.idSupervisor = idSupervisor;
    }

    public int getLimite() {
        return limite;
    }

    public void setLimite(int limite) {
        this.limite = limite;
    }
}
