package com.sdrerc.domain.dto.sdrercapp;

public class UsuarioFiltroDTO {

    private String texto;
    private Boolean activo;
    private Long idRol;
    private Long idEquipo;
    private int limite = 300;

    public UsuarioFiltroDTO() {
    }

    public UsuarioFiltroDTO(String texto, Boolean activo, Long idRol, Long idEquipo, int limite) {
        this.texto = texto;
        this.activo = activo;
        this.idRol = idRol;
        this.idEquipo = idEquipo;
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

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
    }

    public int getLimite() {
        return limite;
    }

    public void setLimite(int limite) {
        this.limite = limite;
    }
}
