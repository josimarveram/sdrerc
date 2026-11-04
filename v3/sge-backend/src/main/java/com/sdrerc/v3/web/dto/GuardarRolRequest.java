package com.sdrerc.v3.web.dto;

/** Alta/edición de Administración &gt; Roles. {@code idRol} nulo = alta; con valor = edición. */
public record GuardarRolRequest(Long idRol, String codigo, String nombre, String descripcion, boolean activo) {
}
