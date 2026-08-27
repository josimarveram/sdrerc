package com.sdrerc.v3.web.dto;

/** Alta/edición de Administración &gt; Equipo Jurídico. {@code idEquipo} nulo = alta. */
public record GuardarEquipoJuridicoRequest(
        Long idEquipo, Long idArea, String codigo, String nombre, String descripcion, boolean activo, Long idResponsable) {
}
