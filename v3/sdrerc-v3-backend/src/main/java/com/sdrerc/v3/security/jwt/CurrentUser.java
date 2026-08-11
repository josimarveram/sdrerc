package com.sdrerc.v3.security.jwt;

import com.sdrerc.v3.domain.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Punto unico para que cualquier controller/Service de V3 obtenga el usuario autenticado del
 * request actual. Deliberadamente el unico lugar del backend que toca
 * {@link SecurityContextHolder} directamente: el resto del codigo (Services, DAOs) debe recibir
 * el {@link AuthenticatedUser} (o su idUsuario/roles) como parametro explicito, replicando el
 * patron ya bueno de {@code VisibilidadBandejaSql} en V2, no volver a consultar un contexto
 * global desde capas internas.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static AuthenticatedUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("No hay un usuario autenticado en el request actual.");
        }
        return user;
    }
}
