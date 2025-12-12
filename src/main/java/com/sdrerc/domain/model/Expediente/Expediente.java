/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model.Expediente;

import java.util.Date;

/**
 *
 * @author usuario
 */
public class Expediente 
{
    private int idExpediente;					
    private Date fechaSolicitud;				
    private String numeroTramiteDocumento;
    private int tipoSolicitud;				
    private int tipoDocumento;				
    private String dniRemitente;					
    private String apellidoNombreRemitente;		
    private String dniSolicitante;				
    private String apellidoNombreSolicitante;	
    private int tipoProcedimientoRegistral;	
    private int tipoActa;						
    private String numeroActa;					
    private int tipoGrupoFamiliar;			
    private String numeroGrupoFamiliar;			
    private String dniTitular;					
    private String apellidoNombreTitular;		
    private int estado;						
    private int idUsuarioCrea;				
    private Date fechaRegistra;				
    private int idUsuarioModifica;			
    private Date fechaModifica;
    
    public Expediente() 
    {
    }
    
    

    public Expediente(int idExpediente, Date fechaSolicitud, String numeroTramiteDocumento,
                      int tipoSolicitud, int tipoDocumento, String dniRemitente,
                      String apellidoNombreRemitente, String dniSolicitante,
                      String apellidoNombreSolicitante, int tipoProcedimientoRegistral,
                      int tipoActa, String numeroActa, int tipoGrupoFamiliar,
                      String numeroGrupoFamiliar, String dniTitular,
                      String apellidoNombreTitular, int estado) {

        this.idExpediente = idExpediente;
        this.fechaSolicitud = fechaSolicitud;
        this.numeroTramiteDocumento = numeroTramiteDocumento;
        this.tipoSolicitud = tipoSolicitud;
        this.tipoDocumento = tipoDocumento;
        this.dniRemitente = dniRemitente;
        this.apellidoNombreRemitente = apellidoNombreRemitente;
        this.dniSolicitante = dniSolicitante;
        this.apellidoNombreSolicitante = apellidoNombreSolicitante;
        this.tipoProcedimientoRegistral = tipoProcedimientoRegistral;
        this.tipoActa = tipoActa;
        this.numeroActa = numeroActa;
        this.tipoGrupoFamiliar = tipoGrupoFamiliar;
        this.numeroGrupoFamiliar = numeroGrupoFamiliar;
        this.dniTitular = dniTitular;
        this.apellidoNombreTitular = apellidoNombreTitular;
        this.estado = estado;
        
        
    }

    public int getIdExpediente() {
        return idExpediente;
    }

    public void setIdExpediente(int idExpediente) {
        this.idExpediente = idExpediente;
    }

    public Date getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(Date fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getNumeroTramiteDocumento() {
        return numeroTramiteDocumento;
    }

    public void setNumeroTramiteDocumento(String numeroTramiteDocumento) {
        this.numeroTramiteDocumento = numeroTramiteDocumento;
    }

    public int getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(int tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public int getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(int tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getDniRemitente() {
        return dniRemitente;
    }

    public void setDniRemitente(String dniRemitente) {
        this.dniRemitente = dniRemitente;
    }

    public String getApellidoNombreRemitente() {
        return apellidoNombreRemitente;
    }

    public void setApellidoNombreRemitente(String apellidoNombreRemitente) {
        this.apellidoNombreRemitente = apellidoNombreRemitente;
    }

    public String getDniSolicitante() {
        return dniSolicitante;
    }

    public void setDniSolicitante(String dniSolicitante) {
        this.dniSolicitante = dniSolicitante;
    }

    public String getApellidoNombreSolicitante() {
        return apellidoNombreSolicitante;
    }

    public void setApellidoNombreSolicitante(String apellidoNombreSolicitante) {
        this.apellidoNombreSolicitante = apellidoNombreSolicitante;
    }

    public int getTipoProcedimientoRegistral() {
        return tipoProcedimientoRegistral;
    }

    public void setTipoProcedimientoRegistral(int tipoProcedimientoRegistral) {
        this.tipoProcedimientoRegistral = tipoProcedimientoRegistral;
    }

    public int getTipoActa() {
        return tipoActa;
    }

    public void setTipoActa(int tipoActa) {
        this.tipoActa = tipoActa;
    }

    public String getNumeroActa() {
        return numeroActa;
    }

    public void setNumeroActa(String numeroActa) {
        this.numeroActa = numeroActa;
    }

    public int getTipoGrupoFamiliar() {
        return tipoGrupoFamiliar;
    }

    public void setTipoGrupoFamiliar(int tipoGrupoFamiliar) {
        this.tipoGrupoFamiliar = tipoGrupoFamiliar;
    }

    public String getNumeroGrupoFamiliar() {
        return numeroGrupoFamiliar;
    }

    public void setNumeroGrupoFamiliar(String numeroGrupoFamiliar) {
        this.numeroGrupoFamiliar = numeroGrupoFamiliar;
    }

    public String getDniTitular() {
        return dniTitular;
    }

    public void setDniTitular(String dniTitular) {
        this.dniTitular = dniTitular;
    }

    public String getApellidoNombreTitular() {
        return apellidoNombreTitular;
    }

    public void setApellidoNombreTitular(String apellidoNombreTitular) {
        this.apellidoNombreTitular = apellidoNombreTitular;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public int getIdUsuarioCrea() {
        return idUsuarioCrea;
    }

    public void setIdUsuarioCrea(int idUsuarioCrea) {
        this.idUsuarioCrea = idUsuarioCrea;
    }

    public Date getFechaRegistra() {
        return fechaRegistra;
    }

    public void setFechaRegistra(Date fechaRegistra) {
        this.fechaRegistra = fechaRegistra;
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
            
    

}
