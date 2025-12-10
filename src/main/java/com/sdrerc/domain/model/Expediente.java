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
public class Expediente {
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

    // Constructor y getters/setters

    public Expediente() {
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

    public Date getFechaSolicitud() {
        return fechaSolicitud;
    }

    public String getNumeroTramiteDocumento() {
        return numeroTramiteDocumento;
    }

    public int getTipoSolicitud() {
        return tipoSolicitud;
    }

    public int getTipoDocumento() {
        return tipoDocumento;
    }

    public String getDniRemitente() {
        return dniRemitente;
    }

    public String getApellidoNombreRemitente() {
        return apellidoNombreRemitente;
    }

    public String getDniSolicitante() {
        return dniSolicitante;
    }

    public String getApellidoNombreSolicitante() {
        return apellidoNombreSolicitante;
    }

    public int getTipoProcedimientoRegistral() {
        return tipoProcedimientoRegistral;
    }

    public int getTipoActa() {
        return tipoActa;
    }

    public String getNumeroActa() {
        return numeroActa;
    }

    public int getTipoGrupoFamiliar() {
        return tipoGrupoFamiliar;
    }

    public String getNumeroGrupoFamiliar() {
        return numeroGrupoFamiliar;
    }

    public String getDniTitular() {
        return dniTitular;
    }

    public String getApellidoNombreTitular() {
        return apellidoNombreTitular;
    }

    public int getEstado() {
        return estado;
    }
    
    public void setIdExpediente(int idExpediente) { this.idExpediente = idExpediente; }
    
    
}
