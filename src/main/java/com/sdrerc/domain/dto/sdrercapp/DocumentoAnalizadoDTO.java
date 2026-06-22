package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class DocumentoAnalizadoDTO {

    private final Long idDocumentoAnalizado;
    private final Long idExpediente;
    private final String tipoDocumentoCodigo;
    private final String tipoDocumentoNombre;
    private final String estadoDocumentoCodigo;
    private final String estadoDocumentoNombre;
    private final LocalDate fechaDocumento;
    private final String descripcion;
    private final boolean notificado;
    private final LocalDate fechaAcuse;
    private final boolean requiereRespuesta;
    private final String confirmacionRespuesta;
    private final LocalDate fechaRespuesta;
    private final String numeroHojaEnvioRespuesta;

    public DocumentoAnalizadoDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String tipoDocumentoCodigo,
            String tipoDocumentoNombre,
            String estadoDocumentoCodigo,
            String estadoDocumentoNombre,
            LocalDate fechaDocumento,
            String descripcion) {
        this(
                idDocumentoAnalizado,
                idExpediente,
                tipoDocumentoCodigo,
                tipoDocumentoNombre,
                estadoDocumentoCodigo,
                estadoDocumentoNombre,
                fechaDocumento,
                descripcion,
                false,
                null,
                false,
                "",
                null,
                "");
    }

    public DocumentoAnalizadoDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String tipoDocumentoCodigo,
            String tipoDocumentoNombre,
            String estadoDocumentoCodigo,
            String estadoDocumentoNombre,
            LocalDate fechaDocumento,
            String descripcion,
            boolean notificado,
            LocalDate fechaAcuse,
            boolean requiereRespuesta,
            String confirmacionRespuesta,
            LocalDate fechaRespuesta,
            String numeroHojaEnvioRespuesta) {
        this.idDocumentoAnalizado = idDocumentoAnalizado;
        this.idExpediente = idExpediente;
        this.tipoDocumentoCodigo = safe(tipoDocumentoCodigo);
        this.tipoDocumentoNombre = safe(tipoDocumentoNombre);
        this.estadoDocumentoCodigo = safe(estadoDocumentoCodigo);
        this.estadoDocumentoNombre = safe(estadoDocumentoNombre);
        this.fechaDocumento = fechaDocumento;
        this.descripcion = safe(descripcion);
        this.notificado = notificado;
        this.fechaAcuse = fechaAcuse;
        this.requiereRespuesta = requiereRespuesta;
        this.confirmacionRespuesta = safe(confirmacionRespuesta);
        this.fechaRespuesta = fechaRespuesta;
        this.numeroHojaEnvioRespuesta = safe(numeroHojaEnvioRespuesta);
    }

    public static DocumentoAnalizadoDTO nuevo(
            String tipoDocumentoCodigo,
            String tipoDocumentoNombre,
            String estadoDocumentoCodigo,
            String estadoDocumentoNombre,
            LocalDate fechaDocumento,
            String descripcion) {
        return new DocumentoAnalizadoDTO(
                null,
                null,
                tipoDocumentoCodigo,
                tipoDocumentoNombre,
                estadoDocumentoCodigo,
                estadoDocumentoNombre,
                fechaDocumento,
                descripcion);
    }

    public Long getIdDocumentoAnalizado() {
        return idDocumentoAnalizado;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getTipoDocumentoCodigo() {
        return tipoDocumentoCodigo;
    }

    public String getTipoDocumentoNombre() {
        return tipoDocumentoNombre;
    }

    public String getEstadoDocumentoCodigo() {
        return estadoDocumentoCodigo;
    }

    public String getEstadoDocumentoNombre() {
        return estadoDocumentoNombre;
    }

    public LocalDate getFechaDocumento() {
        return fechaDocumento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isNotificado() {
        return notificado;
    }

    public LocalDate getFechaAcuse() {
        return fechaAcuse;
    }

    public boolean isRequiereRespuesta() {
        return requiereRespuesta;
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

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
