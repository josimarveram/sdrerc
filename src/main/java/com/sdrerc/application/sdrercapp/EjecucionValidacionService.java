package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.EjecucionRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.EjecucionReversionDTO;
import java.util.ArrayList;
import java.util.List;

public class EjecucionValidacionService {

    public void validarExpedienteSeleccionado(Long idExpediente) {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
    }

    public List<String> validarRegistro(EjecucionRegistroDTO registro, boolean requiereComentario) {
        List<String> errores = new ArrayList<String>();
        if (registro == null) {
            errores.add("Complete los datos de ejecución.");
            return errores;
        }
        if (registro.getIdExpediente() == null) {
            errores.add("Seleccione un expediente.");
        }
        if (!hasText(registro.getAccionCodigo())) {
            errores.add("Seleccione la acción de ejecución.");
        }
        if (requiereComentario && !hasText(registro.getComentario())) {
            errores.add("Ingrese el sustento de la ejecución.");
        }
        return errores;
    }

    public List<String> validarReversion(EjecucionReversionDTO reversion) {
        List<String> errores = new ArrayList<String>();
        if (reversion == null) {
            errores.add("Complete los datos de reversión.");
            return errores;
        }
        if (reversion.getIdExpediente() == null) {
            errores.add("Seleccione un expediente.");
        }
        if (!hasText(reversion.getMotivoReversion())) {
            errores.add("Ingrese el motivo de reversión a Análisis.");
        }
        if (!hasText(reversion.getComentario())) {
            errores.add("Ingrese el sustento de la reversión.");
        }
        return errores;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
