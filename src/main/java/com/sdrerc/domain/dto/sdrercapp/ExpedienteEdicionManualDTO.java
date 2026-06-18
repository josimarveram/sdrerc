package com.sdrerc.domain.dto.sdrercapp;

public class ExpedienteEdicionManualDTO extends RegistroManualExpedienteDTO {

    private Long idExpediente;
    private String numeroExpediente;
    private String etapaCodigo;
    private String estadoCodigo;

    public Long getIdExpediente() {
        return idExpediente;
    }

    public void setIdExpediente(Long idExpediente) {
        this.idExpediente = idExpediente;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public void setNumeroExpediente(String numeroExpediente) {
        this.numeroExpediente = trimToNull(numeroExpediente);
    }

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public void setEtapaCodigo(String etapaCodigo) {
        this.etapaCodigo = trimToNull(etapaCodigo);
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public void setEstadoCodigo(String estadoCodigo) {
        this.estadoCodigo = trimToNull(estadoCodigo);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
