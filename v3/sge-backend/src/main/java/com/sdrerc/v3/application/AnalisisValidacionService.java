package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.AnalisisRegistroDTO;
import com.sdrerc.v3.domain.DocumentoAnalizadoDTO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Port literal de com.sdrerc.application.sdrercapp.AnalisisValidacionService (V2) — validación
 * previa de "Registrar resultado final" (antes de mostrar el diálogo de confirmación, ver
 * {@code CLAUDE.md} raíz: "Validación previa clara ANTES de confirmar"). {@code documentosAnalizados}
 * del {@code AnalisisRegistroDTO} recibido aquí es la lista YA CARGADA por el frontend en la grilla
 * maestro-detalle (para poder validar "al menos 1 documento activo" sin una consulta extra) —
 * distinto de {@code AnalisisExpedienteDAO#registrarAnalisis}, que ignora ese campo porque los
 * documentos ya están persistidos por separado (grid de guardado por fila).
 */
@Service
public class AnalisisValidacionService {

    public List<String> validarRegistroAnalisis(AnalisisRegistroDTO registro) {
        List<String> errores = new ArrayList<>();
        if (registro == null || registro.getIdExpediente() == null) {
            errores.add("Seleccione un expediente para registrar el análisis.");
            return errores;
        }
        if (!hasText(registro.getResultadoCodigo())) {
            errores.add("Seleccione el resultado del análisis.");
        }
        boolean noCorresponde = "NO_CORRESPONDE".equalsIgnoreCase(registro.getResultadoCodigo());
        if (!noCorresponde && registro.getIncorporado() == null) {
            errores.add("Seleccione si el acta está incorporada.");
        }
        List<DocumentoAnalizadoDTO> documentos = registro.getDocumentosAnalizados();
        if (!noCorresponde && !tieneDocumentosActivos(documentos)) {
            errores.add("Agregue al menos un documento analizado.");
        }
        for (DocumentoAnalizadoDTO documento : documentos) {
            if (documento == null || !documento.isActivo()) {
                continue;
            }
            if (!hasText(documento.getTipoDocumentoCodigo())) {
                errores.add("Seleccione el tipo de cada documento analizado.");
                break;
            }
            if (!hasText(documento.getEstadoDocumentoCodigo())) {
                errores.add("Seleccione el estado de cada documento analizado.");
                break;
            }
            if (documento.getNumeroDocumento() != null && documento.getNumeroDocumento().length() > 120) {
                errores.add("El N° Documento de análisis no debe exceder 120 caracteres.");
                break;
            }
        }
        if (noCorresponde) {
            if (!hasText(registro.getMotivoNoCorrespondeCodigo())) {
                errores.add("Seleccione el motivo por el que no corresponde a SDRERC.");
            }
            if (!hasText(registro.getTipoDocumentoNoCorrespondeCodigo())) {
                errores.add("Seleccione el tipo de documento.");
            }
            if (!hasText(registro.getNumeroDocumentoProveido())) {
                errores.add("Ingrese el N° Documento.");
            }
        }
        if (registro.getNumeroDocumentoProveido() != null && registro.getNumeroDocumentoProveido().length() > 100) {
            errores.add("El N° Documento no debe exceder 100 caracteres.");
        }
        String resultado = registro.getResultadoCodigo();
        if (("OBSERVADO".equalsIgnoreCase(resultado) || "OBSERVACION_ADMINISTRATIVA".equalsIgnoreCase(resultado))
                && (registro.getObservacion() == null || !registro.getObservacion().hasDescripcion())) {
            errores.add("Ingrese la observación del análisis.");
        }
        return errores;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static boolean tieneDocumentosActivos(List<DocumentoAnalizadoDTO> documentos) {
        if (documentos == null) {
            return false;
        }
        for (DocumentoAnalizadoDTO documento : documentos) {
            if (documento != null && documento.isActivo()) {
                return true;
            }
        }
        return false;
    }
}
