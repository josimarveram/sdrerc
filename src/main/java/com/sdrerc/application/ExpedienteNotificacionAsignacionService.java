/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.infrastructure.repository.ExpedienteNotificacionAsignacionRepository;
import java.util.List;

/**
 *
 * @author betom
 */
public class ExpedienteNotificacionAsignacionService 
{
    private final ExpedienteNotificacionAsignacionRepository expedienteNotificacionAsignacionRepository;
    
    public ExpedienteNotificacionAsignacionService() 
    {
        this.expedienteNotificacionAsignacionRepository = new ExpedienteNotificacionAsignacionRepository();
    }    
    
    public List<Expediente> ListarExpedientesEjecucion(int estadoItem) throws Exception 
    {
        return expedienteNotificacionAsignacionRepository.ListarExpedientesNotificacion(estadoItem);
    } 
}
