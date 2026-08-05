package com.sdrerc.domain.dto.sdrercapp;

public class CargaLaboralAbogadoDTO {

    private final Long idUsuario;
    private final String abogado;
    private final String supervisor;
    private final int analisisPorRecibir;
    private final int analisisEnProceso;
    private final int analisisObservado;
    private final int analisisCartaIntermedia;
    private final int enVerificacion;
    private final int enEjecucion;
    private final int porVencer;
    private final int vencidos;

    public CargaLaboralAbogadoDTO(
            Long idUsuario,
            String abogado,
            String supervisor,
            int analisisPorRecibir,
            int analisisEnProceso,
            int analisisObservado,
            int analisisCartaIntermedia,
            int enVerificacion,
            int enEjecucion,
            int porVencer,
            int vencidos) {
        this.idUsuario = idUsuario;
        this.abogado = safe(abogado);
        this.supervisor = safe(supervisor);
        this.analisisPorRecibir = analisisPorRecibir;
        this.analisisEnProceso = analisisEnProceso;
        this.analisisObservado = analisisObservado;
        this.analisisCartaIntermedia = analisisCartaIntermedia;
        this.enVerificacion = enVerificacion;
        this.enEjecucion = enEjecucion;
        this.porVencer = porVencer;
        this.vencidos = vencidos;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getAbogado() {
        return abogado;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public int getAnalisisPorRecibir() {
        return analisisPorRecibir;
    }

    public int getAnalisisEnProceso() {
        return analisisEnProceso;
    }

    public int getAnalisisObservado() {
        return analisisObservado;
    }

    public int getAnalisisCartaIntermedia() {
        return analisisCartaIntermedia;
    }

    /** Total de la carga de Analisis (suma de las 4 subcolumnas), incluye lo asignado aun no recibido. */
    public int getEnAnalisis() {
        return analisisPorRecibir + analisisEnProceso + analisisObservado + analisisCartaIntermedia;
    }

    public int getEnVerificacion() {
        return enVerificacion;
    }

    public int getEnEjecucion() {
        return enEjecucion;
    }

    public int getPorVencer() {
        return porVencer;
    }

    public int getVencidos() {
        return vencidos;
    }

    public int getCargaTotal() {
        return getEnAnalisis() + enVerificacion + enEjecucion;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
