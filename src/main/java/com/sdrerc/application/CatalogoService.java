/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.Catalogo;
import com.sdrerc.infrastructure.repository.CatalogoRepository;
import java.util.List;

/**
 *
 * @author David
 */
public class CatalogoService {
    private final CatalogoRepository repository;
    
    public CatalogoService() {
        this.repository = new CatalogoRepository();
    }
    
    public List<Catalogo> obtenerEstados() {        
        return repository.obtenerEstados();
    }
}
