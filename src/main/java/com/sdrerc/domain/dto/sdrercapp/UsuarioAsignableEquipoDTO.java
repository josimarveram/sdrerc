package com.sdrerc.domain.dto.sdrercapp;

public class UsuarioAsignableEquipoDTO {

    private Long idUsuario;
    private String username;
    private String nombreCompleto;
    private String rolesResumen;
    private boolean abogado;
    private boolean supervisor;
    private boolean activo;

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getRolesResumen() {
        return rolesResumen;
    }

    public void setRolesResumen(String rolesResumen) {
        this.rolesResumen = rolesResumen;
    }

    public boolean isAbogado() {
        return abogado;
    }

    public void setAbogado(boolean abogado) {
        this.abogado = abogado;
    }

    public boolean isSupervisor() {
        return supervisor;
    }

    public void setSupervisor(boolean supervisor) {
        this.supervisor = supervisor;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            return username == null ? "" : username;
        }
        return nombreCompleto + (username == null || username.trim().isEmpty() ? "" : " (" + username + ")");
    }
}
