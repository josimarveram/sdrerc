package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class AsignacionCartaRespuestaDTO {

    private final Long idDocumentoAnalizado;
    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroExpedienteSgd;
    private final String titular;
    private final String tipoDocumentoNombre;
    private final String estadoDocumentoNombre;
    private final LocalDate fechaDocumento;
    private final String numeroDocumento;
    private final String descripcion;
    private final boolean requiereRespuesta;
    private final boolean notificado;
    private final LocalDate fechaAcuse;
    private final String confirmacionRespuesta;
    private final LocalDate fechaRespuesta;
    private final String numeroHojaEnvioRespuesta;
    private final boolean requierePublicacion;
    private final LocalDate fechaPublicacion;

    public AsignacionCartaRespuestaDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String numeroExpediente,
            String numeroExpedienteSgd,
            String titular,
            String tipoDocumentoNombre,
            String estadoDocumentoNombre,
            LocalDate fechaDocumento,
            String numeroDocumento,
            String descripcion,
            boolean requiereRespuesta,
            boolean notificado,
            LocalDate fechaAcuse,
            String confirmacionRespuesta,
            LocalDate fechaRespuesta,
            String numeroHojaEnvioRespuesta,
            boolean requierePublicacion,
            LocalDate fechaPublicacion) {
        this.idDocumentoAnalizado = idDocumentoAnalizado;
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroExpedienteSgd = safe(numeroExpedienteSgd);
        this.titular = safe(titular);
        this.tipoDocumentoNombre = safe(tipoDocumentoNombre);
        this.estadoDocumentoNombre = safe(estadoDocumentoNombre);
        this.fechaDocumento = fechaDocumento;
        this.numeroDocumento = safe(numeroDocumento);
        this.descripcion = safe(descripcion);
        this.requiereRespuesta = requiereRespuesta;
        this.notificado = notificado;
        this.fechaAcuse = fechaAcuse;
        this.confirmacionRespuesta = safe(confirmacionRespuesta);
        this.fechaRespuesta = fechaRespuesta;
        this.numeroHojaEnvioRespuesta = safe(numeroHojaEnvioRespuesta);
        this.requierePublicacion = requierePublicacion;
        this.fechaPublicacion = fechaPublicacion;
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

    public String getNumeroExpedienteSgd() {
        return numeroExpedienteSgd;
    }

    public String getTitular() {
        return titular;
    }

    public String getTipoDocumentoNombre() {
        return tipoDocumentoNombre;
    }

    public String getEstadoDocumentoNombre() {
        return estadoDocumentoNombre;
    }

    public LocalDate getFechaDocumento() {
        return fechaDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isRequiereRespuesta() {
        return requiereRespuesta;
    }

    public boolean isNotificado() {
        return notificado;
    }

    public LocalDate getFechaAcuse() {
        return fechaAcuse;
    }

    public String getConfirmacionRespuesta() {
        return confirmacionRespuesta;
    }

    public LocalDate getFechaRespuesta() {
        return fechaRespuesta;
    }

    public String getNumeroHojaEnvioRespuesta() {
        return numeroHojaEnvioRespuesta;
    }

    public boolean isRequierePublicacion() {
        return requierePublicacion;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
