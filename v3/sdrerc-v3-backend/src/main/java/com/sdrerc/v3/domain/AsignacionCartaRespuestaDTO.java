package com.sdrerc.v3.domain;

import java.time.LocalDate;

/** Port literal de com.sdrerc.domain.dto.sdrercapp.AsignacionCartaRespuestaDTO (V2). */
public class AsignacionCartaRespuestaDTO {

    private final Long idDocumentoAnalizado;
    private final Long idExpediente;
    private final String numeroExpediente;
    private final String numeroExpedienteSgd;
    private final String titular;
    private final String tipoDocumentoNombre;
    private final String estadoDocumentoNombre;
    private final LocalDate fechaDocumento;
    private final String numeroDocumento;
    private final String descripcion;
    private final boolean requiereRespuesta;
    private final boolean notificado;
    private final LocalDate fechaAcuse;
    private final String confirmacionRespuesta;
    private final LocalDate fechaRespuesta;
    private final String numeroHojaEnvioRespuesta;
    private final boolean requierePublicacion;
    private final LocalDate fechaPublicacionEdicto;
    private final LocalDate fechaPublicacionNotificacion;
    private final String tipoDocumentoCodigo;
    private final String etapaCodigo;
    private final Long diasVencimiento;
    private final Integer diasPlazoVencimiento;
    private final LocalDate fechaVencimiento;
    private final String tipoAlertaVencimiento;

    public AsignacionCartaRespuestaDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String numeroExpediente,
            String numeroExpedienteSgd,
            String titular,
            String tipoDocumentoNombre,
            String estadoDocumentoNombre,
            LocalDate fechaDocumento,
            String numeroDocumento,
            String descripcion,
            boolean requiereRespuesta,
            boolean notificado,
            LocalDate fechaAcuse,
            String confirmacionRespuesta,
            LocalDate fechaRespuesta,
            String numeroHojaEnvioRespuesta,
            boolean requierePublicacion,
            LocalDate fechaPublicacionEdicto,
            LocalDate fechaPublicacionNotificacion,
            String tipoDocumentoCodigo,
            String etapaCodigo,
            Long diasVencimiento,
            Integer diasPlazoVencimiento,
            LocalDate fechaVencimiento,
            String tipoAlertaVencimiento) {
        this.idDocumentoAnalizado = idDocumentoAnalizado;
        this.idExpediente = idExpediente;
        this.numeroExpediente = safe(numeroExpediente);
        this.numeroExpedienteSgd = safe(numeroExpedienteSgd);
        this.titular = safe(titular);
        this.tipoDocumentoNombre = safe(tipoDocumentoNombre);
        this.estadoDocumentoNombre = safe(estadoDocumentoNombre);
        this.fechaDocumento = fechaDocumento;
        this.numeroDocumento = safe(numeroDocumento);
        this.descripcion = safe(descripcion);
        this.requiereRespuesta = requiereRespuesta;
        this.notificado = notificado;
        this.fechaAcuse = fechaAcuse;
        this.confirmacionRespuesta = safe(confirmacionRespuesta);
        this.fechaRespuesta = fechaRespuesta;
        this.numeroHojaEnvioRespuesta = safe(numeroHojaEnvioRespuesta);
        this.requierePublicacion = requierePublicacion;
        this.fechaPublicacionEdicto = fechaPublicacionEdicto;
        this.fechaPublicacionNotificacion = fechaPublicacionNotificacion;
        this.tipoDocumentoCodigo = safe(tipoDocumentoCodigo);
        this.etapaCodigo = safe(etapaCodigo);
        this.diasVencimiento = diasVencimiento;
        this.diasPlazoVencimiento = diasPlazoVencimiento;
        this.fechaVencimiento = fechaVencimiento;
        this.tipoAlertaVencimiento = safe(tipoAlertaVencimiento);
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

    public String getTitular() {
        return titular;
    }

    public String getTipoDocumentoNombre() {
        return tipoDocumentoNombre;
    }

    public String getEstadoDocumentoNombre() {
        return estadoDocumentoNombre;
    }

    public LocalDate getFechaDocumento() {
        return fechaDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isRequiereRespuesta() {
        return requiereRespuesta;
    }

    public boolean isNotificado() {
        return notificado;
    }

    public LocalDate getFechaAcuse() {
        return fechaAcuse;
    }

    public String getConfirmacionRespuesta() {
        return confirmacionRespuesta;
    }

    public LocalDate getFechaRespuesta() {
        return fechaRespuesta;
    }

    public String getNumeroHojaEnvioRespuesta() {
        return numeroHojaEnvioRespuesta;
    }

    public boolean isRequierePublicacion() {
        return requierePublicacion;
    }

    public LocalDate getFechaPublicacionEdicto() {
        return fechaPublicacionEdicto;
    }

    public LocalDate getFechaPublicacionNotificacion() {
        return fechaPublicacionNotificacion;
    }

    public String getTipoDocumentoCodigo() {
        return tipoDocumentoCodigo;
    }

    public String getEtapaCodigo() {
        return etapaCodigo;
    }

    public Long getDiasVencimiento() {
        return diasVencimiento;
    }

    public Integer getDiasPlazoVencimiento() {
        return diasPlazoVencimiento;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public String getTipoAlertaVencimiento() {
        return tipoAlertaVencimiento;
    }

    /**
     * Columna "Estado" de la Bandeja Cartas de Respuesta (mismo criterio que
     * {@code estadoCartaRespuesta} en JPanelAsignacionV2, V2): "Edicto Publicado" una vez que se
     * registra Fecha Publ. Edicto, "Pendiente de Respuesta" en cualquier otro caso.
     */
    public String getEstadoCarta() {
        return fechaPublicacionEdicto != null ? "Edicto Publicado" : "Pendiente de Respuesta";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
