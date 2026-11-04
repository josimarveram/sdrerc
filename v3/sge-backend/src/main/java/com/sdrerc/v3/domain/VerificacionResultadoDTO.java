package com.sdrerc.v3.domain;

/** Mismo shape que {@code AnalisisResultadoDTO} — resultado de una acción de Verificación. */
public class VerificacionResultadoDTO {

    private final Long idExpediente;
    private final String numeroExpediente;
    private final String accionCodigo;
    private final String etapaDestinoCodigo;
    private final String estadoDestinoCodigo;
    private final String mensaje;

    public VerificacionResultadoDTO(
            Long idExpediente,
            String numeroExpediente,
            String accionCodigo,
            String etapaDestinoCodigo,
            String estadoDestinoCodigo,
            String mensaje) {
        this.idExpediente = idExpediente;
        this.numeroExpediente = numeroExpediente == null ? "" : numeroExpediente;
        this.accionCodigo = accionCodigo == null ? "" : accionCodigo;
        this.etapaDestinoCodigo = etapaDestinoCodigo == null ? "" : etapaDestinoCodigo;
        this.estadoDestinoCodigo = estadoDestinoCodigo == null ? "" : estadoDestinoCodigo;
        this.mensaje = mensaje == null ? "" : mensaje;
    }

    public Long getIdExpediente() {
        return idExpediente;
    }

    public String getNumeroExpediente() {
        return numeroExpediente;
    }

    public String getAccionCodigo() {
        return accionCodigo;
    }

    public String getEtapaDestinoCodigo() {
        return etapaDestinoCodigo;
    }

    public String getEstadoDestinoCodigo() {
        return estadoDestinoCodigo;
    }

    public String getMensaje() {
        return mensaje;
    }
}
