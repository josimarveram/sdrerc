package com.sdrerc.v3.domain;

/**
 * Fila de la tabla UNIDADES_ORGANICAS (catálogo fijo de 8 valores, ver script 101 y v3/CLAUDE.md,
 * iniciativa "Solicitante con 3 tipos"). {@code glosa} son las siglas de la dirección/sub dirección
 * (ej. "SDPRC"), agregadas por el script 107 para que Carga diaria también detecte la unidad cuando
 * "SOLICITADO POR" trae las siglas en vez del nombre completo (ver CargaDiariaReglasService).
 */
public record UnidadOrganicaDTO(Long idUnidadOrganica, String nombre, String glosa) {
}
