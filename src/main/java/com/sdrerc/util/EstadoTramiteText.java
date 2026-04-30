package com.sdrerc.util;

public final class EstadoTramiteText {

    private EstadoTramiteText() {
    }

    public static String paraFiltro(String descripcion) {
        if (descripcion == null) {
            return "";
        }

        String texto = descripcion.trim();
        if (texto.equalsIgnoreCase("Registro Expediente")
                || texto.equalsIgnoreCase("Expediente Registro")) {
            return "REGISTRADO";
        }

        texto = texto.replaceAll("(?i)\\bexpediente\\b", "").trim();
        texto = texto.replaceAll("\\s+", " ");
        return texto.toUpperCase();
    }
}
