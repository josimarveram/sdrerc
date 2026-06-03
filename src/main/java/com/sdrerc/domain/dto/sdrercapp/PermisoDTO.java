package com.sdrerc.domain.dto.sdrercapp;

public class PermisoDTO {

    private Long idPermiso;
    private String codigo;
    private String nombre;
    private String modulo;
    private boolean activo;
    private boolean asignado;

    public PermisoDTO() {
    }

    public PermisoDTO(Long idPermiso, String codigo, String nombre, String modulo, boolean activo, boolean asignado) {
        this.idPermiso = idPermiso;
        this.codigo = codigo;
        this.nombre = nombre;
        this.modulo = modulo;
        this.activo = activo;
        this.asignado = asignado;
    }

    public Long getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(Long idPermiso) {
        this.idPermiso = idPermiso;
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

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isAsignado() {
        return asignado;
    }

    public void setAsignado(boolean asignado) {
        this.asignado = asignado;
    }
}
