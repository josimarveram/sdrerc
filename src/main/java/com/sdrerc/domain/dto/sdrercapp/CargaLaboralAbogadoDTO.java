package com.sdrerc.domain.dto.sdrercapp;

public class CargaLaboralAbogadoDTO {

    private final Long idUsuario;
    private final String abogado;
    private final String equipo;
    private final int expedientesActivos;
    private final int porVencer;
    private final int vencidos;
    private final int enAnalisis;

    public CargaLaboralAbogadoDTO(
            Long idUsuario,
            String abogado,
            String equipo,
            int expedientesActivos,
            int porVencer,
            int vencidos,
            int enAnalisis) {
        this.idUsuario = idUsuario;
        this.abogado = safe(abogado);
        this.equipo = safe(equipo);
        this.expedientesActivos = expedientesActivos;
        this.porVencer = porVencer;
        this.vencidos = vencidos;
        this.enAnalisis = enAnalisis;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getAbogado() {
        return abogado;
    }

    public String getEquipo() {
        return equipo;
    }

    public int getExpedientesActivos() {
        return expedientesActivos;
    }

    public int getPorVencer() {
        return porVencer;
    }

    public int getVencidos() {
        return vencidos;
    }

    public int getEnAnalisis() {
        return enAnalisis;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
