/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.ExpedienteObservacionEjecucion.ExpedienteObservacionEjecucion;
import com.sdrerc.infrastructure.repository.ExpedienteObservacionEjecucionRepository;

/**
 *
 * @author David
 */
public class ExpedienteObservacionEjecucionService {
    private final ExpedienteObservacionEjecucionRepository repository =
        new ExpedienteObservacionEjecucionRepository();

    public void registrarObservacion(ExpedienteObservacionEjecucion o) throws Exception { 
        repository.insertar(o);
    }
}
