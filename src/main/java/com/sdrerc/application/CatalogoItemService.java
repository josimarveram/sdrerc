/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.domain.model.User;
import com.sdrerc.infrastructure.repository.CatalogoDetalleRepository;
import com.sdrerc.infrastructure.repository.UserRepository;
import java.util.List;

/**
 *
 * @author DESARROLLADOR84_USI
 */
public class CatalogoItemService {
    private final CatalogoDetalleRepository repository;
    
    public CatalogoItemService() {
        this.repository = new CatalogoDetalleRepository();
    }
    
    public List<CatalogoItem> listarCatalogoItem(int idCatalogo) throws Exception {
        
        List<CatalogoItem> catalogoitem = repository.listarCatalogoItem(idCatalogo);
        return catalogoitem;
    }
}
