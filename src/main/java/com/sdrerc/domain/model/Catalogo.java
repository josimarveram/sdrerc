/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model;

public class Catalogo {
    private int idCatalogo;
    private String descripcion;
    private int activo;
    
    
    public Catalogo(int idCatalogo, String descripcion, int activo) {
        this.idCatalogo = idCatalogo;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public int getId() { return idCatalogo; }
    public String getDescripcion() { return descripcion; }
    public double getActivo() { return activo; }

    @Override
    public String toString() {
        return descripcion; // lo que se mostrará en el combo
    }
    
}
