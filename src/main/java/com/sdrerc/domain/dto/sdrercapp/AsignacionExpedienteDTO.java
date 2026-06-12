package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AsignacionExpedienteDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroTramiteDocumentario;
    private final String procedimiento;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final String solicitante;
    private final String abogadoAsignado;
    private final Long idAbogadoResponsable;
    private final String numeroDocumentoTitular;
    private final LocalDate fechaRecepcion;
    private final Long diasRestantes;
    private final LocalDateTime fechaRegistro;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final boolean asignacionActiva;
    private final int posiblesRelacionados;
    private final int asociadosConfirmados;
    private final boolean potencialDuplicado;
    private final String observacionSolicitud;

    public AsignacionExpedienteDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroTramiteDocumentario,
            String procedimiento,
            String tipoActa,
            String numeroActa,
            String titular,
            String solicitante,
            String abogadoAsignado,
            Long idAbogadoResponsable,
            String numeroDocumentoTitular,
            LocalDate fechaRecepcion,
            Long diasRestantes,
            LocalDateTime fechaRegistro,
            String etapaCodigo,
            String estadoCodigo,
            boolean asignacionActiva,
            int posiblesRelacionados,
            int asociadosConfirmados,
            boolean potencialDuplicado,
            String observacionSolicitud) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroTramiteDocumentario = safe(numeroTramiteDocumentario);
        this.procedimiento = safe(procedimiento);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.titular = safe(titular);
        this.solicitante = safe(solicitante);
        this.abogadoAsignado = safe(abogadoAsignado);
        this.idAbogadoResponsable = idAbogadoResponsable;
        this.numeroDocumentoTitular = safe(numeroDocumentoTitular);
        this.fechaRecepcion = fechaRecepcion;
        this.diasRestantes = diasRestantes;
        this.fechaRegistro = fechaRegistro;
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.asignacionActiva = asignacionActiva;
        this.posiblesRelacionados = posiblesRelacionados;
        this.asociadosConfirmados = asociadosConfirmados;
        this.potencialDuplicado = potencialDuplicado;
        this.observacionSolicitud = safe(observacionSolicitud);
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

    public String getSolicitante() {
        return solicitante;
    }

    public String getAbogadoAsignado() {
        return abogadoAsignado;
    }

    public Long getIdAbogadoResponsable() {
        return idAbogadoResponsable;
    }

    public String getNumeroDocumentoTitular() {
        return numeroDocumentoTitular;
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public Long getDiasRestantes() {
        return diasRestantes;
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

    public int getAsociadosConfirmados() {
        return asociadosConfirmados;
    }

    public boolean isPotencialDuplicado() {
        return potencialDuplicado;
    }

    public String getObservacionSolicitud() {
        return observacionSolicitud;
    }

    public boolean tieneObservacionRegistro() {
        String observacion = observacionSolicitud.toUpperCase();
        return observacion.contains("ADVERTENCIAS DE VALIDACIÓN")
                || observacion.contains("MOTIVO DUPLICADO");
    }

    public String getAlertaIngreso() {
        if (potencialDuplicado) {
            return "Potencial duplicado";
        }
        if (tieneObservacionRegistro()) {
            return "Con observaciones";
        }
        return "Normal";
    }

    public Long getDiasDesdeRegistro() {
        return diasRestantes;
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
