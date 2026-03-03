/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.infrastructure.repository.ExpedienteEjecucionAsignacionRepository;
import java.util.List;

/**
 *
 * @author betom
 */
public class ExpedienteEjecucionAsignacionService 
{
    private final ExpedienteEjecucionAsignacionRepository expedienteEjecucionAsignacionRepository;
    
    public ExpedienteEjecucionAsignacionService() 
    {
        this.expedienteEjecucionAsignacionRepository = new ExpedienteEjecucionAsignacionRepository();
    }    
    
    public List<Expediente> ListarExpedientesEjecucion(int estadoItem) throws Exception 
    {
        return expedienteEjecucionAsignacionRepository.ListarExpedientesEjecucion(estadoItem);
    } 
}


