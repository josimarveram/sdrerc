package com.sdrerc.domain.dto.sdrercapp;

public class GrupoFamiliarResultadoDTO {

    private final int totalSolicitados;
    private final int totalAsociados;
    private final int totalYaAsociados;
    private final int totalOmitidos;
    private final String mensaje;

    public GrupoFamiliarResultadoDTO(
            int totalSolicitados,
            int totalAsociados,
            int totalYaAsociados,
            int totalOmitidos,
            String mensaje) {
        this.totalSolicitados = totalSolicitados;
        this.totalAsociados = totalAsociados;
        this.totalYaAsociados = totalYaAsociados;
        this.totalOmitidos = totalOmitidos;
        this.mensaje = mensaje == null ? "" : mensaje;
    }

    public int getTotalSolicitados() {
        return totalSolicitados;
    }

    public int getTotalAsociados() {
        return totalAsociados;
    }

    public int getTotalYaAsociados() {
        return totalYaAsociados;
    }

    public int getTotalOmitidos() {
        return totalOmitidos;
    }

    public String getMensaje() {
        return mensaje;
    }
}
