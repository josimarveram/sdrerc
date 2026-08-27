package com.sdrerc.v3.domain;

import java.time.LocalDateTime;

/** Port literal de {@code com.sdrerc.domain.dto.sdrercapp.PlantillaDocumentoDTO} (V2). */
public class PlantillaDocumentoAdminDTO {

    private Long idPlantillaDocumento;
    private Long idTipoDocumentoAdjunto;
    private String tipoDocumentoCodigo;
    private String tipoDocumentoNombre;
    private long version;
    private String nombreArchivo;
    private long tamanoBytes;
    private String comentario;
    private boolean activo;
    private String cargadoPor;
    private LocalDateTime creadoEn;

    public Long getIdPlantillaDocumento() {
        return idPlantillaDocumento;
    }

    public void setIdPlantillaDocumento(Long idPlantillaDocumento) {
        this.idPlantillaDocumento = idPlantillaDocumento;
    }

    public Long getIdTipoDocumentoAdjunto() {
        return idTipoDocumentoAdjunto;
    }

    public void setIdTipoDocumentoAdjunto(Long idTipoDocumentoAdjunto) {
        this.idTipoDocumentoAdjunto = idTipoDocumentoAdjunto;
    }

    public String getTipoDocumentoCodigo() {
        return tipoDocumentoCodigo;
    }

    public void setTipoDocumentoCodigo(String tipoDocumentoCodigo) {
        this.tipoDocumentoCodigo = tipoDocumentoCodigo;
    }

    public String getTipoDocumentoNombre() {
        return tipoDocumentoNombre;
    }

    public void setTipoDocumentoNombre(String tipoDocumentoNombre) {
        this.tipoDocumentoNombre = tipoDocumentoNombre;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getCargadoPor() {
        return cargadoPor;
    }

    public void setCargadoPor(String cargadoPor) {
        this.cargadoPor = cargadoPor;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
