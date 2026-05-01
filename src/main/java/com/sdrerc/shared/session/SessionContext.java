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

    public static Long getUserId() {
        return getUsuarioActual().getUserId();
    }

    public static int getIdTecnicoActual() {
        Long idTecnico = getUsuarioActual().getIdTecnico();
        if (idTecnico == null) {
            throw new IllegalStateException("El usuario actual no tiene técnico/abogado asociado.");
        }
        return Math.toIntExact(idTecnico);
    }

    public static String getUsername() {
        return getUsuarioActual().getUsername();
    }

    public static String getFullName() {
        return getUsuarioActual().getFullName();
    }

    public static boolean hasRole(String roleName) {
        return getUsuarioActual().hasRole(roleName);
    }

    public static boolean hasAnyRole(String... roles) {
        if (roles == null) {
            return false;
        }
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    public static void limpiar() {
        usuarioActual = null;
    }
}
