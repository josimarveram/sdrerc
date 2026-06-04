package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class NotificacionRegistroDTO {

    private final Long idExpediente;
    private final String accionCodigo;
    private final String tipoNotificacionCodigo;
    private final LocalDate fechaNotificacion;
    private final String resultado;
    private final String destinatario;
    private final String comentario;

    public NotificacionRegistroDTO(
            Long idExpediente,
            String accionCodigo,
            String tipoNotificacionCodigo,
            LocalDate fechaNotificacion,
            String resultado,
            String destinatario,
            String comentario) {
        this.idExpediente = idExpediente;
        this.accionCodigo = safe(accionCodigo);
        this.tipoNotificacionCodigo = safe(tipoNotificacionCodigo);
        this.fechaNotificacion = fechaNotificacion;
        this.resultado = safe(resultado);
        this.destinatario = safe(destinatario);
        this.comentario = safe(comentario);
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getAccionCodigo() {
        return accionCodigo;
    }

    public String getTipoNotificacionCodigo() {
        return tipoNotificacionCodigo;
    }

    public LocalDate getFechaNotificacion() {
        return fechaNotificacion;
    }

    public String getResultado() {
        return resultado;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public String getComentario() {
        return comentario;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
