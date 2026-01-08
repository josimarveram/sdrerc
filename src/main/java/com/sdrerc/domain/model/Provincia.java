/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model;

/**
 *
 * @author David
 */
public class Provincia {
    private int idProvincia;
    private int idDepartamento;
    private String descripcion;

    public Provincia(int idProvincia, int idDepartamento, String descripcion) {
        this.idProvincia = idProvincia;
        this.idDepartamento = idDepartamento;
        this.descripcion = descripcion;
    }

    public int getIdProvincia() {
        return idProvincia;
    }

    public int getIdDepartamento() {
        return idDepartamento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
