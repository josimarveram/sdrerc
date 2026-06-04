package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class EjecucionRegistroDTO {

    private final Long idExpediente;
    private final String accionCodigo;
    private final String resultadoCodigo;
    private final String resultadoNombre;
    private final LocalDate fechaEjecucion;
    private final String tipoObservacionCodigo;
    private final String motivoCorreccionCodigo;
    private final String comentario;

    public EjecucionRegistroDTO(
            Long idExpediente,
            String accionCodigo,
            String resultadoCodigo,
            String resultadoNombre,
            LocalDate fechaEjecucion,
            String tipoObservacionCodigo,
            String motivoCorreccionCodigo,
            String comentario) {
        this.idExpediente = idExpediente;
        this.accionCodigo = safe(accionCodigo);
        this.resultadoCodigo = safe(resultadoCodigo);
        this.resultadoNombre = safe(resultadoNombre);
        this.fechaEjecucion = fechaEjecucion;
        this.tipoObservacionCodigo = safe(tipoObservacionCodigo);
        this.motivoCorreccionCodigo = safe(motivoCorreccionCodigo);
        this.comentario = safe(comentario);
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getAccionCodigo() {
        return accionCodigo;
    }

    public String getResultadoCodigo() {
        return resultadoCodigo;
    }

    public String getResultadoNombre() {
        return resultadoNombre;
    }

    public LocalDate getFechaEjecucion() {
        return fechaEjecucion;
    }

    public String getTipoObservacionCodigo() {
        return tipoObservacionCodigo;
    }

    public String getMotivoCorreccionCodigo() {
        return motivoCorreccionCodigo;
    }

    public String getComentario() {
        return comentario;
    }

    public boolean hasComentario() {
        return comentario != null && !comentario.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
