package com.sdrerc.domain.dto.sdrercapp;

public class RegistroManualExpedienteDTO {

    private DatosSolicitudDTO solicitud = new DatosSolicitudDTO();
    private DatosActaDTO acta = new DatosActaDTO();
    private DatosPersonaRegistroDTO titular = new DatosPersonaRegistroDTO();
    private DatosPersonaRegistroDTO remitente = new DatosPersonaRegistroDTO();
    private String observacionesGenerales;
    private String numeroExpedienteVistaPrevia;
    private boolean posibleDuplicado;
    private String motivoDuplicado;

    public DatosSolicitudDTO getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(DatosSolicitudDTO solicitud) {
        this.solicitud = solicitud == null ? new DatosSolicitudDTO() : solicitud;
    }

    public DatosActaDTO getActa() {
        return acta;
    }

    public void setActa(DatosActaDTO acta) {
        this.acta = acta == null ? new DatosActaDTO() : acta;
    }

    public DatosPersonaRegistroDTO getTitular() {
        return titular;
    }

    public void setTitular(DatosPersonaRegistroDTO titular) {
        this.titular = titular == null ? new DatosPersonaRegistroDTO() : titular;
    }

    public DatosPersonaRegistroDTO getRemitente() {
        return remitente;
    }

    public void setRemitente(DatosPersonaRegistroDTO remitente) {
        this.remitente = remitente == null ? new DatosPersonaRegistroDTO() : remitente;
    }

    public String getObservacionesGenerales() {
        return observacionesGenerales;
    }

    public void setObservacionesGenerales(String observacionesGenerales) {
        this.observacionesGenerales = trimToNull(observacionesGenerales);
    }

    public String getNumeroExpedienteVistaPrevia() {
        return numeroExpedienteVistaPrevia;
    }

    public void setNumeroExpedienteVistaPrevia(String numeroExpedienteVistaPrevia) {
        this.numeroExpedienteVistaPrevia = trimToNull(numeroExpedienteVistaPrevia);
    }

    public boolean isPosibleDuplicado() {
        return posibleDuplicado;
    }

    public void setPosibleDuplicado(boolean posibleDuplicado) {
        this.posibleDuplicado = posibleDuplicado;
    }

    public String getMotivoDuplicado() {
        return motivoDuplicado;
    }

    public void setMotivoDuplicado(String motivoDuplicado) {
        this.motivoDuplicado = trimToNull(motivoDuplicado);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
