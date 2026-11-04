package com.sdrerc.v3.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Port literal de {@code com.sdrerc.domain.dto.sdrercapp.FeriadoNacionalDTO} (V2), Administración
 * &gt; Feriados. Distinto de {@code FeriadoNacionalDAO} (V3, ya existente) — ese DAO acotado solo
 * expone {@code listarFechasActivas} para {@code CalendarioLaboralService}, sin CRUD.
 */
public class FeriadoAdminDTO {

    private Long idFeriado;
    private LocalDate fecha;
    private String nombre;
    private String tipo = "NACIONAL";
    private boolean activo = true;
    private String observacion;
    private LocalDateTime creadoEn;
    private LocalDateTime modificadoEn;

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
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = (tipo == null || tipo.trim().isEmpty()) ? "NACIONAL" : tipo.trim();
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
        this.observacion = observacion;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getModificadoEn() {
        return modificadoEn;
    }

    public void setModificadoEn(LocalDateTime modificadoEn) {
        this.modificadoEn = modificadoEn;
    }
}
