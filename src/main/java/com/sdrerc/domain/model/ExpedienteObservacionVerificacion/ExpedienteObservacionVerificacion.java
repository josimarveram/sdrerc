/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model.ExpedienteObservacionVerificacion;

import java.util.Date;

/**
 *
 * @author David
 */
public class ExpedienteObservacionVerificacion {
    private Integer idExpedienteObservacionVerificacion;
    private Integer idExpediente;
    private String hojaEnvio;
    private Boolean tieneObservacion;
    private Integer tipoObservacion;
    private String descripcionObservacion;
    private Date fechaRegistro;
    private int usuarioRegistro;
    private Date fechaModificacion;
    private int usuarioModificacion;
    private int idEstadoExpediente;

    // 🔹 Constructor vacío (obligatorio para frameworks y mapeos)
    public ExpedienteObservacionVerificacion() {
    }

    // 🔹 Constructor completo (opcional)
    public ExpedienteObservacionVerificacion(
            Integer idExpedienteObservacionVerificacion,
            Integer idExpediente,
            String hojaEnvio,
            Boolean tieneObservacion,
            Integer tipoObservacion,
            String descripcionObservacion, 
            Date fechaRegistro,
            int usuarioRegistro, 
            Date fechaModificacion, 
            int usuarioModificacion, 
            int idEstadoExpediente) {

        this.idExpedienteObservacionVerificacion = idExpedienteObservacionVerificacion;
        this.idExpediente = idExpediente;
        this.hojaEnvio = hojaEnvio;
        this.tieneObservacion = tieneObservacion;
        this.tipoObservacion = tipoObservacion;
        this.descripcionObservacion = descripcionObservacion;
        this.fechaRegistro = fechaRegistro;
        this.usuarioRegistro = usuarioRegistro;
        this.fechaModificacion = fechaModificacion;
        this.usuarioModificacion = usuarioModificacion;
        this.idEstadoExpediente = idEstadoExpediente;
    
    }

    // 🔹 GETTERS Y SETTERS

    public Integer getIdExpedienteObservacionVerificacion() {
        return idExpedienteObservacionVerificacion;
    }

    public void setIdExpedienteObservacionVerificacion(Integer idExpedienteObservacionVerificacion) {
        this.idExpedienteObservacionVerificacion = idExpedienteObservacionVerificacion;
    }

    public Integer getIdExpediente() {
        return idExpediente;
    }

    public void setIdExpediente(Integer idExpediente) {
        this.idExpediente = idExpediente;
    }

    public String getHojaEnvio() {
        return hojaEnvio;
    }

    public void setHojaEnvio(String hojaEnvio) {
        this.hojaEnvio = hojaEnvio;
    }

    public Boolean getTieneObservacion() {
        return tieneObservacion;
    }

    public void setTieneObservacion(Boolean tieneObservacion) {
        this.tieneObservacion = tieneObservacion;
    }

    public Integer getTipoObservacion() {
        return tipoObservacion;
    }

    public void setTipoObservacion(Integer tipoObservacion) {
        this.tipoObservacion = tipoObservacion;
    }

    public String getDescripcionObservacion() {
        return descripcionObservacion;
    }

    public void setDescripcionObservacion(String descripcionObservacion) {
        this.descripcionObservacion = descripcionObservacion;
    }
    
    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public int getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(int usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public int getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(int usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }  	
    public int getIdEstadoExpediente() {
        return idEstadoExpediente;
    }

    public void setIdEstadoExpediente(int idEstadoExpediente) {
        this.idEstadoExpediente = idEstadoExpediente;
    }
}
