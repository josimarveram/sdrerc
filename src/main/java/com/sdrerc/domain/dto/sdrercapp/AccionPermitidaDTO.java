package com.sdrerc.domain.dto.sdrercapp;

public class AccionPermitidaDTO {

    private final Long idExpediente;
    private final String codigoAccion;
    private final String nombreAccion;
    private final boolean requiereComentario;
    private final boolean requiereDocumento;
    private final String etapaDestinoCodigo;
    private final String estadoDestinoCodigo;

    public AccionPermitidaDTO(
            Long idExpediente,
            String codigoAccion,
            String nombreAccion,
            boolean requiereComentario,
            boolean requiereDocumento,
            String etapaDestinoCodigo,
            String estadoDestinoCodigo) {
        this.idExpediente = idExpediente;
        this.codigoAccion = safe(codigoAccion);
        this.nombreAccion = safe(nombreAccion);
        this.requiereComentario = requiereComentario;
        this.requiereDocumento = requiereDocumento;
        this.etapaDestinoCodigo = safe(etapaDestinoCodigo);
        this.estadoDestinoCodigo = safe(estadoDestinoCodigo);
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getCodigoAccion() {
        return codigoAccion;
    }

    public String getNombreAccion() {
        return nombreAccion;
    }

    public boolean isRequiereComentario() {
        return requiereComentario;
    }

    public boolean isRequiereDocumento() {
        return requiereDocumento;
    }

    public String getEtapaDestinoCodigo() {
        return etapaDestinoCodigo;
    }

    public String getEstadoDestinoCodigo() {
        return estadoDestinoCodigo;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
