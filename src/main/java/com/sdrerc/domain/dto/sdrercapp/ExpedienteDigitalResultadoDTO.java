package com.sdrerc.domain.dto.sdrercapp;

public class ExpedienteDigitalResultadoDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String accionCodigo;
    private final String etapaDestinoCodigo;
    private final String estadoDestinoCodigo;
    private final String mensaje;

    public ExpedienteDigitalResultadoDTO(
            Long idExpediente,
            String numeroExpediente,
            String accionCodigo,
            String etapaDestinoCodigo,
            String estadoDestinoCodigo,
            String mensaje) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.accionCodigo = safe(accionCodigo);
        this.etapaDestinoCodigo = safe(etapaDestinoCodigo);
        this.estadoDestinoCodigo = safe(estadoDestinoCodigo);
        this.mensaje = safe(mensaje);
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public String getAccionCodigo() {
        return accionCodigo;
    }

    public String getEtapaDestinoCodigo() {
        return etapaDestinoCodigo;
    }

    public String getEstadoDestinoCodigo() {
        return estadoDestinoCodigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
