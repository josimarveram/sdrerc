package com.sdrerc.v3.domain;

/**
 * Port literal de {@code com.sdrerc.domain.dto.sdrercapp.UsuarioAsignableEquipoDTO} (V2): usuario
 * candidato a miembro/responsable de un equipo, o a supervisor. Distinto de
 * {@link UsuarioAsignableDTO} (usado por Asignación de expedientes, otro shape/propósito).
 */
public class UsuarioAsignableEquipoDTO {

    private Long idUsuario;
    private String username;
    private String nombreCompleto;
    private String rolesResumen;
    private boolean activo;
    private boolean abogado;
    private boolean supervisor;

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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
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
}
