/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.infrastructure.repository.ExpedienteAsignacionRepository;

/**
 *
 * @author David
 */
public class ExpedienteAsignacionService {
    
    private final ExpedienteAsignacionRepository expedienteAsignacionRepository;
    
    public ExpedienteAsignacionService() 
    {
        this.expedienteAsignacionRepository = new ExpedienteAsignacionRepository();
    }
    
    public void agregarExpediente(ExpedienteAsignacion asignacion) throws Exception 
    {
        // Validaciones mínimas
        if (asignacion == null) {
            throw new Exception("La asignación no puede ser nula.");
        }      

        if (asignacion.getFechaAsignacion() == null) {
            throw new Exception("Debe seleccionar una fecha de asignación.");
        }

        // Registrar en la BD → Llama al DAO
        expedienteAsignacionRepository.registrar(asignacion);
    }
}
