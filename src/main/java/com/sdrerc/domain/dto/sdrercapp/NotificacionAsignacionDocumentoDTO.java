package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class NotificacionAsignacionDocumentoDTO {

    private final Long idDocumentoAnalizado;
    private final Long idExpediente;
    private final String numeroExpediente;
    private final String clasificacion;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final LocalDate fechaDocumento;
    private final String titular;
    private final String estadoDocumentoCodigo;
    private final String estadoDocumento;

    public NotificacionAsignacionDocumentoDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String numeroExpediente,
            String clasificacion,
            String tipoDocumento,
            String numeroDocumento,
            LocalDate fechaDocumento,
            String titular,
            String estadoDocumentoCodigo,
            String estadoDocumento) {
        this.idDocumentoAnalizado = idDocumentoAnalizado;
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.clasificacion = safe(clasificacion);
        this.tipoDocumento = safe(tipoDocumento);
        this.numeroDocumento = safe(numeroDocumento);
        this.fechaDocumento = fechaDocumento;
        this.titular = safe(titular);
        this.estadoDocumentoCodigo = safe(estadoDocumentoCodigo);
        this.estadoDocumento = safe(estadoDocumento);
    }

    public Long getIdDocumentoAnalizado() {
        return idDocumentoAnalizado;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public LocalDate getFechaDocumento() {
        return fechaDocumento;
    }

    public String getTitular() {
        return titular;
    }

    public String getEstadoDocumentoCodigo() {
        return estadoDocumentoCodigo;
    }

    public String getEstadoDocumento() {
        return estadoDocumento;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
