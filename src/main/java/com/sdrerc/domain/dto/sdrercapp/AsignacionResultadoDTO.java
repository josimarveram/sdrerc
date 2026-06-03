package com.sdrerc.domain.dto.sdrercapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsignacionResultadoDTO {

    private final boolean exitoso;
    private final int totalSolicitados;
    private final int totalAsignados;
    private final String mensaje;
    private final List<String> detalles;

    public AsignacionResultadoDTO(
            boolean exitoso,
            int totalSolicitados,
            int totalAsignados,
            String mensaje,
            List<String> detalles) {
        this.exitoso = exitoso;
        this.totalSolicitados = totalSolicitados;
        this.totalAsignados = totalAsignados;
        this.mensaje = mensaje == null ? "" : mensaje;
        this.detalles = detalles == null
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<String>(detalles));
    }

    public static AsignacionResultadoDTO exito(int totalAsignados, String mensaje, List<String> detalles) {
        return new AsignacionResultadoDTO(true, totalAsignados, totalAsignados, mensaje, detalles);
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public int getTotalSolicitados() {
        return totalSolicitados;
    }

    public int getTotalAsignados() {
        return totalAsignados;
    }

    public String getMensaje() {
        return mensaje;
    }

    public List<String> getDetalles() {
        return detalles;
    }
}
