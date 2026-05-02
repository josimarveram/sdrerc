package com.sdrerc.domain.model;

import java.util.ArrayList;
import java.util.List;

public class EquipoJuridicoImportResult {

    private int totalFilas;
    private int importadas;
    private int omitidas;
    private int fallidas;
    private int conAdvertencias;
    private final List<EquipoJuridicoImportRowResult> resultadosPorFila = new ArrayList<>();

    public int getTotalFilas() {
        return totalFilas;
    }

    public void setTotalFilas(int totalFilas) {
        this.totalFilas = totalFilas;
    }

    public int getImportadas() {
        return importadas;
    }

    public void setImportadas(int importadas) {
        this.importadas = importadas;
    }

    public int getOmitidas() {
        return omitidas;
    }

    public void setOmitidas(int omitidas) {
        this.omitidas = omitidas;
    }

    public int getFallidas() {
        return fallidas;
    }

    public void setFallidas(int fallidas) {
        this.fallidas = fallidas;
    }

    public int getConAdvertencias() {
        return conAdvertencias;
    }

    public void setConAdvertencias(int conAdvertencias) {
        this.conAdvertencias = conAdvertencias;
    }

    public List<EquipoJuridicoImportRowResult> getResultadosPorFila() {
        return resultadosPorFila;
    }

    public void recalcularTotales() {
        totalFilas = resultadosPorFila.size();
        importadas = 0;
        omitidas = 0;
        fallidas = 0;

        for (EquipoJuridicoImportRowResult row : resultadosPorFila) {
            if ("IMPORTADO".equals(row.getEstado())) {
                importadas++;
            } else if ("OMITIDO".equals(row.getEstado())) {
                omitidas++;
            } else if ("ERROR".equals(row.getEstado())) {
                fallidas++;
            }
        }
    }
}
