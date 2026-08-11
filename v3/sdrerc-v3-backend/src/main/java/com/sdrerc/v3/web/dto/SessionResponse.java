package com.sdrerc.v3.web.dto;

import java.util.List;

/**
 * {@code backupCodes} solo viene poblado justo despues de confirmar un enrolamiento TOTP nuevo
 * (se muestran una unica vez, mismo criterio que V2: no se vuelven a exponer despues).
 */
public record SessionResponse(
        String accessToken,
        String username,
        String nombreCompleto,
        List<String> roles,
        List<String> backupCodes) {

    public static SessionResponse sinBackupCodes(String accessToken, String username, String nombreCompleto,
            List<String> roles) {
        return new SessionResponse(accessToken, username, nombreCompleto, roles, null);
    }
}
