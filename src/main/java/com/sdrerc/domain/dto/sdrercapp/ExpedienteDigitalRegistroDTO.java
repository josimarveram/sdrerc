package com.sdrerc.domain.dto.sdrercapp;

public class ExpedienteDigitalRegistroDTO {

    private final Long idExpediente;
    private final String accionCodigo;
    private final String codigoExpedienteDigital;
    private final String rutaCarpeta;
    private final String enlaceCarpeta;
    private final String comentario;

    public ExpedienteDigitalRegistroDTO(
            Long idExpediente,
            String accionCodigo,
            String codigoExpedienteDigital,
            String rutaCarpeta,
            String enlaceCarpeta,
            String comentario) {
        this.idExpediente = idExpediente;
        this.accionCodigo = safe(accionCodigo);
        this.codigoExpedienteDigital = safe(codigoExpedienteDigital);
        this.rutaCarpeta = safe(rutaCarpeta);
        this.enlaceCarpeta = safe(enlaceCarpeta);
        this.comentario = safe(comentario);
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getAccionCodigo() {
        return accionCodigo;
    }

    public String getCodigoExpedienteDigital() {
        return codigoExpedienteDigital;
    }

    public String getRutaCarpeta() {
        return rutaCarpeta;
    }

    public String getEnlaceCarpeta() {
        return enlaceCarpeta;
    }

    public String getComentario() {
        return comentario;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
