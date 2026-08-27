package com.sdrerc.v3.domain;

/**
 * Fila de la previsualización de importación Excel de "Personal por supervisor" (Administración
 * &gt; Equipo Jurídico), versión simplificada respecto a la plantilla legacy de V1
 * ({@code EquipoJuridicoImportService}, ~2570 líneas entre import/plantilla/service): solo
 * vincula abogado↔supervisor por {@code username} de usuarios YA EXISTENTES (sin crear personas
 * ni contraseñas, sin parseo ambiguo de nombres libres). {@code estadoValidacion} es
 * {@code VALIDO}/{@code ADVERTENCIA}/{@code ERROR}, mismo criterio de 3 niveles que V1.
 */
public class EquipoSupervisionImportItemDTO {

    private int fila;
    private String usuarioAbogado;
    private String usuarioSupervisor;
    private Long idAbogado;
    private String nombreAbogado;
    private Long idSupervisor;
    private String nombreSupervisor;
    private String estadoValidacion;
    private String mensaje;

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }

    public String getUsuarioAbogado() {
        return usuarioAbogado;
    }

    public void setUsuarioAbogado(String usuarioAbogado) {
        this.usuarioAbogado = usuarioAbogado;
    }

    public String getUsuarioSupervisor() {
        return usuarioSupervisor;
    }

    public void setUsuarioSupervisor(String usuarioSupervisor) {
        this.usuarioSupervisor = usuarioSupervisor;
    }

    public Long getIdAbogado() {
        return idAbogado;
    }

    public void setIdAbogado(Long idAbogado) {
        this.idAbogado = idAbogado;
    }

    public String getNombreAbogado() {
        return nombreAbogado;
    }

    public void setNombreAbogado(String nombreAbogado) {
        this.nombreAbogado = nombreAbogado;
    }

    public Long getIdSupervisor() {
        return idSupervisor;
    }

    public void setIdSupervisor(Long idSupervisor) {
        this.idSupervisor = idSupervisor;
    }

    public String getNombreSupervisor() {
        return nombreSupervisor;
    }

    public void setNombreSupervisor(String nombreSupervisor) {
        this.nombreSupervisor = nombreSupervisor;
    }

    public String getEstadoValidacion() {
        return estadoValidacion;
    }

    public void setEstadoValidacion(String estadoValidacion) {
        this.estadoValidacion = estadoValidacion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isImportable() {
        return "VALIDO".equals(estadoValidacion) || "ADVERTENCIA".equals(estadoValidacion);
    }
}
