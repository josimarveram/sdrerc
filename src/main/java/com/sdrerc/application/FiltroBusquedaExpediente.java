/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.application;

/**
 *
 * @author David
 */
public class FiltroBusquedaExpediente {
    private Integer tipoBusqueda;   // ID del CatalogoItem
    private String valorBusqueda;

    public FiltroBusquedaExpediente(Integer tipoBusqueda, String valorBusqueda) {
        this.tipoBusqueda = tipoBusqueda;
        this.valorBusqueda = valorBusqueda;
    }

    public Integer getTipoBusqueda() { return tipoBusqueda; }
    public String getValorBusqueda() { return valorBusqueda; }
}
