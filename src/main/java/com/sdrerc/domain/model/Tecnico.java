/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sdrerc.domain.model;

/**
 *
 * @author David
 */
public class Tecnico {
    private int idTecnico;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombres;

    public Tecnico(int idTecnico, String apellidoPaterno, String apellidoMaterno, String nombres) {
        this.idTecnico = idTecnico;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.nombres = nombres;
    }

    public int getIdTecnico() {
        return idTecnico;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public String getNombres() {
        return nombres;
    }

    @Override
    public String toString() {
        return apellidoPaterno + " " + apellidoMaterno + ", " + nombres;
    }
}
