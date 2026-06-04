package com.sdrerc.domain.dto.sdrercapp;

public class EjecucionReversionDTO {

    private final Long idExpediente;
    private final String accionCodigo;
    private final String tipoObservacionCodigo;
    private final String motivoCorreccionCodigo;
    private final String motivoReversion;
    private final String comentario;

    public EjecucionReversionDTO(
            Long idExpediente,
            String accionCodigo,
            String tipoObservacionCodigo,
            String motivoCorreccionCodigo,
            String motivoReversion,
            String comentario) {
        this.idExpediente = idExpediente;
        this.accionCodigo = safe(accionCodigo);
        this.tipoObservacionCodigo = safe(tipoObservacionCodigo);
        this.motivoCorreccionCodigo = safe(motivoCorreccionCodigo);
        this.motivoReversion = safe(motivoReversion);
        this.comentario = safe(comentario);
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getAccionCodigo() {
        return accionCodigo;
    }

    public String getTipoObservacionCodigo() {
        return tipoObservacionCodigo;
    }

    public String getMotivoCorreccionCodigo() {
        return motivoCorreccionCodigo;
    }

    public String getMotivoReversion() {
        return motivoReversion;
    }

    public String getComentario() {
        return comentario;
    }

    public boolean hasMotivo() {
        return motivoReversion != null && !motivoReversion.trim().isEmpty();
    }

    public boolean hasComentario() {
        return comentario != null && !comentario.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
