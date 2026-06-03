package com.sdrerc.domain.dto.sdrercapp;

public class UsuarioEquipoDTO {

    private Long idUsuario;
    private Long idEquipo;
    private String codigoEquipo;
    private String nombreEquipo;
    private String area;
    private boolean activo;

    public UsuarioEquipoDTO() {
    }

    public UsuarioEquipoDTO(Long idUsuario, Long idEquipo, String codigoEquipo, String nombreEquipo, String area, boolean activo) {
        this.idUsuario = idUsuario;
        this.idEquipo = idEquipo;
        this.codigoEquipo = codigoEquipo;
        this.nombreEquipo = nombreEquipo;
        this.area = area;
        this.activo = activo;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
    }

    public String getCodigoEquipo() {
        return codigoEquipo;
    }

    public void setCodigoEquipo(String codigoEquipo) {
        this.codigoEquipo = codigoEquipo;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public void setNombreEquipo(String nombreEquipo) {
        this.nombreEquipo = nombreEquipo;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
