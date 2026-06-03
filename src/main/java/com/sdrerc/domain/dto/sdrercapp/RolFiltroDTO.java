package com.sdrerc.domain.dto.sdrercapp;

public class RolFiltroDTO {

    private String texto;
    private Boolean activo;
    private int limite = 300;

    public RolFiltroDTO() {
    }

    public RolFiltroDTO(String texto, Boolean activo, int limite) {
        this.texto = texto;
        this.activo = activo;
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

    public int getLimite() {
        return limite;
    }

    public void setLimite(int limite) {
        this.limite = limite;
    }
}
