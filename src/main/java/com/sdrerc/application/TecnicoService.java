/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.Tecnico;
import com.sdrerc.infrastructure.repository.TecnicoRepository;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author David
 */
public class TecnicoService {
    private final TecnicoRepository tecnicoRepository;
    
    public TecnicoService() 
    {
        this.tecnicoRepository = new TecnicoRepository();
    }
    public List<Tecnico> listarTecnicos() {
        return tecnicoRepository.listarTecnicos();
    }    
}
