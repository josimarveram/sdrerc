package com.sdrerc.v3.config;

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

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/ping", "/actuator/health", "/actuator/info")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
