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
        String accion = registro.getAccionCodigo();
        if (("REGISTRO_OBSERVACION_VERIFICACION".equalsIgnoreCase(accion)
                || "REVERSION_ESTADO_DOCUMENTO".equalsIgnoreCase(accion)
                || "DEVOLUCION_A_ANALISIS".equalsIgnoreCase(accion))
                && !hasText(registro.getComentario())) {
            errores.add("Ingrese el motivo o sustento de la verificación.");
        }
        if (("REGISTRO_OBSERVACION_VERIFICACION".equalsIgnoreCase(accion)
                || "REVERSION_ESTADO_DOCUMENTO".equalsIgnoreCase(accion)
                || "DEVOLUCION_A_ANALISIS".equalsIgnoreCase(accion))
                && (registro.getObservacion() == null || !registro.getObservacion().hasDescripcion())) {
            errores.add("Ingrese la observación o inconsistencia detectada.");
        }
        return errores;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
