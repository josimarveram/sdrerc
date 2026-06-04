package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class DocumentoVerificacionDTO {

    private final String tipoDocumento;
    private final String estadoDocumento;
    private final LocalDate fechaDocumento;
    private final String descripcion;

    public DocumentoVerificacionDTO(String tipoDocumento, String estadoDocumento, LocalDate fechaDocumento, String descripcion) {
        this.tipoDocumento = safe(tipoDocumento);
        this.estadoDocumento = safe(estadoDocumento);
        this.fechaDocumento = fechaDocumento;
        this.descripcion = safe(descripcion);
    }

    public String getTipoDocumento() {
        return tipoDocumento;
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
