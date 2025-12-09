/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model;

/**
 *
 * @author DESARROLLADOR84_USI
 */
public class CatalogoItem {
    private int idCatalogoItem;
    private int idCatalogo;
    private String descripcion;
    private int activo;
    
    
    public CatalogoItem(int idCatalogoItem,int idCatalogo, String descripcion, int activo) {
        this.idCatalogoItem = idCatalogoItem;
        this.idCatalogo = idCatalogo;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    
    
    
    public int getIdCatalogoItem() { return idCatalogoItem; }
    public int getIdCatalogo() { return idCatalogo; }
    public String getDescripcion() { return descripcion; }
    public double getActivo() { return activo; }

    @Override
    public String toString() {
        return descripcion; // lo que se mostrará en el combo
    }
}
