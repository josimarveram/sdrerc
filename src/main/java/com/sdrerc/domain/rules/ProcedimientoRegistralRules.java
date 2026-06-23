package com.sdrerc.domain.rules;

import com.sdrerc.domain.dto.sdrercapp.PlazoConfiguracionDTO;
import java.text.Normalizer;
import java.util.Locale;

public final class ProcedimientoRegistralRules {

    private static final int PLAZO_GENERAL_HABILES = 30;
    private static final int PLAZO_RECONSIDERACION_HABILES = 15;
    private static final int PLAZO_APELACION_HABILES = 30;

    private ProcedimientoRegistralRules() {
    }

    public static boolean requiereDecisionAsignacionParaNumero(String procedimiento) {
        String normalized = normalizar(procedimiento);
        return esReconsideracion(normalized) || esApelacion(normalized);
    }

    public static String resolverCodigoPlazoSolicitud(String procedimiento) {
        String normalized = normalizar(procedimiento);
        if (esReconsideracion(normalized)) {
            return PlazoConfiguracionDTO.CODIGO_SOLICITUD_RECONSIDERACION;
        }
        if (esApelacion(normalized)) {
            return PlazoConfiguracionDTO.CODIGO_SOLICITUD_APELACION;
        }
        if (esRectificacionAdministrativa(normalized)) {
            return PlazoConfiguracionDTO.CODIGO_SOLICITUD_RECTIFICACION_ADMINISTRATIVA;
        }
        return PlazoConfiguracionDTO.CODIGO_SOLICITUD_SDRERC;
    }

    public static int resolverDiasHabilesFallback(String procedimiento) {
        String normalized = normalizar(procedimiento);
        if (esReconsideracion(normalized)) {
            return PLAZO_RECONSIDERACION_HABILES;
        }
        if (esApelacion(normalized)) {
            return PLAZO_APELACION_HABILES;
        }
        return PLAZO_GENERAL_HABILES;
    }

    public static String mensajeSinNumeroRecepcion() {
        return "Procedimiento registral Reconsideración/Apelación: se registrará sin número de expediente. "
                + "En Asignación se podrá asociar a un expediente principal o generar número a criterio del asignador.";
    }

    public static String etiquetaSinNumero() {
        return "procedimiento";
    }

    private static boolean esReconsideracion(String normalized) {
        return "RECONSIDERACION".equals(normalized) || normalized.contains("RECONSIDERACION");
    }

    private static boolean esApelacion(String normalized) {
        return "APELACION".equals(normalized) || normalized.contains("APELACION");
    }

    private static boolean esRectificacionAdministrativa(String normalized) {
        return normalized.contains("RECTIFICACION") && normalized.contains("ADMINISTRATIVA");
    }

    private static String normalizar(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        return normalized.replaceAll("[^A-Z0-9]", "_").replaceAll("_+", "_").replaceAll("^_|_$", "");
    }
}
