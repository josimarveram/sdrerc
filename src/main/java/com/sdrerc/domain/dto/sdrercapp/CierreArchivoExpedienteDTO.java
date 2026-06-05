package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CierreArchivoExpedienteDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroTramiteDocumentario;
    private final String procedimiento;
    private final String tipoDocumento;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final LocalDate fechaRecepcion;
    private final LocalDateTime fechaUltimoMovimiento;
    private final LocalDateTime fechaCierre;
    private final LocalDateTime fechaArchivo;
    private final String responsable;
    private final String equipo;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final boolean cerrado;
    private final boolean archivado;
    private final boolean expedienteDigitalCompleto;
    private final String ultimaObservacion;
    private final String motivoFinal;
    private final int totalDocumentos;
    private final int totalRelacionados;
    private final Long idResolucion;
    private final String numeroResolucion;
    private final LocalDate fechaResolucion;
    private final Long idNotificacion;
    private final String resultadoNotificacion;
    private final Long idPublicacion;
    private final String estadoPublicacion;
    private final Long idExpedienteDigital;
    private final String rutaDigital;
    private final String enlaceDigital;
    private final Long idDerivacionExterna;
    private final String entidadDestino;
    private final String tipoDerivacion;
    private final String numeroOficio;
    private final LocalDate fechaDerivacion;
    private final String estadoDerivacion;
    private final String comentarioDerivacion;
    private final String accionesPermitidas;

    public CierreArchivoExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroTramiteDocumentario,
            String procedimiento,
            String tipoDocumento,
            String tipoActa,
            String numeroActa,
            String titular,
            LocalDate fechaRecepcion,
            LocalDateTime fechaUltimoMovimiento,
            LocalDateTime fechaCierre,
            LocalDateTime fechaArchivo,
            String responsable,
            String equipo,
            String etapaCodigo,
            String estadoCodigo,
            boolean cerrado,
            boolean archivado,
            boolean expedienteDigitalCompleto,
            String ultimaObservacion,
            String motivoFinal,
            int totalDocumentos,
            int totalRelacionados,
            Long idResolucion,
            String numeroResolucion,
            LocalDate fechaResolucion,
            Long idNotificacion,
            String resultadoNotificacion,
            Long idPublicacion,
            String estadoPublicacion,
            Long idExpedienteDigital,
            String rutaDigital,
            String enlaceDigital,
            Long idDerivacionExterna,
            String entidadDestino,
            String tipoDerivacion,
            String numeroOficio,
            LocalDate fechaDerivacion,
            String estadoDerivacion,
            String comentarioDerivacion,
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
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
        this.fechaCierre = fechaCierre;
        this.fechaArchivo = fechaArchivo;
        this.responsable = safe(responsable);
        this.equipo = safe(equipo);
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.cerrado = cerrado;
        this.archivado = archivado;
        this.expedienteDigitalCompleto = expedienteDigitalCompleto;
        this.ultimaObservacion = safe(ultimaObservacion);
        this.motivoFinal = safe(motivoFinal);
        this.totalDocumentos = totalDocumentos;
        this.totalRelacionados = totalRelacionados;
        this.idResolucion = idResolucion;
        this.numeroResolucion = safe(numeroResolucion);
        this.fechaResolucion = fechaResolucion;
        this.idNotificacion = idNotificacion;
        this.resultadoNotificacion = safe(resultadoNotificacion);
        this.idPublicacion = idPublicacion;
        this.estadoPublicacion = safe(estadoPublicacion);
        this.idExpedienteDigital = idExpedienteDigital;
        this.rutaDigital = safe(rutaDigital);
        this.enlaceDigital = safe(enlaceDigital);
        this.idDerivacionExterna = idDerivacionExterna;
        this.entidadDestino = safe(entidadDestino);
        this.tipoDerivacion = safe(tipoDerivacion);
        this.numeroOficio = safe(numeroOficio);
        this.fechaDerivacion = fechaDerivacion;
        this.estadoDerivacion = safe(estadoDerivacion);
        this.comentarioDerivacion = safe(comentarioDerivacion);
        this.accionesPermitidas = safe(accionesPermitidas);
    }

    public Long getIdExpediente() { return idExpediente; }
    public String getNumeroExpediente() { return numeroExpediente; }
    public String getNumeroTramiteDocumentario() { return numeroTramiteDocumentario; }
    public String getProcedimiento() { return procedimiento; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getTipoActa() { return tipoActa; }
    public String getNumeroActa() { return numeroActa; }
    public String getTitular() { return titular; }
    public LocalDate getFechaRecepcion() { return fechaRecepcion; }
    public LocalDateTime getFechaUltimoMovimiento() { return fechaUltimoMovimiento; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public LocalDateTime getFechaArchivo() { return fechaArchivo; }
    public String getResponsable() { return responsable; }
    public String getEquipo() { return equipo; }
    public String getEtapaCodigo() { return etapaCodigo; }
    public String getEstadoCodigo() { return estadoCodigo; }
    public boolean isCerrado() { return cerrado; }
    public boolean isArchivado() { return archivado; }
    public boolean isExpedienteDigitalCompleto() { return expedienteDigitalCompleto; }
    public String getUltimaObservacion() { return ultimaObservacion; }
    public String getMotivoFinal() { return motivoFinal; }
    public int getTotalDocumentos() { return totalDocumentos; }
    public int getTotalRelacionados() { return totalRelacionados; }
    public Long getIdResolucion() { return idResolucion; }
    public String getNumeroResolucion() { return numeroResolucion; }
    public LocalDate getFechaResolucion() { return fechaResolucion; }
    public Long getIdNotificacion() { return idNotificacion; }
    public String getResultadoNotificacion() { return resultadoNotificacion; }
    public Long getIdPublicacion() { return idPublicacion; }
    public String getEstadoPublicacion() { return estadoPublicacion; }
    public Long getIdExpedienteDigital() { return idExpedienteDigital; }
    public String getRutaDigital() { return rutaDigital; }
    public String getEnlaceDigital() { return enlaceDigital; }
    public Long getIdDerivacionExterna() { return idDerivacionExterna; }
    public String getEntidadDestino() { return entidadDestino; }
    public String getTipoDerivacion() { return tipoDerivacion; }
    public String getNumeroOficio() { return numeroOficio; }
    public LocalDate getFechaDerivacion() { return fechaDerivacion; }
    public String getEstadoDerivacion() { return estadoDerivacion; }
    public String getComentarioDerivacion() { return comentarioDerivacion; }
    public String getAccionesPermitidas() { return accionesPermitidas; }

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

    public boolean isEnCierreArchivo() {
        return "CIERRE_ARCHIVO".equalsIgnoreCase(etapaCodigo);
    }

    public boolean isFinalizado() {
        return isEnCierreArchivo()
                && ("CERRADO".equalsIgnoreCase(estadoCodigo) || "ARCHIVADO".equalsIgnoreCase(estadoCodigo));
    }

    public boolean isDerivacionExternaPendiente() {
        return isEnCierreArchivo() && "DERIVACION_EXTERNA_PENDIENTE".equalsIgnoreCase(estadoCodigo);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
