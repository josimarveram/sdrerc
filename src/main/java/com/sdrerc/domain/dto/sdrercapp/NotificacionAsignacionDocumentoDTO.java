package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class NotificacionAsignacionDocumentoDTO {

    private final Long idDocumentoAnalizado;
    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroExpedienteSgd;
    private final String clasificacion;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final LocalDate fechaDocumento;
    private final String titular;
    private final String estadoDocumentoCodigo;
    private final String estadoDocumento;
    private final int totalRelacionados;
    private final boolean asignado;
    private final String numeroHojaEnvioNotificacion;
    private final String usuarioNotificacionActual;

    public NotificacionAsignacionDocumentoDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String numeroExpediente,
            String numeroExpedienteSgd,
            String clasificacion,
            String tipoDocumento,
            String numeroDocumento,
            LocalDate fechaDocumento,
            String titular,
            String estadoDocumentoCodigo,
            String estadoDocumento,
            int totalRelacionados,
            boolean asignado,
            String numeroHojaEnvioNotificacion,
            String usuarioNotificacionActual) {
        this.idDocumentoAnalizado = idDocumentoAnalizado;
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroExpedienteSgd = safe(numeroExpedienteSgd);
        this.clasificacion = safe(clasificacion);
        this.tipoDocumento = safe(tipoDocumento);
        this.numeroDocumento = safe(numeroDocumento);
        this.fechaDocumento = fechaDocumento;
        this.titular = safe(titular);
        this.estadoDocumentoCodigo = safe(estadoDocumentoCodigo);
        this.estadoDocumento = safe(estadoDocumento);
        this.totalRelacionados = totalRelacionados;
        this.asignado = asignado;
        this.numeroHojaEnvioNotificacion = safe(numeroHojaEnvioNotificacion);
        this.usuarioNotificacionActual = safe(usuarioNotificacionActual);
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

    public String getClasificacion() {
        return clasificacion;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public LocalDate getFechaDocumento() {
        return fechaDocumento;
    }

    public String getTitular() {
        return titular;
    }

    public String getEstadoDocumentoCodigo() {
        return estadoDocumentoCodigo;
    }

    public String getEstadoDocumento() {
        return estadoDocumento;
    }

    public int getTotalRelacionados() {
        return totalRelacionados;
    }

    public boolean isAsignado() {
        return asignado;
    }

    public String getNumeroHojaEnvioNotificacion() {
        return numeroHojaEnvioNotificacion;
    }

    public String getUsuarioNotificacionActual() {
        return usuarioNotificacionActual;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
