package com.sdrerc.domain.dto.sdrercapp;

import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ExpedienteBandejaDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroTramiteDocumentario;
    private final String numeroExpedienteSgd;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final String abogadoInicial;
    private final String responsableActual;
    private final String equipoActual;
    private final LocalDate fechaRecepcion;
    private final LocalDateTime fechaRegistro;
    private final LocalDateTime fechaUltimoMovimiento;
    private final LocalDate fechaVencimiento;
    private final Long diasRestantes;
    private final boolean requierePublicacion;
    private final boolean expedienteDigitalCompleto;
    private final String canal;
    private final String procedimiento;
    private final String tipoActa;
    private final String numeroActa;
    private final String grupoFamiliar;
    private final String alertas;
    private final String titular;
    private final int relacionesConfirmadasComoPrincipal;
    private final boolean relacionadoComoHijo;

    public ExpedienteBandejaDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroTramiteDocumentario,
            String numeroExpedienteSgd,
            String etapaCodigo,
            String estadoCodigo,
            String abogadoInicial,
            String responsableActual,
            String equipoActual,
            LocalDate fechaRecepcion,
            LocalDateTime fechaRegistro,
            LocalDateTime fechaUltimoMovimiento,
            LocalDate fechaVencimiento,
            boolean requierePublicacion,
            boolean expedienteDigitalCompleto) {
        this(
                idExpediente,
                numeroExpediente,
                numeroTramiteDocumentario,
                numeroExpedienteSgd,
                etapaCodigo,
                estadoCodigo,
                abogadoInicial,
                responsableActual,
                equipoActual,
                fechaRecepcion,
                fechaRegistro,
                fechaUltimoMovimiento,
                fechaVencimiento,
                requierePublicacion,
                expedienteDigitalCompleto,
                "",
                "",
                "",
                "",
                "",
                "",
                "");
    }

    public ExpedienteBandejaDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroTramiteDocumentario,
            String numeroExpedienteSgd,
            String etapaCodigo,
            String estadoCodigo,
            String abogadoInicial,
            String responsableActual,
            String equipoActual,
            LocalDate fechaRecepcion,
            LocalDateTime fechaRegistro,
            LocalDateTime fechaUltimoMovimiento,
            LocalDate fechaVencimiento,
            boolean requierePublicacion,
            boolean expedienteDigitalCompleto,
            String canal,
            String procedimiento,
            String tipoActa,
            String numeroActa,
            String grupoFamiliar,
            String alertas,
            String titular) {
        this(
                idExpediente,
                numeroExpediente,
                numeroTramiteDocumentario,
                numeroExpedienteSgd,
                etapaCodigo,
                estadoCodigo,
                abogadoInicial,
                responsableActual,
                equipoActual,
                fechaRecepcion,
                fechaRegistro,
                fechaUltimoMovimiento,
                fechaVencimiento,
                requierePublicacion,
                expedienteDigitalCompleto,
                canal,
                procedimiento,
                tipoActa,
                numeroActa,
                grupoFamiliar,
                alertas,
                titular,
                0,
                false,
                null);
    }

    public ExpedienteBandejaDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroTramiteDocumentario,
            String numeroExpedienteSgd,
            String etapaCodigo,
            String estadoCodigo,
            String abogadoInicial,
            String responsableActual,
            String equipoActual,
            LocalDate fechaRecepcion,
            LocalDateTime fechaRegistro,
            LocalDateTime fechaUltimoMovimiento,
            LocalDate fechaVencimiento,
            boolean requierePublicacion,
            boolean expedienteDigitalCompleto,
            String canal,
            String procedimiento,
            String tipoActa,
            String numeroActa,
            String grupoFamiliar,
            String alertas,
            String titular,
            int relacionesConfirmadasComoPrincipal,
            boolean relacionadoComoHijo,
            Long diasRestantes) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroTramiteDocumentario = safe(numeroTramiteDocumentario);
        this.numeroExpedienteSgd = safe(numeroExpedienteSgd);
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.abogadoInicial = safe(abogadoInicial);
        this.responsableActual = safe(responsableActual);
        this.equipoActual = safe(equipoActual);
        this.fechaRecepcion = fechaRecepcion;
        this.fechaRegistro = fechaRegistro;
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
        this.fechaVencimiento = fechaVencimiento;
        this.diasRestantes = diasRestantes;
        this.requierePublicacion = requierePublicacion;
        this.expedienteDigitalCompleto = expedienteDigitalCompleto;
        this.canal = safe(canal);
        this.procedimiento = safeProcedimiento(procedimiento);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.grupoFamiliar = safe(grupoFamiliar);
        this.alertas = safe(alertas);
        this.titular = safe(titular);
        this.relacionesConfirmadasComoPrincipal = relacionesConfirmadasComoPrincipal;
        this.relacionadoComoHijo = relacionadoComoHijo;
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

    public String getNumeroExpedienteSgd() {
        return numeroExpedienteSgd;
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

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
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

    public String getCanal() {
        return canal;
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

    public String getGrupoFamiliar() {
        return grupoFamiliar;
    }

    public String getAlertas() {
        return alertas;
    }

    public String getTitular() {
        return titular;
    }

    public int getRelacionesConfirmadasComoPrincipal() {
        return relacionesConfirmadasComoPrincipal;
    }

    public boolean isRelacionadoComoHijo() {
        return relacionadoComoHijo;
    }

    public Long getDiasRestantes() {
        return diasRestantes;
    }

    public Long getDiasDesdeSolicitud() {
        LocalDate base = fechaRecepcion;
        if (base == null && fechaRegistro != null) {
            base = fechaRegistro.toLocalDate();
        }
        return base == null ? null : ChronoUnit.DAYS.between(base, LocalDate.now());
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String safeProcedimiento(String value) {
        String canonico = ProcedimientoRegistralRules.nombreCanonico(value);
        return canonico == null ? "" : canonico;
    }
}
