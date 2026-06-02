package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ExpedienteConsolaDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroTramiteDocumentario;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final String abogadoInicial;
    private final String responsableActual;
    private final String equipoActual;
    private final LocalDateTime fechaRegistro;
    private final LocalDateTime fechaUltimoMovimiento;
    private final LocalDate fechaVencimiento;
    private final boolean requierePublicacion;
    private final boolean expedienteDigitalCompleto;
    private final Integer totalDocumentos;
    private final Integer observacionesPendientes;
    private final Integer totalNotificaciones;
    private final Integer totalCargos;

    public ExpedienteConsolaDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroTramiteDocumentario,
            String etapaCodigo,
            String estadoCodigo,
            String abogadoInicial,
            String responsableActual,
            String equipoActual,
            LocalDateTime fechaRegistro,
            LocalDateTime fechaUltimoMovimiento,
            LocalDate fechaVencimiento,
            boolean requierePublicacion,
            boolean expedienteDigitalCompleto,
            Integer totalDocumentos,
            Integer observacionesPendientes,
            Integer totalNotificaciones,
            Integer totalCargos) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroTramiteDocumentario = safe(numeroTramiteDocumentario);
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.abogadoInicial = safe(abogadoInicial);
        this.responsableActual = safe(responsableActual);
        this.equipoActual = safe(equipoActual);
        this.fechaRegistro = fechaRegistro;
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
        this.fechaVencimiento = fechaVencimiento;
        this.requierePublicacion = requierePublicacion;
        this.expedienteDigitalCompleto = expedienteDigitalCompleto;
        this.totalDocumentos = safe(totalDocumentos);
        this.observacionesPendientes = safe(observacionesPendientes);
        this.totalNotificaciones = safe(totalNotificaciones);
        this.totalCargos = safe(totalCargos);
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public String getNumeroTramiteDocumentario() {
        return numeroTramiteDocumentario;
    }

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public String getAbogadoInicial() {
        return abogadoInicial;
    }

    public String getResponsableActual() {
        return responsableActual;
    }

    public String getEquipoActual() {
        return equipoActual;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public LocalDateTime getFechaUltimoMovimiento() {
        return fechaUltimoMovimiento;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public boolean isRequierePublicacion() {
        return requierePublicacion;
    }

    public boolean isExpedienteDigitalCompleto() {
        return expedienteDigitalCompleto;
    }

    public Integer getTotalDocumentos() {
        return totalDocumentos;
    }

    public Integer getObservacionesPendientes() {
        return observacionesPendientes;
    }

    public Integer getTotalNotificaciones() {
        return totalNotificaciones;
    }

    public Integer getTotalCargos() {
        return totalCargos;
    }

    public Long getDiasRestantes() {
        if (fechaVencimiento == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static Integer safe(Integer value) {
        return value == null ? 0 : value;
    }
}
