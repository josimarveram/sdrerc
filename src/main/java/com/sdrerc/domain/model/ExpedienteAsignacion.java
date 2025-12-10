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
public class ExpedienteAsignacion {
    private int idExpedienteAsignacion;
    private int idExpediente;
    private int idTecnico;
    private Date fechaAsignacion;

    public ExpedienteAsignacion() {}

    public ExpedienteAsignacion(int idExpediente, int idTecnico, Date fechaAsignacion) {
        this.idExpediente = idExpediente;
        this.idTecnico = idTecnico;
        this.fechaAsignacion = fechaAsignacion;
    }

    // Getters y setters
    public int getIdExpediente() { return idExpediente; }
    public int getIdTecnico() { return idTecnico; }
    public Date getFechaAsignacion() { return fechaAsignacion; }
    
    public void setFechaAsignacion(Date fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }
    
    public void setIdTecnico(int idTecnico) {
        this.idTecnico = idTecnico;
    }
    
    public void setIdExpediente(int idExpediente) {
        this.idExpediente = idExpediente;
    }
}
