package com.sdrerc.domain.dto.sdrercapp;

/** Par etiqueta/total genérico, reutilizado por los gráficos de distribución del Dashboard
 * (expedientes por etapa, resultado de análisis, estado final de notificación). */
public class DashboardConteoDTO {

    private final String etiqueta;
    private final int total;

    public DashboardConteoDTO(String etiqueta, int total) {
        this.etiqueta = etiqueta == null ? "" : etiqueta;
        this.total = total;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public int getTotal() {
        return total;
    }
}
