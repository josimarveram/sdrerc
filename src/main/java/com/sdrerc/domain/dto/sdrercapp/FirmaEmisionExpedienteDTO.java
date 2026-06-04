package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class FirmaEmisionExpedienteDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroTramiteDocumentario;
    private final String procedimiento;
    private final String tipoDocumento;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final LocalDate fechaRecepcion;
    private final LocalDateTime fechaEnvioFirma;
    private final LocalDateTime fechaUltimoMovimiento;
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
    private final LocalDateTime fechaFirma;

    public FirmaEmisionExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroTramiteDocumentario,
            String procedimiento,
            String tipoDocumento,
            String tipoActa,
            String numeroActa,
            String titular,
            LocalDate fechaRecepcion,
            LocalDateTime fechaEnvioFirma,
            LocalDateTime fechaUltimoMovimiento,
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
            LocalDateTime fechaFirma) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroTramiteDocumentario = safe(numeroTramiteDocumentario);
        this.procedimiento = safe(procedimiento);
        this.tipoDocumento = safe(tipoDocumento);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.titular = safe(titular);
        this.fechaRecepcion = fechaRecepcion;
        this.fechaEnvioFirma = fechaEnvioFirma;
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
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
        this.fechaFirma = fechaFirma;
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

    public LocalDateTime getFechaEnvioFirma() {
        return fechaEnvioFirma;
    }

    public LocalDateTime getFechaUltimoMovimiento() {
        return fechaUltimoMovimiento;
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

    public LocalDateTime getFechaFirma() {
        return fechaFirma;
    }

    public Long getDiasEnEtapa() {
        return fechaUltimoMovimiento == null ? null : ChronoUnit.DAYS.between(fechaUltimoMovimiento.toLocalDate(), LocalDate.now());
    }

    public boolean isFirmable() {
        return "FIRMA_EMISION".equalsIgnoreCase(etapaCodigo) && "PARA_FIRMA".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isEmitible() {
        return "FIRMA_EMISION".equalsIgnoreCase(etapaCodigo) && "FIRMADO".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isNumerable() {
        return "FIRMA_EMISION".equalsIgnoreCase(etapaCodigo) && "EMITIDO".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isEnviableEjecucion() {
        return "FIRMA_EMISION".equalsIgnoreCase(etapaCodigo) && "RESOLUCION_NUMERADA".equalsIgnoreCase(estadoCodigo);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
