/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.Expediente.Expediente;
import com.sdrerc.domain.model.Expediente.ExpedienteResponse;
import com.sdrerc.domain.model.ExpedienteAsignacion;
import com.sdrerc.infrastructure.repository.ExpedienteAsignacionRepository;
import java.util.List;

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
    
    public void agregarExpediente(ExpedienteAsignacion asignacion, Expediente expediente) throws Exception 
    {
        // Validaciones mínimas
        if (asignacion == null) {
            throw new Exception("La asignación no puede ser nula.");
        }      

        if (asignacion.getFechaAsignacion() == null) {
            throw new Exception("Debe seleccionar una fecha de asignación.");
        }

        // Registrar en la BD → Llama al DAO
        expedienteAsignacionRepository.registrar(asignacion,expediente);
    }

    public boolean RegistrarAsigancionExpedienteTO(ExpedienteAsignacion oExpedienteAsignacion) throws Exception 
    {
        // Validaciones mínimas
        if (oExpedienteAsignacion == null) 
        {           
            throw new Exception("El expediente no puede ser nulo.");
        }
        // Llamar al repositorio (DAO)
        boolean respuesta = expedienteAsignacionRepository.RegistrarAsigancionExpedienteTO(oExpedienteAsignacion);
        return respuesta; 
    } 
    
    public boolean  actualizarRecepcionExpediente(ExpedienteAsignacion oExpedienteAsignacion) throws Exception 
    {
        // Validaciones mínimas
        if (oExpedienteAsignacion == null) {
            throw new Exception("El expediente no puede ser nulo.");
        }
        // Llamar al repositorio (DAO)
        boolean respuesta = expedienteAsignacionRepository.actualizarRecepcionExpediente(oExpedienteAsignacion);
        return respuesta; 
    } 
    
    public List<Expediente> ListarExpedientesAsignadosPorTrabajador(int idTecnico, int aceptaRecepcion,int estadoItem,int esPorVerificar, int esPorNotificar) throws Exception 
    {
        return expedienteAsignacionRepository.ListarExpedientesAsignadosPorTrabajador(idTecnico, aceptaRecepcion, estadoItem, esPorVerificar, esPorNotificar);
    }
}
