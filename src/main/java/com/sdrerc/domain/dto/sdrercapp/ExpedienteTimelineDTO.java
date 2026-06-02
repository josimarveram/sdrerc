package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDateTime;

public class ExpedienteTimelineDTO {

    private final Long idExpedienteHistorial;
    private final Long idExpediente;
    private final LocalDateTime fechaMovimiento;
    private final String movimiento;
    private final String etapaOrigen;
    private final String etapaDestino;
    private final String estadoOrigen;
    private final String estadoDestino;
    private final String usuarioOrigen;
    private final String usuarioDestino;
    private final String comentario;
    private final String motivo;

    public ExpedienteTimelineDTO(
            Long idExpedienteHistorial,
            Long idExpediente,
            LocalDateTime fechaMovimiento,
            String movimiento,
            String etapaOrigen,
            String etapaDestino,
            String estadoOrigen,
            String estadoDestino,
            String usuarioOrigen,
            String usuarioDestino,
            String comentario,
            String motivo) {
        this.idExpedienteHistorial = idExpedienteHistorial;
        this.idExpediente = idExpediente;
        this.fechaMovimiento = fechaMovimiento;
        this.movimiento = safe(movimiento);
        this.etapaOrigen = safe(etapaOrigen);
        this.etapaDestino = safe(etapaDestino);
        this.estadoOrigen = safe(estadoOrigen);
        this.estadoDestino = safe(estadoDestino);
        this.usuarioOrigen = safe(usuarioOrigen);
        this.usuarioDestino = safe(usuarioDestino);
        this.comentario = safe(comentario);
        this.motivo = safe(motivo);
    }

    public Long getIdExpedienteHistorial() {
        return idExpedienteHistorial;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public String getMovimiento() {
        return movimiento;
    }

    public String getEtapaOrigen() {
        return etapaOrigen;
    }

    public String getEtapaDestino() {
        return etapaDestino;
    }

    public String getEstadoOrigen() {
        return estadoOrigen;
    }

    public String getEstadoDestino() {
        return estadoDestino;
    }

    public String getUsuarioOrigen() {
        return usuarioOrigen;
    }

    public String getUsuarioDestino() {
        return usuarioDestino;
    }

    public String getComentario() {
        return comentario;
    }

    public String getMotivo() {
        return motivo;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
