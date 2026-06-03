package com.sdrerc.domain.dto.sdrercapp;

public class AreaDTO {

    private Long idArea;
    private String codigo;
    private String nombre;
    private String descripcion;
    private boolean activo;

    public AreaDTO() {
        this.activo = true;
    }

    public AreaDTO(Long idArea, String codigo, String nombre, String descripcion, boolean activo) {
        this.idArea = idArea;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public Long getIdArea() {
        return idArea;
    }

    public void setIdArea(Long idArea) {
        this.idArea = idArea;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre == null || nombre.trim().isEmpty() ? codigo : nombre;
    }
}
