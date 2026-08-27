package com.sdrerc.v3.domain;

/**
 * Port literal de com.sdrerc.domain.dto.sdrercapp.ObservacionVerificacionDTO (V2). En el flujo real
 * y vigente (rama OBSERVADO de "Registrar Verificación"), V2 siempre construye este objeto con
 * {@code tipoObservacionCodigo}/{@code tipoObservacionNombre}/{@code motivoCorreccionCodigo}/
 * {@code motivoCorreccionNombre} vacíos (los combos {@code cmbTipoObservacion}/
 * {@code cmbMotivoCorreccion} existen en el código pero nunca se agregan a un panel visible, ver
 * v3/CLAUDE.md) — solo {@code descripcion} (el comentario) llega con contenido real. Se porta la
 * clase completa por fidelidad de forma, no porque V3 vaya a poblar esos 4 campos en este
 * incremento.
 */
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

    // Nombre deliberadamente fuera de la convención JavaBean get/is (mismo criterio que
    // ObservacionAnalisisDTO) — uso interno de validación, no debe serializarse como propiedad JSON.
    public boolean hasDescripcion() {
        return !descripcion.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
