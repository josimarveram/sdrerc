package com.sdrerc.v3.web.dto;

import java.util.List;

/**
 * Alta/edición de Administración &gt; Usuarios. {@code idUsuario} nulo = alta; con valor = edición.
 */
public record GuardarUsuarioRequest(
        Long idUsuario,
        String username,
        String nombreCompleto,
        String tipoDocumento,
        String numeroDocumento,
        String correo,
        String telefono,
        boolean activo,
        List<Long> idsRoles,
        List<Long> idsEquipos) {
}
