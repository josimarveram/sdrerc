package com.sdrerc.v3.web.dto;

import java.util.List;

/** "Guardar permisos" de Administración &gt; Roles &gt; "Permisos del rol". */
public record GuardarPermisosRolRequest(List<Long> idsPermiso) {
}
