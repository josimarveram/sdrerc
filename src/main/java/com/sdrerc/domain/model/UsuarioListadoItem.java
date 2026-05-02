package com.sdrerc.domain.model;

public class UsuarioListadoItem {

    private Long userId;
    private String username;
    private String nombreVisible;
    private String status;
    private Long idTecnico;
    private boolean esSupervision;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    public void setNombreVisible(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getIdTecnico() {
        return idTecnico;
    }

    public void setIdTecnico(Long idTecnico) {
        this.idTecnico = idTecnico;
    }

    public boolean isEsSupervision() {
        return esSupervision;
    }

    public void setEsSupervision(boolean esSupervision) {
        this.esSupervision = esSupervision;
    }
}
