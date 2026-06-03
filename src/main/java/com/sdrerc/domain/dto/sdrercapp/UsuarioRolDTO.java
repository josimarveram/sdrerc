package com.sdrerc.domain.dto.sdrercapp;

public class UsuarioRolDTO {

    private Long idUsuario;
    private Long idRol;
    private String codigoRol;
    private String nombreRol;
    private boolean activo;

    public UsuarioRolDTO() {
    }

    public UsuarioRolDTO(Long idUsuario, Long idRol, String codigoRol, String nombreRol, boolean activo) {
        this.idUsuario = idUsuario;
        this.idRol = idRol;
        this.codigoRol = codigoRol;
        this.nombreRol = nombreRol;
        this.activo = activo;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
    }

    public String getCodigoRol() {
        return codigoRol;
    }

    public void setCodigoRol(String codigoRol) {
        this.codigoRol = codigoRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
