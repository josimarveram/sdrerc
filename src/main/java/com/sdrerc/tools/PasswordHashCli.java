package com.sdrerc.tools;

import com.sdrerc.infrastructure.security.PasswordEncoder;

/**
 * Utilidad standalone (no se referencia desde la aplicacion) para generar el
 * hash BCrypt de una contraseña temporal, usado exclusivamente para el
 * bootstrap manual del primer usuario ADMIN_SISTEMA del login V2 (ver
 * db/sdrerc_app/scripts/61_login_2fa_usuario.sql). Evita escribir la
 * contraseña en texto plano en cualquier archivo versionado: se ejecuta
 * localmente y el hash resultante se pega a mano en el script SQL.
 *
 * Uso: java -cp SDRERC-V2.jar com.sdrerc.tools.PasswordHashCli "<contraseña>"
 */
public final class PasswordHashCli {

    private PasswordHashCli() {
    }

    public static void main(String[] args) {
        if (args.length != 1 || args[0].trim().isEmpty()) {
            System.err.println("Uso: java -cp SDRERC-V2.jar com.sdrerc.tools.PasswordHashCli \"<contraseña>\"");
            System.exit(1);
            return;
        }
        System.out.println(PasswordEncoder.hash(args[0]));
    }
}
