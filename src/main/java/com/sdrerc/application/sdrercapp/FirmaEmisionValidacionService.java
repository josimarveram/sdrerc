package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.FirmaEmisionRegistroDTO;
import java.util.ArrayList;
import java.util.List;

public class FirmaEmisionValidacionService {

    public void validarExpedienteSeleccionado(Long idExpediente) {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
    }

    public List<String> validarRegistro(FirmaEmisionRegistroDTO registro, boolean requiereNumero) {
        List<String> errores = new ArrayList<String>();
        if (registro == null) {
            errores.add("Complete los datos del documento emitido.");
            return errores;
        }
        if (registro.getIdExpediente() == null) {
            errores.add("Seleccione un expediente.");
        }
        if (!hasText(registro.getAccionCodigo())) {
            errores.add("Seleccione la acción a registrar.");
        }
        if (requiereNumero && !hasText(registro.getNumeroResolucion())) {
            errores.add("Ingrese el número de resolución o documento.");
        }
        return errores;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
