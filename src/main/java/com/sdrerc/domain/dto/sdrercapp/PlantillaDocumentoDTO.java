package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDateTime;

public class PlantillaDocumentoDTO {

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

    public PlantillaDocumentoDTO() {
    }

    public PlantillaDocumentoDTO(
            Long idPlantillaDocumento,
            Long idTipoDocumentoAdjunto,
            String tipoDocumentoCodigo,
            String tipoDocumentoNombre,
            long version,
            String nombreArchivo,
            long tamanoBytes,
            String comentario,
            boolean activo,
            String cargadoPor,
            LocalDateTime creadoEn) {
        this.idPlantillaDocumento = idPlantillaDocumento;
        this.idTipoDocumentoAdjunto = idTipoDocumentoAdjunto;
        this.tipoDocumentoCodigo = tipoDocumentoCodigo;
        this.tipoDocumentoNombre = tipoDocumentoNombre;
        this.version = version;
        this.nombreArchivo = nombreArchivo;
        this.tamanoBytes = tamanoBytes;
        this.comentario = comentario;
        this.activo = activo;
        this.cargadoPor = cargadoPor;
        this.creadoEn = creadoEn;
    }

    public Long getIdPlantillaDocumento() {
        return idPlantillaDocumento;
    }

    public Long getIdTipoDocumentoAdjunto() {
        return idTipoDocumentoAdjunto;
    }

    public String getTipoDocumentoCodigo() {
        return tipoDocumentoCodigo;
    }

    public String getTipoDocumentoNombre() {
        return tipoDocumentoNombre;
    }

    public long getVersion() {
        return version;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public String getComentario() {
        return comentario;
    }

    public boolean isActivo() {
        return activo;
    }

    public String getCargadoPor() {
        return cargadoPor;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
}
