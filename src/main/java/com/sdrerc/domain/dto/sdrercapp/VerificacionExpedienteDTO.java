package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class VerificacionExpedienteDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroTramiteDocumentario;
    private final String procedimiento;
    private final String tipoDocumento;
    private final String numeroDocumentoTitular;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final LocalDate fechaRecepcion;
    private final Long diasRestantes;
    private final LocalDateTime fechaEnvioVerificacion;
    private final LocalDateTime fechaUltimoMovimiento;
    private final String responsable;
    private final String equipo;
    private final String responsableAnalisis;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final boolean tieneObservacionPendiente;
    private final int totalRelacionados;
    private final int totalDocumentosAnalizados;
    private final String ultimoResultadoAnalisis;
    private final String fundamentoAnalisis;
    private final String ultimaObservacionVerificacion;

    public VerificacionExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroTramiteDocumentario,
            String procedimiento,
            String tipoDocumento,
            String numeroDocumentoTitular,
            String tipoActa,
            String numeroActa,
            String titular,
            LocalDate fechaRecepcion,
            Long diasRestantes,
            LocalDateTime fechaEnvioVerificacion,
            LocalDateTime fechaUltimoMovimiento,
            String responsable,
            String equipo,
            String responsableAnalisis,
            String etapaCodigo,
            String estadoCodigo,
            boolean tieneObservacionPendiente,
            int totalRelacionados,
            int totalDocumentosAnalizados,
            String ultimoResultadoAnalisis,
            String fundamentoAnalisis,
            String ultimaObservacionVerificacion) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroTramiteDocumentario = safe(numeroTramiteDocumentario);
        this.procedimiento = safe(procedimiento);
        this.tipoDocumento = safe(tipoDocumento);
        this.numeroDocumentoTitular = safe(numeroDocumentoTitular);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.titular = safe(titular);
        this.fechaRecepcion = fechaRecepcion;
        this.diasRestantes = diasRestantes;
        this.fechaEnvioVerificacion = fechaEnvioVerificacion;
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
        this.responsable = safe(responsable);
        this.equipo = safe(equipo);
        this.responsableAnalisis = safe(responsableAnalisis);
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.tieneObservacionPendiente = tieneObservacionPendiente;
        this.totalRelacionados = totalRelacionados;
        this.totalDocumentosAnalizados = totalDocumentosAnalizados;
        this.ultimoResultadoAnalisis = safe(ultimoResultadoAnalisis);
        this.fundamentoAnalisis = safe(fundamentoAnalisis);
        this.ultimaObservacionVerificacion = safe(ultimaObservacionVerificacion);
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

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
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

    public LocalDateTime getFechaEnvioVerificacion() {
        return fechaEnvioVerificacion;
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

    public String getResponsableAnalisis() {
        return responsableAnalisis;
    }

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public boolean isTieneObservacionPendiente() {
        return tieneObservacionPendiente;
    }

    public int getTotalRelacionados() {
        return totalRelacionados;
    }

    public int getTotalDocumentosAnalizados() {
        return totalDocumentosAnalizados;
    }

    public String getUltimoResultadoAnalisis() {
        return ultimoResultadoAnalisis;
    }

    public String getFundamentoAnalisis() {
        return fundamentoAnalisis;
    }

    public String getUltimaObservacionVerificacion() {
        return ultimaObservacionVerificacion;
    }

    public Long getDiasEnEtapa() {
        return diasRestantes;
    }

    public boolean isRegistrableVerificacion() {
        return "VERIFICACION".equalsIgnoreCase(etapaCodigo) && "EN_VERIFICACION".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isEnviableFirma() {
        return "VERIFICACION".equalsIgnoreCase(etapaCodigo) && "VERIFICADO".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isDevolvibleAnalisis() {
        return "VERIFICACION".equalsIgnoreCase(etapaCodigo)
                && ("REQUIERE_CORRECCION".equalsIgnoreCase(estadoCodigo)
                || "DOCUMENTO_INCONSISTENTE".equalsIgnoreCase(estadoCodigo));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
