package com.sdrerc.v3.domain;

/** Port literal de com.sdrerc.domain.dto.sdrercapp.RegistroManualResultadoDTO (V2). */
public class RegistroManualResultadoDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String mensaje;

    public RegistroManualResultadoDTO(Long idExpediente, String numeroExpediente, String mensaje) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = numeroExpediente == null ? "" : numeroExpediente;
        this.mensaje = mensaje == null ? "" : mensaje;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public String getMensaje() {
        return mensaje;
    }
}
