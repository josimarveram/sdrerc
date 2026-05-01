package com.sdrerc.shared.session;

import com.sdrerc.domain.model.User;

public final class SessionContext {

    private static User usuarioActual;

    private SessionContext() {
    }

    public static void setUsuarioActual(User usuario) {
        usuarioActual = usuario;
    }

    public static User getUsuarioActual() {
        if (usuarioActual == null) {
            throw new IllegalStateException("No existe un usuario autenticado en la sesion actual.");
        }
        return usuarioActual;
    }

    public static int getIdUsuarioActual() {
        Long userId = getUsuarioActual().getUserId();
        if (userId == null) {
            throw new IllegalStateException("El usuario autenticado no tiene USER_ID.");
        }
        return Math.toIntExact(userId);
    }

    public static int getIdTecnicoActual() {
        // TODO: Confirmar relacion formal APP_USERS.USER_ID -> TECNICO.ID_TECNICO en BD.
        return getIdUsuarioActual();
    }

    public static void limpiar() {
        usuarioActual = null;
    }
}
