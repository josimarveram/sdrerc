package com.sdrerc.v3.domain;

/**
 * Fila de la tabla CENTROS_ATENCION (catálogo, ver script 101/102 y v3/CLAUDE.md, iniciativa
 * "Solicitante con 3 tipos"). Solo los campos que necesita el selector de Solicitante: buscar por
 * denominación/código SIO/ubicación y mostrar el resumen de solo lectura tras seleccionar.
 */
public record CentroAtencionDTO(
        Long idCentroAtencion,
        String denominacion,
        String categorizacion,
        String departamento,
        String provincia,
        String distrito,
        String tipoLocal,
        String oficinaRegional,
        String codigoSio,
        // Nombre con el que se conocía la oficina antes de la Res.Jef. N° 000019-2026/JNAC/RENIEC
        // (ej. "OR CHICLAYO"). 14/08/2026, Carga diaria (ver v3/CLAUDE.md, iniciativa "Solicitante
        // con 3 tipos"): el texto libre "SOLICITADO POR" del Excel real todavía usa esta nomenclatura
        // antigua en la enorme mayoría de los casos, así que la identificación automática de Centro
        // de Atención necesita comparar contra este campo además de `denominacion`.
        String nombreAnteriorRes019) {
}
