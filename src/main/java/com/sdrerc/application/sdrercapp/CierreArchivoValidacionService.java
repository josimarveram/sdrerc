package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.ArchivoExpedienteDTO;
import com.sdrerc.domain.dto.sdrercapp.CierreExpedienteDTO;
import java.util.ArrayList;
import java.util.List;

public class CierreArchivoValidacionService {

    public List<String> validarCierre(CierreExpedienteDTO cierre) {
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

    public List<String> validarArchivo(ArchivoExpedienteDTO archivo) {
        List<String> errores = new ArrayList<String>();
        if (archivo == null) {
            errores.add("Complete los datos de archivo.");
            return errores;
        }
        if (archivo.getIdExpediente() == null) {
            errores.add("Seleccione un expediente.");
        }
        if (!hasText(archivo.getAccionCodigo())) {
            errores.add("Seleccione la acción de archivo.");
        }
        if (!hasText(archivo.getMotivo())) {
            errores.add("Ingrese el motivo de archivo.");
        }
        if (!hasText(archivo.getComentario())) {
            errores.add("Ingrese el sustento de archivo.");
        }
        return errores;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
