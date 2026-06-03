package com.sdrerc.domain.dto.sdrercapp;

public class ExpedienteRelacionResultadoDTO {

    private final int totalSolicitados;
    private final int totalAsociados;
    private final int totalOmitidos;
    private final String mensaje;

    public ExpedienteRelacionResultadoDTO(int totalSolicitados, int totalAsociados, int totalOmitidos, String mensaje) {
        this.totalSolicitados = totalSolicitados;
        this.totalAsociados = totalAsociados;
        this.totalOmitidos = totalOmitidos;
        this.mensaje = mensaje == null ? "" : mensaje;
    }

    public int getTotalSolicitados() {
        return totalSolicitados;
    }

    public int getTotalAsociados() {
        return totalAsociados;
    }

    public int getTotalOmitidos() {
        return totalOmitidos;
    }

    public String getMensaje() {
        return mensaje;
    }
}
