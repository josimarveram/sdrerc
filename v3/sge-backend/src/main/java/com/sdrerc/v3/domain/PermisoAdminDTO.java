package com.sdrerc.v3.domain;

/**
 * Port literal de {@code com.sdrerc.domain.dto.sdrercapp.PermisoDTO} (V2): un permiso del
 * catálogo {@code PERMISO}, con {@code asignado} indicando si ya está otorgado al rol consultado
 * (ver {@code PermisoAdminDAO.listarPermisosPorRol}). Se agrupa por {@code modulo} en el frontend
 * (Accordion) — mejora sobre la tabla plana sin agrupar de V2 (`JPanelRolesV2`).
 */
public record PermisoAdminDTO(Long idPermiso, String codigo, String nombre, String modulo, boolean activo, boolean asignado) {
}
