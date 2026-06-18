package com.sdrerc.domain.dto.sdrercapp;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FeriadoNacionalDTO {

    private Long idFeriado;
    private LocalDate fecha;
    private String nombre;
    private String tipo;
    private boolean activo;
    private String observacion;
    private LocalDateTime creadoEn;
    private LocalDateTime modificadoEn;

    public FeriadoNacionalDTO() {
        this.tipo = "NACIONAL";
        this.activo = true;
    }

    public FeriadoNacionalDTO(
            Long idFeriado,
            LocalDate fecha,
            String nombre,
            String tipo,
            boolean activo,
            String observacion,
            LocalDateTime creadoEn,
            LocalDateTime modificadoEn) {
        this.idFeriado = idFeriado;
        this.fecha = fecha;
        this.nombre = safe(nombre);
        this.tipo = hasText(tipo) ? tipo.trim() : "NACIONAL";
        this.activo = activo;
        this.observacion = safe(observacion);
        this.creadoEn = creadoEn;
        this.modificadoEn = modificadoEn;
    }

    public Long getIdFeriado() {
        return idFeriado;
    }

    public void setIdFeriado(Long idFeriado) {
        this.idFeriado = idFeriado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = safe(nombre);
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = hasText(tipo) ? tipo.trim() : "NACIONAL";
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = safe(observacion);
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public LocalDateTime getModificadoEn() {
        return modificadoEn;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
