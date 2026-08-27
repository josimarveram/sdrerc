package com.sdrerc.v3.web.dto;

import java.util.List;

/**
 * {@code backupCodes} solo viene poblado justo despues de confirmar un enrolamiento TOTP nuevo
 * (se muestran una unica vez, mismo criterio que V2: no se vuelven a exponer despues).
 *
 * <p>{@code permisos} (20/08/2026): códigos {@code PERMISO} (MENU_* / BANDEJA_*) resueltos para los
 * roles activos del usuario — ver {@code PermisoRolDAO}. Lista vacía = fail-open (mismo criterio
 * que V2), el frontend debe interpretarlo como "sin restricciones conocidas, mostrar todo".</p>
 */
public record SessionResponse(
        String accessToken,
        String username,
        String nombreCompleto,
        List<String> roles,
        List<String> permisos,
        List<String> backupCodes) {

    public static SessionResponse sinBackupCodes(String accessToken, String username, String nombreCompleto,
            List<String> roles, List<String> permisos) {
        return new SessionResponse(accessToken, username, nombreCompleto, roles, permisos, null);
    }
}
