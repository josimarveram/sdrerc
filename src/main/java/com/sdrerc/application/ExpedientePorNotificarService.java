/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.infrastructure.repository.ExpedientePorNotificarRepository;
import java.util.List;

/**
 *
 * @author betom
 */
public class ExpedientePorNotificarService 
{
    private final ExpedientePorNotificarRepository expedientePorNotificarRepository;
    
    public ExpedientePorNotificarService() 
    {
        this.expedientePorNotificarRepository = new ExpedientePorNotificarRepository();
    }
    
    public List<Expediente> ListarExpedientesPorNotificar(int estadoItem) throws Exception 
    {
        return expedientePorNotificarRepository.ListarExpedientesPorNotificar(estadoItem);
    }      
}
