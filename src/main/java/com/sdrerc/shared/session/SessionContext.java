package com.sdrerc.shared.session;

import com.sdrerc.domain.model.User;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class SessionContext {

    private static User usuarioActual;
    private static Set<String> permisos = Collections.emptySet();

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

    /**
     * Registra el conjunto de códigos de permiso resueltos para el usuario de sesión actual.
     * Debe ser poblado por la capa de UI/Service tras iniciar sesión (p. ej. PermisoRolService);
     * SessionContext no consulta la base de datos por sí mismo.
     */
    public static void setPermisos(Set<String> nuevosPermisos) {
        if (nuevosPermisos == null || nuevosPermisos.isEmpty()) {
            permisos = Collections.emptySet();
            return;
        }
        Set<String> normalizados = new HashSet<String>();
        for (String permiso : nuevosPermisos) {
            if (permiso != null && !permiso.trim().isEmpty()) {
                normalizados.add(permiso.trim().toUpperCase(Locale.ROOT));
            }
        }
        permisos = normalizados;
    }

    public static Set<String> getPermisos() {
        return Collections.unmodifiableSet(permisos);
    }

    /**
     * Indica si el usuario actual tiene el permiso indicado. Si aún no se resolvió ningún
     * permiso para la sesión (catálogo vacío, sin sesión, o falla la consulta), retorna true
     * ("fail-open"): no se bloquea nada hasta que exista un catálogo de permisos real,
     * preservando el comportamiento actual de la aplicación.
     */
    public static boolean tienePermiso(String codigoPermiso) {
        if (permisos.isEmpty()) {
            return true;
        }
        if (codigoPermiso == null || codigoPermiso.trim().isEmpty()) {
            return true;
        }
        return permisos.contains(codigoPermiso.trim().toUpperCase(Locale.ROOT));
    }

    public static void limpiar() {
        usuarioActual = null;
        permisos = Collections.emptySet();
    }
}
