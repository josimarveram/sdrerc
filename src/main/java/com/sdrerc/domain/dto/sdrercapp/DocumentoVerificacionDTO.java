package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class DocumentoVerificacionDTO {

    private final Long idDocumentoAnalizado;
    private final Long idExpediente;
    private final String tipoDocumento;
    private final String estadoDocumentoCodigo;
    private final String estadoDocumento;
    private final LocalDate fechaDocumento;
    private final String numeroDocumento;
    private final String descripcion;
    private final boolean requiereRespuesta;
    private final LocalDate fechaAcuse;
    private final String confirmacionRespuesta;
    private final LocalDate fechaRespuesta;
    private final String numeroHojaEnvioRespuesta;
    private final boolean notificado;
    private final String detalleObservacion;

    public DocumentoVerificacionDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String tipoDocumento,
            String estadoDocumentoCodigo,
            String estadoDocumento,
            LocalDate fechaDocumento,
            String descripcion) {
        this(
                idDocumentoAnalizado,
                idExpediente,
                tipoDocumento,
                estadoDocumentoCodigo,
                estadoDocumento,
                fechaDocumento,
                "",
                descripcion,
                false,
                null,
                "",
                null,
                "",
                false,
                "");
    }

    public DocumentoVerificacionDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String tipoDocumento,
            String estadoDocumentoCodigo,
            String estadoDocumento,
            LocalDate fechaDocumento,
            String numeroDocumento,
            String descripcion,
            boolean requiereRespuesta,
            LocalDate fechaAcuse,
            String confirmacionRespuesta,
            LocalDate fechaRespuesta,
            String numeroHojaEnvioRespuesta,
            boolean notificado,
            String detalleObservacion) {
        this.idDocumentoAnalizado = idDocumentoAnalizado;
        this.idExpediente = idExpediente;
        this.tipoDocumento = safe(tipoDocumento);
        this.estadoDocumentoCodigo = safe(estadoDocumentoCodigo);
        this.estadoDocumento = safe(estadoDocumento);
        this.fechaDocumento = fechaDocumento;
        this.numeroDocumento = safe(numeroDocumento);
        this.descripcion = safe(descripcion);
        this.requiereRespuesta = requiereRespuesta;
        this.fechaAcuse = fechaAcuse;
        this.confirmacionRespuesta = safe(confirmacionRespuesta);
        this.fechaRespuesta = fechaRespuesta;
        this.numeroHojaEnvioRespuesta = safe(numeroHojaEnvioRespuesta);
        this.notificado = notificado;
        this.detalleObservacion = safe(detalleObservacion);
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

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isRequiereRespuesta() {
        return requiereRespuesta;
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

    public boolean isNotificado() {
        return notificado;
    }

    public String getDetalleObservacion() {
        return detalleObservacion;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
