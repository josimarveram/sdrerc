/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.infrastructure.repository.ExpedienteEjecucionPorTrabajarRepository;
import java.util.List;

/**
 *
 * @author betom
 */
public class ExpedienteEjecucionPorTrabajarService 
{
    private final ExpedienteEjecucionPorTrabajarRepository expedienteEjecucionPorTrabajarRepository;
    
    public ExpedienteEjecucionPorTrabajarService() 
    {
        this.expedienteEjecucionPorTrabajarRepository = new ExpedienteEjecucionPorTrabajarRepository();
    }    
    
    public List<Expediente> ListarExpedientesEjecucionPorTrabajar(int estadoItem) throws Exception 
    {
        return expedienteEjecucionPorTrabajarRepository.ListarExpedientesEjecucionPorTrabajar(estadoItem);
    } 
}
