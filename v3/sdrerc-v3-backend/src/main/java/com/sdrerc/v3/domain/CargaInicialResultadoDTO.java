package com.sdrerc.v3.domain;

/**
 * Resumen agregado de una corrida del batch de carga inicial (ver v3/CLAUDE.md) — solo conteos,
 * nunca datos de fila. Seguro de imprimir/loguear en consola al terminar el batch.
 */
public class CargaInicialResultadoDTO {

    private final int totalFilas;
    private final int importados;
    private final int incompletos;
    private final int errores;

    public CargaInicialResultadoDTO(int totalFilas, int importados, int incompletos, int errores) {
        this.totalFilas = totalFilas;
        this.importados = importados;
        this.incompletos = incompletos;
        this.errores = errores;
    }

    public int getTotalFilas() {
        return totalFilas;
    }

    public int getImportados() {
        return importados;
    }

    public int getIncompletos() {
        return incompletos;
    }

    public int getErrores() {
        return errores;
    }

    @Override
    public String toString() {
        return "CargaInicialResultadoDTO{totalFilas=" + totalFilas
                + ", importados=" + importados
                + ", incompletos=" + incompletos
                + ", errores=" + errores + "}";
    }
}
