package com.sdrerc.v3.config;

import com.sdrerc.v3.security.jwt.JsonAccessDeniedHandler;
import com.sdrerc.v3.security.jwt.JsonAuthenticationEntryPoint;
import com.sdrerc.v3.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Sin equivalente en V2 (una app de escritorio no expone HTTP). Autenticacion stateless via JWT
 * (sin sesion HTTP de servidor, ver plan de migracion seccion 2): cada request se autentica de
 * nuevo por su propio Authorization header, nunca por una sesion guardada en el servidor.
 *
 * <p>Rutas publicas: el flujo completo de login/2FA ({@code /api/auth/**}, porque ahi es donde
 * se obtiene el JWT) y los endpoints de salud/smoke-test de la Fase 0. Todo lo demas exige un
 * JWT de sesion valido; los controllers de modulos de negocio (Fase 1 en adelante) se agregan
 * aqui a la lista de rutas protegidas por defecto, sin tener que declarar nada extra.</p>
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
    private final JsonAccessDeniedHandler jsonAccessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint,
            JsonAccessDeniedHandler jsonAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jsonAuthenticationEntryPoint = jsonAuthenticationEntryPoint;
        this.jsonAccessDeniedHandler = jsonAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/ping", "/actuator/health", "/actuator/info",
                                "/api/admin/plantillas/onlyoffice/documento/**", "/api/admin/plantillas/onlyoffice/callback")
                        .permitAll()
                        .anyRequest().authenticated())
                // 01/09/2026: sin esto, un token ausente/vencido/invalido devolvia 403 con cuerpo
                // vacio (default de Spring Security) - el frontend (http.ts#handleResponse) no
                // podia distinguirlo de un error real y mostraba "Ocurrio un error inesperado."
                // en vez de avisar que la sesion vencio. Ver v3/CLAUDE.md.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonAuthenticationEntryPoint)
                        .accessDeniedHandler(jsonAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
