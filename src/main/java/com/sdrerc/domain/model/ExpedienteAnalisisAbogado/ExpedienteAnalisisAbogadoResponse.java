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
public class ExpedienteAnalisisAbogadoResponse extends ExpedienteAnalisisAbogado
{

    public ExpedienteAnalisisAbogadoResponse(int idExpedienteAnalisisAbogado, int idExpediente, int idAbogado, int idAnalisis, String descFundamento, Date fechaAtencion, Date fechaRegistro, int usuarioRegistro, Date fechaModificacion, int usuarioModificacion, int idEstadoExpediente) {
        super(idExpedienteAnalisisAbogado, idExpediente, idAbogado, idAnalisis, descFundamento, fechaAtencion, fechaRegistro, usuarioRegistro, fechaModificacion, usuarioModificacion, idEstadoExpediente);
    }

    public ExpedienteAnalisisAbogadoResponse() {
    }
    
}
