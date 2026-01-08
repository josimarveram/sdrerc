/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model;

/**
 *
 * @author David
 */
public class Distrito {
    private int idDistrito;
    private int idProvincia;
    private String descripcion;

    public Distrito(int idDistrito, int idProvincia, String descripcion) {
        this.idDistrito = idDistrito;
        this.idProvincia = idProvincia;
        this.descripcion = descripcion;
    }

    public int getIdDistrito() {
        return idDistrito;
    }

    public int getIdProvincia() {
        return idProvincia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
