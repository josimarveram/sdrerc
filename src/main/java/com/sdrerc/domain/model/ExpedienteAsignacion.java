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
    private String hojaEnvioAsignacion;    
    private int etapaFlujo; // Trabajador Analisis / Ejecucion / Notificacion 
    private int tipoProcedimientoRegistral;
    private String numeroResolucion;
    private int tipoActa;        
    //private int idEstadoExpediente;    
    private int idUsuarioModifica;
    private Date fechaModifica;    
    private String nombreTecnico;
    
    public ExpedienteAsignacion() 
    {        
    }   
        
    public ExpedienteAsignacion(int idExpedienteAsignacion, int idExpediente, int idTecnico, Date fechaAsignacion, int aceptaRecepcion, Date fechaRecepcion, String hojaEnvioAsignacion, int etapaFlujo, int tipoProcedimientoRegistral, String numeroResolucion, int tipoActa, int idUsuarioModifica, Date fechaModifica) {
        this.idExpedienteAsignacion = idExpedienteAsignacion;
        this.idExpediente = idExpediente;
        this.idTecnico = idTecnico;
        this.fechaAsignacion = fechaAsignacion;
        this.aceptaRecepcion = aceptaRecepcion;
        this.fechaRecepcion = fechaRecepcion;
        this.hojaEnvioAsignacion = hojaEnvioAsignacion;
        this.etapaFlujo = etapaFlujo;
        this.tipoProcedimientoRegistral = tipoProcedimientoRegistral;
        this.numeroResolucion = numeroResolucion;
        this.tipoActa = tipoActa;
        this.idUsuarioModifica = idUsuarioModifica;
        this.fechaModifica = fechaModifica;
    }  
    
    public int getIdExpedienteAsignacion() {
        return idExpedienteAsignacion;
    }

    public void setIdExpedienteAsignacion(int idExpedienteAsignacion) {
        this.idExpedienteAsignacion = idExpedienteAsignacion;
    }

    public int getIdExpediente() {
        return idExpediente;
    }

    public void setIdExpediente(int idExpediente) {
        this.idExpediente = idExpediente;
    }

    public int getIdTecnico() {
        return idTecnico;
    }

    public void setIdTecnico(int idTecnico) {
        this.idTecnico = idTecnico;
    }

    public Date getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(Date fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public int getAceptaRecepcion() {
        return aceptaRecepcion;
    }

    public void setAceptaRecepcion(int aceptaRecepcion) {
        this.aceptaRecepcion = aceptaRecepcion;
    }

    public Date getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(Date fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getHojaEnvioAsignacion() {
        return hojaEnvioAsignacion;
    }

    public void setHojaEnvioAsignacion(String hojaEnvioAsignacion) {
        this.hojaEnvioAsignacion = hojaEnvioAsignacion;
    }

    public int getEtapaFlujo() {
        return etapaFlujo;
    }

    public void setEtapaFlujo(int etapaFlujo) {
        this.etapaFlujo = etapaFlujo;
    }

    public int getTipoProcedimientoRegistral() {
        return tipoProcedimientoRegistral;
    }

    public void setTipoProcedimientoRegistral(int tipoProcedimientoRegistral) {
        this.tipoProcedimientoRegistral = tipoProcedimientoRegistral;
    }

    public String getNumeroResolucion() {
        return numeroResolucion;
    }

    public void setNumeroResolucion(String numeroResolucion) {
        this.numeroResolucion = numeroResolucion;
    }

    public int getTipoActa() {
        return tipoActa;
    }

    public void setTipoActa(int tipoActa) {
        this.tipoActa = tipoActa;
    }

    public int getIdUsuarioModifica() {
        return idUsuarioModifica;
    }

    public void setIdUsuarioModifica(int idUsuarioModifica) {
        this.idUsuarioModifica = idUsuarioModifica;
    }

    public Date getFechaModifica() {
        return fechaModifica;
    }

    public void setFechaModifica(Date fechaModifica) {
        this.fechaModifica = fechaModifica;
    }

    public String getNombreTecnico() {
        return nombreTecnico;
    }

    public void setNombreTecnico(String nombreTecnico) {
        this.nombreTecnico = nombreTecnico;
    }
    
    
    
    
}
