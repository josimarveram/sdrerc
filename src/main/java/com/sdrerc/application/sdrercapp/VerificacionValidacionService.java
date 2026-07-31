package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.VerificacionRegistroDTO;
import java.util.ArrayList;
import java.util.List;

public class VerificacionValidacionService {

    public void validarExpedienteSeleccionado(Long idExpediente) {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
    }

    public List<String> validarRegistroVerificacion(VerificacionRegistroDTO registro) {
        List<String> errores = new ArrayList<String>();
        if (registro == null) {
            errores.add("Complete los datos de verificación.");
            return errores;
        }
        if (registro.getIdExpediente() == null) {
            errores.add("Seleccione un expediente para registrar la verificación.");
        }
        if (!hasText(registro.getAccionCodigo())) {
            errores.add("Seleccione el resultado de verificación.");
        }
        return errores;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
