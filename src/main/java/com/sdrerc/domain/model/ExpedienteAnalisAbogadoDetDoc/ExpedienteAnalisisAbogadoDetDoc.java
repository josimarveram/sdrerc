/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model.ExpedienteAnalisAbogadoDetDoc;

import java.util.Date;

/**
 *
 * @author betom
 */
public class ExpedienteAnalisisAbogadoDetDoc 
{
    private int idExpedienteAnalisisAbogadoDetDoc; 
    private int idExpedienteAnalisisAbogado;     
    private int idTipoDocumentoAnalizado;
    private String descTipoDocumentoAnalizado;
    private String descDocumento;
    private int active; 
    private Date fechaRegistro; 
    private int usuarioRegistro; 
    private Date fechaModificacion; 
    private int usuarioModificacion;

    public ExpedienteAnalisisAbogadoDetDoc(int idExpedienteAnalisisAbogadoDetDoc, int idExpedienteAnalisisAbogado, int idTipoDocumentoAnalizado, String descTipoDocumentoAnalizado, String descDocumento, int active, Date fechaRegistro, int usuarioRegistro, Date fechaModificacion, int usuarioModificacion) {
        this.idExpedienteAnalisisAbogadoDetDoc = idExpedienteAnalisisAbogadoDetDoc;
        this.idExpedienteAnalisisAbogado = idExpedienteAnalisisAbogado;
        this.idTipoDocumentoAnalizado = idTipoDocumentoAnalizado;
        this.descTipoDocumentoAnalizado = descTipoDocumentoAnalizado;
        this.descDocumento = descDocumento;
        this.active = active;
        this.fechaRegistro = fechaRegistro;
        this.usuarioRegistro = usuarioRegistro;
        this.fechaModificacion = fechaModificacion;
        this.usuarioModificacion = usuarioModificacion;
    }

    public ExpedienteAnalisisAbogadoDetDoc() {
    }

    public int getIdExpedienteAnalisisAbogadoDetDoc() {
        return idExpedienteAnalisisAbogadoDetDoc;
    }

    public void setIdExpedienteAnalisisAbogadoDetDoc(int idExpedienteAnalisisAbogadoDetDoc) {
        this.idExpedienteAnalisisAbogadoDetDoc = idExpedienteAnalisisAbogadoDetDoc;
    }

    public int getIdExpedienteAnalisisAbogado() {
        return idExpedienteAnalisisAbogado;
    }

    public void setIdExpedienteAnalisisAbogado(int idExpedienteAnalisisAbogado) {
        this.idExpedienteAnalisisAbogado = idExpedienteAnalisisAbogado;
    }

    public int getIdTipoDocumentoAnalizado() {
        return idTipoDocumentoAnalizado;
    }

    public void setIdTipoDocumentoAnalizado(int idTipoDocumentoAnalizado) {
        this.idTipoDocumentoAnalizado = idTipoDocumentoAnalizado;
    }
    
    public String getDescTipoDocumentoAnalizado() {
        return descTipoDocumentoAnalizado;
    }

    public void setDescTipoDocumentoAnalizado(String descTipoDocumentoAnalizado) {
        this.descTipoDocumentoAnalizado = descTipoDocumentoAnalizado;
    }

    public String getDescDocumento() {
        return descDocumento;
    }

    public void setDescDocumento(String descDocumento) {
        this.descDocumento = descDocumento;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
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
