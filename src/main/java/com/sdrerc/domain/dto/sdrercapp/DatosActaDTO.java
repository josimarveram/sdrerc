package com.sdrerc.domain.dto.sdrercapp;

public class DatosActaDTO {

    private String tipoActaCodigo;
    private String tipoActaNombre;
    private String numeroActa;
    private Integer anioActa;
    private String ubicacionRegistral;
    private String origenRegistral;
    private String observacion;

    public String getTipoActaCodigo() {
        return tipoActaCodigo;
    }

    public void setTipoActaCodigo(String tipoActaCodigo) {
        this.tipoActaCodigo = trimToNull(tipoActaCodigo);
    }

    public String getTipoActaNombre() {
        return tipoActaNombre;
    }

    public void setTipoActaNombre(String tipoActaNombre) {
        this.tipoActaNombre = trimToNull(tipoActaNombre);
    }

    public String getNumeroActa() {
        return numeroActa;
    }

    public void setNumeroActa(String numeroActa) {
        this.numeroActa = trimToNull(numeroActa);
    }

    public Integer getAnioActa() {
        return anioActa;
    }

    public void setAnioActa(Integer anioActa) {
        this.anioActa = anioActa;
    }

    public String getUbicacionRegistral() {
        return ubicacionRegistral;
    }

    public void setUbicacionRegistral(String ubicacionRegistral) {
        this.ubicacionRegistral = trimToNull(ubicacionRegistral);
    }

    public String getOrigenRegistral() {
        return origenRegistral;
    }

    public void setOrigenRegistral(String origenRegistral) {
        this.origenRegistral = trimToNull(origenRegistral);
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
