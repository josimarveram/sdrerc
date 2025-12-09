/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.domain.model.Expediente.ExpedienteResponse;
import com.sdrerc.infrastructure.repository.ExpedienteRepository;

/**
 *
 * @author usuario
 */
public class ExpedienteService 
{
    private final ExpedienteRepository expedienteRepository_;
    
    public ExpedienteService() 
    {
        this.expedienteRepository_ = new ExpedienteRepository();
    }

    public ExpedienteResponse ListarExpediente(String username, String password) throws Exception 
    {
        //if(username.isBlank() || password.isBlank()) 
        //{
        //   throw new Exception("Debe ingresar usuario y contraseña.");
        //}

        ExpedienteResponse expedienteResponse = expedienteRepository_.ListarExpediente(username, password);

        return expedienteResponse;
    }   
    
    public ExpedienteResponse agregarExpediente(Expediente expediente) throws Exception 
    {
        // Validaciones mínimas
        if (expediente == null) {
            throw new Exception("El expediente no puede ser nulo.");
        }
        // Llamar al repositorio (DAO)
        ExpedienteResponse response = expedienteRepository_.agregarExpediente(expediente);
        return response;
    }
    
}
