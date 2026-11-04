package com.sdrerc.v3.domain;

import java.util.List;

/**
 * Identidad del usuario autenticado, resuelta por request a partir del JWT (nunca de un
 * singleton estatico). Reemplaza en V3 al {@code SessionContext} estatico de V2 (que asumia
 * "un proceso JVM = un usuario", valido en escritorio pero no en un servidor web con requests
 * concurrentes de usuarios distintos). Ver plan de migracion, seccion 6, riesgo 1.
 */
public record AuthenticatedUser(Long idUsuario, String username, String nombreCompleto, List<String> roles) {

    public boolean hasRole(String codigoRol) {
        return codigoRol != null && roles.stream().anyMatch(r -> r.equalsIgnoreCase(codigoRol));
    }
}
