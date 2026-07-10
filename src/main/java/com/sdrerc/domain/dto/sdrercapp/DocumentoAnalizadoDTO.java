package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private final String usuarioRegistro;
    private final LocalDateTime fechaRegistro;
    private final String usuarioModificacion;
    private final LocalDateTime fechaModificacion;
    private final Boolean existeOposicion;

    public DocumentoAnalizadoDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String tipoDocumentoCodigo,
            String tipoDocumentoNombre,
            String estadoDocumentoCodigo,
            String estadoDocumentoNombre,
            LocalDate fechaDocumento,
            String descripcion) {
        this(
                idDocumentoAnalizado,
                idExpediente,
                tipoDocumentoCodigo,
                tipoDocumentoNombre,
                estadoDocumentoCodigo,
                estadoDocumentoNombre,
                fechaDocumento,
                "",
                descripcion,
                false,
                null,
                false,
                "",
                null,
                "",
                false,
                null,
                "");
    }

    public DocumentoAnalizadoDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String tipoDocumentoCodigo,
            String tipoDocumentoNombre,
            String estadoDocumentoCodigo,
            String estadoDocumentoNombre,
            LocalDate fechaDocumento,
            String numeroDocumento,
            String descripcion) {
        this(
                idDocumentoAnalizado,
                idExpediente,
                tipoDocumentoCodigo,
                tipoDocumentoNombre,
                estadoDocumentoCodigo,
                estadoDocumentoNombre,
                fechaDocumento,
                numeroDocumento,
                descripcion,
                false,
                null,
                false,
                "",
                null,
                "",
                false,
                null,
                "");
    }

    public DocumentoAnalizadoDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
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
            String numeroHojaEnvioRespuesta) {
        this(
                idDocumentoAnalizado,
                idExpediente,
                tipoDocumentoCodigo,
                tipoDocumentoNombre,
                estadoDocumentoCodigo,
                estadoDocumentoNombre,
                fechaDocumento,
                numeroDocumento,
                descripcion,
                notificado,
                fechaAcuse,
                requiereRespuesta,
                confirmacionRespuesta,
                fechaRespuesta,
                numeroHojaEnvioRespuesta,
                false,
                null,
                "");
    }

    public DocumentoAnalizadoDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String tipoDocumentoCodigo,
            String tipoDocumentoNombre,
            String estadoDocumentoCodigo,
            String estadoDocumentoNombre,
            LocalDate fechaDocumento,
            String descripcion,
            boolean notificado,
            LocalDate fechaAcuse,
            boolean requiereRespuesta,
            String confirmacionRespuesta,
            LocalDate fechaRespuesta,
            String numeroHojaEnvioRespuesta,
            boolean requierePublicacion,
            LocalDate fechaPublicacion) {
        this(
                idDocumentoAnalizado,
                idExpediente,
                tipoDocumentoCodigo,
                tipoDocumentoNombre,
                estadoDocumentoCodigo,
                estadoDocumentoNombre,
                fechaDocumento,
                "",
                descripcion,
                notificado,
                fechaAcuse,
                requiereRespuesta,
                confirmacionRespuesta,
                fechaRespuesta,
                numeroHojaEnvioRespuesta,
                requierePublicacion,
                fechaPublicacion,
                "");
    }

    public DocumentoAnalizadoDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
            String tipoDocumentoCodigo,
            String tipoDocumentoNombre,
            String estadoDocumentoCodigo,
            String estadoDocumentoNombre,
            LocalDate fechaDocumento,
            String descripcion,
            boolean notificado,
            LocalDate fechaAcuse,
            boolean requiereRespuesta,
            String confirmacionRespuesta,
            LocalDate fechaRespuesta,
            String numeroHojaEnvioRespuesta) {
        this(
                idDocumentoAnalizado,
                idExpediente,
                tipoDocumentoCodigo,
                tipoDocumentoNombre,
                estadoDocumentoCodigo,
                estadoDocumentoNombre,
                fechaDocumento,
                "",
                descripcion,
                notificado,
                fechaAcuse,
                requiereRespuesta,
                confirmacionRespuesta,
                fechaRespuesta,
                numeroHojaEnvioRespuesta,
                false,
                null,
                "");
    }

    public DocumentoAnalizadoDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
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
            LocalDate fechaPublicacion) {
        this(
                idDocumentoAnalizado,
                idExpediente,
                tipoDocumentoCodigo,
                tipoDocumentoNombre,
                estadoDocumentoCodigo,
                estadoDocumentoNombre,
                fechaDocumento,
                numeroDocumento,
                descripcion,
                notificado,
                fechaAcuse,
                requiereRespuesta,
                confirmacionRespuesta,
                fechaRespuesta,
                numeroHojaEnvioRespuesta,
                requierePublicacion,
                fechaPublicacion,
                "");
    }

    public DocumentoAnalizadoDTO(
            Long idDocumentoAnalizado,
            Long idExpediente,
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
            String detalleObservacion) {
        this(
                idDocumentoAnalizado,
                idExpediente,
                null,
                tipoDocumentoCodigo,
                tipoDocumentoNombre,
                estadoDocumentoCodigo,
                estadoDocumentoNombre,
                fechaDocumento,
                numeroDocumento,
                descripcion,
                notificado,
                fechaAcuse,
                requiereRespuesta,
                confirmacionRespuesta,
                fechaRespuesta,
                numeroHojaEnvioRespuesta,
                requierePublicacion,
                fechaPublicacion,
                detalleObservacion,
                null,
                0,
                0,
                "",
                true,
                "",
                null,
                "",
                null,
                null);
    }

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
            String detalleObservacion) {
        this(
                idDocumentoAnalizado,
                idExpediente,
                idExpedienteAnalisis,
                tipoDocumentoCodigo,
                tipoDocumentoNombre,
                estadoDocumentoCodigo,
                estadoDocumentoNombre,
                fechaDocumento,
                numeroDocumento,
                descripcion,
                notificado,
                fechaAcuse,
                requiereRespuesta,
                confirmacionRespuesta,
                fechaRespuesta,
                numeroHojaEnvioRespuesta,
                requierePublicacion,
                fechaPublicacion,
                detalleObservacion,
                null,
                0,
                0,
                "",
                true,
                "",
                null,
                "",
                null,
                null);
    }

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
            String usuarioRegistro,
            LocalDateTime fechaRegistro,
            String usuarioModificacion,
            LocalDateTime fechaModificacion,
            Boolean existeOposicion) {
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
        this.usuarioRegistro = safe(usuarioRegistro);
        this.fechaRegistro = fechaRegistro;
        this.usuarioModificacion = safe(usuarioModificacion);
        this.fechaModificacion = fechaModificacion;
        this.existeOposicion = existeOposicion;
    }

    public static DocumentoAnalizadoDTO nuevo(
            String tipoDocumentoCodigo,
            String tipoDocumentoNombre,
            String estadoDocumentoCodigo,
            String estadoDocumentoNombre,
            LocalDate fechaDocumento,
            String descripcion) {
        return new DocumentoAnalizadoDTO(
                null,
                null,
                tipoDocumentoCodigo,
                tipoDocumentoNombre,
                estadoDocumentoCodigo,
                estadoDocumentoNombre,
                fechaDocumento,
                "",
                descripcion);
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

    public String getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public String getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public Boolean getExisteOposicion() {
        return existeOposicion;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
