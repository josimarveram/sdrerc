package com.sdrerc.v3.application;

import com.sdrerc.v3.domain.VerificacionRegistroDTO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Port literal de com.sdrerc.application.sdrercapp.VerificacionValidacionService (V2) —
 * validación MÍNIMA (2 reglas), mucho más simple que {@code AnalisisValidacionService}: la
 * validación "real" (equipo+usuario destino requeridos en la rama "destino operativo") vive inline
 * en {@code VerificacionExpedienteDAO#aprobarVerificacionConDestino} (misma ubicación que en V2).
 */
@Service
public class VerificacionValidacionService {

    public List<String> validarRegistroVerificacion(VerificacionRegistroDTO registro) {
        List<String> errores = new ArrayList<>();
        if (registro == null || registro.getIdExpediente() == null) {
            errores.add("Seleccione un expediente para registrar la verificación.");
            return errores;
        }
        if (!hasText(registro.getResultadoNombre())) {
            errores.add("Seleccione el resultado de verificación.");
        }
        return errores;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
