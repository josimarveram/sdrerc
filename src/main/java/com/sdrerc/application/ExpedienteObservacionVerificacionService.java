/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.ExpedienteObservacionVerificacion.ExpedienteObservacionVerificacion;
import com.sdrerc.infrastructure.repository.ExpedienteObservacionVerificacionRepository;

/**
 *
 * @author David
 */
public class ExpedienteObservacionVerificacionService {
    private final ExpedienteObservacionVerificacionRepository repository;

    public ExpedienteObservacionVerificacionService() {
        this.repository = new ExpedienteObservacionVerificacionRepository();
    }
    
    public void registrarObservacion(ExpedienteObservacionVerificacion entity)throws Exception
    {
        repository.insertar(entity);
    }
}
