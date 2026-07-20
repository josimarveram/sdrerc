package com.sdrerc.domain.dto.sdrercapp;

public class GrupoFamiliarIntegranteDTO {

    private final Long idPersona;
    private final String nombreCompleto;
    private final Long idExpediente;
    private final String numeroExpediente;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final String abogadoAsignado;

    public GrupoFamiliarIntegranteDTO(
            Long idPersona,
            String nombreCompleto,
            Long idExpediente,
            String numeroExpediente,
            String etapaCodigo,
            String estadoCodigo,
            String abogadoAsignado) {
        this.idPersona = idPersona;
        this.nombreCompleto = safe(nombreCompleto);
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.abogadoAsignado = safe(abogadoAsignado);
    }

    public Long getIdPersona() {
        return idPersona;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public String getAbogadoAsignado() {
        return abogadoAsignado;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
