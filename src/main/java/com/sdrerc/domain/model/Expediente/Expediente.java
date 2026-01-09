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
    private int esRegistroSdrerc;
    private String hojaEnvioExpediente;
    private String numeroTramiteDocumento;
    private Date fechaRecepcion;
    private Date fechaSolicitud;
    private int tipoDocumento;
    private String numeroDocumento;
    private int tipoActa;
    private String numeroActa;
    private int tipoGrupoFamiliar;
    private int gradoParentesco;
    private int tipoProcedimientoRegistral;
    private int tipoSolicitud;
    private String dniRemitente;
    private String apellidoNombreRemitente;
    private int unidadOrganica;
    private String dniTitular;
    private String apellidoNombreTitular;
    private int departamento;
    private int provincia;
    private int distrito;
    private int direccionDomiciliaria;
    private String domicilio;
    private String correoElectronico;
    private String celular;
    private int estado;
    private int idUsuarioCrea;
    private Date fechaRegistra;
    private int idUsuarioModifica;
    private Date fechaModifica;
    
    
    public Expediente() 
    {
    }

    public Expediente(int idExpediente, int esRegistroSdrerc, String hojaEnvioExpediente, String numeroTramiteDocumento, Date fechaRecepcion, Date fechaSolicitud, int tipoDocumento, String numeroDocumento, int tipoActa, String numeroActa, int tipoGrupoFamiliar, int gradoParentesco, int tipoProcedimientoRegistral, int tipoSolicitud, String dniRemitente, String apellidoNombreRemitente, int unidadOrganica, String dniTitular, String apellidoNombreTitular, int departamento, int provincia, int distrito, int direccionDomiciliaria, String domicilio, String correoElectronico, String celular, int estado, int idUsuarioCrea, Date fechaRegistra, int idUsuarioModifica, Date fechaModifica) {
        this.idExpediente = idExpediente;
        this.esRegistroSdrerc = esRegistroSdrerc;
        this.hojaEnvioExpediente = hojaEnvioExpediente;
        this.numeroTramiteDocumento = numeroTramiteDocumento;
        this.fechaRecepcion = fechaRecepcion;
        this.fechaSolicitud = fechaSolicitud;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.tipoActa = tipoActa;
        this.numeroActa = numeroActa;
        this.tipoGrupoFamiliar = tipoGrupoFamiliar;
        this.gradoParentesco = gradoParentesco;
        this.tipoProcedimientoRegistral = tipoProcedimientoRegistral;
        this.tipoSolicitud = tipoSolicitud;
        this.dniRemitente = dniRemitente;
        this.apellidoNombreRemitente = apellidoNombreRemitente;
        this.unidadOrganica = unidadOrganica;
        this.dniTitular = dniTitular;
        this.apellidoNombreTitular = apellidoNombreTitular;
        this.departamento = departamento;
        this.provincia = provincia;
        this.distrito = distrito;
        this.direccionDomiciliaria = direccionDomiciliaria;
        this.domicilio = domicilio;
        this.correoElectronico = correoElectronico;
        this.celular = celular;
        this.estado = estado;
        this.idUsuarioCrea = idUsuarioCrea;
        this.fechaRegistra = fechaRegistra;
        this.idUsuarioModifica = idUsuarioModifica;
        this.fechaModifica = fechaModifica;
    }

    public int getIdExpediente() {
        return idExpediente;
    }

    public void setIdExpediente(int idExpediente) {
        this.idExpediente = idExpediente;
    }

    public int getEsRegistroSdrerc() {
        return esRegistroSdrerc;
    }

    public void setEsRegistroSdrerc(int esRegistroSdrerc) {
        this.esRegistroSdrerc = esRegistroSdrerc;
    }

    public String getHojaEnvioExpediente() {
        return hojaEnvioExpediente;
    }

    public void setHojaEnvioExpediente(String hojaEnvioExpediente) {
        this.hojaEnvioExpediente = hojaEnvioExpediente;
    }

    public String getNumeroTramiteDocumento() {
        return numeroTramiteDocumento;
    }

    public void setNumeroTramiteDocumento(String numeroTramiteDocumento) {
        this.numeroTramiteDocumento = numeroTramiteDocumento;
    }

    public Date getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(Date fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public Date getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(Date fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public int getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(int tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
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

    public int getGradoParentesco() {
        return gradoParentesco;
    }

    public void setGradoParentesco(int gradoParentesco) {
        this.gradoParentesco = gradoParentesco;
    }

    public int getTipoProcedimientoRegistral() {
        return tipoProcedimientoRegistral;
    }

    public void setTipoProcedimientoRegistral(int tipoProcedimientoRegistral) {
        this.tipoProcedimientoRegistral = tipoProcedimientoRegistral;
    }

    public int getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(int tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
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

    public int getUnidadOrganica() {
        return unidadOrganica;
    }

    public void setUnidadOrganica(int unidadOrganica) {
        this.unidadOrganica = unidadOrganica;
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

    public int getDepartamento() {
        return departamento;
    }

    public void setDepartamento(int departamento) {
        this.departamento = departamento;
    }

    public int getProvincia() {
        return provincia;
    }

    public void setProvincia(int provincia) {
        this.provincia = provincia;
    }

    public int getDistrito() {
        return distrito;
    }

    public void setDistrito(int distrito) {
        this.distrito = distrito;
    }

    public int getDireccionDomiciliaria() {
        return direccionDomiciliaria;
    }

    public void setDireccionDomiciliaria(int direccionDomiciliaria) {
        this.direccionDomiciliaria = direccionDomiciliaria;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
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
