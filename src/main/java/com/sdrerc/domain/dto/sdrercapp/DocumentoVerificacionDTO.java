package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class DocumentoVerificacionDTO {

    private final Long idDocumentoAnalizado;
    private final Long idExpediente;
    private final String tipoDocumento;
    private final String estadoDocumentoCodigo;
    private final String estadoDocumento;
    private final LocalDate fechaDocumento;
    private final String descripcion;

    public DocumentoVerificacionDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String tipoDocumento,
            String estadoDocumentoCodigo,
            String estadoDocumento,
            LocalDate fechaDocumento,
            String descripcion) {
        this.idDocumentoAnalizado = idDocumentoAnalizado;
        this.idExpediente = idExpediente;
        this.tipoDocumento = safe(tipoDocumento);
        this.estadoDocumentoCodigo = safe(estadoDocumentoCodigo);
        this.estadoDocumento = safe(estadoDocumento);
        this.fechaDocumento = fechaDocumento;
        this.descripcion = safe(descripcion);
    }

    public Long getIdDocumentoAnalizado() {
        return idDocumentoAnalizado;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getEstadoDocumentoCodigo() {
        return estadoDocumentoCodigo;
    }

    public String getEstadoDocumento() {
        return estadoDocumento;
    }

    public LocalDate getFechaDocumento() {
        return fechaDocumento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
