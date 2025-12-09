/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model.Expediente;

import java.sql.Date;

/**
 *
 * @author usuario
 */
public class ExpedienteResponse extends  Expediente
{
    
    public ExpedienteResponse(int idExpediente, Date fechaSolicitud, String numeroTramiteDocumento, int tipoSolicitud, int tipoDocumento, String dniRemitente, String apellidoNombreRemitente, String dniSolicitante, String apellidoNombreSolicitante, int tipoProcedimientoRegistral, int tipoActa, String numeroActa, int tipoGrupoFamiliar, String numeroGrupoFamiliar, String dniTitular, String apellidoNombreTitular, String estado, int idUsuarioCrea, Date fechaRegistra, int idUsuarioModifica, Date fechaModifica) {
        super(idExpediente, fechaSolicitud, numeroTramiteDocumento, tipoSolicitud, tipoDocumento, dniRemitente, apellidoNombreRemitente, dniSolicitante, apellidoNombreSolicitante, tipoProcedimientoRegistral, tipoActa, numeroActa, tipoGrupoFamiliar, numeroGrupoFamiliar, dniTitular, apellidoNombreTitular, estado, idUsuarioCrea, fechaRegistra, idUsuarioModifica, fechaModifica);
    }
    
}
