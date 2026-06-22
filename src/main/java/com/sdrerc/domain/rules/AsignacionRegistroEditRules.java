package com.sdrerc.domain.rules;

import java.text.Normalizer;
import java.util.Locale;

public final class AsignacionRegistroEditRules {

    private static final String PROCEDIMIENTO_RECONSIDERACION = "RECONSIDERACION";
    private static final String PROCEDIMIENTO_APELACION = "APELACION";

    private AsignacionRegistroEditRules() {
    }

    public static boolean esProcedimientoPermitido(String value) {
        String normalized = normalizar(value);
        return PROCEDIMIENTO_RECONSIDERACION.equals(normalized)
                || PROCEDIMIENTO_APELACION.equals(normalized)
                || normalized.contains(PROCEDIMIENTO_RECONSIDERACION)
                || normalized.contains(PROCEDIMIENTO_APELACION);
    }

    public static String mensajeProcedimientoPermitido() {
        return "En Asignación solo se permite cambiar el procedimiento registral a Reconsideración o Apelación.";
    }

    public static String normalizar(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        return normalized.replaceAll("[^A-Z0-9]", "_").replaceAll("_+", "_").replaceAll("^_|_$", "");
    }
}
