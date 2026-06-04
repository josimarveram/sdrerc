package com.sdrerc.domain.dto.sdrercapp;

public class ObservacionAnalisisDTO {

    private final String tipoObservacionCodigo;
    private final String tipoObservacionNombre;
    private final String descripcion;

    public ObservacionAnalisisDTO(String tipoObservacionCodigo, String tipoObservacionNombre, String descripcion) {
        this.tipoObservacionCodigo = safe(tipoObservacionCodigo);
        this.tipoObservacionNombre = safe(tipoObservacionNombre);
        this.descripcion = safe(descripcion);
    }

    public String getTipoObservacionCodigo() {
        return tipoObservacionCodigo;
    }

    public String getTipoObservacionNombre() {
        return tipoObservacionNombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean hasDescripcion() {
        return !descripcion.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
