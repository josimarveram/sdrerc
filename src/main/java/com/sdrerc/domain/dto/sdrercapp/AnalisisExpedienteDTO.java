package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AnalisisExpedienteDTO {

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
    private final LocalDateTime fechaRegistro;
    private final LocalDateTime fechaAsignacion;
    private final LocalDateTime fechaUltimoMovimiento;
    private final String responsable;
    private final String equipo;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final boolean tieneObservacionPendiente;
    private final int totalRelacionados;
    private final int totalDocumentosAnalizados;
    private final String ultimoResultadoAnalisis;

    public AnalisisExpedienteDTO(
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
            LocalDateTime fechaRegistro,
            LocalDateTime fechaAsignacion,
            LocalDateTime fechaUltimoMovimiento,
            String responsable,
            String equipo,
            String etapaCodigo,
            String estadoCodigo,
            boolean tieneObservacionPendiente,
            int totalRelacionados,
            int totalDocumentosAnalizados,
            String ultimoResultadoAnalisis) {
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
        this.fechaRegistro = fechaRegistro;
        this.fechaAsignacion = fechaAsignacion;
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
        this.responsable = safe(responsable);
        this.equipo = safe(equipo);
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.tieneObservacionPendiente = tieneObservacionPendiente;
        this.totalRelacionados = totalRelacionados;
        this.totalDocumentosAnalizados = totalDocumentosAnalizados;
        this.ultimoResultadoAnalisis = safe(ultimoResultadoAnalisis);
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
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

    public Long getDiasEnEtapa() {
        LocalDate base = fechaRecepcion;
        if (base == null && fechaUltimoMovimiento != null) {
            base = fechaUltimoMovimiento.toLocalDate();
        }
        if (base == null && fechaAsignacion != null) {
            base = fechaAsignacion.toLocalDate();
        }
        if (base == null && fechaRegistro != null) {
            base = fechaRegistro.toLocalDate();
        }
        return base == null ? null : ChronoUnit.DAYS.between(base, LocalDate.now());
    }

    public boolean isRecibible() {
        return "ASIGNACION".equalsIgnoreCase(etapaCodigo) && "ASIGNADO".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isRegistrable() {
        return "ANALISIS".equalsIgnoreCase(etapaCodigo)
                && ("RECIBIDO_POR_ABOGADO".equalsIgnoreCase(estadoCodigo)
                || "OBSERVADO".equalsIgnoreCase(estadoCodigo)
                || "SUBSANADO".equalsIgnoreCase(estadoCodigo));
    }

    public boolean isEnviableVerificacion() {
        return "ANALISIS".equalsIgnoreCase(etapaCodigo)
                && ("ATENDIDO".equalsIgnoreCase(estadoCodigo) || "SUBSANADO".equalsIgnoreCase(estadoCodigo));
    }

    public boolean isDerivableNotificacionEspecial() {
        return "ANALISIS".equalsIgnoreCase(etapaCodigo)
                && ("EN_ABANDONO".equalsIgnoreCase(estadoCodigo)
                || "OBSERVACION_ADMINISTRATIVA".equalsIgnoreCase(estadoCodigo));
    }

    public boolean isArchivableNoCorresponde() {
        return "ANALISIS".equalsIgnoreCase(etapaCodigo) && "NO_CORRESPONDE".equalsIgnoreCase(estadoCodigo);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
