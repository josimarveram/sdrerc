package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class PublicacionRegistroDTO {

    private final Long idExpediente;
    private final String accionCodigo;
    private final String tipoPublicacion;
    private final LocalDate fechaPublicacion;
    private final String medioPublicacion;
    private final String numeroPublicacion;
    private final String resultadoPublicacion;
    private final String comentario;

    public PublicacionRegistroDTO(
            Long idExpediente,
            String accionCodigo,
            String tipoPublicacion,
            LocalDate fechaPublicacion,
            String medioPublicacion,
            String numeroPublicacion,
            String resultadoPublicacion,
            String comentario) {
        this.idExpediente = idExpediente;
        this.accionCodigo = safe(accionCodigo);
        this.tipoPublicacion = safe(tipoPublicacion);
        this.fechaPublicacion = fechaPublicacion;
        this.medioPublicacion = safe(medioPublicacion);
        this.numeroPublicacion = safe(numeroPublicacion);
        this.resultadoPublicacion = safe(resultadoPublicacion);
        this.comentario = safe(comentario);
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getAccionCodigo() {
        return accionCodigo;
    }

    public String getTipoPublicacion() {
        return tipoPublicacion;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public String getMedioPublicacion() {
        return medioPublicacion;
    }

    public String getNumeroPublicacion() {
        return numeroPublicacion;
    }

    public String getResultadoPublicacion() {
        return resultadoPublicacion;
    }

    public String getComentario() {
        return comentario;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
