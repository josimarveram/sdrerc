package com.sdrerc.v3.domain;

import java.time.LocalDateTime;

/** Port literal de {@code com.sdrerc.domain.dto.sdrercapp.RolDTO} (V2), Administración &gt; Roles. */
public class RolAdminDTO {

    private Long idRol;
    private String codigo;
    private String nombre;
    private String descripcion;
    private boolean activo;
    private int usuariosAsociados;
    private int permisosAsociados;
    private LocalDateTime creadoEn;
    private LocalDateTime modificadoEn;

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
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

    public int getUsuariosAsociados() {
        return usuariosAsociados;
    }

    public void setUsuariosAsociados(int usuariosAsociados) {
        this.usuariosAsociados = usuariosAsociados;
    }

    public int getPermisosAsociados() {
        return permisosAsociados;
    }

    public void setPermisosAsociados(int permisosAsociados) {
        this.permisosAsociados = permisosAsociados;
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
