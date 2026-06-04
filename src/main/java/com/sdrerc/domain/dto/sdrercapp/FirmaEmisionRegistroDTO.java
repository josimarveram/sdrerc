package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class FirmaEmisionRegistroDTO {

    private final Long idExpediente;
    private final String accionCodigo;
    private final String tipoResolucionCodigo;
    private final String tipoResolucionNombre;
    private final String numeroResolucion;
    private final LocalDate fechaFirma;
    private final LocalDate fechaEmision;
    private final LocalDate fechaResolucion;
    private final String comentario;

    public FirmaEmisionRegistroDTO(
            Long idExpediente,
            String accionCodigo,
            String tipoResolucionCodigo,
            String tipoResolucionNombre,
            String numeroResolucion,
            LocalDate fechaFirma,
            LocalDate fechaEmision,
            LocalDate fechaResolucion,
            String comentario) {
        this.idExpediente = idExpediente;
        this.accionCodigo = safe(accionCodigo);
        this.tipoResolucionCodigo = safe(tipoResolucionCodigo);
        this.tipoResolucionNombre = safe(tipoResolucionNombre);
        this.numeroResolucion = safe(numeroResolucion);
        this.fechaFirma = fechaFirma;
        this.fechaEmision = fechaEmision;
        this.fechaResolucion = fechaResolucion;
        this.comentario = safe(comentario);
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getAccionCodigo() {
        return accionCodigo;
    }

    public String getTipoResolucionCodigo() {
        return tipoResolucionCodigo;
    }

    public String getTipoResolucionNombre() {
        return tipoResolucionNombre;
    }

    public String getNumeroResolucion() {
        return numeroResolucion;
    }

    public LocalDate getFechaFirma() {
        return fechaFirma;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public LocalDate getFechaResolucion() {
        return fechaResolucion;
    }

    public String getComentario() {
        return comentario;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
