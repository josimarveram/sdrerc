package com.sdrerc.domain.dto.sdrercapp;

import com.sdrerc.domain.rules.ProcedimientoRegistralRules;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpedienteRelacionadoDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroExpedienteSgd;
    private final String numeroTramiteDocumentario;
    private final String numeroDocumento;
    private final String tipoActa;
    private final String numeroActa;
    private final String titular;
    private final String procedimiento;
    private final String solicitante;
    private final String equipoAsignado;
    private final Long idEquipoResponsable;
    private final String abogadoAsignado;
    private final Long idAbogadoResponsable;
    private final String etapaCodigo;
    private final String estadoCodigo;
    private final LocalDate fechaRecepcion;
    private final String motivoCoincidencia;
    private final String tipoRelacion;
    private final String descripcionRelacion;
    private final LocalDateTime fechaRelacion;
    private final String usuarioRelacion;

    public ExpedienteRelacionadoDTO(
            Long idExpediente,
            String numeroExpediente,
            String numeroExpedienteSgd,
            String numeroTramiteDocumentario,
            String numeroDocumento,
            String tipoActa,
            String numeroActa,
            String titular,
            String procedimiento,
            String solicitante,
            String equipoAsignado,
            Long idEquipoResponsable,
            String abogadoAsignado,
            Long idAbogadoResponsable,
            String etapaCodigo,
            String estadoCodigo,
            LocalDate fechaRecepcion,
            String motivoCoincidencia,
            String tipoRelacion,
            String descripcionRelacion,
            LocalDateTime fechaRelacion,
            String usuarioRelacion) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroExpedienteSgd = safe(numeroExpedienteSgd);
        this.numeroTramiteDocumentario = safe(numeroTramiteDocumentario);
        this.numeroDocumento = safe(numeroDocumento);
        this.tipoActa = safe(tipoActa);
        this.numeroActa = safe(numeroActa);
        this.titular = safe(titular);
        this.procedimiento = safeProcedimiento(procedimiento);
        this.solicitante = safe(solicitante);
        this.equipoAsignado = safe(equipoAsignado);
        this.idEquipoResponsable = idEquipoResponsable;
        this.abogadoAsignado = safe(abogadoAsignado);
        this.idAbogadoResponsable = idAbogadoResponsable;
        this.etapaCodigo = safe(etapaCodigo);
        this.estadoCodigo = safe(estadoCodigo);
        this.fechaRecepcion = fechaRecepcion;
        this.motivoCoincidencia = safe(motivoCoincidencia);
        this.tipoRelacion = safe(tipoRelacion);
        this.descripcionRelacion = safe(descripcionRelacion);
        this.fechaRelacion = fechaRelacion;
        this.usuarioRelacion = safe(usuarioRelacion);
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public String getNumeroExpedienteSgd() {
        return numeroExpedienteSgd;
    }

    public String getNumeroTramiteDocumentario() {
        return numeroTramiteDocumentario;
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

    public String getTitular() {
        return titular;
    }

    public String getProcedimiento() {
        return procedimiento;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public String getEquipoAsignado() {
        return equipoAsignado;
    }

    public Long getIdEquipoResponsable() {
        return idEquipoResponsable;
    }

    public String getAbogadoAsignado() {
        return abogadoAsignado;
    }

    public Long getIdAbogadoResponsable() {
        return idAbogadoResponsable;
    }

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public String getMotivoCoincidencia() {
        return motivoCoincidencia;
    }

    public String getTipoRelacion() {
        return tipoRelacion;
    }

    public String getDescripcionRelacion() {
        return descripcionRelacion;
    }

    public LocalDateTime getFechaRelacion() {
        return fechaRelacion;
    }

    public LocalDate getFechaAsociacion() {
        return fechaRelacion == null ? null : fechaRelacion.toLocalDate();
    }

    public String getUsuarioRelacion() {
        return usuarioRelacion;
    }

    public boolean isRecibidoPorAbogado() {
        return "RECIBIDO_POR_ABOGADO".equalsIgnoreCase(estadoCodigo);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String safeProcedimiento(String value) {
        String canonico = ProcedimientoRegistralRules.nombreCanonico(value);
        return canonico == null ? "" : canonico;
    }
}
