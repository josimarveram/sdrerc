package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.ExpedienteDigitalRegistroDTO;
import java.util.ArrayList;
import java.util.List;

public class ExpedienteDigitalValidacionService {

    public List<String> validarCarpeta(ExpedienteDigitalRegistroDTO registro) {
        List<String> errores = validarBase(registro);
        if (registro != null && !hasText(registro.getRutaCarpeta())) {
            errores.add("Ingrese la ruta o carpeta digital.");
        }
        return errores;
    }

    public List<String> validarEnlace(ExpedienteDigitalRegistroDTO registro) {
        List<String> errores = validarBase(registro);
        if (registro != null && !hasText(registro.getEnlaceCarpeta())) {
            errores.add("Ingrese el enlace del expediente digital.");
        }
        return errores;
    }

    public List<String> validarMarcadoCompleto(ExpedienteDigitalRegistroDTO registro) {
        List<String> errores = validarBase(registro);
        return errores;
    }

    private List<String> validarBase(ExpedienteDigitalRegistroDTO registro) {
        List<String> errores = new ArrayList<String>();
        if (registro == null) {
            errores.add("Complete los datos del expediente digital.");
            return errores;
        }
        if (registro.getIdExpediente() == null) {
            errores.add("Seleccione un expediente.");
        }
        if (!hasText(registro.getAccionCodigo())) {
            errores.add("Seleccione la acción de expediente digital.");
        }
        return errores;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
