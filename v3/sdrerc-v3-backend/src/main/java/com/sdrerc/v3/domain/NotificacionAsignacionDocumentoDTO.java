package com.sdrerc.v3.domain;

import java.time.LocalDate;

/**
 * Port literal de com.sdrerc.domain.dto.sdrercapp.NotificacionAsignacionDocumentoDTO (V2) — fila
 * a nivel de DOCUMENTO (no de expediente) que alimenta las 4 grillas reales de Notificación
 * (Asignación/Validación/Notificación/Publicación), todas construidas sobre
 * {@code EXPEDIENTE_DOCUMENTO_ANALIZADO} vía {@code DocumentoAnalisisDAO.listarDocumentosNotificacionPareado}.
 * Se porta el DTO completo (no solo los campos de la Bandeja Asignación) porque las 3 bandejas
 * siguientes de este mismo módulo (Validación/Notificación/Publicación) lo van a reutilizar tal
 * cual — mismo criterio "crece, no se reemplaza" ya aplicado a {@code DocumentoAnalisisDAO}.
 */
public class NotificacionAsignacionDocumentoDTO {

    private final Long idDocumentoAnalizado;
    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroExpedienteSgd;
    private final String clasificacion;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final LocalDate fechaDocumento;
    private final String titular;
    private final String estadoDocumentoCodigo;
    private final String estadoDocumento;
    private final int totalRelacionados;
    private final boolean asignado;
    private final String numeroHojaEnvioNotificacion;
    private final String usuarioNotificacionActual;
    private final String estadoExpedienteCodigo;
    private final String estadoExpediente;
    private final LocalDate fechaVencimiento;
    private final Long diasRestantes;
    private final int totalIntentos;
    private final String estadoFinalNotificacionCodigo;
    private final String estadoFinalNotificacion;
    private final LocalDate fechaPublicacionNotif;

    public NotificacionAsignacionDocumentoDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String numeroExpediente,
            String numeroExpedienteSgd,
            String clasificacion,
            String tipoDocumento,
            String numeroDocumento,
            LocalDate fechaDocumento,
            String titular,
            String estadoDocumentoCodigo,
            String estadoDocumento,
            int totalRelacionados,
            boolean asignado,
            String numeroHojaEnvioNotificacion,
            String usuarioNotificacionActual,
            String estadoExpedienteCodigo,
            String estadoExpediente,
            LocalDate fechaVencimiento,
            Long diasRestantes,
            int totalIntentos,
            String estadoFinalNotificacionCodigo,
            String estadoFinalNotificacion,
            LocalDate fechaPublicacionNotif) {
        this.idDocumentoAnalizado = idDocumentoAnalizado;
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroExpedienteSgd = safe(numeroExpedienteSgd);
        this.clasificacion = safe(clasificacion);
        this.tipoDocumento = safe(tipoDocumento);
        this.numeroDocumento = safe(numeroDocumento);
        this.fechaDocumento = fechaDocumento;
        this.titular = safe(titular);
        this.estadoDocumentoCodigo = safe(estadoDocumentoCodigo);
        this.estadoDocumento = safe(estadoDocumento);
        this.totalRelacionados = totalRelacionados;
        this.asignado = asignado;
        this.numeroHojaEnvioNotificacion = safe(numeroHojaEnvioNotificacion);
        this.usuarioNotificacionActual = safe(usuarioNotificacionActual);
        this.estadoExpedienteCodigo = safe(estadoExpedienteCodigo);
        this.estadoExpediente = safe(estadoExpediente);
        this.fechaVencimiento = fechaVencimiento;
        this.diasRestantes = diasRestantes;
        this.totalIntentos = totalIntentos;
        this.estadoFinalNotificacionCodigo = safe(estadoFinalNotificacionCodigo);
        this.estadoFinalNotificacion = safe(estadoFinalNotificacion);
        this.fechaPublicacionNotif = fechaPublicacionNotif;
    }

    public Long getIdDocumentoAnalizado() {
        return idDocumentoAnalizado;
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

    public String getClasificacion() {
        return clasificacion;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public LocalDate getFechaDocumento() {
        return fechaDocumento;
    }

    public String getTitular() {
        return titular;
    }

    public String getEstadoDocumentoCodigo() {
        return estadoDocumentoCodigo;
    }

    public String getEstadoDocumento() {
        return estadoDocumento;
    }

    public int getTotalRelacionados() {
        return totalRelacionados;
    }

    public boolean isAsignado() {
        return asignado;
    }

    public String getNumeroHojaEnvioNotificacion() {
        return numeroHojaEnvioNotificacion;
    }

    public String getUsuarioNotificacionActual() {
        return usuarioNotificacionActual;
    }

    public String getEstadoExpedienteCodigo() {
        return estadoExpedienteCodigo;
    }

    public String getEstadoExpediente() {
        return estadoExpediente;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public Long getDiasRestantes() {
        return diasRestantes;
    }

    public int getTotalIntentos() {
        return totalIntentos;
    }

    public String getEstadoFinalNotificacionCodigo() {
        return estadoFinalNotificacionCodigo;
    }

    public String getEstadoFinalNotificacion() {
        return estadoFinalNotificacion;
    }

    public LocalDate getFechaPublicacionNotif() {
        return fechaPublicacionNotif;
    }

    /** KPI "Pendientes de asignación" (Bandeja Asignación de Notificación). */
    public boolean isPendienteAsignacion() {
        return !asignado;
    }

    /** KPI "Listos para firma" (Bandeja Asignación): documento FINAL ya validado, a la espera de firma. */
    public boolean isListoParaFirma() {
        return "FINAL".equalsIgnoreCase(clasificacion) && "VALIDADO".equalsIgnoreCase(estadoDocumentoCodigo);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
