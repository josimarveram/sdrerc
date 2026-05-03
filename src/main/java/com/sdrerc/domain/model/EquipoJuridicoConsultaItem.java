package com.sdrerc.domain.model;

public class EquipoJuridicoConsultaItem {

    private Long abogadoId;
    private String abogadoUsername;
    private String abogadoNombre;
    private Long supervisorId;
    private String supervisorUsername;
    private String supervisorNombre;
    private String estado;
    private String roles;
    private String tipoPersonal;

    public Long getAbogadoId() {
        return abogadoId;
    }

    public void setAbogadoId(Long abogadoId) {
        this.abogadoId = abogadoId;
    }

    public String getAbogadoUsername() {
        return abogadoUsername;
    }

    public void setAbogadoUsername(String abogadoUsername) {
        this.abogadoUsername = abogadoUsername;
    }

    public String getAbogadoNombre() {
        return abogadoNombre;
    }

    public void setAbogadoNombre(String abogadoNombre) {
        this.abogadoNombre = abogadoNombre;
    }

    public Long getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(Long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getSupervisorUsername() {
        return supervisorUsername;
    }

    public void setSupervisorUsername(String supervisorUsername) {
        this.supervisorUsername = supervisorUsername;
    }

    public String getSupervisorNombre() {
        return supervisorNombre;
    }

    public void setSupervisorNombre(String supervisorNombre) {
        this.supervisorNombre = supervisorNombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getTipoPersonal() {
        return tipoPersonal;
    }

    public void setTipoPersonal(String tipoPersonal) {
        this.tipoPersonal = tipoPersonal;
    }
}
