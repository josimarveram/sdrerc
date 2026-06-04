package com.sdrerc.domain.dto.sdrercapp;

public class ObservacionVerificacionDTO {

    private final String tipoObservacionCodigo;
    private final String tipoObservacionNombre;
    private final String motivoCorreccionCodigo;
    private final String motivoCorreccionNombre;
    private final String descripcion;

    public ObservacionVerificacionDTO(
            String tipoObservacionCodigo,
            String tipoObservacionNombre,
            String motivoCorreccionCodigo,
            String motivoCorreccionNombre,
            String descripcion) {
        this.tipoObservacionCodigo = safe(tipoObservacionCodigo);
        this.tipoObservacionNombre = safe(tipoObservacionNombre);
        this.motivoCorreccionCodigo = safe(motivoCorreccionCodigo);
        this.motivoCorreccionNombre = safe(motivoCorreccionNombre);
        this.descripcion = safe(descripcion);
    }

    public String getTipoObservacionCodigo() {
        return tipoObservacionCodigo;
    }

    public String getTipoObservacionNombre() {
        return tipoObservacionNombre;
    }

    public String getMotivoCorreccionCodigo() {
        return motivoCorreccionCodigo;
    }

    public String getMotivoCorreccionNombre() {
        return motivoCorreccionNombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean hasDescripcion() {
        return descripcion != null && !descripcion.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
