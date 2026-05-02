package com.sdrerc.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EquipoJuridicoImportRowResult {

    private String item;
    private String abogado;
    private String supervisor;
    private String usernameAbogado;
    private String usernameSupervisor;
    private String estado;
    private String mensaje;
    private final List<String> acciones = new ArrayList<>();

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getAbogado() {
        return abogado;
    }

    public void setAbogado(String abogado) {
        this.abogado = abogado;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public String getUsernameAbogado() {
        return usernameAbogado;
    }

    public void setUsernameAbogado(String usernameAbogado) {
        this.usernameAbogado = usernameAbogado;
    }

    public String getUsernameSupervisor() {
        return usernameSupervisor;
    }

    public void setUsernameSupervisor(String usernameSupervisor) {
        this.usernameSupervisor = usernameSupervisor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<String> getAcciones() {
        return acciones;
    }

    public String getAccionesResumen() {
        return acciones.stream().collect(Collectors.joining(", "));
    }
}
