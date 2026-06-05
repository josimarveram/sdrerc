package com.sdrerc.domain.dto.sdrercapp;

public class ExpedienteDigitalFiltroDTO {

    private final String textoLibre;
    private final String estadoCodigo;
    private final int limite;

    public ExpedienteDigitalFiltroDTO(String textoLibre, String estadoCodigo, int limite) {
        this.textoLibre = safe(textoLibre);
        this.estadoCodigo = safe(estadoCodigo);
        this.limite = limite;
    }

    public String getTextoLibre() {
        return textoLibre;
    }

    public String getEstadoCodigo() {
        return estadoCodigo;
    }

    public int getLimite() {
        return limite;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
