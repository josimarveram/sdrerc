package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.CierrePublicacionDTO;
import com.sdrerc.domain.dto.sdrercapp.PublicacionRegistroDTO;
import java.util.ArrayList;
import java.util.List;

public class PublicacionValidacionService {

    public List<String> validarRegistro(PublicacionRegistroDTO registro) {
        List<String> errores = new ArrayList<String>();
        if (registro == null) {
            errores.add("Complete los datos de publicación.");
            return errores;
        }
        if (registro.getIdExpediente() == null) {
            errores.add("Seleccione un expediente.");
        }
        if (!hasText(registro.getAccionCodigo())) {
            errores.add("Seleccione la acción de publicación.");
        }
        if (registro.getFechaPublicacion() == null) {
            errores.add("Ingrese la fecha de publicación.");
        }
        if (!hasText(registro.getMedioPublicacion())) {
            errores.add("Ingrese el medio de publicación.");
        }
        return errores;
    }

    public List<String> validarCierre(CierrePublicacionDTO cierre) {
        List<String> errores = new ArrayList<String>();
        if (cierre == null) {
            errores.add("Complete los datos de cierre.");
            return errores;
        }
        if (cierre.getIdExpediente() == null) {
            errores.add("Seleccione un expediente.");
        }
        if (!hasText(cierre.getAccionCodigo())) {
            errores.add("Seleccione la acción de cierre.");
        }
        return errores;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
