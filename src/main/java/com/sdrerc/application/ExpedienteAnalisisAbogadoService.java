/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.ExpedienteAnalisisAbogado.ExpedienteAnalisisAbogado;
import com.sdrerc.domain.model.ExpedienteAnalisisAbogado.ExpedienteAnalisisAbogadoResponse;
import com.sdrerc.infrastructure.repository.ExpedienteAnalisisAbogadoRepository;
import java.util.List;

/**
 *
 * @author betom
 */
public class ExpedienteAnalisisAbogadoService 
{
    private final ExpedienteAnalisisAbogadoRepository expedienteAnalisisAbogadoRepository;
    
    public ExpedienteAnalisisAbogadoService() 
    {
        this.expedienteAnalisisAbogadoRepository = new ExpedienteAnalisisAbogadoRepository();
    }
    
    public boolean  agregarAnalisisAbogado(ExpedienteAnalisisAbogado oExpedienteAnalisisAbogado) throws Exception 
    {
        if (oExpedienteAnalisisAbogado == null) 
        {
            throw new Exception("El expediente no puede ser nulo.");
        }
        boolean respuesta = expedienteAnalisisAbogadoRepository.InsertarAnalisisAbogado(oExpedienteAnalisisAbogado);
        return respuesta; 
    }
    
    public ExpedienteAnalisisAbogadoResponse ObtenerExpedientesPorNotificarXidExpediente(int idExpediente) throws Exception 
    {
        ExpedienteAnalisisAbogadoResponse expedienteResponse = expedienteAnalisisAbogadoRepository.ObtenerExpedientesPorNotificarXidExpediente(idExpediente);
        return expedienteResponse;
    } 
    
}
