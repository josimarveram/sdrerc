package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class DocumentoFirmaDTO {

    private final String tipoDocumento;
    private final String estadoDocumento;
    private final String numeroDocumento;
    private final String nombreDocumento;
    private final LocalDate fechaDocumento;

    public DocumentoFirmaDTO(
            String tipoDocumento,
            String estadoDocumento,
            String numeroDocumento,
            String nombreDocumento,
            LocalDate fechaDocumento) {
        this.tipoDocumento = safe(tipoDocumento);
        this.estadoDocumento = safe(estadoDocumento);
        this.numeroDocumento = safe(numeroDocumento);
        this.nombreDocumento = safe(nombreDocumento);
        this.fechaDocumento = fechaDocumento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getEstadoDocumento() {
        return estadoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getNombreDocumento() {
        return nombreDocumento;
    }

    public LocalDate getFechaDocumento() {
        return fechaDocumento;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
