package com.sdrerc.domain.dto.sdrercapp;

public class GrupoFamiliarCandidatoDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final Long idPersona;
    private final String titular;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final String abogadoAsignado;
    private final Long idGrupoFamiliarActual;

    public GrupoFamiliarCandidatoDTO(
            Long idExpediente,
            String numeroExpediente,
            Long idPersona,
            String titular,
            String etapaCodigo,
            String estadoCodigo,
            String abogadoAsignado,
            Long idGrupoFamiliarActual) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.idPersona = idPersona;
        this.titular = safe(titular);
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.abogadoAsignado = safe(abogadoAsignado);
        this.idGrupoFamiliarActual = idGrupoFamiliarActual;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public Long getIdPersona() {
        return idPersona;
    }

    public String getTitular() {
        return titular;
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

    public Long getIdGrupoFamiliarActual() {
        return idGrupoFamiliarActual;
    }

    public boolean isYaEnGrupo() {
        return idGrupoFamiliarActual != null;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
