package com.sdrerc.v3.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Port literal de com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO (V2, 28 campos) —
 * "documento analizado" de la subpestaña "Análisis" (20/08/2026). Jerarquía de máximo 2 niveles:
 * {@code idDocumentoPadre} nulo = documento principal (nivel 0); no nulo = documento
 * relacionado/respuesta (nivel 1, nunca nietos). V2 tiene 4 constructores sobrecargados por
 * compatibilidad histórica — V3 usa uno solo, el completo, como referencia canónica (mismo
 * criterio ya establecido para otros DTOs de V3).
 */
public class DocumentoAnalizadoDTO {

    private final Long idDocumentoAnalizado;
    private final Long idExpediente;
    private final Long idExpedienteAnalisis;
    private final String tipoDocumentoCodigo;
    private final String tipoDocumentoNombre;
    private final String estadoDocumentoCodigo;
    private final String estadoDocumentoNombre;
    private final LocalDate fechaDocumento;
    private final String numeroDocumento;
    private final String descripcion;
    private final boolean notificado;
    private final LocalDate fechaAcuse;
    private final boolean requiereRespuesta;
    private final String confirmacionRespuesta;
    private final LocalDate fechaRespuesta;
    private final String numeroHojaEnvioRespuesta;
    private final boolean requierePublicacion;
    private final LocalDate fechaPublicacion;
    private final String detalleObservacion;
    private final Long idDocumentoPadre;
    private final int nivel;
    private final int orden;
    private final String estadoRespuesta;
    private final boolean activo;
    private final LocalDateTime fechaRegistro;
    private final LocalDateTime fechaModificacion;
    private final Boolean existeOposicion;
    /** `TIPO_DOCUMENTO_ADJUNTO.clasificacion` (`FINAL`/`INTERMEDIO`/`NULL` para informes sin clasificar
     * — ver `CLAUDE.md` raíz, sección Análisis) — 30/08/2026, agregado para que las nested grid de
     * Bandeja de supervisión/Validación (Notificación) puedan filtrar solo cartas intermedias/finales,
     * excluyendo informes/resoluciones sin clasificación. */
    private final String clasificacion;

    public DocumentoAnalizadoDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            Long idExpedienteAnalisis,
            String tipoDocumentoCodigo,
            String tipoDocumentoNombre,
            String estadoDocumentoCodigo,
            String estadoDocumentoNombre,
            LocalDate fechaDocumento,
            String numeroDocumento,
            String descripcion,
            boolean notificado,
            LocalDate fechaAcuse,
            boolean requiereRespuesta,
            String confirmacionRespuesta,
            LocalDate fechaRespuesta,
            String numeroHojaEnvioRespuesta,
            boolean requierePublicacion,
            LocalDate fechaPublicacion,
            String detalleObservacion,
            Long idDocumentoPadre,
            int nivel,
            int orden,
            String estadoRespuesta,
            boolean activo,
            LocalDateTime fechaRegistro,
            LocalDateTime fechaModificacion,
            Boolean existeOposicion,
            String clasificacion) {
        this.idDocumentoAnalizado = idDocumentoAnalizado;
        this.idExpediente = idExpediente;
        this.idExpedienteAnalisis = idExpedienteAnalisis;
        this.tipoDocumentoCodigo = safe(tipoDocumentoCodigo);
        this.tipoDocumentoNombre = safe(tipoDocumentoNombre);
        this.estadoDocumentoCodigo = safe(estadoDocumentoCodigo);
        this.estadoDocumentoNombre = safe(estadoDocumentoNombre);
        this.fechaDocumento = fechaDocumento;
        this.numeroDocumento = safe(numeroDocumento);
        this.descripcion = safe(descripcion);
        this.notificado = notificado;
        this.fechaAcuse = fechaAcuse;
        this.requiereRespuesta = requiereRespuesta;
        this.confirmacionRespuesta = safe(confirmacionRespuesta);
        this.fechaRespuesta = fechaRespuesta;
        this.numeroHojaEnvioRespuesta = safe(numeroHojaEnvioRespuesta);
        this.requierePublicacion = requierePublicacion;
        this.fechaPublicacion = fechaPublicacion;
        this.detalleObservacion = safe(detalleObservacion);
        this.idDocumentoPadre = idDocumentoPadre;
        this.nivel = nivel;
        this.orden = orden;
        this.estadoRespuesta = safe(estadoRespuesta);
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
        this.fechaModificacion = fechaModificacion;
        this.existeOposicion = existeOposicion;
        this.clasificacion = safe(clasificacion);
    }

    public Long getIdDocumentoAnalizado() {
        return idDocumentoAnalizado;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public Long getIdExpedienteAnalisis() {
        return idExpedienteAnalisis;
    }

    public String getTipoDocumentoCodigo() {
        return tipoDocumentoCodigo;
    }

    public String getTipoDocumentoNombre() {
        return tipoDocumentoNombre;
    }

    public String getEstadoDocumentoCodigo() {
        return estadoDocumentoCodigo;
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

    public boolean isNotificado() {
        return notificado;
    }

    public LocalDate getFechaAcuse() {
        return fechaAcuse;
    }

    public boolean isRequiereRespuesta() {
        return requiereRespuesta;
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

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public String getDetalleObservacion() {
        return detalleObservacion;
    }

    public Long getIdDocumentoPadre() {
        return idDocumentoPadre;
    }

    public int getNivel() {
        return nivel;
    }

    public int getOrden() {
        return orden;
    }

    public String getEstadoRespuesta() {
        return estadoRespuesta;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public Boolean getExisteOposicion() {
        return existeOposicion;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
