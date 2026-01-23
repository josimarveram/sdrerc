/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model.ExpedienteAnalisisAbogado;

import java.util.Date;

/**
 *
 * @author betom
 */
public class ExpedienteAnalisisAbogado 
{
    private int idExpedienteAnalisisAbogado; 
    private int idExpediente; 
    private int idAbogado;
    private int idAnalisis; 
    private String descFundamento; 
    private Date fechaAtencion; 	
    private Date fechaRegistro; 
    private int  usuarioRegistro; 
    private Date fechaModificacion; 
    private int  usuarioModificacion;    
    private int  idEstadoExpediente;
  
    public ExpedienteAnalisisAbogado(int idExpedienteAnalisisAbogado, int idExpediente, int idAbogado, int idAnalisis, String descFundamento, Date fechaAtencion, Date fechaRegistro, int usuarioRegistro, Date fechaModificacion, int usuarioModificacion, int idEstadoExpediente) {
        this.idExpedienteAnalisisAbogado = idExpedienteAnalisisAbogado;
        this.idExpediente = idExpediente;
        this.idAbogado = idAbogado;
        this.idAnalisis = idAnalisis;
        this.descFundamento = descFundamento;
        this.fechaAtencion = fechaAtencion;
        this.fechaRegistro = fechaRegistro;
        this.usuarioRegistro = usuarioRegistro;
        this.fechaModificacion = fechaModificacion;
        this.usuarioModificacion = usuarioModificacion;
        this.idEstadoExpediente = idEstadoExpediente;
    }

    public ExpedienteAnalisisAbogado() {}

    public int getIdExpedienteAnalisisAbogado() {
        return idExpedienteAnalisisAbogado;
    }

    public void setIdExpedienteAnalisisAbogado(int idExpedienteAnalisisAbogado) {
        this.idExpedienteAnalisisAbogado = idExpedienteAnalisisAbogado;
    }

    public int getIdExpediente() {
        return idExpediente;
    }

    public void setIdExpediente(int idExpediente) {
        this.idExpediente = idExpediente;
    }

    public int getIdAbogado() {
        return idAbogado;
    }

    public void setIdAbogado(int idAbogado) {
        this.idAbogado = idAbogado;
    }

    public int getIdAnalisis() {
        return idAnalisis;
    }

    public void setIdAnalisis(int idAnalisis) {
        this.idAnalisis = idAnalisis;
    }

    public String getDescFundamento() {
        return descFundamento;
    }

    public void setDescFundamento(String descFundamento) {
        this.descFundamento = descFundamento;
    }

    public Date getFechaAtencion() {
        return fechaAtencion;
    }

    public void setFechaAtencion(Date fechaAtencion) {
        this.fechaAtencion = fechaAtencion;
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
