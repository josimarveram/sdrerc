package com.sdrerc.domain.model;

import java.util.ArrayList;
import java.util.List;

public class EquipoJuridicoImportPreview {

    private int totalFilasLeidas;
    private int totalValidas;
    private int totalAdvertencias;
    private int totalErrores;
    private final List<EquipoJuridicoImportItem> items = new ArrayList<>();

    public int getTotalFilasLeidas() {
        return totalFilasLeidas;
    }

    public void setTotalFilasLeidas(int totalFilasLeidas) {
        this.totalFilasLeidas = totalFilasLeidas;
    }

    public int getTotalValidas() {
        return totalValidas;
    }

    public void setTotalValidas(int totalValidas) {
        this.totalValidas = totalValidas;
    }

    public int getTotalAdvertencias() {
        return totalAdvertencias;
    }

    public void setTotalAdvertencias(int totalAdvertencias) {
        this.totalAdvertencias = totalAdvertencias;
    }

    public int getTotalErrores() {
        return totalErrores;
    }

    public void setTotalErrores(int totalErrores) {
        this.totalErrores = totalErrores;
    }

    public List<EquipoJuridicoImportItem> getItems() {
        return items;
    }

    public void recalcularTotales() {
        totalFilasLeidas = items.size();
        totalValidas = 0;
        totalAdvertencias = 0;
        totalErrores = 0;

        for (EquipoJuridicoImportItem item : items) {
            if (item.tieneErrores()) {
                totalErrores++;
            } else if (item.tieneAdvertencias()) {
                totalAdvertencias++;
            } else {
                totalValidas++;
            }
        }
    }
}
