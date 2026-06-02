package com.sdrerc.domain.dto.sdrercapp;

public class DatosPersonaRegistroDTO {

    private String nombreCompleto;
    private String tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private String correo;
    private String direccion;
    private String tipoRemitente;
    private String observacion;

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = trimToNull(nombreCompleto);
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = trimToNull(tipoDocumento);
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = trimToNull(numeroDocumento);
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = trimToNull(telefono);
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = trimToNull(correo);
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = trimToNull(direccion);
    }

    public String getTipoRemitente() {
        return tipoRemitente;
    }

    public void setTipoRemitente(String tipoRemitente) {
        this.tipoRemitente = trimToNull(tipoRemitente);
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = trimToNull(observacion);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
