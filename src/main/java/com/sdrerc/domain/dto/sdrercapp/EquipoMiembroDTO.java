package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDateTime;

public class EquipoMiembroDTO {

    private Long idEquipoUsuario;
    private Long idEquipo;
    private Long idUsuario;
    private String username;
    private String nombreCompleto;
    private String rolesResumen;
    private String areaNombre;
    private boolean usuarioActivo;
    private boolean relacionActiva;
    private boolean responsable;
    private boolean abogado;
    private boolean supervisor;
    private LocalDateTime creadoEn;
    private LocalDateTime modificadoEn;

    public Long getIdEquipoUsuario() {
        return idEquipoUsuario;
    }

    public void setIdEquipoUsuario(Long idEquipoUsuario) {
        this.idEquipoUsuario = idEquipoUsuario;
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
    }

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

    public String getAreaNombre() {
        return areaNombre;
    }

    public void setAreaNombre(String areaNombre) {
        this.areaNombre = areaNombre;
    }

    public boolean isUsuarioActivo() {
        return usuarioActivo;
    }

    public void setUsuarioActivo(boolean usuarioActivo) {
        this.usuarioActivo = usuarioActivo;
    }

    public boolean isRelacionActiva() {
        return relacionActiva;
    }

    public void setRelacionActiva(boolean relacionActiva) {
        this.relacionActiva = relacionActiva;
    }

    public boolean isResponsable() {
        return responsable;
    }

    public void setResponsable(boolean responsable) {
        this.responsable = responsable;
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
