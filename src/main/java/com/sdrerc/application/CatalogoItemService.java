/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

import com.sdrerc.domain.model.CatalogoItem;
import com.sdrerc.infrastructure.repository.CatalogoDetalleRepository;
import com.sdrerc.util.EstadoTramiteText;
import java.util.ArrayList;
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
    
    public List<CatalogoItem> listarCatalogoItem(int idCatalogo){        
        return repository.listarCatalogoItem(idCatalogo);
    }
    
    public List<CatalogoItem> obtenerEstados() {        
        return normalizarEstados(repository.obtenerEstados());
    }

    public CatalogoItem crearCatalogoItem(int idCatalogo, String descripcion) {
        return repository.crearCatalogoItem(idCatalogo, descripcion);
    }

    private List<CatalogoItem> normalizarEstados(List<CatalogoItem> estados) {
        List<CatalogoItem> normalizados = new ArrayList<>();
        for (CatalogoItem estado : estados) {
            normalizados.add(new CatalogoItem(
                    estado.getIdCatalogoItem(),
                    estado.getIdCatalogo(),
                    EstadoTramiteText.paraFiltro(estado.getDescripcion()),
                    (int) estado.getActivo()
            ));
        }
        return normalizados;
    }
}
