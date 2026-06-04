package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class CargoAcuseDTO {

    private final Long idExpediente;
    private final String accionCodigo;
    private final String estadoCargoCodigo;
    private final LocalDate fechaCargo;
    private final String recibidoPor;
    private final String comentario;

    public CargoAcuseDTO(
            Long idExpediente,
            String accionCodigo,
            String estadoCargoCodigo,
            LocalDate fechaCargo,
            String recibidoPor,
            String comentario) {
        this.idExpediente = idExpediente;
        this.accionCodigo = safe(accionCodigo);
        this.estadoCargoCodigo = safe(estadoCargoCodigo);
        this.fechaCargo = fechaCargo;
        this.recibidoPor = safe(recibidoPor);
        this.comentario = safe(comentario);
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getAccionCodigo() {
        return accionCodigo;
    }

    public String getEstadoCargoCodigo() {
        return estadoCargoCodigo;
    }

    public LocalDate getFechaCargo() {
        return fechaCargo;
    }

    public String getRecibidoPor() {
        return recibidoPor;
    }

    public String getComentario() {
        return comentario;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
