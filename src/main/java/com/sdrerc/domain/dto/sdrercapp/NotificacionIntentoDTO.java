package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class NotificacionIntentoDTO {

    private final Long idExpedienteNotificacion;
    private final Long idExpediente;
    private final Long idDocumentoAnalizado;
    private final int numeroIntento;
    private final String tipoNotificacionCodigo;
    private final String tipoNotificacion;
    private final LocalDateTime fechaEnvio;
    private final String estadoNotificacionCodigo;
    private final String estadoNotificacion;
    private final String codigoNotificacion;
    private final LocalDateTime fechaRecepcion;
    private final boolean ubicado;
    private final LocalDate fechaPublicacion;

    public NotificacionIntentoDTO(
            Long idExpedienteNotificacion,
            Long idExpediente,
            Long idDocumentoAnalizado,
            int numeroIntento,
            String tipoNotificacionCodigo,
            String tipoNotificacion,
            LocalDateTime fechaEnvio,
            String estadoNotificacionCodigo,
            String estadoNotificacion,
            String codigoNotificacion,
            LocalDateTime fechaRecepcion,
            boolean ubicado,
            LocalDate fechaPublicacion) {
        this.idExpedienteNotificacion = idExpedienteNotificacion;
        this.idExpediente = idExpediente;
        this.idDocumentoAnalizado = idDocumentoAnalizado;
        this.numeroIntento = numeroIntento;
        this.tipoNotificacionCodigo = safe(tipoNotificacionCodigo);
        this.tipoNotificacion = safe(tipoNotificacion);
        this.fechaEnvio = fechaEnvio;
        this.estadoNotificacionCodigo = safe(estadoNotificacionCodigo);
        this.estadoNotificacion = safe(estadoNotificacion);
        this.codigoNotificacion = safe(codigoNotificacion);
        this.fechaRecepcion = fechaRecepcion;
        this.ubicado = ubicado;
        this.fechaPublicacion = fechaPublicacion;
    }

    public Long getIdExpedienteNotificacion() {
        return idExpedienteNotificacion;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public Long getIdDocumentoAnalizado() {
        return idDocumentoAnalizado;
    }

    public int getNumeroIntento() {
        return numeroIntento;
    }

    public String getTipoNotificacionCodigo() {
        return tipoNotificacionCodigo;
    }

    public String getTipoNotificacion() {
        return tipoNotificacion;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public String getEstadoNotificacionCodigo() {
        return estadoNotificacionCodigo;
    }

    public String getEstadoNotificacion() {
        return estadoNotificacion;
    }

    public String getCodigoNotificacion() {
        return codigoNotificacion;
    }

    public LocalDateTime getFechaRecepcion() {
        return fechaRecepcion;
    }

    public boolean isUbicado() {
        return ubicado;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
