package com.sdrerc.application.sdrercapp;

import java.time.LocalDate;

public class CorrelativoExpedienteService {

    public int anioActual() {
        return LocalDate.now().getYear();
    }

    public String generarPreliminar(int correlativo) {
        return generar(anioActual(), correlativo);
    }

    public String generar(int anio, int correlativo) {
        return String.format("SDRERC-EXP-%d-%06d", anio, Math.max(correlativo, 1));
    }

    public String generarDesdeId(Long idExpediente) {
        if (idExpediente == null || idExpediente <= 0) {
            throw new IllegalArgumentException("No se pudo generar el número de expediente: falta id de expediente.");
        }
        return generar(anioActual(), idExpediente.intValue());
    }
}
