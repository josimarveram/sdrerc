package com.sdrerc.ui.appv2.util;

public final class DisplayNameMapperV2 {

    private DisplayNameMapperV2() {
    }

    public static String etapa(String codigo) {
        String normalized = normalize(codigo);
        switch (normalized) {
            case "REGISTRO":
                return "Registro";
            case "ASIGNACION":
                return "Asignación";
            case "ANALISIS":
                return "Análisis";
            case "VERIFICACION":
                return "Verificación";
            case "FIRMA_EMISION":
                return "Firma / Emisión";
            case "EJECUCION":
                return "Ejecución";
            case "NOTIFICACION":
                return "Notificación";
            case "PUBLICACION_CONDICIONAL":
                return "Publicación";
            case "EXPEDIENTE_DIGITAL":
                return "Expediente digital";
            case "CIERRE_ARCHIVO":
                return "Cierre / Archivo";
            default:
                return humanize(codigo);
        }
    }

    public static String estado(String codigo) {
        return humanize(codigo);
    }

    public static String accion(String codigo) {
        return humanize(codigo);
    }

    public static boolean estadoObservado(String estado) {
        String normalized = normalize(estado);
        return normalized.contains("OBSERVADO")
                || normalized.contains("REQUIERE_CORRECCION")
                || normalized.contains("DOCUMENTO_INCONSISTENTE");
    }

    private static String humanize(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return "-";
        }
        String[] parts = codigo.trim().toLowerCase().split("_+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                result.append(part.substring(1));
            }
        }
        return result.length() == 0 ? codigo : result.toString();
    }

    private static String normalize(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }
}
