package com.sdrerc.domain.dto.sdrercapp;

public class RolPermisoDTO {

    private Long idRol;
    private Long idPermiso;
    private boolean activo;

    public RolPermisoDTO() {
    }

    public RolPermisoDTO(Long idRol, Long idPermiso, boolean activo) {
        this.idRol = idRol;
        this.idPermiso = idPermiso;
        this.activo = activo;
    }

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
    }

    public Long getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(Long idPermiso) {
        this.idPermiso = idPermiso;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
