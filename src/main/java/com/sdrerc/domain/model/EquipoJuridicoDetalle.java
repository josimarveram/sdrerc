package com.sdrerc.domain.model;

public class EquipoJuridicoDetalle {

    private Long abogadoId;
    private Integer idTecnico;
    private String username;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombres;
    private Long numeroDocumento;
    private Integer idTipoPersonal;
    private String tipoPersonal;
    private Long supervisorId;
    private String estado;
    private Integer tecnicoActive;

    public Long getAbogadoId() {
        return abogadoId;
    }

    public void setAbogadoId(Long abogadoId) {
        this.abogadoId = abogadoId;
    }

    public Integer getIdTecnico() {
        return idTecnico;
    }

    public void setIdTecnico(Integer idTecnico) {
        this.idTecnico = idTecnico;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public Long getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(Long numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public Integer getIdTipoPersonal() {
        return idTipoPersonal;
    }

    public void setIdTipoPersonal(Integer idTipoPersonal) {
        this.idTipoPersonal = idTipoPersonal;
    }

    public String getTipoPersonal() {
        return tipoPersonal;
    }

    public void setTipoPersonal(String tipoPersonal) {
        this.tipoPersonal = tipoPersonal;
    }

    public Long getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(Long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getTecnicoActive() {
        return tecnicoActive;
    }

    public void setTecnicoActive(Integer tecnicoActive) {
        this.tecnicoActive = tecnicoActive;
    }
}
