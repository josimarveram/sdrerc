package com.sdrerc.ui.views.registrorecepcion;

import java.time.LocalDate;

public class CargaDiariaPreviewDTO {

    private final String numeroTramite;
    private final String tipoProcedimiento;
    private final String titular;
    private final String acta;
    private final LocalDate fechaRecepcion;
    private final String remitente;
    private final String estadoValidacion;
    private final boolean posibleDuplicado;
    private final String numeroExpedienteGenerado;
    private final String observacion;

    public CargaDiariaPreviewDTO(
            String numeroTramite,
            String tipoProcedimiento,
            String titular,
            String acta,
            LocalDate fechaRecepcion,
            String remitente,
            String estadoValidacion,
            boolean posibleDuplicado,
            String numeroExpedienteGenerado,
            String observacion) {
        this.numeroTramite = numeroTramite;
        this.tipoProcedimiento = tipoProcedimiento;
        this.titular = titular;
        this.acta = acta;
        this.fechaRecepcion = fechaRecepcion;
        this.remitente = remitente;
        this.estadoValidacion = estadoValidacion;
        this.posibleDuplicado = posibleDuplicado;
        this.numeroExpedienteGenerado = numeroExpedienteGenerado;
        this.observacion = observacion;
    }

    public String getNumeroTramite() {
        return numeroTramite;
    }

    public String getTipoProcedimiento() {
        return tipoProcedimiento;
    }

    public String getTitular() {
        return titular;
    }

    public String getActa() {
        return acta;
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public String getRemitente() {
        return remitente;
    }

    public String getEstadoValidacion() {
        return estadoValidacion;
    }

    public boolean isPosibleDuplicado() {
        return posibleDuplicado;
    }

    public String getNumeroExpedienteGenerado() {
        return numeroExpedienteGenerado;
    }

    public String getObservacion() {
        return observacion;
    }
}
