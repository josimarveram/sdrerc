package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class DatosSolicitudDTO {

    private String numeroTramite;
    private LocalDate fechaRecepcion;
    private String tipoProcedimientoCodigo;
    private String tipoProcedimientoNombre;
    private String tipoDocumentoCodigo;
    private String tipoDocumentoNombre;
    private String canalCodigo;
    private String canalNombre;
    private String prioridad;
    private String validacionInicial;
    private String observacionInicial;

    public String getNumeroTramite() {
        return numeroTramite;
    }

    public void setNumeroTramite(String numeroTramite) {
        this.numeroTramite = trimToNull(numeroTramite);
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(LocalDate fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getTipoProcedimientoCodigo() {
        return tipoProcedimientoCodigo;
    }

    public void setTipoProcedimientoCodigo(String tipoProcedimientoCodigo) {
        this.tipoProcedimientoCodigo = trimToNull(tipoProcedimientoCodigo);
    }

    public String getTipoProcedimientoNombre() {
        return tipoProcedimientoNombre;
    }

    public void setTipoProcedimientoNombre(String tipoProcedimientoNombre) {
        this.tipoProcedimientoNombre = trimToNull(tipoProcedimientoNombre);
    }

    public String getTipoDocumentoCodigo() {
        return tipoDocumentoCodigo;
    }

    public void setTipoDocumentoCodigo(String tipoDocumentoCodigo) {
        this.tipoDocumentoCodigo = trimToNull(tipoDocumentoCodigo);
    }

    public String getTipoDocumentoNombre() {
        return tipoDocumentoNombre;
    }

    public void setTipoDocumentoNombre(String tipoDocumentoNombre) {
        this.tipoDocumentoNombre = trimToNull(tipoDocumentoNombre);
    }

    public String getCanalCodigo() {
        return canalCodigo;
    }

    public void setCanalCodigo(String canalCodigo) {
        this.canalCodigo = trimToNull(canalCodigo);
    }

    public String getCanalNombre() {
        return canalNombre;
    }

    public void setCanalNombre(String canalNombre) {
        this.canalNombre = trimToNull(canalNombre);
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = trimToNull(prioridad);
    }

    public String getValidacionInicial() {
        return validacionInicial;
    }

    public void setValidacionInicial(String validacionInicial) {
        this.validacionInicial = trimToNull(validacionInicial);
    }

    public String getObservacionInicial() {
        return observacionInicial;
    }

    public void setObservacionInicial(String observacionInicial) {
        this.observacionInicial = trimToNull(observacionInicial);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
