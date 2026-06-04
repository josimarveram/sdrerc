package com.sdrerc.domain.dto.sdrercapp;

public class VerificacionRegistroDTO {

    private final Long idExpediente;
    private final String accionCodigo;
    private final String resultadoNombre;
    private final String comentario;
    private final ObservacionVerificacionDTO observacion;

    public VerificacionRegistroDTO(
            Long idExpediente,
            String accionCodigo,
            String resultadoNombre,
            String comentario,
            ObservacionVerificacionDTO observacion) {
        this.idExpediente = idExpediente;
        this.accionCodigo = safe(accionCodigo);
        this.resultadoNombre = safe(resultadoNombre);
        this.comentario = safe(comentario);
        this.observacion = observacion;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getAccionCodigo() {
        return accionCodigo;
    }

    public String getResultadoNombre() {
        return resultadoNombre;
    }

    public String getComentario() {
        return comentario;
    }

    public ObservacionVerificacionDTO getObservacion() {
        return observacion;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
