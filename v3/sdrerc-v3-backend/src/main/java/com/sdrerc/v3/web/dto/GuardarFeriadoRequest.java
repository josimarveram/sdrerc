package com.sdrerc.v3.web.dto;

import java.time.LocalDate;

/** Alta/edición de Administración &gt; Feriados. {@code idFeriado} nulo = alta. */
public record GuardarFeriadoRequest(Long idFeriado, LocalDate fecha, String nombre, String tipo, boolean activo, String observacion) {
}
