package com.sdrerc.domain.dto.sdrercapp;

public class CierreExpedienteDTO {

    private final Long idExpediente;
    private final String accionCodigo;
    private final String motivo;
    private final String comentario;

    public CierreExpedienteDTO(Long idExpediente, String accionCodigo, String motivo, String comentario) {
        this.idExpediente = idExpediente;
        this.accionCodigo = safe(accionCodigo);
        this.motivo = safe(motivo);
        this.comentario = safe(comentario);
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getAccionCodigo() {
        return accionCodigo;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getComentario() {
        return comentario;
    }

    public CierreArchivoRegistroDTO toRegistro() {
        return new CierreArchivoRegistroDTO(idExpediente, accionCodigo, motivo, comentario);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
