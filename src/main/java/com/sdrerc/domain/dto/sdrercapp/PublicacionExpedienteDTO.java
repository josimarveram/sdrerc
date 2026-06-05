package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PublicacionExpedienteDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroTramiteDocumentario;
    private final String procedimiento;
    private final String tipoDocumento;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final LocalDate fechaRecepcion;
    private final LocalDateTime fechaIngresoPublicacion;
    private final LocalDateTime fechaUltimoMovimiento;
    private final String responsable;
    private final String equipo;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final String resultadoAnalisis;
    private final String resultadoVerificacion;
    private final String resultadoEjecucion;
    private final String ultimaObservacion;
    private final int totalDocumentos;
    private final int totalRelacionados;
    private final Long idResolucion;
    private final String tipoResolucion;
    private final String numeroResolucion;
    private final LocalDate fechaResolucion;
    private final Long idNotificacion;
    private final String tipoNotificacion;
    private final String estadoNotificacion;
    private final String resultadoNotificacion;
    private final Boolean requierePublicacionNotificacion;
    private final Long idCargoAcuse;
    private final String estadoCargo;
    private final LocalDateTime fechaCargo;
    private final Long idPublicacion;
    private final String tipoPublicacion;
    private final String estadoPublicacion;
    private final LocalDate fechaGeneracionPublicacion;
    private final LocalDate fechaPublicacion;
    private final String medioPublicacion;
    private final String numeroPublicacion;
    private final String observacionPublicacion;
    private final String accionesPermitidas;

    public PublicacionExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroTramiteDocumentario,
            String procedimiento,
            String tipoDocumento,
            String tipoActa,
            String numeroActa,
            String titular,
            LocalDate fechaRecepcion,
            LocalDateTime fechaIngresoPublicacion,
            LocalDateTime fechaUltimoMovimiento,
            String responsable,
            String equipo,
            String etapaCodigo,
            String estadoCodigo,
            String resultadoAnalisis,
            String resultadoVerificacion,
            String resultadoEjecucion,
            String ultimaObservacion,
            int totalDocumentos,
            int totalRelacionados,
            Long idResolucion,
            String tipoResolucion,
            String numeroResolucion,
            LocalDate fechaResolucion,
            Long idNotificacion,
            String tipoNotificacion,
            String estadoNotificacion,
            String resultadoNotificacion,
            Boolean requierePublicacionNotificacion,
            Long idCargoAcuse,
            String estadoCargo,
            LocalDateTime fechaCargo,
            Long idPublicacion,
            String tipoPublicacion,
            String estadoPublicacion,
            LocalDate fechaGeneracionPublicacion,
            LocalDate fechaPublicacion,
            String medioPublicacion,
            String numeroPublicacion,
            String observacionPublicacion,
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
        this.fechaIngresoPublicacion = fechaIngresoPublicacion;
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
        this.responsable = safe(responsable);
        this.equipo = safe(equipo);
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.resultadoAnalisis = safe(resultadoAnalisis);
        this.resultadoVerificacion = safe(resultadoVerificacion);
        this.resultadoEjecucion = safe(resultadoEjecucion);
        this.ultimaObservacion = safe(ultimaObservacion);
        this.totalDocumentos = totalDocumentos;
        this.totalRelacionados = totalRelacionados;
        this.idResolucion = idResolucion;
        this.tipoResolucion = safe(tipoResolucion);
        this.numeroResolucion = safe(numeroResolucion);
        this.fechaResolucion = fechaResolucion;
        this.idNotificacion = idNotificacion;
        this.tipoNotificacion = safe(tipoNotificacion);
        this.estadoNotificacion = safe(estadoNotificacion);
        this.resultadoNotificacion = safe(resultadoNotificacion);
        this.requierePublicacionNotificacion = requierePublicacionNotificacion;
        this.idCargoAcuse = idCargoAcuse;
        this.estadoCargo = safe(estadoCargo);
        this.fechaCargo = fechaCargo;
        this.idPublicacion = idPublicacion;
        this.tipoPublicacion = safe(tipoPublicacion);
        this.estadoPublicacion = safe(estadoPublicacion);
        this.fechaGeneracionPublicacion = fechaGeneracionPublicacion;
        this.fechaPublicacion = fechaPublicacion;
        this.medioPublicacion = safe(medioPublicacion);
        this.numeroPublicacion = safe(numeroPublicacion);
        this.observacionPublicacion = safe(observacionPublicacion);
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

    public LocalDateTime getFechaIngresoPublicacion() {
        return fechaIngresoPublicacion;
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

    public String getResultadoVerificacion() {
        return resultadoVerificacion;
    }

    public String getResultadoEjecucion() {
        return resultadoEjecucion;
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

    public Long getIdNotificacion() {
        return idNotificacion;
    }

    public String getTipoNotificacion() {
        return tipoNotificacion;
    }

    public String getEstadoNotificacion() {
        return estadoNotificacion;
    }

    public String getResultadoNotificacion() {
        return resultadoNotificacion;
    }

    public Boolean getRequierePublicacionNotificacion() {
        return requierePublicacionNotificacion;
    }

    public Long getIdCargoAcuse() {
        return idCargoAcuse;
    }

    public String getEstadoCargo() {
        return estadoCargo;
    }

    public LocalDateTime getFechaCargo() {
        return fechaCargo;
    }

    public Long getIdPublicacion() {
        return idPublicacion;
    }

    public String getTipoPublicacion() {
        return tipoPublicacion;
    }

    public String getEstadoPublicacion() {
        return estadoPublicacion;
    }

    public LocalDate getFechaGeneracionPublicacion() {
        return fechaGeneracionPublicacion;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public String getMedioPublicacion() {
        return medioPublicacion;
    }

    public String getNumeroPublicacion() {
        return numeroPublicacion;
    }

    public String getObservacionPublicacion() {
        return observacionPublicacion;
    }

    public String getAccionesPermitidas() {
        return accionesPermitidas;
    }

    public Long getDiasEnEtapa() {
        return fechaUltimoMovimiento == null ? null : ChronoUnit.DAYS.between(fechaUltimoMovimiento.toLocalDate(), LocalDate.now());
    }

    public boolean hasAccion(String codigoAccion) {
        if (codigoAccion == null || codigoAccion.trim().isEmpty() || accionesPermitidas.trim().isEmpty()) {
            return false;
        }
        Set<String> acciones = new HashSet<String>(Arrays.asList(accionesPermitidas.toUpperCase(Locale.ROOT).split(",")));
        return acciones.contains(codigoAccion.trim().toUpperCase(Locale.ROOT));
    }

    public boolean isPendientePublicacion() {
        return "PUBLICACION_CONDICIONAL".equalsIgnoreCase(etapaCodigo)
                && "PENDIENTE_PUBLICACION".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isPublicacionRegistrada() {
        return "PUBLICACION_CONDICIONAL".equalsIgnoreCase(etapaCodigo)
                && "PUBLICACION_REGISTRADA".equalsIgnoreCase(estadoCodigo);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
