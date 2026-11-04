package com.sdrerc.v3.domain;

/**
 * Catálogo mínimo de {@code ROL} (id/código/nombre), usado por Administración &gt; Usuarios para
 * el multi-select "Roles y equipos". Administración &gt; Roles (incremento siguiente) reemplazará
 * {@link com.sdrerc.v3.infrastructure.dao.RolAdminDAO} con el CRUD completo — este record se
 * reutilizará tal cual como catálogo liviano, no se descarta.
 */
public record RolResumenDTO(Long idRol, String codigo, String nombre) {
}
