package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class DatosSolicitudDTO {

    private String numeroTramite;
    private String numeroDocumento;
    private String numeroExpedienteSgd;
    private LocalDate fechaRecepcion;
    private String tipoSolicitudCodigo;
    private String tipoSolicitudNombre;
    private String tipoProcedimientoCodigo;
    private String tipoProcedimientoNombre;
    private String tipoDocumentoCodigo;
    private String tipoDocumentoNombre;
    private String canalCodigo;
    private String canalNombre;
    private String prioridad;
    private String validacionInicial;
    private String hojaEnvio;
    private String observacionInicial;
    private boolean grupoFamiliar;
    private String criterioGrupoFamiliar;
    private String observacionGrupoFamiliar;

    public String getNumeroTramite() {
        return numeroTramite;
    }

    public void setNumeroTramite(String numeroTramite) {
        this.numeroTramite = trimToNull(numeroTramite);
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = trimToNull(numeroDocumento);
    }

    public String getNumeroExpedienteSgd() {
        return numeroExpedienteSgd;
    }

    public void setNumeroExpedienteSgd(String numeroExpedienteSgd) {
        this.numeroExpedienteSgd = trimToNull(numeroExpedienteSgd);
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(LocalDate fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getTipoSolicitudCodigo() {
        return tipoSolicitudCodigo;
    }

    public void setTipoSolicitudCodigo(String tipoSolicitudCodigo) {
        this.tipoSolicitudCodigo = trimToNull(tipoSolicitudCodigo);
    }

    public String getTipoSolicitudNombre() {
        return tipoSolicitudNombre;
    }

    public void setTipoSolicitudNombre(String tipoSolicitudNombre) {
        this.tipoSolicitudNombre = normalizarTipoSolicitud(tipoSolicitudNombre);
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

    public String getHojaEnvio() {
        return hojaEnvio;
    }

    public void setHojaEnvio(String hojaEnvio) {
        this.hojaEnvio = trimToNull(hojaEnvio);
    }

    public String getObservacionInicial() {
        return observacionInicial;
    }

    public void setObservacionInicial(String observacionInicial) {
        this.observacionInicial = trimToNull(observacionInicial);
    }

    public boolean isGrupoFamiliar() {
        return grupoFamiliar;
    }

    public void setGrupoFamiliar(boolean grupoFamiliar) {
        this.grupoFamiliar = grupoFamiliar;
    }

    public String getCriterioGrupoFamiliar() {
        return criterioGrupoFamiliar;
    }

    public void setCriterioGrupoFamiliar(String criterioGrupoFamiliar) {
        this.criterioGrupoFamiliar = trimToNull(criterioGrupoFamiliar);
    }

    public String getObservacionGrupoFamiliar() {
        return observacionGrupoFamiliar;
    }

    public void setObservacionGrupoFamiliar(String observacionGrupoFamiliar) {
        this.observacionGrupoFamiliar = trimToNull(observacionGrupoFamiliar);
    }

    public boolean isPosibleGrupoFamiliar() {
        return !grupoFamiliar && (hasText(criterioGrupoFamiliar) || hasText(observacionGrupoFamiliar));
    }

    public String getGrupoFamiliarTexto() {
        return grupoFamiliar ? "Sí" : "No";
    }

    public void limpiarDeteccionGrupoFamiliar() {
        if (criterioGrupoFamiliar != null && criterioGrupoFamiliar.startsWith("COINCIDENCIA_APELLIDOS")) {
            criterioGrupoFamiliar = null;
        }
        if (observacionGrupoFamiliar != null) {
            StringBuilder limpio = new StringBuilder();
            String[] partes = observacionGrupoFamiliar.split("\\|");
            for (String parte : partes) {
                String texto = trimToNull(parte);
                if (texto == null || texto.startsWith("Posible grupo familiar")) {
                    continue;
                }
                if (limpio.length() > 0) {
                    limpio.append(" | ");
                }
                limpio.append(texto);
            }
            observacionGrupoFamiliar = limpio.length() == 0 ? null : limpio.toString();
        }
    }

    public void agregarObservacionGrupoFamiliar(String criterio, String observacion) {
        if (!hasText(criterioGrupoFamiliar)) {
            setCriterioGrupoFamiliar(criterio);
        }
        String texto = trimToNull(observacion);
        if (texto == null) {
            return;
        }
        if (observacionGrupoFamiliar == null || observacionGrupoFamiliar.trim().isEmpty()) {
            observacionGrupoFamiliar = texto;
        } else if (!observacionGrupoFamiliar.contains(texto)) {
            observacionGrupoFamiliar = observacionGrupoFamiliar + " | " + texto;
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizarTipoSolicitud(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        if ("PARTE".equalsIgnoreCase(trimmed)) {
            return "Parte";
        }
        if ("OFICIO".equalsIgnoreCase(trimmed)) {
            return "Oficio";
        }
        return trimmed;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
