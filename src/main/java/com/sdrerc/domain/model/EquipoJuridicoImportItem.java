package com.sdrerc.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EquipoJuridicoImportItem {

    private String item;
    private String abogado;
    private String supervisor;
    private String personal;
    private String estado;
    private String usernameSugerido;
    private String accionPrevista;
    private final List<EquipoJuridicoImportError> mensajes = new ArrayList<>();

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

    public String getPersonal() {
        return personal;
    }

    public void setPersonal(String personal) {
        this.personal = personal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUsernameSugerido() {
        return usernameSugerido;
    }

    public void setUsernameSugerido(String usernameSugerido) {
        this.usernameSugerido = usernameSugerido;
    }

    public String getAccionPrevista() {
        return accionPrevista;
    }

    public void setAccionPrevista(String accionPrevista) {
        this.accionPrevista = accionPrevista;
    }

    public List<EquipoJuridicoImportError> getMensajes() {
        return mensajes;
    }

    public void agregarError(String mensaje) {
        mensajes.add(new EquipoJuridicoImportError("ERROR", mensaje));
    }

    public void agregarAdvertencia(String mensaje) {
        mensajes.add(new EquipoJuridicoImportError("ADVERTENCIA", mensaje));
    }

    public boolean tieneErrores() {
        return mensajes.stream().anyMatch(EquipoJuridicoImportError::isError);
    }

    public boolean tieneAdvertencias() {
        return mensajes.stream().anyMatch(EquipoJuridicoImportError::isAdvertencia);
    }

    public String getEstadoValidacion() {
        if (tieneErrores()) {
            return "ERROR";
        }
        if (tieneAdvertencias()) {
            return "ADVERTENCIA";
        }
        return "VALIDO";
    }

    public String getMensajesResumen() {
        return mensajes.stream()
                .map(EquipoJuridicoImportError::toString)
                .collect(Collectors.joining(" | "));
    }
}
