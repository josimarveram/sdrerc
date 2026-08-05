package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDateTime;

public class AbogadoSupervisadoDTO {

    private Long idUsuario;
    private String username;
    private String nombreCompleto;
    private String rolesResumen;
    private String equiposResumen;
    private boolean usuarioActivo;
    private boolean relacionActiva;
    private LocalDateTime asignadoEn;

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

    public String getEquiposResumen() {
        return equiposResumen;
    }

    public void setEquiposResumen(String equiposResumen) {
        this.equiposResumen = equiposResumen;
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

    public LocalDateTime getAsignadoEn() {
        return asignadoEn;
    }

    public void setAsignadoEn(LocalDateTime asignadoEn) {
        this.asignadoEn = asignadoEn;
    }
}
