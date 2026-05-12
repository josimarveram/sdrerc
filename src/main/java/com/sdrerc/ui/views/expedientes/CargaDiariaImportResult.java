package com.sdrerc.ui.views.expedientes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CargaDiariaImportResult {

    private final List<CargaDiariaExcelRow> filas = new ArrayList<>();
    private int registrados;
    private int omitidos;

    public void addFila(CargaDiariaExcelRow fila) {
        filas.add(fila);
    }

    public List<CargaDiariaExcelRow> getFilas() {
        return Collections.unmodifiableList(filas);
    }

    public int getTotal() {
        return filas.size();
    }

    public int getValidas() {
        return contarPorEstado(CargaDiariaExcelRow.ESTADO_VALIDO);
    }

    public int getAdvertencias() {
        return contarPorEstado(CargaDiariaExcelRow.ESTADO_ADVERTENCIA);
    }

    public int getErrores() {
        return contarPorEstado(CargaDiariaExcelRow.ESTADO_ERROR);
    }

    public int getDuplicadas() {
        return contarPorEstado(CargaDiariaExcelRow.ESTADO_DUPLICADO);
    }

    public int getImportables() {
        int total = 0;
        for (CargaDiariaExcelRow fila : filas) {
            if (fila.esImportable()) {
                total++;
            }
        }
        return total;
    }

    public int getRegistrados() {
        return registrados;
    }

    public void setRegistrados(int registrados) {
        this.registrados = registrados;
    }

    public int getOmitidos() {
        return omitidos;
    }

    public void setOmitidos(int omitidos) {
        this.omitidos = omitidos;
    }

    private int contarPorEstado(String estado) {
        int total = 0;
        for (CargaDiariaExcelRow fila : filas) {
            if (estado.equals(fila.getEstadoValidacion())) {
                total++;
            }
        }
        return total;
    }
}
