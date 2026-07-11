package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDateTime;

public class AsignacionHistorialDTO {

    private final Long idExpedienteAsignacion;
    private final String abogado;
    private final String equipo;
    private final String numeroHojaEnvio;
    private final LocalDateTime fechaAsignacion;
    private final boolean activa;
    private final boolean reasignacionExcepcional;
    private final String motivo;

    public AsignacionHistorialDTO(
            Long idExpedienteAsignacion,
            String abogado,
            String equipo,
            String numeroHojaEnvio,
            LocalDateTime fechaAsignacion,
            boolean activa,
            boolean reasignacionExcepcional,
            String motivo) {
        this.idExpedienteAsignacion = idExpedienteAsignacion;
        this.abogado = safe(abogado);
        this.equipo = safe(equipo);
        this.numeroHojaEnvio = safe(numeroHojaEnvio);
        this.fechaAsignacion = fechaAsignacion;
        this.activa = activa;
        this.reasignacionExcepcional = reasignacionExcepcional;
        this.motivo = safe(motivo);
    }

    public Long getIdExpedienteAsignacion() {
        return idExpedienteAsignacion;
    }

    public String getAbogado() {
        return abogado;
    }

    public String getEquipo() {
        return equipo;
    }

    public String getNumeroHojaEnvio() {
        return numeroHojaEnvio;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public boolean isActiva() {
        return activa;
    }

    public boolean isReasignacionExcepcional() {
        return reasignacionExcepcional;
    }

    public String getMotivo() {
        return motivo;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
