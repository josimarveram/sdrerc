package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ResolucionDocumentoDTO {

    private final Long idResolucion;
    private final Long idExpediente;
    private final String tipoResolucion;
    private final String numeroResolucion;
    private final LocalDate fechaResolucion;
    private final LocalDateTime fechaFirma;

    public ResolucionDocumentoDTO(
            Long idResolucion,
            Long idExpediente,
            String tipoResolucion,
            String numeroResolucion,
            LocalDate fechaResolucion,
            LocalDateTime fechaFirma) {
        this.idResolucion = idResolucion;
        this.idExpediente = idExpediente;
        this.tipoResolucion = safe(tipoResolucion);
        this.numeroResolucion = safe(numeroResolucion);
        this.fechaResolucion = fechaResolucion;
        this.fechaFirma = fechaFirma;
    }

    public Long getIdResolucion() {
        return idResolucion;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getTipoResolucion() {
        return tipoResolucion;
    }

    public String getNumeroResolucion() {
        return numeroResolucion;
    }

    public LocalDate getFechaResolucion() {
        return fechaResolucion;
    }

    public LocalDateTime getFechaFirma() {
        return fechaFirma;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
