package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDateTime;

public class EquipoJuridicoDTO {

    private Long idEquipo;
    private Long idArea;
    private String codigo;
    private String nombre;
    private String descripcion;
    private boolean activo;
    private String areaNombre;
    private Long idResponsable;
    private String responsableNombre;
    private int miembrosActivos;
    private int abogadosActivos;
    private LocalDateTime creadoEn;
    private LocalDateTime modificadoEn;

    public EquipoJuridicoDTO() {
        this.activo = true;
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
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

    public String getAreaNombre() {
        return areaNombre;
    }

    public void setAreaNombre(String areaNombre) {
        this.areaNombre = areaNombre;
    }

    public Long getIdResponsable() {
        return idResponsable;
    }

    public void setIdResponsable(Long idResponsable) {
        this.idResponsable = idResponsable;
    }

    public String getResponsableNombre() {
        return responsableNombre;
    }

    public void setResponsableNombre(String responsableNombre) {
        this.responsableNombre = responsableNombre;
    }

    public int getMiembrosActivos() {
        return miembrosActivos;
    }

    public void setMiembrosActivos(int miembrosActivos) {
        this.miembrosActivos = miembrosActivos;
    }

    public int getAbogadosActivos() {
        return abogadosActivos;
    }

    public void setAbogadosActivos(int abogadosActivos) {
        this.abogadosActivos = abogadosActivos;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getModificadoEn() {
        return modificadoEn;
    }

    public void setModificadoEn(LocalDateTime modificadoEn) {
        this.modificadoEn = modificadoEn;
    }
}
