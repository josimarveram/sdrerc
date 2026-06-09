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
    private final String titular;
    private final String titularDocumento;
    private final String remitente;
    private final String remitenteDocumento;
    private final String procedimiento;
    private final String canalRecepcion;
    private final LocalDate fechaRecepcion;
    private final String tipoSolicitud;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final String tipoActa;
    private final String numeroActa;
    private final Integer anioActa;
    private final String oficinaRegistral;
    private final String tipoResolucion;
    private final String numeroResolucion;
    private final LocalDate fechaResolucion;
    private final LocalDateTime fechaFirma;
    private final String tipoNotificacion;
    private final String estadoNotificacion;
    private final String resultadoNotificacion;
    private final String estadoCargoAcuse;
    private final String estadoPublicacion;
    private final String medioPublicacion;
    private final String numeroPublicacion;
    private final String rutaCarpetaDigital;
    private final String enlaceCarpetaDigital;
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
            String titular,
            String titularDocumento,
            String remitente,
            String remitenteDocumento,
            String procedimiento,
            String canalRecepcion,
            LocalDate fechaRecepcion,
            String tipoSolicitud,
            String tipoDocumento,
            String numeroDocumento,
            String tipoActa,
            String numeroActa,
            Integer anioActa,
            String oficinaRegistral,
            String tipoResolucion,
            String numeroResolucion,
            LocalDate fechaResolucion,
            LocalDateTime fechaFirma,
            String tipoNotificacion,
            String estadoNotificacion,
            String resultadoNotificacion,
            String estadoCargoAcuse,
            String estadoPublicacion,
            String medioPublicacion,
            String numeroPublicacion,
            String rutaCarpetaDigital,
            String enlaceCarpetaDigital,
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
        this.titular = safe(titular);
        this.titularDocumento = safe(titularDocumento);
        this.remitente = safe(remitente);
        this.remitenteDocumento = safe(remitenteDocumento);
        this.procedimiento = safe(procedimiento);
        this.canalRecepcion = safe(canalRecepcion);
        this.fechaRecepcion = fechaRecepcion;
        this.tipoSolicitud = safe(tipoSolicitud);
        this.tipoDocumento = safe(tipoDocumento);
        this.numeroDocumento = safe(numeroDocumento);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.anioActa = anioActa;
        this.oficinaRegistral = safe(oficinaRegistral);
        this.tipoResolucion = safe(tipoResolucion);
        this.numeroResolucion = safe(numeroResolucion);
        this.fechaResolucion = fechaResolucion;
        this.fechaFirma = fechaFirma;
        this.tipoNotificacion = safe(tipoNotificacion);
        this.estadoNotificacion = safe(estadoNotificacion);
        this.resultadoNotificacion = safe(resultadoNotificacion);
        this.estadoCargoAcuse = safe(estadoCargoAcuse);
        this.estadoPublicacion = safe(estadoPublicacion);
        this.medioPublicacion = safe(medioPublicacion);
        this.numeroPublicacion = safe(numeroPublicacion);
        this.rutaCarpetaDigital = safe(rutaCarpetaDigital);
        this.enlaceCarpetaDigital = safe(enlaceCarpetaDigital);
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

    public String getTitular() {
        return titular;
    }

    public String getTitularDocumento() {
        return titularDocumento;
    }

    public String getRemitente() {
        return remitente;
    }

    public String getRemitenteDocumento() {
        return remitenteDocumento;
    }

    public String getProcedimiento() {
        return procedimiento;
    }

    public String getCanalRecepcion() {
        return canalRecepcion;
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getTipoActa() {
        return tipoActa;
    }

    public String getNumeroActa() {
        return numeroActa;
    }

    public Integer getAnioActa() {
        return anioActa;
    }

    public String getOficinaRegistral() {
        return oficinaRegistral;
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

    public String getTipoNotificacion() {
        return tipoNotificacion;
    }

    public String getEstadoNotificacion() {
        return estadoNotificacion;
    }

    public String getResultadoNotificacion() {
        return resultadoNotificacion;
    }

    public String getEstadoCargoAcuse() {
        return estadoCargoAcuse;
    }

    public String getEstadoPublicacion() {
        return estadoPublicacion;
    }

    public String getMedioPublicacion() {
        return medioPublicacion;
    }

    public String getNumeroPublicacion() {
        return numeroPublicacion;
    }

    public String getRutaCarpetaDigital() {
        return rutaCarpetaDigital;
    }

    public String getEnlaceCarpetaDigital() {
        return enlaceCarpetaDigital;
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
