package com.sdrerc.v3.domain;

/**
 * Port literal de com.sdrerc.domain.dto.sdrercapp.CatalogoItemDTO (V2). {@code clasificacion}
 * (24/08/2026, nuevo) solo se puebla en {@code CatalogoLookupDAO#listarTiposDocumentoAdjuntoAnalisis}
 * (necesario para que el frontend sepa si un tipo de documento es INTERMEDIO/FINAL al elegirlo en el
 * combo "Tipo documento" de Análisis) — el resto de catálogos que reutilizan este mismo record
 * (canales, procedimientos, tipos de acta, etc.) lo dejan en {@code null} vía el constructor de 2
 * argumentos, sin cambio de comportamiento.
 */
public record CatalogoItemDTO(String codigo, String nombre, String clasificacion) {
    public CatalogoItemDTO(String codigo, String nombre) {
        this(codigo, nombre, null);
    }
}
