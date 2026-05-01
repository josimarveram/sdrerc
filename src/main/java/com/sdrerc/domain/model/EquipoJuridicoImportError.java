package com.sdrerc.domain.model;

public class EquipoJuridicoImportError {

    private final String severidad;
    private final String mensaje;

    public EquipoJuridicoImportError(String severidad, String mensaje) {
        this.severidad = severidad;
        this.mensaje = mensaje;
    }

    public String getSeveridad() {
        return severidad;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean isError() {
        return "ERROR".equalsIgnoreCase(severidad);
    }

    public boolean isAdvertencia() {
        return "ADVERTENCIA".equalsIgnoreCase(severidad);
    }

    @Override
    public String toString() {
        return severidad + ": " + mensaje;
    }
}
