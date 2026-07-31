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
    private final String tipoDocumentoCodigo;
    private final String etapaCodigo;
    private final Long diasVencimiento;
    private final Integer diasPlazoVencimiento;
    private final LocalDate fechaVencimiento;
    private final String tipoAlertaVencimiento;

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
            LocalDate fechaPublicacion,
            String tipoDocumentoCodigo,
            String etapaCodigo,
            Long diasVencimiento,
            Integer diasPlazoVencimiento,
            LocalDate fechaVencimiento,
            String tipoAlertaVencimiento) {
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
        this.tipoDocumentoCodigo = safe(tipoDocumentoCodigo);
        this.etapaCodigo = safe(etapaCodigo);
        this.diasVencimiento = diasVencimiento;
        this.diasPlazoVencimiento = diasPlazoVencimiento;
        this.fechaVencimiento = fechaVencimiento;
        this.tipoAlertaVencimiento = safe(tipoAlertaVencimiento);
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

    public String getTipoDocumentoCodigo() {
        return tipoDocumentoCodigo;
    }

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public Long getDiasVencimiento() {
        return diasVencimiento;
    }

    public Integer getDiasPlazoVencimiento() {
        return diasPlazoVencimiento;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public String getTipoAlertaVencimiento() {
        return tipoAlertaVencimiento;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
