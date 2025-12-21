/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model;

import java.util.Date;

/**
 *
 * @author David
 */
public class ExpedienteAsignacion 
{
    private int idExpedienteAsignacion;
    private int idExpediente;
    private int idTecnico;
    private Date fechaAsignacion;
    private int aceptaRecepcion;
    private Date fechaRecepcion;
    private int idUsuarioModifica;
    private Date fechaModifica;        
       
    public ExpedienteAsignacion() {}
    
    public ExpedienteAsignacion(int idExpediente, int idTecnico, Date fechaAsignacion,int aceptaRecepcion, Date fechaRecepcion, int idUsuarioModifica, Date fechaModifica) 
    {
        this.idExpediente = idExpediente;
        this.idTecnico = idTecnico;
        this.fechaAsignacion = fechaAsignacion;
        this.aceptaRecepcion = aceptaRecepcion;
        this.fechaRecepcion = fechaRecepcion;        
        this.idUsuarioModifica = idUsuarioModifica;
        this.fechaModifica = fechaModifica;
    }
    
    // Getters y setters
    public int getIdExpediente() { return idExpediente; }
    public int getIdTecnico() { return idTecnico; }
    public Date getFechaAsignacion() { return fechaAsignacion; }
    public int getAceptaRecepcion() { return aceptaRecepcion; }
    public Date getFechaRecepcion() { return fechaRecepcion; }
    public int getIdUsuarioModifica() { return idUsuarioModifica; }
    public Date getFechaModifica() { return fechaModifica; }
    
    public void setFechaAsignacion(Date fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }
    
    public void setIdTecnico(int idTecnico) {
        this.idTecnico = idTecnico;
    }
    
    public void setIdExpediente(int idExpediente) {
        this.idExpediente = idExpediente;
    }
    
    public void setIdUsuarioModifica(int idUsuarioModifica) 
    {
        this.idUsuarioModifica = idUsuarioModifica;
    }   

    public void setFechaModifica(Date fechaModifica) 
    {
        this.fechaModifica = fechaModifica;
    } 
    
    public void setAceptaRecepcion(int aceptaRecepcion) 
    {
        this.aceptaRecepcion = aceptaRecepcion; 
    }
    
    public void setFechaRecepcion(Date fechaRecepcion) 
    {
        this.fechaRecepcion = fechaRecepcion;
    }
    
}
