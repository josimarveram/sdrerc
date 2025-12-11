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
    private int numeroDocumento;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombres;
    private String nombreCompleto;

    public Tecnico(int idTecnico, int numeroDocumento,String apellidoPaterno, String apellidoMaterno, String nombres, String nombreCompleto) {
        this.idTecnico = idTecnico;
        this.numeroDocumento = numeroDocumento;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.nombres = nombres;
        this.nombreCompleto = nombreCompleto;
    }

    public int getIdTecnico() {
        return idTecnico;
    }
    
    public int getNumeroDocumento() {
        return numeroDocumento;
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
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    @Override
    public String toString() {
        return apellidoPaterno + " " + apellidoMaterno + ", " + nombres;
    }
}
