package com.sdrerc.application.sdrercapp;

import com.sdrerc.domain.dto.sdrercapp.AnalisisRegistroDTO;
import com.sdrerc.domain.dto.sdrercapp.DocumentoAnalizadoDTO;
import java.util.ArrayList;
import java.util.Collections;
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
        boolean noCorresponde = "NO_CORRESPONDE".equalsIgnoreCase(registro.getResultadoCodigo());
        if (!noCorresponde && registro.getIncorporado() == null) {
            errores.add("Seleccione si el acta está incorporada.");
        }
        if (!hasText(registro.getFundamento())) {
            errores.add("Ingrese el sustento o conclusión del análisis.");
        }
        if (!noCorresponde && registro.getDocumentosAnalizados().isEmpty()) {
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
        if (noCorresponde) {
            if (!hasText(registro.getMotivoNoCorrespondeCodigo())) {
                errores.add("Seleccione el motivo por el que no corresponde a SDRERC.");
            }
            if (!hasText(registro.getNumeroDocumentoProveido())) {
                errores.add("Ingrese el N° Documento (Proveído).");
            }
        }
        if (registro.getNumeroDocumentoProveido().length() > 100) {
            errores.add("El N° Documento (Proveído) no debe exceder 100 caracteres.");
        }
        String resultado = registro.getResultadoCodigo();
        if (("OBSERVADO".equalsIgnoreCase(resultado)
                || "OBSERVACION_ADMINISTRATIVA".equalsIgnoreCase(resultado))
                && (registro.getObservacion() == null || !registro.getObservacion().hasDescripcion())) {
            errores.add("Ingrese la observación del análisis.");
        }
        return errores;
    }

    public List<String> validarDocumentosAnalisis(Long idExpediente, List<DocumentoAnalizadoDTO> documentos) {
        List<String> errores = new ArrayList<>();
        if (idExpediente == null) {
            errores.add("Seleccione un expediente para guardar documentos de análisis.");
        }
        List<DocumentoAnalizadoDTO> items = documentos == null
                ? Collections.<DocumentoAnalizadoDTO>emptyList()
                : documentos;
        if (items.isEmpty()) {
            errores.add("Agregue al menos un documento de análisis.");
            return errores;
        }
        for (DocumentoAnalizadoDTO documento : items) {
            if (!hasText(documento.getTipoDocumentoCodigo())) {
                errores.add("Seleccione el tipo de cada documento de análisis.");
                break;
            }
            if (!hasText(documento.getEstadoDocumentoCodigo())) {
                errores.add("Seleccione el estado de cada documento de análisis.");
                break;
            }
            if (!hasText(documento.getDescripcion())) {
                errores.add("Ingrese la descripción de cada documento de análisis.");
                break;
            }
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
