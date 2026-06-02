package com.sdrerc.domain.dto.sdrercapp;

public class DatosNotificacionDTO {

    private String tipoNotificacionCodigo;
    private String tipoNotificacionNombre;
    private String correo;
    private String telefono;
    private String direccion;
    private String distrito;
    private String provincia;
    private String departamento;
    private String referenciaDireccion;
    private String personaContacto;
    private String observacion;

    public String getTipoNotificacionCodigo() {
        return tipoNotificacionCodigo;
    }

    public void setTipoNotificacionCodigo(String tipoNotificacionCodigo) {
        this.tipoNotificacionCodigo = trimToNull(tipoNotificacionCodigo);
    }

    public String getTipoNotificacionNombre() {
        return tipoNotificacionNombre;
    }

    public void setTipoNotificacionNombre(String tipoNotificacionNombre) {
        this.tipoNotificacionNombre = trimToNull(tipoNotificacionNombre);
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = trimToNull(correo);
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = trimToNull(telefono);
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = trimToNull(direccion);
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = trimToNull(distrito);
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = trimToNull(provincia);
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = trimToNull(departamento);
    }

    public String getReferenciaDireccion() {
        return referenciaDireccion;
    }

    public void setReferenciaDireccion(String referenciaDireccion) {
        this.referenciaDireccion = trimToNull(referenciaDireccion);
    }

    public String getPersonaContacto() {
        return personaContacto;
    }

    public void setPersonaContacto(String personaContacto) {
        this.personaContacto = trimToNull(personaContacto);
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = trimToNull(observacion);
    }

    public boolean requiereVirtual() {
        return "VIRTUAL".equalsIgnoreCase(tipoNotificacionCodigo)
                || "AMBAS".equalsIgnoreCase(tipoNotificacionCodigo);
    }

    public boolean requiereFisica() {
        return "PRESENCIAL_1".equalsIgnoreCase(tipoNotificacionCodigo)
                || "AMBAS".equalsIgnoreCase(tipoNotificacionCodigo);
    }

    public boolean requiereRegistroNotificacion() {
        return requiereVirtual() || requiereFisica();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
