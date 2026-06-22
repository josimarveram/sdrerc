package com.sdrerc.domain.rules;

import java.text.Normalizer;
import java.util.Locale;

public final class ProcedimientoRegistralRules {

    private ProcedimientoRegistralRules() {
    }

    public static boolean requiereDecisionAsignacionParaNumero(String procedimiento) {
        String normalized = normalizar(procedimiento);
        return "RECONSIDERACION".equals(normalized)
                || "APELACION".equals(normalized)
                || normalized.contains("RECONSIDERACION")
                || normalized.contains("APELACION");
    }

    public static String mensajeSinNumeroRecepcion() {
        return "Procedimiento registral Reconsideración/Apelación: se registrará sin número de expediente. "
                + "En Asignación se podrá asociar a un expediente principal o generar número a criterio del asignador.";
    }

    public static String etiquetaSinNumero() {
        return "procedimiento";
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
