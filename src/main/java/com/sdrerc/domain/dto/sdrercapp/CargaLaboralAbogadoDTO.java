package com.sdrerc.domain.dto.sdrercapp;

public class CargaLaboralAbogadoDTO {

    private final Long idUsuario;
    private final String abogado;
    private final String supervisor;
    private final int enAnalisis;
    private final String analisisDetalle;
    private final int enVerificacion;
    private final String verificacionDetalle;
    private final int enEjecucion;
    private final String ejecucionDetalle;
    private final int porVencer;
    private final int vencidos;

    public CargaLaboralAbogadoDTO(
            Long idUsuario,
            String abogado,
            String supervisor,
            int enAnalisis,
            String analisisDetalle,
            int enVerificacion,
            String verificacionDetalle,
            int enEjecucion,
            String ejecucionDetalle,
            int porVencer,
            int vencidos) {
        this.idUsuario = idUsuario;
        this.abogado = safe(abogado);
        this.supervisor = safe(supervisor);
        this.enAnalisis = enAnalisis;
        this.analisisDetalle = safe(analisisDetalle);
        this.enVerificacion = enVerificacion;
        this.verificacionDetalle = safe(verificacionDetalle);
        this.enEjecucion = enEjecucion;
        this.ejecucionDetalle = safe(ejecucionDetalle);
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

    public int getEnAnalisis() {
        return enAnalisis;
    }

    public String getAnalisisDetalle() {
        return analisisDetalle;
    }

    public int getEnVerificacion() {
        return enVerificacion;
    }

    public String getVerificacionDetalle() {
        return verificacionDetalle;
    }

    public int getEnEjecucion() {
        return enEjecucion;
    }

    public String getEjecucionDetalle() {
        return ejecucionDetalle;
    }

    public int getPorVencer() {
        return porVencer;
    }

    public int getVencidos() {
        return vencidos;
    }

    public int getCargaTotal() {
        return enAnalisis + enVerificacion + enEjecucion;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
