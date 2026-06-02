package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;

public class CargaDiariaPreviewDTO {

    private int fila;
    private String numeroTramite;
    private String tipoProcedimiento;
    private String tipoDocumento;
    private String acta;
    private String titular;
    private String remitente;
    private LocalDate fechaRecepcion;
    private String fechaRecepcionTexto;
    private String observacionInicial;
    private String estadoValidacion;
    private String mensajeValidacion;
    private boolean posibleDuplicado;
    private String motivoDuplicado;
    private String numeroExpedienteGenerado;
    private boolean listoParaRegistrar;
    private boolean registrado;
    private Long idExpedienteRegistrado;

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public String getNumeroTramite() {
        return numeroTramite;
    }

    public void setNumeroTramite(String numeroTramite) {
        this.numeroTramite = trimToNull(numeroTramite);
    }

    public String getTipoProcedimiento() {
        return tipoProcedimiento;
    }

    public void setTipoProcedimiento(String tipoProcedimiento) {
        this.tipoProcedimiento = trimToNull(tipoProcedimiento);
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = trimToNull(tipoDocumento);
    }

    public String getActa() {
        return acta;
    }

    public void setActa(String acta) {
        this.acta = trimToNull(acta);
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = trimToNull(titular);
    }

    public String getRemitente() {
        return remitente;
    }

    public void setRemitente(String remitente) {
        this.remitente = trimToNull(remitente);
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(LocalDate fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getFechaRecepcionTexto() {
        return fechaRecepcionTexto;
    }

    public void setFechaRecepcionTexto(String fechaRecepcionTexto) {
        this.fechaRecepcionTexto = trimToNull(fechaRecepcionTexto);
    }

    public String getObservacionInicial() {
        return observacionInicial;
    }

    public void setObservacionInicial(String observacionInicial) {
        this.observacionInicial = trimToNull(observacionInicial);
    }

    public String getEstadoValidacion() {
        return estadoValidacion;
    }

    public void setEstadoValidacion(String estadoValidacion) {
        this.estadoValidacion = trimToNull(estadoValidacion);
    }

    public String getMensajeValidacion() {
        return mensajeValidacion;
    }

    public void setMensajeValidacion(String mensajeValidacion) {
        this.mensajeValidacion = trimToNull(mensajeValidacion);
    }

    public boolean isPosibleDuplicado() {
        return posibleDuplicado;
    }

    public void setPosibleDuplicado(boolean posibleDuplicado) {
        this.posibleDuplicado = posibleDuplicado;
    }

    public String getMotivoDuplicado() {
        return motivoDuplicado;
    }

    public void setMotivoDuplicado(String motivoDuplicado) {
        this.motivoDuplicado = trimToNull(motivoDuplicado);
    }

    public String getNumeroExpedienteGenerado() {
        return numeroExpedienteGenerado;
    }

    public void setNumeroExpedienteGenerado(String numeroExpedienteGenerado) {
        this.numeroExpedienteGenerado = trimToNull(numeroExpedienteGenerado);
    }

    public boolean isListoParaRegistrar() {
        return listoParaRegistrar;
    }

    public void setListoParaRegistrar(boolean listoParaRegistrar) {
        this.listoParaRegistrar = listoParaRegistrar;
    }

    public boolean isRegistrado() {
        return registrado;
    }

    public void setRegistrado(boolean registrado) {
        this.registrado = registrado;
    }

    public Long getIdExpedienteRegistrado() {
        return idExpedienteRegistrado;
    }

    public void setIdExpedienteRegistrado(Long idExpedienteRegistrado) {
        this.idExpedienteRegistrado = idExpedienteRegistrado;
    }

    public void reiniciarValidacion() {
        estadoValidacion = "Pendiente";
        mensajeValidacion = null;
        posibleDuplicado = false;
        motivoDuplicado = null;
        numeroExpedienteGenerado = null;
        listoParaRegistrar = false;
        registrado = false;
        idExpedienteRegistrado = null;
    }

    public void agregarMensaje(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return;
        }
        if (mensajeValidacion == null || mensajeValidacion.trim().isEmpty()) {
            mensajeValidacion = mensaje.trim();
        } else {
            mensajeValidacion = mensajeValidacion + " | " + mensaje.trim();
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
