package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AsignacionExpedienteDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroTramiteDocumentario;
    private final String procedimiento;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final String numeroDocumentoTitular;
    private final LocalDate fechaRecepcion;
    private final LocalDateTime fechaRegistro;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final boolean asignacionActiva;
    private final int posiblesRelacionados;

    public AsignacionExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroTramiteDocumentario,
            String procedimiento,
            String tipoActa,
            String numeroActa,
            String titular,
            String numeroDocumentoTitular,
            LocalDate fechaRecepcion,
            LocalDateTime fechaRegistro,
            String etapaCodigo,
            String estadoCodigo,
            boolean asignacionActiva,
            int posiblesRelacionados) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroTramiteDocumentario = safe(numeroTramiteDocumentario);
        this.procedimiento = safe(procedimiento);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.titular = safe(titular);
        this.numeroDocumentoTitular = safe(numeroDocumentoTitular);
        this.fechaRecepcion = fechaRecepcion;
        this.fechaRegistro = fechaRegistro;
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.asignacionActiva = asignacionActiva;
        this.posiblesRelacionados = posiblesRelacionados;
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

    public String getProcedimiento() {
        return procedimiento;
    }

    public String getTipoActa() {
        return tipoActa;
    }

    public String getNumeroActa() {
        return numeroActa;
    }

    public String getTitular() {
        return titular;
    }

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public boolean isAsignacionActiva() {
        return asignacionActiva;
    }

    public int getPosiblesRelacionados() {
        return posiblesRelacionados;
    }

    public Long getDiasDesdeRegistro() {
        LocalDate base = fechaRecepcion;
        if (base == null && fechaRegistro != null) {
            base = fechaRegistro.toLocalDate();
        }
        if (base == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(base, LocalDate.now());
    }

    public boolean isAsignable() {
        return "REGISTRO".equalsIgnoreCase(etapaCodigo)
                && "REGISTRADO".equalsIgnoreCase(estadoCodigo)
                && !asignacionActiva;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
