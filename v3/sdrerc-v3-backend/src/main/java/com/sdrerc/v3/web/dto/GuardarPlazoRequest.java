package com.sdrerc.v3.web.dto;

import java.time.LocalDate;

/** Alta/edición de Administración &gt; Plazos. {@code idPlazoConfiguracion} nulo = alta. */
public record GuardarPlazoRequest(
        Long idPlazoConfiguracion,
        String codigo,
        String nombre,
        String ambito,
        Integer diasPlazo,
        String unidadPlazo,
        Integer porcentajeVerdeDesde,
        Integer porcentajeAmarilloDesde,
        Integer porcentajeRojoDesde,
        LocalDate fechaVigenciaDesde,
        LocalDate fechaVigenciaHasta,
        boolean activo,
        String observacion) {
}
