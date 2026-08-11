package com.sdrerc.v3.security.jwt;

import com.sdrerc.v3.domain.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Resuelve la identidad autenticada de cada request a partir del header {@code Authorization:
 * Bearer <token>} y la deja en {@link SecurityContextHolder} (que es thread-local por request,
 * a diferencia del {@code SessionContext} estatico de V2). Sin token o token invalido, la
 * request sigue sin autenticar; {@link com.sdrerc.v3.config.SecurityConfig} decide que rutas lo
 * exigen.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length()).trim();
            try {
                AuthenticatedUser user = jwtService.parseSessionToken(token);
                List<GrantedAuthority> authorities = user.roles().stream()
                        .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol))
                        .map(GrantedAuthority.class::cast)
                        .toList();
                SecurityContextHolder.getContext().setAuthentication(
                        new AuthenticatedUserToken(user, authorities));
            } catch (JwtService.InvalidTokenException ex) {
                // Token ausente/invalido: la request sigue sin autenticar; el SecurityFilterChain
                // decide si la ruta exige autenticacion (responde 401 en ese caso, no acá).
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private static final class AuthenticatedUserToken extends AbstractAuthenticationToken {

        private final AuthenticatedUser principal;

        private AuthenticatedUserToken(AuthenticatedUser principal, List<GrantedAuthority> authorities) {
            super(authorities);
            this.principal = principal;
            setAuthenticated(true);
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return principal;
        }
    }
}
