/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model.ExpedienteObservacionEjecucion;

import java.util.Date;

/**
 *
 * @author David
 */
public class ExpedienteObservacionEjecucion {
    
    
    private Integer idExpedienteObservacionEjecucion;
    private Integer idExpediente;
    private Integer idEstadoEjecucion;
    private Boolean tieneObservacion;
    private String descripcionObservacion;
    private Date fechaEjecucion;
     private Date fechaRegistro;
    private int usuarioRegistro;
    private Date fechaModificacion;
    private int usuarioModificacion;
    private int idEstadoExpediente;
    
    public ExpedienteObservacionEjecucion() {
    }
    
    public ExpedienteObservacionEjecucion(
            Integer idExpedienteObservacionEjecucion,
            Integer idExpediente,
            Integer idEstadoEjecucion,
            Boolean tieneObservacion,
            String descripcionObservacion, 
            Date fechaEjecucion,
            Date fechaRegistro,
            int usuarioRegistro, 
            Date fechaModificacion, 
            int usuarioModificacion, 
            int idEstadoExpediente) {

        this.idExpedienteObservacionEjecucion = idExpedienteObservacionEjecucion;
        this.idExpediente = idExpediente;
        this.idEstadoEjecucion = idEstadoEjecucion;
        this.tieneObservacion = tieneObservacion;
        this.descripcionObservacion = descripcionObservacion;
        this.fechaEjecucion = fechaEjecucion;
        this.fechaRegistro = fechaRegistro;
        this.usuarioRegistro = usuarioRegistro;
        this.fechaModificacion = fechaModificacion;
        this.usuarioModificacion = usuarioModificacion;
        this.idEstadoExpediente = idEstadoExpediente;
    
    }

    // getters y setters
    public Integer getIdExpedienteObservacionEjecucion() {
        return idExpedienteObservacionEjecucion;
    }

    public void setIdExpedienteObservacionEjecucion(Integer id) {
        this.idExpedienteObservacionEjecucion = id;
    }

    public Integer getIdExpediente() {
        return idExpediente;
    }

    public void setIdExpediente(Integer idExpediente) {
        this.idExpediente = idExpediente;
    }
    
    public Integer getIdEstadoEjecucion() {
        return idEstadoEjecucion;
    }

    public void setIdEstadoEjecucion(Integer idEstadoEjecucion) {
        this.idEstadoEjecucion = idEstadoEjecucion;
    }


    public Boolean getTieneObservacion() {
        return tieneObservacion;
    }

    public void setTieneObservacion(Boolean tieneObservacion) {
        this.tieneObservacion = tieneObservacion;
    }

    public String getDescripcionObservacion() {
        return descripcionObservacion;
    }

    public void setDescripcionObservacion(String descripcionObservacion) {
        this.descripcionObservacion = descripcionObservacion;
    }

    public Date getFechaEjecucion() {
        return fechaEjecucion;
    }

    public void setFechaEjecucion(Date fechaEjecucion) {
        this.fechaEjecucion = fechaEjecucion;
    }
    
    public int getIdEstadoExpediente() {
        return idEstadoExpediente;
    }

    public void setIdEstadoExpediente(int idEstadoExpediente) {
        this.idEstadoExpediente = idEstadoExpediente;
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

}
