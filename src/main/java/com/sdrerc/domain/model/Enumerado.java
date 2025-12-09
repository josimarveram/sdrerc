/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model;

/**
 *
 * @author usuario
 */
public class Enumerado 
{
   public enum TipoSolicitud {
    PARTE(1, "PARTE"),
    OFICIO(2, "OFICIO"),
    CARTA(3, "CARTA");

    private final int id;
    private final String descripcion;

    TipoSolicitud(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public String getDescripcion() { return descripcion; }

    // Buscar por descripcion
    public static TipoSolicitud fromDescripcion(String descripcion) {
        for (TipoSolicitud t : values()) {
            if (t.getDescripcion().equalsIgnoreCase(descripcion)) {
                return t;
            }
        }
        return null;
    }
}
    
}
