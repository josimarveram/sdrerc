package com.sdrerc.domain.model;

public class PlazoAtencionResultado {
    public enum Nivel {
        VERDE,
        AMARILLO,
        ROJO,
        VENCIDO,
        SIN_CONFIG
    }

    private final String texto;
    private final String tooltip;
    private final Nivel nivel;
    private final Integer diasRestantes;

    private PlazoAtencionResultado(String texto, String tooltip, Nivel nivel, Integer diasRestantes) {
        this.texto = texto;
        this.tooltip = tooltip;
        this.nivel = nivel;
        this.diasRestantes = diasRestantes;
    }

    public static PlazoAtencionResultado sinConfig() {
        return new PlazoAtencionResultado("Sin config.", "Sin plazo configurado para el tipo de documento.", Nivel.SIN_CONFIG, null);
    }

    public static PlazoAtencionResultado sinFecha() {
        return new PlazoAtencionResultado("Sin fecha", "No existe fecha de solicitud para calcular el plazo.", Nivel.SIN_CONFIG, null);
    }

    public static PlazoAtencionResultado of(int diasRestantes, int diasPlazo, Nivel nivel) {
        String texto = diasRestantes + "d";
        String detalle;
        if (diasRestantes < 0) {
            int exceso = Math.abs(diasRestantes);
            detalle = exceso + (exceso == 1 ? " dia en exceso" : " dias en exceso");
        } else if (diasRestantes == 0) {
            detalle = "Vence hoy";
        } else {
            detalle = diasRestantes + (diasRestantes == 1 ? " dia restante" : " dias restantes");
        }
        String tooltip = detalle + ". Plazo configurado: " + diasPlazo + " dias.";
        return new PlazoAtencionResultado(texto, tooltip, nivel, diasRestantes);
    }

    public String getTexto() {
        return texto;
    }

    public String getTooltip() {
        return tooltip;
    }

    public Nivel getNivel() {
        return nivel;
    }

    public Integer getDiasRestantes() {
        return diasRestantes;
    }

    @Override
    public String toString() {
        return texto;
    }
}
