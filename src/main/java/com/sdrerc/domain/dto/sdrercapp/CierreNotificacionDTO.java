package com.sdrerc.domain.dto.sdrercapp;

public class CierreNotificacionDTO {

    private final Long idExpediente;
    private final String accionCodigo;
    private final String comentario;

    public CierreNotificacionDTO(Long idExpediente, String accionCodigo, String comentario) {
        this.idExpediente = idExpediente;
        this.accionCodigo = safe(accionCodigo);
        this.comentario = safe(comentario);
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getAccionCodigo() {
        return accionCodigo;
    }

    public String getComentario() {
        return comentario;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
