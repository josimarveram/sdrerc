/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model;

/**
 *
 * @author David
 */
public class Departamento {
    private int idDepartamento;
    private String descripcion;

    public Departamento(int idDepartamento, String descripcion) {
        this.idDepartamento = idDepartamento;
        this.descripcion = descripcion;
    }

    public int getIdDepartamento() {
        return idDepartamento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion; // lo que se muestra en el combo
    }
}
