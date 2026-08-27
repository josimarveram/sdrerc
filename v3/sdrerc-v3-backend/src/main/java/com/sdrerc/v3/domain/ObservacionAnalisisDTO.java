package com.sdrerc.v3.domain;

/** Port literal de com.sdrerc.domain.dto.sdrercapp.ObservacionAnalisisDTO (V2). */
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

    // Nombre deliberadamente fuera de la convención JavaBean get/is (hasDescripcion, no
    // isHasDescripcion) — uso exclusivamente interno en AnalisisValidacionService, no debe
    // serializarse como propiedad JSON (Jackson solo detecta getX/isX automáticamente).
    public boolean hasDescripcion() {
        return !descripcion.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
