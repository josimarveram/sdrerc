package com.sdrerc.application.sdrercapp;

import java.time.LocalDate;

public class CorrelativoExpedienteService {

    public String generarPreliminar(int indice) {
        return String.format("SDRERC_EXP_%d_%06d", LocalDate.now().getYear(), Math.max(indice, 1));
    }

    public String generarDesdeId(Long idExpediente) {
        if (idExpediente == null || idExpediente <= 0) {
            throw new IllegalArgumentException("No se pudo generar el número de expediente: falta id de expediente.");
        }
        return String.format("SDRERC_EXP_%d_%06d", LocalDate.now().getYear(), idExpediente);
    }
}
