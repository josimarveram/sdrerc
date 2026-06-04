package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.AnalisisRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import java.util.ArrayList;
import java.util.List;

public class AnalisisValidacionService {

    public List<String> validarRegistroAnalisis(AnalisisRegistroDTO registro) {
        List<String> errores = new ArrayList<>();
        if (registro == null) {
            errores.add("Complete los datos del análisis.");
            return errores;
        }
        if (registro.getIdExpediente() == null) {
            errores.add("Seleccione un expediente para registrar el análisis.");
        }
        if (!hasText(registro.getResultadoCodigo())) {
            errores.add("Seleccione el resultado del análisis.");
        }
        if (registro.getIncorporado() == null) {
            errores.add("Seleccione si el acta está incorporada.");
        }
        if (!hasText(registro.getFundamento())) {
            errores.add("Ingrese el sustento o conclusión del análisis.");
        }
        if (registro.getDocumentosAnalizados().isEmpty()) {
            errores.add("Agregue al menos un documento analizado.");
        }
        for (DocumentoAnalizadoDTO documento : registro.getDocumentosAnalizados()) {
            if (!hasText(documento.getTipoDocumentoCodigo())) {
                errores.add("Seleccione el tipo de cada documento analizado.");
                break;
            }
            if (!hasText(documento.getEstadoDocumentoCodigo())) {
                errores.add("Seleccione el estado de cada documento analizado.");
                break;
            }
            if (!hasText(documento.getDescripcion())) {
                errores.add("Ingrese la descripción de cada documento analizado.");
                break;
            }
        }
        String resultado = registro.getResultadoCodigo();
        if (("OBSERVADO".equalsIgnoreCase(resultado)
                || "OBSERVACION_ADMINISTRATIVA".equalsIgnoreCase(resultado))
                && (registro.getObservacion() == null || !registro.getObservacion().hasDescripcion())) {
            errores.add("Ingrese la observación del análisis.");
        }
        return errores;
    }

    public void validarExpedienteSeleccionado(Long idExpediente) {
        if (idExpediente == null) {
            throw new IllegalArgumentException("Seleccione un expediente.");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
