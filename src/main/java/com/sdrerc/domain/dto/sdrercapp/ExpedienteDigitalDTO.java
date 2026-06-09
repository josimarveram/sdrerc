package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ExpedienteDigitalDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroTramiteDocumentario;
    private final String procedimiento;
    private final String tipoDocumento;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final LocalDate fechaRecepcion;
    private final LocalDateTime fechaIngresoDigital;
    private final LocalDateTime fechaUltimoMovimiento;
    private final String responsable;
    private final String equipo;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final String ultimaObservacion;
    private final int totalDocumentos;
    private final int totalDocumentosAnalizados;
    private final int totalRelacionados;
    private final Long idResolucion;
    private final String tipoResolucion;
    private final String numeroResolucion;
    private final LocalDate fechaResolucion;
    private final Long idNotificacion;
    private final String resultadoNotificacion;
    private final Long idPublicacion;
    private final String estadoPublicacion;
    private final Long idExpedienteDigital;
    private final String codigoExpedienteDigital;
    private final String rutaCarpeta;
    private final String enlaceCarpeta;
    private final boolean documentosCargados;
    private final boolean completo;
    private final String responsableDigital;
    private final String custodioDigital;
    private final LocalDateTime fechaCreacionCarpeta;
    private final LocalDateTime fechaActualizacion;
    private final String accionesPermitidas;

    public ExpedienteDigitalDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroTramiteDocumentario,
            String procedimiento,
            String tipoDocumento,
            String tipoActa,
            String numeroActa,
            String titular,
            LocalDate fechaRecepcion,
            LocalDateTime fechaIngresoDigital,
            LocalDateTime fechaUltimoMovimiento,
            String responsable,
            String equipo,
            String etapaCodigo,
            String estadoCodigo,
            String ultimaObservacion,
            int totalDocumentos,
            int totalDocumentosAnalizados,
            int totalRelacionados,
            Long idResolucion,
            String tipoResolucion,
            String numeroResolucion,
            LocalDate fechaResolucion,
            Long idNotificacion,
            String resultadoNotificacion,
            Long idPublicacion,
            String estadoPublicacion,
            Long idExpedienteDigital,
            String codigoExpedienteDigital,
            String rutaCarpeta,
            String enlaceCarpeta,
            boolean documentosCargados,
            boolean completo,
            String responsableDigital,
            String custodioDigital,
            LocalDateTime fechaCreacionCarpeta,
            LocalDateTime fechaActualizacion,
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
        this.fechaIngresoDigital = fechaIngresoDigital;
        this.fechaUltimoMovimiento = fechaUltimoMovimiento;
        this.responsable = safe(responsable);
        this.equipo = safe(equipo);
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.ultimaObservacion = safe(ultimaObservacion);
        this.totalDocumentos = totalDocumentos;
        this.totalDocumentosAnalizados = totalDocumentosAnalizados;
        this.totalRelacionados = totalRelacionados;
        this.idResolucion = idResolucion;
        this.tipoResolucion = safe(tipoResolucion);
        this.numeroResolucion = safe(numeroResolucion);
        this.fechaResolucion = fechaResolucion;
        this.idNotificacion = idNotificacion;
        this.resultadoNotificacion = safe(resultadoNotificacion);
        this.idPublicacion = idPublicacion;
        this.estadoPublicacion = safe(estadoPublicacion);
        this.idExpedienteDigital = idExpedienteDigital;
        this.codigoExpedienteDigital = safe(codigoExpedienteDigital);
        this.rutaCarpeta = safe(rutaCarpeta);
        this.enlaceCarpeta = safe(enlaceCarpeta);
        this.documentosCargados = documentosCargados;
        this.completo = completo;
        this.responsableDigital = safe(responsableDigital);
        this.custodioDigital = safe(custodioDigital);
        this.fechaCreacionCarpeta = fechaCreacionCarpeta;
        this.fechaActualizacion = fechaActualizacion;
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

    public LocalDateTime getFechaIngresoDigital() {
        return fechaIngresoDigital;
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

    public String getUltimaObservacion() {
        return ultimaObservacion;
    }

    public int getTotalDocumentos() {
        return totalDocumentos;
    }

    public int getTotalDocumentosAnalizados() {
        return totalDocumentosAnalizados;
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

    public String getResultadoNotificacion() {
        return resultadoNotificacion;
    }

    public Long getIdPublicacion() {
        return idPublicacion;
    }

    public String getEstadoPublicacion() {
        return estadoPublicacion;
    }

    public Long getIdExpedienteDigital() {
        return idExpedienteDigital;
    }

    public String getCodigoExpedienteDigital() {
        return codigoExpedienteDigital;
    }

    public String getRutaCarpeta() {
        return rutaCarpeta;
    }

    public String getEnlaceCarpeta() {
        return enlaceCarpeta;
    }

    public boolean isDocumentosCargados() {
        return documentosCargados;
    }

    public boolean isCompleto() {
        return completo;
    }

    public String getResponsableDigital() {
        return responsableDigital;
    }

    public String getCustodioDigital() {
        return custodioDigital;
    }

    public LocalDateTime getFechaCreacionCarpeta() {
        return fechaCreacionCarpeta;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
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

    public int getTotalDocumentosMetadata() {
        return totalDocumentos + totalDocumentosAnalizados;
    }

    public boolean hasAccion(String codigoAccion) {
        if (codigoAccion == null || codigoAccion.trim().isEmpty() || accionesPermitidas.trim().isEmpty()) {
            return false;
        }
        Set<String> acciones = new HashSet<String>(Arrays.asList(accionesPermitidas.toUpperCase(Locale.ROOT).split(",")));
        return acciones.contains(codigoAccion.trim().toUpperCase(Locale.ROOT));
    }

    public boolean isCarpetaCreada() {
        return "EXPEDIENTE_DIGITAL".equalsIgnoreCase(etapaCodigo)
                && "CARPETA_CREADA".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isLinkRegistrado() {
        return "EXPEDIENTE_DIGITAL".equalsIgnoreCase(etapaCodigo)
                && "LINK_REGISTRADO".equalsIgnoreCase(estadoCodigo);
    }

    public boolean isExpedienteDigitalCompleto() {
        return "EXPEDIENTE_DIGITAL".equalsIgnoreCase(etapaCodigo)
                && "EXPEDIENTE_DIGITAL_COMPLETO".equalsIgnoreCase(estadoCodigo);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
