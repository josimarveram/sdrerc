package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EjecucionExpedienteDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroTramiteDocumentario;
    private final String procedimiento;
    private final String tipoDocumento;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final LocalDate fechaRecepcion;
    private final LocalDateTime fechaIngresoEjecucion;
    private final LocalDateTime fechaUltimoMovimiento;
    private final String responsable;
    private final String equipo;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final String resultadoAnalisis;
    private final String fundamentoAnalisis;
    private final String resultadoVerificacion;
    private final String ultimaObservacion;
    private final int totalDocumentos;
    private final int totalRelacionados;
    private final Long idResolucion;
    private final String tipoResolucion;
    private final String numeroResolucion;
    private final LocalDate fechaResolucion;
    private final String accionesPermitidas;

    public EjecucionExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroTramiteDocumentario,
            String procedimiento,
            String tipoDocumento,
            String tipoActa,
            String numeroActa,
            String titular,
            LocalDate fechaRecepcion,
            LocalDateTime fechaIngresoEjecucion,
            LocalDateTime fechaUltimoMovimiento,
            String responsable,
            String equipo,
            String etapaCodigo,
            String estadoCodigo,
            String resultadoAnalisis,
            String fundamentoAnalisis,
            String resultadoVerificacion,
            String ultimaObservacion,
            int totalDocumentos,
            int totalRelacionados,
            Long idResolucion,
            String tipoResolucion,
            String numeroResolucion,
            LocalDate fechaResolucion,
            String accionesPermitidas) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroTramiteDocumentario = safe(numeroTramiteDocumentario);
        this.procedimiento = safe(procedimiento);
        this.tipoDocumento = safe(tipoDocumento);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.titular = safe(titular);
        this.fechaRecepcion = fechaRecepcion;
        this.fechaIngresoEjecucion = fechaIngresoEjecucion;
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
        this.responsable = safe(responsable);
        this.equipo = safe(equipo);
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.resultadoAnalisis = safe(resultadoAnalisis);
        this.fundamentoAnalisis = safe(fundamentoAnalisis);
        this.resultadoVerificacion = safe(resultadoVerificacion);
        this.ultimaObservacion = safe(ultimaObservacion);
        this.totalDocumentos = totalDocumentos;
        this.totalRelacionados = totalRelacionados;
        this.idResolucion = idResolucion;
        this.tipoResolucion = safe(tipoResolucion);
        this.numeroResolucion = safe(numeroResolucion);
        this.fechaResolucion = fechaResolucion;
        this.accionesPermitidas = safe(accionesPermitidas);
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

    public String getTipoDocumento() {
        return tipoDocumento;
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

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public LocalDateTime getFechaIngresoEjecucion() {
        return fechaIngresoEjecucion;
    }

    public LocalDateTime getFechaUltimoMovimiento() {
        return fechaUltimoMovimiento;
    }

    public String getResponsable() {
        return responsable;
    }

    public String getEquipo() {
        return equipo;
    }

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public String getResultadoAnalisis() {
        return resultadoAnalisis;
    }

    public String getFundamentoAnalisis() {
        return fundamentoAnalisis;
    }

    public String getResultadoVerificacion() {
        return resultadoVerificacion;
    }

    public String getUltimaObservacion() {
        return ultimaObservacion;
    }

    public int getTotalDocumentos() {
        return totalDocumentos;
    }

    public int getTotalRelacionados() {
        return totalRelacionados;
    }

    public Long getIdResolucion() {
        return idResolucion;
    }

    public String getTipoResolucion() {
        return tipoResolucion;
    }

    public String getNumeroResolucion() {
        return numeroResolucion;
    }

    public LocalDate getFechaResolucion() {
        return fechaResolucion;
    }

    public String getAccionesPermitidas() {
        return accionesPermitidas;
    }

    public Long getDiasEnEtapa() {
        LocalDate base = fechaRecepcion;
        if (base == null && fechaUltimoMovimiento != null) {
            base = fechaUltimoMovimiento.toLocalDate();
        }
        return base == null ? null : ChronoUnit.DAYS.between(base, LocalDate.now());
    }

    public boolean hasAccion(String codigoAccion) {
        if (codigoAccion == null || codigoAccion.trim().isEmpty() || accionesPermitidas.trim().isEmpty()) {
            return false;
        }
        Set<String> acciones = new HashSet<String>(Arrays.asList(accionesPermitidas.toUpperCase(Locale.ROOT).split(",")));
        return acciones.contains(codigoAccion.trim().toUpperCase(Locale.ROOT));
    }

    public boolean isEnEjecucion() {
        return "EJECUCION".equalsIgnoreCase(etapaCodigo) && "EN_EJECUCION".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isEjecutado() {
        return "EJECUCION".equalsIgnoreCase(etapaCodigo) && "EJECUTADO".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isCorregible() {
        return "EJECUCION".equalsIgnoreCase(etapaCodigo)
                && ("DOCUMENTO_INCONSISTENTE".equalsIgnoreCase(estadoCodigo)
                || "REQUIERE_CORRECCION".equalsIgnoreCase(estadoCodigo));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
