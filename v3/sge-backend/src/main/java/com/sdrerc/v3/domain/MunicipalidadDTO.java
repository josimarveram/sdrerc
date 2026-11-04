package com.sdrerc.v3.domain;

/**
 * Fila de la tabla MUNICIPALIDADES (catálogo, ver scripts 103/104/105 y v3/CLAUDE.md, 4to tipo de
 * solicitante "Municipalidad"). Solo los campos que necesita el selector de Solicitante: buscar por
 * denominación/ubicación y mostrar el resumen de solo lectura tras seleccionar.
 *
 * <p>{@code centroPoblado} (18/08/2026, pedido explícito del usuario): agregado para poder
 * desambiguar homónimos reales del catálogo — hay 141 filas con la misma {@code denominacion} pero
 * distinta ubicación (ej. dos "Municipalidad de Centro Poblado Bello Horizonte", una en
 * Arequipa/Caylloma/Majes y otra en San Martín/San Martín/La Banda de Shilcayo). Solo tiene valor
 * real para tipo {@code MUNICIPALIDAD DE CENTRO POBLADO}/{@code COMUNIDAD NATIVA} (en
 * {@code DISTRITO}/{@code PROVINCIA} queda {@code NULL} en la tabla, ver script 103) — es distinto
 * de {@code distrito} (el distrito al que pertenece el centro poblado, no el propio centro poblado).
 */
public record MunicipalidadDTO(
        Long idMunicipalidad,
        String denominacion,
        String tipo,
        String codigo,
        String departamento,
        String provincia,
        String distrito,
        String centroPoblado) {
}
